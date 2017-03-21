package org.openmrs.api.impl;

import org.junit.Assert;

/**
 * Created by ira on 3/8/17.
 */
public class PersonServiceImplTest {
    /**
     * @verifies Return Null When Person Is Null
     * @see PersonServiceImpl#voidPerson(org.openmrs.Person, String)
     */
    @org.junit.Test
    public void voidPerson_shouldReturnNullWhenPersonIsNull() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies Return Null When Person Is Null
     * @see PersonServiceImpl#unvoidPerson(org.openmrs.Person)
     */
    @org.junit.Test
    public void unvoidPerson_shouldReturnNullWhenPersonIsNull() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies return null when personId is null
     * @see PersonServiceImpl#getPerson(Integer)
     */
    @org.junit.Test
    public void getPerson_shouldReturnNullWhenPersonIdIsNull() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies not allow someone to be in a relationship with themselves
     * @see PersonServiceImpl#saveRelationship(org.openmrs.Relationship)
     */
    @org.junit.Test
    public void saveRelationship_shouldNotAllowSomeoneToBeInARelationshipWithThemselves() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies void a relationship
     * @see PersonServiceImpl#voidRelationship(org.openmrs.Relationship, String)
     */
    @org.junit.Test
    public void voidRelationship_shouldVoidARelationship() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies save a relationship type
     * @see PersonServiceImpl#saveRelationshipType(org.openmrs.RelationshipType)
     */
    @org.junit.Test
    public void saveRelationshipType_shouldSaveARelationshipType() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies return a list of all person attribute types if viewtype is null
     * @see PersonServiceImpl#getPersonAttributeTypes(org.openmrs.util.OpenmrsConstants.PERSON_TYPE, org.openmrs.api.PersonService.ATTR_VIEW_TYPE)
     */
    @org.junit.Test
    public void getPersonAttributeTypes_shouldReturnAListOfAllPersonAttributeTypesIfViewtypeIsNull() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies parse a name with 2 given names and 1 family name
     * @see PersonServiceImpl#parsePersonName(String)
     */
    @org.junit.Test
    public void parsePersonName_shouldParseANameWith2GivenNamesAnd1FamilyName() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies parse a name with family name and 2 given names
     * @see PersonServiceImpl#parsePersonName(String)
     */
    @org.junit.Test
    public void parsePersonName_shouldParseANameWithFamilyNameAnd2GivenNames() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies return a map of relationships
     * @see PersonServiceImpl#getRelationshipMap(org.openmrs.RelationshipType)
     */
    @org.junit.Test
    public void getRelationshipMap_shouldReturnAMapOfRelationships() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies retire a RealtionshipType
     * @see PersonServiceImpl#retireRelationshipType(org.openmrs.RelationshipType, String)
     */
    @org.junit.Test
    public void retireRelationshipType_shouldRetireARealtionshipType() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies throw an error when trying to retire RelationshipType when the explanation string is of length 0
     * @see PersonServiceImpl#retireRelationshipType(org.openmrs.RelationshipType, String)
     */
    @org.junit.Test
    public void retireRelationshipType_shouldThrowAnErrorWhenTryingToRetireRelationshipTypeWhenTheExplanationStringIsOfLength0() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies should throw an error when trying to retire RelationshipType when the explanation string is null
     * @see PersonServiceImpl#retireRelationshipType(org.openmrs.RelationshipType, String)
     */
    @org.junit.Test
    public void retireRelationshipType_shouldShouldThrowAnErrorWhenTryingToRetireRelationshipTypeWhenTheExplanationStringIsNull() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies unretire a retired RelationshipType
     * @see PersonServiceImpl#unretireRelationshipType(org.openmrs.RelationshipType)
     */
    @org.junit.Test
    public void unretireRelationshipType_shouldUnretireARetiredRelationshipType() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }

    /**
     * @verifies unretire person attribute type when retire reason is not null nor empty
     * @see PersonServiceImpl#unretireRelationshipType(org.openmrs.RelationshipType)
     */
    @org.junit.Test
    public void unretireRelationshipType_shouldUnretirePersonAttributeTypeWhenRetireReasonIsNotNullNorEmpty() throws Exception {
        //TODO auto-generated
        Assert.fail("Not yet implemented");
    }
}
