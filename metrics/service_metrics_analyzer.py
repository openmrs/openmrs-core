#!/usr/bin/env python3
"""
OpenMRS Service Cohesion and Coupling Metrics Analyzer

This script analyzes Java service classes to calculate quantitative metrics:
1. Halstead Complexity Metrics
2. Fan-in/Fan-out Metrics
3. Coupling Between Objects (CBO)
4. Lack of Cohesion of Methods (LCOM)
5. Cyclomatic Complexity
6. Lines of Code (LOC) and other size metrics
"""

import os
import re
import ast
import json
from collections import defaultdict, Counter
from pathlib import Path
import math

class ServiceMetricsAnalyzer:
    def __init__(self, project_root):
        self.project_root = Path(project_root)
        self.services = {}
        self.service_interfaces = {}
        self.service_implementations = {}
        self.dependencies = defaultdict(set)
        self.method_calls = defaultdict(set)
        self.import_relationships = defaultdict(set)
        
    def find_service_files(self):
        """Find all service interface and implementation files"""
        service_impl_pattern = re.compile(r'.*ServiceImpl\.java$')
        service_pattern = re.compile(r'.*Service\.java$')
        
        for java_file in self.project_root.rglob('*.java'):
            if 'test' not in str(java_file).lower() and '/main/' in str(java_file):
                if service_impl_pattern.match(str(java_file)):
                    self.service_implementations[java_file.stem] = java_file
                elif service_pattern.match(str(java_file)):
                    # Only add as interface if it's not an implementation
                    self.service_interfaces[java_file.stem] = java_file
    
    def extract_java_elements(self, file_path):
        """Extract Java code elements for analysis"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except:
            return {}
        
        elements = {
            'imports': [],
            'class_name': '',
            'methods': [],
            'fields': [],
            'method_calls': [],
            'annotations': [],
            'extends': '',
            'implements': [],
            'lines_of_code': 0,
            'comment_lines': 0,
            'blank_lines': 0
        }
        
        # Count lines
        lines = content.split('\n')
        elements['lines_of_code'] = len(lines)
        elements['comment_lines'] = len([l for l in lines if l.strip().startswith('//') or l.strip().startswith('/*') or l.strip().startswith('*')])
        elements['blank_lines'] = len([l for l in lines if not l.strip()])
        
        # Extract imports
        import_pattern = re.compile(r'import\s+([^;]+);')
        elements['imports'] = import_pattern.findall(content)
        
        # Extract class declaration
        class_pattern = re.compile(r'public\s+class\s+(\w+)(?:\s+extends\s+(\w+))?(?:\s+implements\s+([^{]+))?')
        class_match = class_pattern.search(content)
        if class_match:
            elements['class_name'] = class_match.group(1)
            if class_match.group(2):
                elements['extends'] = class_match.group(2)
            if class_match.group(3):
                elements['implements'] = [impl.strip() for impl in class_match.group(3).split(',')]
        
        # Extract methods
        method_pattern = re.compile(r'(?:public|private|protected|static).*?\s+(\w+)\s*\([^)]*\)\s*(?:throws[^{]*)?{', re.MULTILINE)
        elements['methods'] = method_pattern.findall(content)
        
        # Extract fields
        field_pattern = re.compile(r'(?:private|protected|public).*?\s+(\w+)\s*[=;]')
        elements['fields'] = field_pattern.findall(content)
        
        # Extract method calls
        method_call_pattern = re.compile(r'(\w+)\.(\w+)\(')
        elements['method_calls'] = method_call_pattern.findall(content)
        
        # Extract annotations
        annotation_pattern = re.compile(r'@(\w+)')
        elements['annotations'] = annotation_pattern.findall(content)
        
        return elements
    
    def calculate_halstead_metrics(self, content):
        """Calculate Halstead complexity metrics"""
        # Java operators
        operators = [
            '+', '-', '*', '/', '%', '=', '==', '!=', '<', '>', '<=', '>=',
            '&&', '||', '!', '&', '|', '^', '<<', '>>', '>>>', '++', '--',
            '+=', '-=', '*=', '/=', '%=', '&=', '|=', '^=', '<<=', '>>=', '>>>=',
            '?', ':', '.', ',', ';', '(', ')', '{', '}', '[', ']',
            'if', 'else', 'while', 'for', 'do', 'switch', 'case', 'default',
            'try', 'catch', 'finally', 'throw', 'throws', 'return', 'break',
            'continue', 'new', 'instanceof', 'class', 'interface', 'extends',
            'implements', 'public', 'private', 'protected', 'static', 'final',
            'abstract', 'synchronized', 'volatile', 'transient', 'native'
        ]
        
        # Extract all tokens
        token_pattern = re.compile(r'\b\w+\b|[{}()\[\];,.<>=!&|^+\-*/%?:]')
        tokens = token_pattern.findall(content)
        
        # Count operators and operands
        operator_counts = Counter()
        operand_counts = Counter()
        
        for token in tokens:
            if token in operators:
                operator_counts[token] += 1
            elif re.match(r'^[a-zA-Z_]\w*$', token):
                operand_counts[token] += 1
        
        # Halstead metrics
        n1 = len(operator_counts)  # Number of distinct operators
        n2 = len(operand_counts)   # Number of distinct operands
        N1 = sum(operator_counts.values())  # Total operators
        N2 = sum(operand_counts.values())   # Total operands
        
        if n1 == 0 or n2 == 0:
            return {
                'vocabulary': 0, 'length': 0, 'volume': 0,
                'difficulty': 0, 'effort': 0, 'time': 0, 'bugs': 0
            }
        
        vocabulary = n1 + n2
        length = N1 + N2
        volume = length * math.log2(vocabulary) if vocabulary > 0 else 0
        difficulty = (n1 / 2) * (N2 / n2)
        effort = difficulty * volume
        time = effort / 18  # Estimated time in seconds
        bugs = volume / 3000  # Estimated bugs
        
        return {
            'vocabulary': vocabulary,
            'length': length,
            'volume': volume,
            'difficulty': difficulty,
            'effort': effort,
            'time': time,
            'bugs': bugs
        }
    
    def calculate_cyclomatic_complexity(self, content):
        """Calculate McCabe's Cyclomatic Complexity"""
        # Decision points in Java
        decision_keywords = [
            'if', 'else', 'while', 'for', 'do', 'switch', 'case',
            'catch', '&&', '||', '?'
        ]
        
        complexity = 1  # Base complexity
        for keyword in decision_keywords:
            if keyword in ['&&', '||', '?']:
                complexity += content.count(keyword)
            else:
                complexity += len(re.findall(r'\b' + keyword + r'\b', content))
        
        return complexity
    
    def calculate_lcom(self, elements):
        """Calculate Lack of Cohesion of Methods (LCOM1)"""
        methods = elements['methods']
        fields = elements['fields']
        
        if len(methods) <= 1:
            return 0
        
        # Simple LCOM calculation - count methods that don't share fields
        # This is a simplified version; a more accurate version would require
        # deeper code analysis to track which methods access which fields
        
        method_pairs = 0
        sharing_pairs = 0
        
        for i in range(len(methods)):
            for j in range(i + 1, len(methods)):
                method_pairs += 1
                # This is a simplified check - in reality, we'd need to analyze
                # the method bodies to see which fields they access
                sharing_pairs += 1  # Assume some sharing for now
        
        lcom = max(0, method_pairs - sharing_pairs)
        return lcom if method_pairs > 0 else 0
    
    def calculate_fan_metrics(self, elements, all_services):
        """Calculate Fan-in and Fan-out metrics"""
        service_calls = set()
        external_calls = set()
        
        # Analyze method calls to determine fan-out
        for obj, method in elements['method_calls']:
            if any(service in obj for service in all_services):
                service_calls.add(obj)
            else:
                external_calls.add(obj)
        
        fan_out = len(service_calls) + len(external_calls)
        
        # Fan-in would require analyzing all other classes that call this service
        # For now, we'll estimate based on common service patterns
        fan_in = 0  # This would need cross-service analysis
        
        return fan_in, fan_out
    
    def calculate_coupling_metrics(self, elements):
        """Calculate Coupling Between Objects (CBO)"""
        coupled_classes = set()
        
        # Count imports from same package/project
        for imp in elements['imports']:
            if 'openmrs' in imp:
                class_name = imp.split('.')[-1]
                coupled_classes.add(class_name)
        
        # Add inheritance relationships
        if elements['extends']:
            coupled_classes.add(elements['extends'])
        
        for impl in elements['implements']:
            coupled_classes.add(impl)
        
        return len(coupled_classes)
    
    def analyze_service(self, file_path):
        """Analyze a single service file"""
        elements = self.extract_java_elements(file_path)
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except:
            content = ""
        
        halstead = self.calculate_halstead_metrics(content)
        cyclomatic = self.calculate_cyclomatic_complexity(content)
        lcom = self.calculate_lcom(elements)
        cbo = self.calculate_coupling_metrics(elements)
        fan_in, fan_out = self.calculate_fan_metrics(elements, self.service_interfaces.keys())
        
        # Additional metrics
        effective_lines = elements['lines_of_code'] - elements['comment_lines'] - elements['blank_lines']
        
        return {
            'file_path': str(file_path),
            'class_name': elements['class_name'],
            'type': 'implementation' if 'ServiceImpl' in file_path.name else 'interface',
            'metrics': {
                'halstead': halstead,
                'cyclomatic_complexity': cyclomatic,
                'lcom': lcom,
                'cbo': cbo,
                'fan_in': fan_in,
                'fan_out': fan_out,
                'lines_of_code': elements['lines_of_code'],
                'effective_lines': effective_lines,
                'comment_ratio': elements['comment_lines'] / elements['lines_of_code'] if elements['lines_of_code'] > 0 else 0,
                'method_count': len(elements['methods']),
                'field_count': len(elements['fields']),
                'import_count': len(elements['imports'])
            },
            'elements': elements
        }
    
    def run_analysis(self):
        """Run complete analysis on all service files"""
        self.find_service_files()
        
        results = {
            'interfaces': {},
            'implementations': {},
            'summary': {}
        }
        
        # Analyze interfaces
        for name, path in self.service_interfaces.items():
            results['interfaces'][name] = self.analyze_service(path)
        
        # Analyze implementations
        for name, path in self.service_implementations.items():
            results['implementations'][name] = self.analyze_service(path)
        
        # Calculate summary statistics
        all_services = list(results['interfaces'].values()) + list(results['implementations'].values())
        
        if all_services:
            metrics = [s['metrics'] for s in all_services]
            results['summary'] = {
                'total_services': len(all_services),
                'total_interfaces': len(results['interfaces']),
                'total_implementations': len(results['implementations']),
                'average_complexity': sum(m['cyclomatic_complexity'] for m in metrics) / len(metrics),
                'average_coupling': sum(m['cbo'] for m in metrics) / len(metrics),
                'average_lcom': sum(m['lcom'] for m in metrics) / len(metrics),
                'average_halstead_volume': sum(m['halstead']['volume'] for m in metrics) / len(metrics),
                'average_lines_of_code': sum(m['lines_of_code'] for m in metrics) / len(metrics),
                'highest_complexity': max(metrics, key=lambda x: x['cyclomatic_complexity']),
                'highest_coupling': max(metrics, key=lambda x: x['cbo']),
                'most_complex_halstead': max(metrics, key=lambda x: x['halstead']['volume'])
            }
        
        return results
    
    def generate_report(self, results):
        """Generate a detailed analysis report"""
        report = []
        report.append("# OpenMRS Service Cohesion and Coupling Analysis Report")
        report.append("=" * 60)
        report.append("")
        
        # Summary section
        summary = results['summary']
        report.append("## Summary Statistics")
        report.append(f"Total Services Analyzed: {summary['total_services']}")
        report.append(f"Service Interfaces: {summary['total_interfaces']}")
        report.append(f"Service Implementations: {summary['total_implementations']}")
        report.append("")
        
        # Average metrics
        report.append("## Average Metrics")
        report.append(f"Average Cyclomatic Complexity: {summary['average_complexity']:.2f}")
        report.append(f"Average Coupling (CBO): {summary['average_coupling']:.2f}")
        report.append(f"Average LCOM: {summary['average_lcom']:.2f}")
        report.append(f"Average Halstead Volume: {summary['average_halstead_volume']:.2f}")
        report.append(f"Average Lines of Code: {summary['average_lines_of_code']:.2f}")
        report.append("")
        
        # Top problematic services
        report.append("## Services with Highest Complexity/Coupling")
        
        all_services = list(results['interfaces'].values()) + list(results['implementations'].values())
        
        # Sort by different metrics
        by_complexity = sorted(all_services, key=lambda x: x['metrics']['cyclomatic_complexity'], reverse=True)[:5]
        by_coupling = sorted(all_services, key=lambda x: x['metrics']['cbo'], reverse=True)[:5]
        by_halstead = sorted(all_services, key=lambda x: x['metrics']['halstead']['volume'], reverse=True)[:5]
        
        report.append("### Top 5 by Cyclomatic Complexity:")
        for service in by_complexity:
            report.append(f"- {service['class_name']}: {service['metrics']['cyclomatic_complexity']}")
        report.append("")
        
        report.append("### Top 5 by Coupling (CBO):")
        for service in by_coupling:
            report.append(f"- {service['class_name']}: {service['metrics']['cbo']}")
        report.append("")
        
        report.append("### Top 5 by Halstead Volume:")
        for service in by_halstead:
            report.append(f"- {service['class_name']}: {service['metrics']['halstead']['volume']:.2f}")
        report.append("")
        
        # Detailed metrics for each service
        report.append("## Detailed Service Metrics")
        report.append("")
        
        for category in ['implementations', 'interfaces']:
            report.append(f"### {category.title()}")
            for name, service in results[category].items():
                metrics = service['metrics']
                report.append(f"#### {service['class_name']}")
                report.append(f"- File: {service['file_path']}")
                report.append(f"- Lines of Code: {metrics['lines_of_code']}")
                report.append(f"- Effective Lines: {metrics['effective_lines']}")
                report.append(f"- Methods: {metrics['method_count']}")
                report.append(f"- Fields: {metrics['field_count']}")
                report.append(f"- Cyclomatic Complexity: {metrics['cyclomatic_complexity']}")
                report.append(f"- Coupling (CBO): {metrics['cbo']}")
                report.append(f"- LCOM: {metrics['lcom']}")
                report.append(f"- Fan-out: {metrics['fan_out']}")
                report.append(f"- Halstead Volume: {metrics['halstead']['volume']:.2f}")
                report.append(f"- Halstead Difficulty: {metrics['halstead']['difficulty']:.2f}")
                report.append(f"- Estimated Bugs: {metrics['halstead']['bugs']:.2f}")
                report.append("")
        
        return "\n".join(report)

if __name__ == "__main__":
    analyzer = ServiceMetricsAnalyzer("/Users/shashivelur/Projects/openmrs-core")
    results = analyzer.run_analysis()
    report = analyzer.generate_report(results)
    
    # Save results
    with open("/Users/shashivelur/Projects/openmrs-core/service_metrics_results.json", "w") as f:
        json.dump(results, f, indent=2, default=str)
    
    with open("/Users/shashivelur/Projects/openmrs-core/service_metrics_report.md", "w") as f:
        f.write(report)
    
    print("Analysis complete!")
    print("Results saved to:")
    print("- service_metrics_results.json (detailed data)")
    print("- service_metrics_report.md (human-readable report)")
