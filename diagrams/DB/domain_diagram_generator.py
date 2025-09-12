#!/usr/bin/env python3
"""
OpenMRS Domain-Specific ER Diagram Generator
Creates separate ER diagrams for each major domain
"""

import os
import re
from pathlib import Path

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
                    auto_increment = 'autoIncrement="true"' in col_def
                    
                    columns.append({
                        'name': col_name,
                        'type': col_type,
                        'primary_key': is_pk,
                        'nullable': is_nullable,
                        'unique': is_unique,
                        'auto_increment': auto_increment
                    })
                
                tables[table_name] = {
                    'columns': columns
                }
    
    except Exception as e:
        print(f"Error reading Liquibase schema: {e}")
    
    return tables

def get_domain_definitions():
    """Define domains and their tables"""
    return {
        'patient_management': {
            'name': 'Patient Management',
            'tables': [
                'patient', 'person', 'person_name', 'person_address', 'person_attribute', 
                'person_attribute_type', 'patient_identifier', 'patient_identifier_type',
                'allergy', 'allergy_reaction', 'person_merge_log'
            ]
        },
        'clinical_encounters': {
            'name': 'Clinical Encounters & Observations',
            'tables': [
                'encounter', 'encounter_type', 'encounter_role', 'encounter_provider',
                'encounter_diagnosis', 'obs', 'obs_reference_range', 'diagnosis_attribute',
                'diagnosis_attribute_type'
            ]
        },
        'orders_medications': {
            'name': 'Orders & Medications',
            'tables': [
                'orders', 'order_type', 'order_group', 'order_group_attribute',
                'order_group_attribute_type', 'order_attribute', 'order_attribute_type',
                'order_frequency', 'drug', 'drug_ingredient', 'drug_reference_map',
                'medication_dispense', 'drug_order', 'test_order', 'referral_order'
            ]
        },
        'clinical_concepts': {
            'name': 'Clinical Concepts & Terminology',
            'tables': [
                'concept', 'concept_name', 'concept_description', 'concept_class',
                'concept_datatype', 'concept_answer', 'concept_set', 'concept_numeric',
                'concept_complex', 'concept_attribute', 'concept_attribute_type',
                'concept_reference_map', 'concept_reference_source', 'concept_reference_term',
                'concept_reference_term_map', 'concept_map_type', 'concept_reference_range',
                'concept_proposal', 'concept_state_conversion', 'concept_stop_word'
            ]
        },
        'programs_workflows': {
            'name': 'Programs & Workflows',
            'tables': [
                'program', 'program_workflow', 'program_workflow_state', 'patient_program',
                'patient_state', 'program_attribute_type', 'patient_program_attribute'
            ]
        },
        'visits_locations': {
            'name': 'Visits & Locations',
            'tables': [
                'visit', 'visit_type', 'visit_attribute', 'visit_attribute_type',
                'location', 'location_tag', 'location_tag_map', 'location_attribute',
                'location_attribute_type'
            ]
        },
        'users_security': {
            'name': 'Users, Roles & Security',
            'tables': [
                'users', 'role', 'privilege', 'user_role', 'role_role', 'role_privilege',
                'user_property', 'provider', 'provider_attribute', 'provider_attribute_type',
                'provider_role'
            ]
        },
        'forms_fields': {
            'name': 'Forms & Fields',
            'tables': [
                'form', 'form_field', 'form_resource', 'field', 'field_type', 'field_answer'
            ]
        },
        'conditions_relationships': {
            'name': 'Conditions & Relationships',
            'tables': [
                'conditions', 'relationship', 'relationship_type'
            ]
        }
    }

def infer_relationships_for_domain(tables, domain_tables):
    """Infer relationships within a domain"""
    relationships = []
    
    for table_name in domain_tables:
        if table_name not in tables:
            continue
            
        table_data = tables[table_name]
        for column in table_data['columns']:
            col_name = column['name']
            
            # Look for foreign key patterns within the domain
            if col_name.endswith('_id') and not column['primary_key']:
                referenced_table = col_name[:-3]
                
                # Special case mappings
                if referenced_table == 'user' and 'users' in domain_tables:
                    referenced_table = 'users'
                elif referenced_table == 'person' and 'person' in domain_tables:
                    pass  # Keep as person
                elif referenced_table == 'patient' and 'patient' in domain_tables:
                    pass  # Keep as patient
                elif referenced_table == 'encounter' and 'encounter' in domain_tables:
                    pass  # Keep as encounter
                
                if referenced_table in domain_tables and referenced_table in tables:
                    relationships.append((table_name, referenced_table, col_name))
    
    return relationships

def generate_domain_plantuml(domain_name, domain_tables, tables, relationships):
    """Generate PlantUML for a specific domain"""
    safe_name = domain_name.replace(' ', '_').replace('&', 'and')
    
    lines = [
        f'@startuml OpenMRS_{safe_name}_Domain',
        '!theme plain',
        'skinparam linetype ortho',
        'skinparam packageStyle rectangle',
        f'title OpenMRS {domain_name} Domain Model',
        ''
    ]
    
    # Generate entities
    for table_name in sorted(domain_tables):
        if table_name not in tables:
            continue
            
        table_data = tables[table_name]
        lines.append(f'entity "{table_name}" {{')
        
        # Add primary key columns first
        pk_columns = [col for col in table_data['columns'] if col['primary_key']]
        for col in pk_columns:
            auto_inc = ' AI' if col['auto_increment'] else ''
            lines.append(f'  + {col["name"]} : {col["type"]} <<PK{auto_inc}>>')
        
        # Add unique columns
        unique_columns = [col for col in table_data['columns'] if col['unique'] and not col['primary_key']]
        for col in unique_columns:
            prefix = '* ' if not col['nullable'] else '  '
            lines.append(f'{prefix}{col["name"]} : {col["type"]} <<UK>>')
        
        # Add foreign key columns
        fk_columns = [col for col in table_data['columns'] 
                     if col['name'].endswith('_id') and not col['primary_key'] and not col['unique']]
        for col in fk_columns:
            prefix = '* ' if not col['nullable'] else '  '
            lines.append(f'{prefix}{col["name"]} : {col["type"]} <<FK>>')
        
        # Add other important columns (limit to key ones)
        other_columns = [col for col in table_data['columns'] 
                       if not col['primary_key'] and not col['unique'] and not col['name'].endswith('_id')]
        
        # Prioritize important columns
        important_patterns = ['name', 'uuid', 'description', 'value', 'code', 'title', 'gender', 'birthdate']
        important_columns = []
        remaining_columns = []
        
        for col in other_columns:
            if any(pattern in col['name'].lower() for pattern in important_patterns):
                important_columns.append(col)
            else:
                remaining_columns.append(col)
        
        # Show important columns first (limit to 10 total non-FK columns)
        display_columns = important_columns[:8] + remaining_columns[:2]
        
        for col in display_columns:
            prefix = '* ' if not col['nullable'] else '  '
            lines.append(f'{prefix}{col["name"]} : {col["type"]}')
        
        if len(other_columns) > 10:
            lines.append('  ... (additional columns)')
        
        lines.append('}')
        lines.append('')
    
    # Add relationships
    lines.append('/' + '/' + ' Relationships')
    processed_relationships = set()
    
    for source_table, target_table, fk_column in relationships:
        rel_key = (source_table, target_table, fk_column)
        if rel_key not in processed_relationships:
            lines.append(f'{source_table} }}o--|| {target_table} : {fk_column}')
            processed_relationships.add(rel_key)
    
    lines.append('')
    lines.append('@enduml')
    return '\n'.join(lines)

def main():
    print("Generating domain-specific ER diagrams...")
    
    # Extract table definitions
    tables = extract_tables_from_liquibase()
    print(f"Found {len(tables)} tables")
    
    # Get domain definitions
    domains = get_domain_definitions()
    
    # Generate diagrams for each domain
    for domain_key, domain_info in domains.items():
        domain_name = domain_info['name']
        domain_tables = domain_info['tables']
        
        # Filter tables that actually exist
        existing_tables = [t for t in domain_tables if t in tables]
        
        if not existing_tables:
            print(f"No tables found for domain: {domain_name}")
            continue
        
        # Infer relationships within domain
        relationships = infer_relationships_for_domain(tables, existing_tables)
        
        # Generate PlantUML
        plantuml_content = generate_domain_plantuml(domain_name, existing_tables, tables, relationships)
        
        # Write to file
        filename = f"/Users/shashivelur/Projects/openmrs-core/openmrs_{domain_key}_domain.puml"
        with open(filename, 'w') as f:
            f.write(plantuml_content)
        
        print(f"Generated {domain_name}: {len(existing_tables)} tables, {len(relationships)} relationships")
    
    # Generate a comprehensive index
    index_content = generate_index_document(domains, tables)
    with open("/Users/shashivelur/Projects/openmrs-core/database_domains_index.md", 'w') as f:
        f.write(index_content)
    
    print(f"\nGenerated {len(domains)} domain-specific ER diagrams")
    print("Files created:")
    for domain_key in domains.keys():
        print(f"  - openmrs_{domain_key}_domain.puml")
    print("  - database_domains_index.md")

def generate_index_document(domains, tables):
    """Generate an index document listing all domains and their purposes"""
    lines = [
        '# OpenMRS Database Domain Models',
        '',
        'This document provides an overview of the OpenMRS database schema organized by functional domains.',
        '',
        '## Domain Overview',
        ''
    ]
    
    for domain_key, domain_info in domains.items():
        domain_name = domain_info['name']
        domain_tables = domain_info['tables']
        existing_tables = [t for t in domain_tables if t in tables]
        
        lines.extend([
            f'### {domain_name}',
            f'**File:** `openmrs_{domain_key}_domain.puml`',
            f'**Tables:** {len(existing_tables)}',
            '',
            f'**Purpose:** {get_domain_description(domain_key)}',
            '',
            '**Key Tables:**',
        ])
        
        # List key tables with brief descriptions
        for table in existing_tables[:5]:  # Show first 5 tables
            description = get_table_description(table)
            lines.append(f'- `{table}` - {description}')
        
        if len(existing_tables) > 5:
            lines.append(f'- ... and {len(existing_tables) - 5} more tables')
        
        lines.extend(['', '---', ''])
    
    lines.extend([
        '## Using the Diagrams',
        '',
        '1. **PlantUML Files**: Use with PlantUML to generate visual diagrams',
        '2. **Online Viewer**: Copy content to plantuml.com/plantuml for instant visualization',
        '3. **VS Code**: Use PlantUML extension for integrated viewing',
        '',
        '## Legend',
        '',
        '- **PK**: Primary Key',
        '- **FK**: Foreign Key',
        '- **UK**: Unique Key',
        '- **AI**: Auto-increment',
        '- `*` prefix: NOT NULL constraint',
        '',
        '## Relationships',
        '',
        '- `}o--||`: Many-to-One relationship',
        '- `||--o{`: One-to-Many relationship',
        '- `||--||`: One-to-One relationship',
    ])
    
    return '\n'.join(lines)

def get_domain_description(domain_key):
    """Get description for each domain"""
    descriptions = {
        'patient_management': 'Core patient demographics, identifiers, personal information, and allergies',
        'clinical_encounters': 'Clinical visits, observations, diagnoses, and encounter-related data',
        'orders_medications': 'Medical orders, prescriptions, medication dispensing, and drug information',
        'clinical_concepts': 'Medical terminology, concept dictionaries, and reference mappings',
        'programs_workflows': 'Care programs, treatment workflows, and patient program enrollment',
        'visits_locations': 'Patient visits, healthcare facility locations, and location management',
        'users_security': 'System users, roles, privileges, healthcare providers, and security',
        'forms_fields': 'Data collection forms, form fields, and form resources',
        'conditions_relationships': 'Patient conditions, diagnoses, and person-to-person relationships'
    }
    return descriptions.get(domain_key, 'No description available')

def get_table_description(table_name):
    """Get brief description for common tables"""
    descriptions = {
        'patient': 'Core patient records',
        'person': 'Person demographics and basic info',
        'person_name': 'Person name components',
        'person_address': 'Address information',
        'encounter': 'Clinical encounters/visits',
        'obs': 'Clinical observations and measurements',
        'orders': 'Medical orders and prescriptions',
        'concept': 'Medical concepts and terminology',
        'drug': 'Medication and drug information',
        'location': 'Healthcare facilities and locations',
        'users': 'System users and authentication',
        'provider': 'Healthcare providers',
        'visit': 'Patient visits',
        'form': 'Data collection forms',
        'program': 'Care programs',
        'allergy': 'Patient allergies and reactions'
    }
    return descriptions.get(table_name, 'Data table')

if __name__ == "__main__":
    main()
