#!/usr/bin/env python3
"""
Enhanced OpenMRS Service Dependency and Coupling Analysis

This script provides more detailed analysis of service dependencies and coupling
metrics including coupling matrices, dependency graphs, and fan-in/fan-out analysis.
"""

import os
import re
import json
from collections import defaultdict, Counter
from pathlib import Path
import ast

class EnhancedServiceAnalyzer:
    def __init__(self, project_root):
        self.project_root = Path(project_root)
        self.services = {}
        self.service_files = {}
        self.dependencies = defaultdict(set)
        self.method_dependencies = defaultdict(set)
        self.coupling_matrix = {}
        self.fan_in = defaultdict(int)
        self.fan_out = defaultdict(int)
        
    def find_all_service_files(self):
        """Find all service related files"""
        service_files = []
        
        # Find service interfaces
        for java_file in self.project_root.rglob('*.java'):
            if 'test' not in str(java_file).lower() and '/main/' in str(java_file):
                if re.search(r'Service\.java$', str(java_file)):
                    service_files.append(java_file)
                elif re.search(r'ServiceImpl\.java$', str(java_file)):
                    service_files.append(java_file)
        
        return service_files
    
    def extract_service_calls(self, content, service_names):
        """Extract calls to other services from code content"""
        service_calls = set()
        
        # Look for Context.getXxxService() calls
        context_pattern = re.compile(r'Context\.get(\w*Service)\(\)')
        for match in context_pattern.finditer(content):
            service_name = match.group(1)
            if service_name in service_names:
                service_calls.add(service_name)
        
        # Look for direct service field/variable usage
        for service_name in service_names:
            # Pattern like: serviceField.method() or service.method()
            service_usage_pattern = re.compile(rf'\b{service_name.lower()}[A-Za-z]*\.(\w+)\(')
            if service_usage_pattern.search(content):
                service_calls.add(service_name)
        
        # Look for dependency injection patterns
        inject_pattern = re.compile(r'@Autowired\s+private\s+(\w*Service)\s+')
        for match in inject_pattern.finditer(content):
            service_name = match.group(1)
            if service_name in service_names:
                service_calls.add(service_name)
        
        return service_calls
    
    def calculate_detailed_metrics(self, file_path, all_service_names):
        """Calculate detailed metrics for a service file"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except:
            return {}
        
        lines = content.split('\n')
        
        # Basic metrics
        total_lines = len(lines)
        code_lines = len([l for l in lines if l.strip() and not l.strip().startswith('//')])
        comment_lines = len([l for l in lines if l.strip().startswith('//') or '/*' in l or '*/' in l])
        
        # Extract class name
        class_pattern = re.compile(r'public\s+(?:class|interface)\s+(\w+)')
        class_match = class_pattern.search(content)
        class_name = class_match.group(1) if class_match else file_path.stem
        
        # Count methods
        method_pattern = re.compile(r'(?:public|private|protected).*?\s+(\w+)\s*\([^)]*\)\s*(?:throws[^{]*)?{')
        methods = method_pattern.findall(content)
        
        # Count fields
        field_pattern = re.compile(r'(?:private|protected|public).*?\s+(\w+)\s*[=;]')
        fields = field_pattern.findall(content)
        
        # Extract service dependencies
        service_deps = self.extract_service_calls(content, all_service_names)
        
        # Count import statements
        import_pattern = re.compile(r'import\s+([^;]+);')
        imports = import_pattern.findall(content)
        openmrs_imports = [imp for imp in imports if 'openmrs' in imp]
        
        # Calculate complexity metrics
        # Cyclomatic complexity - count decision points
        decision_keywords = ['if', 'else', 'while', 'for', 'do', 'switch', 'case', 'catch', '&&', '||', '?']
        complexity = 1  # Base complexity
        for keyword in decision_keywords:
            if keyword in ['&&', '||', '?']:
                complexity += content.count(keyword)
            else:
                complexity += len(re.findall(rf'\b{keyword}\b', content))
        
        # Interface segregation metric (for interfaces)
        is_interface = 'interface' in content
        
        return {
            'class_name': class_name,
            'file_type': 'interface' if is_interface else 'implementation',
            'total_lines': total_lines,
            'code_lines': code_lines,
            'comment_lines': comment_lines,
            'method_count': len(methods),
            'field_count': len(fields),
            'import_count': len(imports),
            'openmrs_import_count': len(openmrs_imports),
            'service_dependencies': list(service_deps),
            'dependency_count': len(service_deps),
            'cyclomatic_complexity': complexity,
            'comment_ratio': comment_lines / total_lines if total_lines > 0 else 0,
            'methods': methods,
            'fields': fields
        }
    
    def build_coupling_matrix(self, service_metrics):
        """Build a coupling matrix between services"""
        service_names = list(service_metrics.keys())
        n = len(service_names)
        
        # Initialize matrix as list of lists
        coupling_matrix = [[0 for _ in range(n)] for _ in range(n)]
        name_to_idx = {name: i for i, name in enumerate(service_names)}
        
        # Fill matrix with dependencies
        for service_name, metrics in service_metrics.items():
            if service_name in name_to_idx:
                service_idx = name_to_idx[service_name]
                for dep in metrics['service_dependencies']:
                    if dep in name_to_idx:
                        dep_idx = name_to_idx[dep]
                        coupling_matrix[service_idx][dep_idx] = 1
        
        return coupling_matrix, service_names
    
    def calculate_fan_metrics(self, coupling_matrix, service_names):
        """Calculate fan-in and fan-out metrics"""
        fan_out = {name: sum(coupling_matrix[i]) for i, name in enumerate(service_names)}
        fan_in = {name: sum(coupling_matrix[j][i] for j in range(len(coupling_matrix))) 
                  for i, name in enumerate(service_names)}
        
        return fan_in, fan_out
    
    def calculate_instability(self, fan_in, fan_out):
        """Calculate instability metric (fan-out / (fan-in + fan-out))"""
        instability = {}
        for service in fan_in.keys():
            total_coupling = fan_in[service] + fan_out[service]
            if total_coupling > 0:
                instability[service] = fan_out[service] / total_coupling
            else:
                instability[service] = 0
        return instability
    
    def identify_service_clusters(self, coupling_matrix, service_names, threshold=0.3):
        """Identify clusters of tightly coupled services"""
        n = len(service_names)
        clusters = []
        
        # Simple clustering based on coupling strength
        for i in range(n):
            cluster = [service_names[i]]
            for j in range(n):
                if i != j and coupling_matrix[i][j] > threshold:
                    cluster.append(service_names[j])
            
            if len(cluster) > 1:
                clusters.append(cluster)
        
        return clusters
    
    def run_analysis(self):
        """Run the complete enhanced analysis"""
        service_files = self.find_all_service_files()
        
        # Extract all service names first
        all_service_names = set()
        for file_path in service_files:
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                class_pattern = re.compile(r'public\s+(?:class|interface)\s+(\w+)')
                class_match = class_pattern.search(content)
                if class_match:
                    all_service_names.add(class_match.group(1))
            except:
                continue
        
        # Analyze each service
        service_metrics = {}
        for file_path in service_files:
            metrics = self.calculate_detailed_metrics(file_path, all_service_names)
            if metrics:
                service_metrics[metrics['class_name']] = metrics
        
        # Build coupling matrix
        coupling_matrix, service_names = self.build_coupling_matrix(service_metrics)
        
        # Calculate fan metrics
        fan_in, fan_out = self.calculate_fan_metrics(coupling_matrix, service_names)
        
        # Calculate instability
        instability = self.calculate_instability(fan_in, fan_out)
        
        # Identify clusters
        clusters = self.identify_service_clusters(coupling_matrix, service_names)
        
        # Calculate summary statistics
        total_services = len(service_metrics)
        avg_fan_in = sum(fan_in.values()) / total_services if total_services > 0 else 0
        avg_fan_out = sum(fan_out.values()) / total_services if total_services > 0 else 0
        avg_instability = sum(instability.values()) / total_services if total_services > 0 else 0
        
        # Identify problematic services
        high_fan_out = {k: v for k, v in fan_out.items() if v > avg_fan_out * 1.5}
        high_instability = {k: v for k, v in instability.items() if v > 0.7}
        
        return {
            'service_metrics': service_metrics,
            'coupling_matrix': coupling_matrix,
            'service_names': service_names,
            'fan_in': fan_in,
            'fan_out': fan_out,
            'instability': instability,
            'clusters': clusters,
            'summary': {
                'total_services': total_services,
                'average_fan_in': avg_fan_in,
                'average_fan_out': avg_fan_out,
                'average_instability': avg_instability,
                'high_fan_out_services': high_fan_out,
                'highly_unstable_services': high_instability
            }
        }
    
    def generate_detailed_report(self, results):
        """Generate a comprehensive analysis report"""
        report = []
        report.append("# Enhanced OpenMRS Service Coupling and Dependency Analysis")
        report.append("=" * 70)
        report.append("")
        
        summary = results['summary']
        report.append("## Executive Summary")
        report.append(f"- Total Services Analyzed: {summary['total_services']}")
        report.append(f"- Average Fan-in: {summary['average_fan_in']:.2f}")
        report.append(f"- Average Fan-out: {summary['average_fan_out']:.2f}")
        report.append(f"- Average Instability: {summary['average_instability']:.2f}")
        report.append("")
        
        # Service rankings
        fan_in_sorted = sorted(results['fan_in'].items(), key=lambda x: x[1], reverse=True)
        fan_out_sorted = sorted(results['fan_out'].items(), key=lambda x: x[1], reverse=True)
        instability_sorted = sorted(results['instability'].items(), key=lambda x: x[1], reverse=True)
        
        report.append("## Service Dependency Rankings")
        
        report.append("### Top 10 Most Depended Upon Services (High Fan-in):")
        for service, count in fan_in_sorted[:10]:
            report.append(f"- {service}: {count} dependencies")
        report.append("")
        
        report.append("### Top 10 Most Dependent Services (High Fan-out):")
        for service, count in fan_out_sorted[:10]:
            report.append(f"- {service}: depends on {count} other services")
        report.append("")
        
        report.append("### Top 10 Most Unstable Services:")
        for service, inst in instability_sorted[:10]:
            report.append(f"- {service}: {inst:.3f} instability")
        report.append("")
        
        # Coupling matrix analysis
        report.append("## Coupling Matrix Analysis")
        total_connections = sum(sum(row) for row in results['coupling_matrix'])
        total_possible = len(results['service_names']) ** 2
        coupling_density = total_connections / total_possible if total_possible > 0 else 0
        report.append(f"Coupling Density: {coupling_density:.3f}")
        report.append("")
        
        # Service clusters
        if results['clusters']:
            report.append("## Tightly Coupled Service Clusters")
            for i, cluster in enumerate(results['clusters']):
                report.append(f"### Cluster {i+1}:")
                for service in cluster:
                    report.append(f"- {service}")
                report.append("")
        
        # Detailed service metrics
        report.append("## Detailed Service Metrics")
        
        for service_name, metrics in results['service_metrics'].items():
            report.append(f"### {service_name} ({metrics['file_type']})")
            report.append(f"- Lines of Code: {metrics['code_lines']}")
            report.append(f"- Methods: {metrics['method_count']}")
            report.append(f"- Fields: {metrics['field_count']}")
            report.append(f"- Cyclomatic Complexity: {metrics['cyclomatic_complexity']}")
            report.append(f"- Service Dependencies: {metrics['dependency_count']}")
            if metrics['service_dependencies']:
                report.append(f"  - Depends on: {', '.join(metrics['service_dependencies'])}")
            report.append(f"- Fan-in: {results['fan_in'].get(service_name, 0)}")
            report.append(f"- Fan-out: {results['fan_out'].get(service_name, 0)}")
            report.append(f"- Instability: {results['instability'].get(service_name, 0):.3f}")
            report.append(f"- Comment Ratio: {metrics['comment_ratio']:.2%}")
            report.append("")
        
        # Recommendations
        report.append("## Recommendations")
        
        high_fan_out = summary['high_fan_out_services']
        if high_fan_out:
            report.append("### Services with High Fan-out (Consider Refactoring):")
            for service, count in high_fan_out.items():
                report.append(f"- {service}: {count} dependencies")
            report.append("")
        
        high_instability = summary['highly_unstable_services']
        if high_instability:
            report.append("### Highly Unstable Services (Review Interface Design):")
            for service, inst in high_instability.items():
                report.append(f"- {service}: {inst:.3f} instability")
            report.append("")
        
        report.append("### General Recommendations:")
        report.append("1. Services with high fan-out may benefit from dependency injection refactoring")
        report.append("2. Highly unstable services should be reviewed for interface segregation")
        report.append("3. Tightly coupled clusters may indicate opportunities for service consolidation")
        report.append("4. Services with zero fan-in might be candidates for removal if unused")
        
        return "\n".join(report)

if __name__ == "__main__":
    analyzer = EnhancedServiceAnalyzer("/Users/shashivelur/Projects/openmrs-core")
    results = analyzer.run_analysis()
    report = analyzer.generate_detailed_report(results)
    
    # Save results
    with open("/Users/shashivelur/Projects/openmrs-core/enhanced_service_analysis.json", "w") as f:
        # Convert numpy arrays to lists for JSON serialization
        json_results = {k: v for k, v in results.items()}
        json.dump(json_results, f, indent=2, default=str)
    
    with open("/Users/shashivelur/Projects/openmrs-core/enhanced_service_report.md", "w") as f:
        f.write(report)
    
    print("Enhanced analysis complete!")
    print("Results saved to:")
    print("- enhanced_service_analysis.json (detailed data)")
    print("- enhanced_service_report.md (comprehensive report)")
