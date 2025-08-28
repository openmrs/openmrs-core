#!/usr/bin/env python3
"""
OpenMRS Database Entity Analyzer
Extracts all JPA entities and their relationships from the OpenMRS codebase
to generate ER diagrams.
"""

import os
import re
import glob
from pathlib import Path
from collections import defaultdict, namedtuple
import json

# Data structures
Entity = namedtuple('Entity', ['name', 'table_name', 'file_path', 'columns', 'relationships'])
Column = namedtuple('Column', ['name', 'type', 'annotations', 'nullable'])
Relationship = namedtuple('Relationship', ['type', 'target_entity', 'join_column', 'mapped_by'])

class EntityAnalyzer:
    def __init__(self, base_path):
        self.base_path = Path(base_path)
        self.entities = {}
        self.tables = defaultdict(list)
        
    def find_entity_files(self):
        """Find all Java files with @Entity annotation"""
        java_files = []
        for root, dirs, files in os.walk(self.base_path / "api" / "src" / "main" / "java"):
            for file in files:
                if file.endswith('.java'):
                    file_path = Path(root) / file
                    try:
                        with open(file_path, 'r', encoding='utf-8') as f:
                            content = f.read()
                            if '@Entity' in content:
                                java_files.append(file_path)
                    except Exception as e:
                        print(f"Error reading {file_path}: {e}")
        return java_files
    
    def parse_entity_file(self, file_path):
        """Parse a single entity file to extract entity information"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            print(f"Error reading {file_path}: {e}")
            return None
            
        # Extract entity name (class name)
        class_match = re.search(r'public\s+class\s+(\w+)', content)
        if not class_match:
            return None
        entity_name = class_match.group(1)
        
        # Extract table name
        table_match = re.search(r'@Table\s*\(\s*name\s*=\s*["\']([^"\']+)["\']', content)
        table_name = table_match.group(1) if table_match else entity_name.lower()
        
        # Extract columns
        columns = []
        relationships = []
        
        # Find all field declarations with annotations
        field_pattern = r'(@[^\n]+\n)*\s*(private|protected|public)?\s+([^\s]+)\s+(\w+);'
        
        lines = content.split('\n')
        i = 0
        while i < len(lines):
            line = lines[i].strip()
            
            # Look for annotation blocks
            annotations = []
            while i < len(lines) and lines[i].strip().startswith('@'):
                annotations.append(lines[i].strip())
                i += 1
            
            if i < len(lines):
                line = lines[i].strip()
                # Check if this is a field declaration
                field_match = re.search(r'(private|protected|public)?\s+([^\s]+)\s+(\w+);', line)
                if field_match:
                    field_type = field_match.group(2)
                    field_name = field_match.group(3)
                    
                    # Check for relationship annotations
                    rel_annotations = [a for a in annotations if any(rel in a for rel in ['@OneToMany', '@ManyToOne', '@OneToOne', '@ManyToMany'])]
                    
                    if rel_annotations:
                        # This is a relationship
                        rel_type = None
                        target_entity = None
                        join_column = None
                        mapped_by = None
                        
                        for ann in rel_annotations:
                            if '@OneToMany' in ann:
                                rel_type = 'OneToMany'
                            elif '@ManyToOne' in ann:
                                rel_type = 'ManyToOne'
                            elif '@OneToOne' in ann:
                                rel_type = 'OneToOne'
                            elif '@ManyToMany' in ann:
                                rel_type = 'ManyToMany'
                            
                            # Extract target entity
                            target_match = re.search(r'targetEntity\s*=\s*(\w+)\.class', ann)
                            if target_match:
                                target_entity = target_match.group(1)
                            else:
                                # Try to infer from field type
                                if '<' in field_type:
                                    generic_match = re.search(r'<(\w+)>', field_type)
                                    if generic_match:
                                        target_entity = generic_match.group(1)
                                elif field_type not in ['String', 'Integer', 'Long', 'Boolean', 'Date', 'Double', 'Float']:
                                    target_entity = field_type
                            
                            # Extract mappedBy
                            mapped_by_match = re.search(r'mappedBy\s*=\s*["\']([^"\']+)["\']', ann)
                            if mapped_by_match:
                                mapped_by = mapped_by_match.group(1)
                        
                        # Look for @JoinColumn annotation
                        join_annotations = [a for a in annotations if '@JoinColumn' in a]
                        if join_annotations:
                            join_match = re.search(r'name\s*=\s*["\']([^"\']+)["\']', join_annotations[0])
                            if join_match:
                                join_column = join_match.group(1)
                        
                        relationships.append(Relationship(rel_type, target_entity, join_column, mapped_by))
                    
                    else:
                        # This is a regular column
                        nullable = True
                        column_annotations = [a for a in annotations if '@Column' in a or '@Id' in a or '@GeneratedValue' in a]
                        
                        # Check for nullable
                        for ann in column_annotations:
                            if 'nullable' in ann:
                                nullable_match = re.search(r'nullable\s*=\s*(true|false)', ann)
                                if nullable_match:
                                    nullable = nullable_match.group(1) == 'true'
                        
                        columns.append(Column(field_name, field_type, annotations, nullable))
            
            i += 1
        
        return Entity(entity_name, table_name, str(file_path), columns, relationships)
    
    def analyze_all_entities(self):
        """Analyze all entity files"""
        entity_files = self.find_entity_files()
        print(f"Found {len(entity_files)} entity files")
        
        for file_path in entity_files:
            entity = self.parse_entity_file(file_path)
            if entity:
                self.entities[entity.name] = entity
                self.tables[entity.table_name].append(entity.name)
        
        print(f"Parsed {len(self.entities)} entities")
        return self.entities
    
    def generate_er_diagram(self, output_format='plantuml'):
        """Generate ER diagram in specified format"""
        if output_format == 'plantuml':
            return self._generate_plantuml_er()
        elif output_format == 'mermaid':
            return self._generate_mermaid_er()
        else:
            raise ValueError(f"Unsupported format: {output_format}")
    
    def _generate_plantuml_er(self):
        """Generate PlantUML ER diagram"""
        lines = ['@startuml OpenMRS_Database_Schema', '!theme plain', '']
        
        # Sort entities by domain/category
        domain_entities = self._categorize_entities()
        
        for domain, entities in domain_entities.items():
            lines.append(f'package "{domain}" {{')
            
            for entity_name in entities:
                entity = self.entities[entity_name]
                lines.append(f'  entity "{entity.table_name}" as {entity.name} {{')
                
                # Add primary key columns first
                pk_columns = [col for col in entity.columns if any('@Id' in ann for ann in col.annotations)]
                for col in pk_columns:
                    lines.append(f'    * {col.name} : {col.type}')
                
                # Add other columns
                other_columns = [col for col in entity.columns if not any('@Id' in ann for ann in col.annotations)]
                for col in other_columns:
                    prefix = '' if col.nullable else '* '
                    lines.append(f'    {prefix}{col.name} : {col.type}')
                
                lines.append('  }')
            
            lines.append('}')
            lines.append('')
        
        # Add relationships
        lines.append('/' + '/' + ' Relationships')
        for entity in self.entities.values():
            for rel in entity.relationships:
                if rel.target_entity in self.entities:
                    if rel.type == 'ManyToOne':
                        lines.append(f'{entity.name} }}o--|| {rel.target_entity}')
                    elif rel.type == 'OneToMany':
                        lines.append(f'{entity.name} ||--o{{ {rel.target_entity}')
                    elif rel.type == 'OneToOne':
                        lines.append(f'{entity.name} ||--|| {rel.target_entity}')
                    elif rel.type == 'ManyToMany':
                        lines.append(f'{entity.name} }}o--o{{ {rel.target_entity}')
        
        lines.append('@enduml')
        return '\n'.join(lines)
    
    def _generate_mermaid_er(self):
        """Generate Mermaid ER diagram"""
        lines = ['erDiagram']
        
        # Add entities
        for entity in self.entities.values():
            lines.append(f'  {entity.name} {{')
            for col in entity.columns:
                pk_indicator = 'PK' if any('@Id' in ann for ann in col.annotations) else ''
                lines.append(f'    {col.type} {col.name} {pk_indicator}')
            lines.append('  }')
        
        # Add relationships
        for entity in self.entities.values():
            for rel in entity.relationships:
                if rel.target_entity in self.entities:
                    if rel.type == 'ManyToOne':
                        lines.append(f'  {entity.name} }}o--|| {rel.target_entity} : ""')
                    elif rel.type == 'OneToMany':
                        lines.append(f'  {entity.name} ||--o{{ {rel.target_entity} : ""')
                    elif rel.type == 'OneToOne':
                        lines.append(f'  {entity.name} ||--|| {rel.target_entity} : ""')
                    elif rel.type == 'ManyToMany':
                        lines.append(f'  {entity.name} }}o--o{{ {rel.target_entity} : ""')
        
        return '\n'.join(lines)
    
    def _categorize_entities(self):
        """Categorize entities by domain"""
        domains = {
            'Patient Management': ['Patient', 'Person', 'PersonName', 'PersonAddress', 'PersonAttribute', 
                                   'PersonAttributeType', 'PatientIdentifier', 'PatientIdentifierType', 
                                   'PatientProgram', 'PatientState', 'Allergy', 'AllergyReaction'],
            'Clinical Data': ['Encounter', 'EncounterType', 'EncounterRole', 'EncounterProvider', 
                              'Obs', 'Order', 'OrderType', 'OrderGroup', 'Diagnosis', 'Condition'],
            'Concepts': ['Concept', 'ConceptName', 'ConceptDescription', 'ConceptClass', 'ConceptDatatype',
                         'ConceptAnswer', 'ConceptSet', 'ConceptNumeric', 'ConceptComplex', 'ConceptMap',
                         'ConceptSource', 'ConceptReferenceTerm', 'ConceptReferenceTermMap', 'ConceptMapType'],
            'Forms': ['Form', 'FormField', 'FormResource', 'Field', 'FieldType', 'FieldAnswer'],
            'Medications': ['Drug', 'DrugIngredient', 'DrugReferenceMap', 'MedicationDispense'],
            'Users & Roles': ['User', 'Role', 'Privilege', 'UserRole', 'RoleRole'],
            'Location': ['Location', 'LocationTag', 'LocationAttribute', 'LocationAttributeType'],
            'Programs': ['Program', 'ProgramWorkflow', 'ProgramWorkflowState', 'ProgramAttribute', 'ProgramAttributeType'],
            'Visits': ['Visit', 'VisitType', 'VisitAttribute', 'VisitAttributeType'],
            'Providers': ['Provider', 'ProviderAttribute', 'ProviderAttributeType', 'ProviderRole'],
            'System': ['GlobalProperty', 'Cohort', 'CohortMember', 'HL7InQueue', 'HL7InArchive', 'HL7InError'],
            'Other': []
        }
        
        categorized = defaultdict(list)
        uncategorized = []
        
        for entity_name in self.entities.keys():
            placed = False
            for domain, domain_entities in domains.items():
                if entity_name in domain_entities:
                    categorized[domain].append(entity_name)
                    placed = True
                    break
            
            if not placed:
                uncategorized.append(entity_name)
        
        if uncategorized:
            categorized['Other'] = uncategorized
        
        return dict(categorized)
    
    def export_to_json(self, filename):
        """Export entity information to JSON"""
        data = {}
        for name, entity in self.entities.items():
            data[name] = {
                'table_name': entity.table_name,
                'file_path': entity.file_path,
                'columns': [
                    {
                        'name': col.name,
                        'type': col.type,
                        'annotations': col.annotations,
                        'nullable': col.nullable
                    } for col in entity.columns
                ],
                'relationships': [
                    {
                        'type': rel.type,
                        'target_entity': rel.target_entity,
                        'join_column': rel.join_column,
                        'mapped_by': rel.mapped_by
                    } for rel in entity.relationships
                ]
            }
        
        with open(filename, 'w') as f:
            json.dump(data, f, indent=2)

if __name__ == "__main__":
    # Analyze the OpenMRS codebase
    analyzer = EntityAnalyzer('/Users/shashivelur/Projects/openmrs-core')
    entities = analyzer.analyze_all_entities()
    
    # Generate PlantUML diagram
    plantuml_diagram = analyzer.generate_er_diagram('plantuml')
    with open('/Users/shashivelur/Projects/openmrs-core/openmrs_er_diagram.puml', 'w') as f:
        f.write(plantuml_diagram)
    
    # Generate Mermaid diagram
    mermaid_diagram = analyzer.generate_er_diagram('mermaid')
    with open('/Users/shashivelur/Projects/openmrs-core/openmrs_er_diagram.md', 'w') as f:
        f.write('```mermaid\n')
        f.write(mermaid_diagram)
        f.write('\n```')
    
    # Export to JSON
    analyzer.export_to_json('/Users/shashivelur/Projects/openmrs-core/entities.json')
    
    print("Analysis complete!")
    print(f"Generated ER diagrams and exported {len(entities)} entities")
    print("Files created:")
    print("- openmrs_er_diagram.puml (PlantUML)")
    print("- openmrs_er_diagram.md (Mermaid)")
    print("- entities.json (Raw data)")
