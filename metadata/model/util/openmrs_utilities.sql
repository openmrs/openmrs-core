delimiter //


############################################################
# openmrs_utilities is a collection of convenience functions
############################################################

#-----------------------------------------------------------
# openmrs_schema() provides the name of the openmrs schema.
# This is hackish way to specify the name as a constant to
# other routines that need it. 
#-----------------------------------------------------------
DROP FUNCTION IF EXISTS openmrs_schema;
CREATE FUNCTION openmrs_schema ()
	RETURNS VARCHAR(20)
	RETURN (SELECT 'openmrs' as 'openmrs_schema' from dual);
//

#-----------------------------------------------------------
# table_exists() is a convenience function used to check
# whether a given table exists in the database. 
#-----------------------------------------------------------
DROP FUNCTION IF EXISTS table_exists;
CREATE FUNCTION table_exists (table_to_find varchar(50))
	RETURNS INT(1)
	RETURN (SELECT COUNT(table_name)<>0 
		FROM information_schema.tables 
		WHERE table_schema = openmrs_schema() AND table_name = table_to_find);
//
# unit tests
select 'table_exists() Unit Tests' as 'Step:';
select '1' as 'expected_value', table_exists("concept_description");
select '0' as 'expected_value', table_exists("no such table");
select '1' as 'expected_value', table_exists("concept_proposal_tag_map");
select '1' as 'expected_value', table_exists("concept_synonym");

#-----------------------------------------------------------
# column_exists() is a convenience function used to check
# whether a given table has a particular column
#-----------------------------------------------------------
DROP FUNCTION IF EXISTS column_exists;
CREATE FUNCTION column_exists (table_to_find varchar(50),
	column_to_find varchar(20))
	RETURNS INT(1)
	RETURN (SELECT COUNT(column_name)<>0 from information_schema.columns 
		where table_schema=openmrs_schema() AND table_name=table_to_find
		AND column_name=column_to_find);
//
# unit tests
#select 'column_exists() Unit Tests' as 'Step:';
#select '1' as 'expected_value', column_exists("concept_name", "name");
#select '0' as 'expected_value', column_exists("concept_name", "no such column");
#select '1' as 'expected_value', column_exists("concept_name", "short_name");
