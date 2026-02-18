#!/usr/bin/env python3
"""
OpenMRS Database Entity Extractor - Simplified Version
Creates comprehensive ER diagrams from entity annotations and Liquibase schema
"""

import os
import re
from pathlib import Path
from collections import defaultdict

def extract_entities_from_annotations():
    """Extract entities from @Entity/@Table annotations"""
    base_path = Path("/Users/shashivelur/Projects/openmrs-core")
    entities = {}
    
    # Find all entity files
    for root, dirs, files in os.walk(base_path / "api" / "src" / "main" / "java"):
        for file in files:
            if file.endswith('.java'):
                file_path = Path(root) / file
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        content = f.read()
                        
                        if '@Entity' in content:
                            # Extract class name
                            class_match = re.search(r'public\s+class\s+(\w+)', content)
                            if class_match:
                                class_name = class_match.group(1)
                                
                                # Extract table name
                                table_match = re.search(r'@Table\s*\(\s*name\s*=\s*["\']([^"\']+)["\']', content)
                                table_name = table_match.group(1) if table_match else class_name.lower()
                                
                                entities[class_name] = {
                                    'table_name': table_name,
                                    'file_path': str(file_path.relative_to(base_path))
                                }
                                
                except Exception as e:
                    pass
    
    return entities

def extract_tables_from_liquibase():
    """Extract table definitions from Liquibase schema"""
    schema_file = "/Users/shashivelur/Projects/openmrs-core/api/src/main/resources/org/openmrs/liquibase/snapshots/schema-only/liquibase-schema-only-2.8.x.xml"
    tables = {}
    
    try:
        with open(schema_file, 'r', encoding='utf-8') as f:
            content = f.read()
            
            # Extract table definitions
            table_pattern = r'<createTable tableName="([^"]+)">(.*?)</createTable>'
            
            for match in re.finditer(table_pattern, content, re.DOTALL):
                table_name = match.group(1)
                table_def = match.group(2)
                
                # Extract columns
                columns = []
                column_pattern = r'<column[^>]*name="([^"]+)"[^>]*type="([^"]+)"[^>]*(?:/>|>[^<]*</column>)'
                
                for col_match in re.finditer(column_pattern, table_def):
                    col_name = col_match.group(1)
                    col_type = col_match.group(2)
                    
                    # Check for constraints
                    col_def = col_match.group(0)
                    is_pk = 'primaryKey="true"' in col_def
                    is_nullable = 'nullable="false"' not in col_def
                    is_unique = 'unique="true"' in col_def
                    
                    columns.append({
                        'name': col_name,
                        'type': col_type,
                        'primary_key': is_pk,
                        'nullable': is_nullable,
                        'unique': is_unique
                    })
                
                tables[table_name] = {
                    'columns': columns
                }
    
    except Exception as e:
        print(f"Error reading Liquibase schema: {e}")
    
    return tables

def categorize_tables(tables):
    """Categorize tables by domain"""
    domains = {
        'Core Patient Data': [
            'patient', 'person', 'person_name', 'person_address', 'person_attribute', 
            'person_attribute_type', 'patient_identifier', 'patient_identifier_type',
            'allergy', 'allergy_reaction'
        ],
        'Clinical Encounters': [
            'encounter', 'encounter_type', 'encounter_role', 'encounter_provider',
            'encounter_diagnosis', 'obs', 'obs_reference_range'
        ],
        'Orders & Medications': [
            'orders', 'order_type', 'order_group', 'order_group_attribute',
            'order_group_attribute_type', 'order_attribute', 'order_attribute_type',
            'order_frequency', 'drug', 'drug_ingredient', 'drug_reference_map',
            'medication_dispense'
        ],
        'Clinical Concepts': [
            'concept', 'concept_name', 'concept_description', 'concept_class',
            'concept_datatype', 'concept_answer', 'concept_set', 'concept_numeric',
            'concept_complex', 'concept_attribute', 'concept_attribute_type',
            'concept_reference_map', 'concept_reference_source', 'concept_reference_term',
            'concept_reference_term_map', 'concept_map_type', 'concept_reference_range',
            'concept_proposal', 'concept_state_conversion', 'concept_stop_word'
        ],
        'Forms & Fields': [
            'form', 'form_field', 'form_resource', 'field', 'field_type', 'field_answer'
        ],
        'Programs & Workflows': [
            'program', 'program_workflow', 'program_workflow_state', 'patient_program',
            'patient_state', 'program_attribute', 'program_attribute_type'
        ],
        'Visits': [
            'visit', 'visit_type', 'visit_attribute', 'visit_attribute_type'
        ],
        'Locations': [
            'location', 'location_tag', 'location_tag_map', 'location_attribute',
            'location_attribute_type'
        ],
        'Users & Security': [
            'users', 'role', 'privilege', 'user_role', 'role_role', 'user_property'
        ],
        'Providers': [
            'provider', 'provider_attribute', 'provider_attribute_type', 'provider_role'
        ],
        'Cohorts': [
            'cohort', 'cohort_member'
        ],
        'Conditions': [
            'conditions'
        ],
        'HL7 Integration': [
            'hl7_in_queue', 'hl7_in_archive', 'hl7_in_error'
        ],
        'Notifications': [
            'notification_alert', 'notification_alert_recipient', 'notification_template'
        ],
        'Scheduler': [
            'scheduler_task_config'
        ],
        'System': [
            'global_property', 'serialized_object', 'clob_datatype_storage',
            'care_setting', 'revision_entity', 'databasechangelog', 'databasechangeloglock'
        ]
    }
    
    categorized = defaultdict(list)
    uncategorized = []
    
    for table_name in tables.keys():
        placed = False
        for domain, domain_tables in domains.items():
            if table_name in domain_tables:
                categorized[domain].append(table_name)
                placed = True
                break
        
        if not placed:
            uncategorized.append(table_name)
    
    if uncategorized:
        categorized['Other'] = uncategorized
    
    return dict(categorized)

def infer_relationships(tables):
    """Infer relationships from foreign key column names"""
    relationships = []
    
    for table_name, table_data in tables.items():
        for column in table_data['columns']:
            col_name = column['name']
            
            # Look for foreign key patterns
            if col_name.endswith('_id') and not column['primary_key']:
                # Potential foreign key
                referenced_table = col_name[:-3]  # Remove '_id'
                
                # Handle special cases
                if referenced_table == 'patient' and 'patient' in tables:
                    relationships.append((table_name, 'patient', col_name, 'ManyToOne'))
                elif referenced_table == 'person' and 'person' in tables:
                    relationships.append((table_name, 'person', col_name, 'ManyToOne'))
                elif referenced_table == 'concept' and 'concept' in tables:
                    relationships.append((table_name, 'concept', col_name, 'ManyToOne'))
                elif referenced_table == 'encounter' and 'encounter' in tables:
                    relationships.append((table_name, 'encounter', col_name, 'ManyToOne'))
                elif referenced_table == 'user' and 'users' in tables:
                    relationships.append((table_name, 'users', col_name, 'ManyToOne'))
                elif referenced_table in tables:
                    relationships.append((table_name, referenced_table, col_name, 'ManyToOne'))
                
                # Handle composite foreign keys
                elif referenced_table + 's' in tables:
                    relationships.append((table_name, referenced_table + 's', col_name, 'ManyToOne'))
    
    return relationships

def generate_plantuml_diagram(tables, relationships, categorized_tables):
    """Generate comprehensive PlantUML ER diagram"""
    lines = [
        '@startuml OpenMRS_Complete_Database_Schema',
        '!theme plain',
        'skinparam linetype ortho',
        'skinparam packageStyle rectangle',
        ''
    ]
    
    # Generate entities by domain
    for domain, table_list in categorized_tables.items():
        if not table_list:
            continue
            
        lines.append(f'package "{domain}" {{')
        
        for table_name in sorted(table_list):
            if table_name in tables:
                table_data = tables[table_name]
                lines.append(f'  entity "{table_name}" {{')
                
                # Add primary key columns first
                pk_columns = [col for col in table_data['columns'] if col['primary_key']]
                for col in pk_columns:
                    lines.append(f'    + {col["name"]} : {col["type"]} <<PK>>')
                
                # Add unique columns
                unique_columns = [col for col in table_data['columns'] if col['unique'] and not col['primary_key']]
                for col in unique_columns:
                    prefix = '* ' if not col['nullable'] else ''
                    lines.append(f'    {prefix}{col["name"]} : {col["type"]} <<UK>>')
                
                # Add foreign key columns
                fk_columns = [col for col in table_data['columns'] 
                             if col['name'].endswith('_id') and not col['primary_key'] and not col['unique']]
                for col in fk_columns:
                    prefix = '* ' if not col['nullable'] else ''
                    lines.append(f'    {prefix}{col["name"]} : {col["type"]} <<FK>>')
                
                # Add other columns
                other_columns = [col for col in table_data['columns'] 
                               if not col['primary_key'] and not col['unique'] and not col['name'].endswith('_id')]
                for col in other_columns:
                    prefix = '* ' if not col['nullable'] else ''
                    lines.append(f'    {prefix}{col["name"]} : {col["type"]}')
                
                lines.append('  }')
        
        lines.append('}')
        lines.append('')
    
    # Add relationships
    lines.append('/' + '/' + ' Relationships')
    processed_relationships = set()
    
    for source_table, target_table, fk_column, rel_type in relationships:
        rel_key = (source_table, target_table, fk_column)
        if rel_key not in processed_relationships and source_table in tables and target_table in tables:
            if rel_type == 'ManyToOne':
                lines.append(f'{source_table} }}o--|| {target_table} : {fk_column}')
            processed_relationships.add(rel_key)
    
    lines.append('')
    lines.append('@enduml')
    return '\n'.join(lines)

def generate_summary_report(tables, categorized_tables):
    """Generate a summary report of the database schema"""
    lines = [
        '# OpenMRS Database Schema Analysis',
        '',
        f'Total tables: {len(tables)}',
        '',
        '## Tables by Domain',
        ''
    ]
    
    total_columns = 0
    for domain, table_list in categorized_tables.items():
        if not table_list:
            continue
            
        lines.append(f'### {domain} ({len(table_list)} tables)')
        lines.append('')
        
        for table_name in sorted(table_list):
            if table_name in tables:
                table_data = tables[table_name]
                column_count = len(table_data['columns'])
                total_columns += column_count
                
                pk_count = len([col for col in table_data['columns'] if col['primary_key']])
                fk_count = len([col for col in table_data['columns'] if col['name'].endswith('_id') and not col['primary_key']])
                
                lines.append(f'- **{table_name}** ({column_count} columns, {pk_count} PK, {fk_count} FK)')
        
        lines.append('')
    
    lines.extend([
        f'## Summary Statistics',
        f'- Total tables: {len(tables)}',
        f'- Total columns: {total_columns}',
        f'- Average columns per table: {total_columns / len(tables):.1f}',
        ''
    ])
    
    return '\n'.join(lines)

if __name__ == "__main__":
    print("Extracting OpenMRS database schema...")
    
    # Extract from both sources
    entities = extract_entities_from_annotations()
    tables = extract_tables_from_liquibase()
    
    print(f"Found {len(entities)} entities and {len(tables)} tables")
    
    # Categorize tables
    categorized_tables = categorize_tables(tables)
    
    # Infer relationships
    relationships = infer_relationships(tables)
    print(f"Inferred {len(relationships)} relationships")
    
    # Generate PlantUML diagram
    plantuml_diagram = generate_plantuml_diagram(tables, relationships, categorized_tables)
    
    with open('/Users/shashivelur/Projects/openmrs-core/openmrs_complete_schema.puml', 'w') as f:
        f.write(plantuml_diagram)
    
    # Generate summary report
    summary_report = generate_summary_report(tables, categorized_tables)
    
    with open('/Users/shashivelur/Projects/openmrs-core/schema_analysis_report.md', 'w') as f:
        f.write(summary_report)
    
    print("Schema analysis complete!")
    print("Generated files:")
    print("- openmrs_complete_schema.puml (Complete ER diagram)")
    print("- schema_analysis_report.md (Summary report)")
    
    # Print some stats
    print(f"\nSchema Statistics:")
    print(f"- Total tables: {len(tables)}")
    print(f"- Total relationships: {len(relationships)}")
    print(f"- Domains: {len(categorized_tables)}")
