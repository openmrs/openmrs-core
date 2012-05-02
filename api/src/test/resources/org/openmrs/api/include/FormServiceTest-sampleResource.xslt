<?xml version="1.0" encoding="UTF-8"?>

<!--
OpenMRS FormEntry Form HL7 Translation

This XSLT is used to translate OpenMRS forms from XML into HL7 2.5 format

@author Burke Mamlin, MD
@author Ben Wolfe
@author Jeremy Keiper
@version 1.9.9

1.9.9 - added nk1 (relationship) segment encoding template
1.9.8 - escape strings according to HL7 encoding rules
1.9.7 - moved encounter/encounter.encounter_id to use PV1-19 instead
1.9.6 - added encounter/encounter.encounter_id to PV1-1
1.9.5 - allow for organizing sections under "obs" section
1.9.4 - add support for message uid (as HL7 control id) and transform of patient.health_center to Discharge to Location (PV1-37)
1.9.3 - fixed rounding error on timestamp (tenths of seconds getting rounded up, causing "60" seconds in some cases) 
1.9.2 - first generally useful version
-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xdt="http://www.w3.org/2005/xpath-datatypes">
	<xsl:output method="text" version="1.0" encoding="UTF-8" indent="no"/>

<xsl:variable name="SENDING-APPLICATION">FORMENTRY</xsl:variable>
<xsl:variable name="SENDING-FACILITY">AMRS.ELD</xsl:variable>
<xsl:variable name="RECEIVING-APPLICATION">HL7LISTENER</xsl:variable>
<xsl:variable name="RECEIVING-FACILITY">AMRS.ELD</xsl:variable>
<xsl:variable name="PATIENT-AUTHORITY"></xsl:variable> <!-- leave blank for internal id, max 20 characters -->
                                                       <!-- for now, must match patient_identifier_type.name -->
<xsl:variable name="LOCAL-AUTHORITY">L</xsl:variable>
<xsl:variable name="PATIENT-ID-TYPE">PI</xsl:variable>
<xsl:variable name="PERSON-ID-TYPE">PN</xsl:variable>
<xsl:variable name="EXTERNAL-ID-TYPE">PT</xsl:variable>
<xsl:variable name="UUID-VERSION">v4</xsl:variable>
<xsl:variable name="FORM-AUTHORITY">AMRS.ELD.FORMID</xsl:variable> <!-- max 20 characters -->

<xsl:template match="/">
	<xsl:apply-templates />
</xsl:template>

<!-- Form template -->
<xsl:template match="form">
	<!-- MSH Header -->
	<xsl:text>MSH|^~\&amp;</xsl:text>   <!-- Message header, field separator, and encoding characters -->
	<xsl:text>|</xsl:text>              <!-- MSH-3 Sending application -->
	<xsl:value-of select="$SENDING-APPLICATION" />
	<xsl:text>|</xsl:text>              <!-- MSH-4 Sending facility -->
	<xsl:value-of select="$SENDING-FACILITY" />
	<xsl:text>|</xsl:text>              <!-- MSH-5 Receiving application -->
	<xsl:value-of select="$RECEIVING-APPLICATION" />
	<xsl:text>|</xsl:text>              <!-- MSH-6 Receiving facility -->
	<xsl:value-of select="$RECEIVING-FACILITY" />
	<xsl:text>|</xsl:text>              <!-- MSH-7 Date/time message sent -->
	<xsl:call-template name="hl7Timestamp">
		<xsl:with-param name="date" select="current-dateTime()" />
	</xsl:call-template>
	<xsl:text>|</xsl:text>              <!-- MSH-8 Security -->
	<xsl:text>|ORU^R01</xsl:text>       <!-- MSH-9 Message type ^ Event type (observation report unsolicited) -->
	<xsl:text>|</xsl:text>              <!-- MSH-10 Message control ID -->
	<xsl:choose>
		<xsl:when test="header/uid">
			<xsl:value-of select="header/uid" />
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="patient/patient.patient_id" />
			<xsl:call-template name="hl7Timestamp">
				<xsl:with-param name="date" select="current-dateTime()" />
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text>|P</xsl:text>             <!-- MSH-11 Processing ID -->
	<xsl:text>|2.5</xsl:text>           <!-- MSH-12 HL7 version -->
	<xsl:text>|1</xsl:text>             <!-- MSH-13 Message sequence number -->
	<xsl:text>|</xsl:text>              <!-- MSH-14 Continuation Pointer -->
	<xsl:text>|</xsl:text>              <!-- MSH-15 Accept Acknowledgement Type -->
	<xsl:text>|</xsl:text>              <!-- MSH-16 Application Acknowledgement Type -->
	<xsl:text>|</xsl:text>              <!-- MSH-17 Country Code -->
	<xsl:text>|</xsl:text>              <!-- MSH-18 Character Set -->
	<xsl:text>|</xsl:text>              <!-- MSH-19 Principal Language of Message -->
	<xsl:text>|</xsl:text>              <!-- MSH-20 Alternate Character Set Handling Scheme -->
	<xsl:text>|</xsl:text>              <!-- MSH-21 Message Profile Identifier -->
	<xsl:value-of select="@id" />
	<xsl:text>^</xsl:text>
	<xsl:value-of select="$FORM-AUTHORITY" />
	<xsl:text>&#x000d;</xsl:text>

	<!-- PID header -->
	<xsl:text>PID</xsl:text>            <!-- Message type -->
	<xsl:text>|</xsl:text>              <!-- PID-1 Set ID -->
	<xsl:text>|</xsl:text>              <!-- PID-2 (deprecated) Patient ID -->
	<xsl:text>|</xsl:text>              <!-- PID-3 Patient Identifier List -->
	<xsl:call-template name="patient_id">
		<xsl:with-param name="pid" select="patient/patient.patient_id" />
		<xsl:with-param name="auth" select="$LOCAL-AUTHORITY" />
		<xsl:with-param name="type" select="$PATIENT-ID-TYPE" />
	</xsl:call-template>
	<xsl:if test="patient/patient.previous_mrn and string-length(patient/patient.previous_mrn) > 0">
		<xsl:text>~</xsl:text>
		<xsl:call-template name="patient_id">
			<xsl:with-param name="pid" select="patient/patient.previous_mrn" />
			<xsl:with-param name="auth" select="$LOCAL-AUTHORITY" />
			<xsl:with-param name="type" select="'PRIOR'" />
		</xsl:call-template>
	</xsl:if>
	<!-- Additional patient identifiers -->
	<!-- This example is for an MTCT-PLUS identifier used in the AMPATH project in Kenya (skipped if not present) -->
	<xsl:if test="patient/patient.mtctplus_id and string-length(patient/patient.mtctplus_id) > 0">
		<xsl:text>~</xsl:text>
		<xsl:call-template name="patient_id">
			<xsl:with-param name="pid" select="patient/patient.mtctplus_id" />
			<xsl:with-param name="auth" select="'MTCTPLUS'" />
			<xsl:with-param name="type" select="$EXTERNAL-ID-TYPE" />
		</xsl:call-template>
	</xsl:if>
	<xsl:text>|</xsl:text>              <!-- PID-4 (deprecated) Alternate patient ID -->
	<!-- PID-5 Patient name -->
	<xsl:text>|</xsl:text>              <!-- Family name -->
	<xsl:value-of select="patient/patient.family_name" />
	<xsl:text>^</xsl:text>              <!-- Given name -->
	<xsl:value-of select="patient/patient.given_name" />
	<xsl:text>^</xsl:text>              <!-- Middle name -->
	<xsl:value-of select="patient/patient.middle_name" />
	<xsl:text>|</xsl:text>              <!-- PID-6 Mother's maiden name -->
	<xsl:text>|</xsl:text>              <!-- PID-7 Date/Time of Birth -->
	<xsl:value-of select="patient/patient.birthdate" />
	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->

	<!-- Relationship(s) -->
	<xsl:variable name="nk1List" select="patient/patient_relationship[patient_relationship.exists = 0]" />
	<!-- Relationship NK1(s) -->
	<xsl:for-each select="$nk1List">
		<xsl:if test="string-length(patient_relationship.relationship_type_id) > 0">
			<xsl:call-template name="nk1">
				<xsl:with-param name="setId" select="position()" />
				<xsl:with-param name="relationshipType" select="patient_relationship.relationship_type_id" />
				<xsl:with-param name="relationshipSide" select="patient_relationship.a_or_b" />
				<xsl:with-param name="relationshipDescription" select="patient_relationship.description" />
				<xsl:with-param name="relationshipVoided" select="patient_relationship.voided" />
				<xsl:with-param name="relative" select="relative" />
			</xsl:call-template>
		</xsl:if>
	</xsl:for-each>

	<!-- PV1 header -->
	<xsl:text>PV1</xsl:text>            <!-- Message type -->
	<xsl:text>|</xsl:text>              <!-- PV1-1 Sub ID -->
	<xsl:text>|O</xsl:text>             <!-- PV1-2 Patient class (O = outpatient) -->
	<xsl:text>|</xsl:text>              <!-- PV1-3 Patient location -->
	<xsl:value-of select="encounter/encounter.location_id" />
	<xsl:text>|</xsl:text>              <!-- PV1-4 Admission type (2 = return) -->
	<xsl:text>|</xsl:text>              <!-- PV1-5 Pre-Admin Number -->
	<xsl:text>|</xsl:text>              <!-- PV1-6 Prior Patient Location -->
	<xsl:text>|</xsl:text>              <!-- PV1-7 Attending Doctor -->
	<xsl:value-of select="encounter/encounter.provider_id" />
	<xsl:text>|</xsl:text>              <!-- PV1-8 Referring Doctor -->
	<xsl:text>|</xsl:text>              <!-- PV1-9 Consulting Doctor -->
	<xsl:text>|</xsl:text>              <!-- PV1-10 Hospital Service -->
	<xsl:text>|</xsl:text>              <!-- PV1-11 Temporary Location -->
	<xsl:text>|</xsl:text>              <!-- PV1-12 Preadmin Test Indicator -->
	<xsl:text>|</xsl:text>              <!-- PV1-13 Re-adminssion Indicator -->
	<xsl:text>|</xsl:text>              <!-- PV1-14 Admit Source -->
	<xsl:text>|</xsl:text>              <!-- PV1-15 Ambulatory Status -->
	<xsl:text>|</xsl:text>              <!-- PV1-16 VIP Indicator -->
	<xsl:text>|</xsl:text>              <!-- PV1-17 Admitting Doctor -->
	<xsl:text>|</xsl:text>              <!-- PV1-18 Patient Type -->
	<xsl:text>|</xsl:text>              <!-- PV1-19 Visit Number -->
	<xsl:value-of select="encounter/encounter.encounter_id" />
	<xsl:text>|</xsl:text>              <!-- PV1-20 Financial Class -->
	<xsl:text>|</xsl:text>              <!-- PV1-21 Charge Price Indicator -->
	<xsl:text>|</xsl:text>              <!-- PV1-22 Courtesy Code -->
	<xsl:text>|</xsl:text>              <!-- PV1-23 Credit Rating -->
	<xsl:text>|</xsl:text>              <!-- PV1-24 Contract Code -->
	<xsl:text>|</xsl:text>              <!-- PV1-25 Contract Effective Date -->
	<xsl:text>|</xsl:text>              <!-- PV1-26 Contract Amount -->
	<xsl:text>|</xsl:text>              <!-- PV1-27 Contract Period -->
	<xsl:text>|</xsl:text>              <!-- PV1-28 Interest Code -->
	<xsl:text>|</xsl:text>              <!-- PV1-29 Transfer to Bad Debt Code -->
	<xsl:text>|</xsl:text>              <!-- PV1-30 Transfer to Bad Debt Date -->
	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Agency Code -->
	<xsl:text>|</xsl:text>              <!-- PV1-31 Bad Debt Transfer Amount -->
	<xsl:text>|</xsl:text>              <!-- PV1-33 Bad Debt Recovery Amount -->
	<xsl:text>|</xsl:text>              <!-- PV1-34 Delete Account Indicator -->
	<xsl:text>|</xsl:text>              <!-- PV1-35 Delete Account Date -->
	<xsl:text>|</xsl:text>              <!-- PV1-36 Discharge Disposition -->
	<xsl:text>|</xsl:text>              <!-- PV1-37 Discharge To Location -->
	<xsl:if test="patient/patient.health_center">
		<xsl:value-of select="replace(patient/patient.health_center,'\^','&amp;')" />
	</xsl:if>
	<xsl:text>|</xsl:text>              <!-- PV1-38 Diet Type -->
	<xsl:text>|</xsl:text>              <!-- PV1-39 Servicing Facility -->
	<xsl:text>|</xsl:text>              <!-- PV1-40 Bed Status -->
	<xsl:text>|</xsl:text>              <!-- PV1-41 Account Status -->
	<xsl:text>|</xsl:text>              <!-- PV1-42 Pending Location -->
	<xsl:text>|</xsl:text>              <!-- PV1-43 Prior Temporary Location -->
	<xsl:text>|</xsl:text>              <!-- PV1-44 Admit Date/Time -->
	<xsl:call-template name="hl7Date">
		<xsl:with-param name="date" select="encounter/encounter.encounter_datetime" />
	</xsl:call-template>
	<xsl:text>|</xsl:text>              <!-- PV1-45 Discharge Date/Time -->
	<xsl:text>|</xsl:text>              <!-- PV1-46 Current Patient Balance -->
	<xsl:text>|</xsl:text>              <!-- PV1-47 Total Charges -->
	<xsl:text>|</xsl:text>              <!-- PV1-48 Total Adjustments -->
	<xsl:text>|</xsl:text>              <!-- PV1-49 Total Payments -->
	<xsl:text>|</xsl:text>              <!-- PV1-50 Alternate Visit ID -->
	<xsl:text>|V</xsl:text>             <!-- PV1-51 Visit Indicator -->
	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->

	<!-- We use encounter date as the timestamp for each observation -->
	<xsl:variable name="encounterTimestamp">
		<xsl:call-template name="hl7Date">
			<xsl:with-param name="date" select="encounter/encounter.encounter_datetime" />
		</xsl:call-template>
	</xsl:variable>
	
	<!-- ORC Common Order Segment -->
	<xsl:text>ORC</xsl:text>            <!-- Message type -->
	<xsl:text>|RE</xsl:text>            <!-- ORC-1 Order Control (RE = obs to follow) -->
	<xsl:text>|</xsl:text>              <!-- ORC-2 Placer Order Number -->
	<xsl:text>|</xsl:text>              <!-- ORC-3 Filler Order Number -->
	<xsl:text>|</xsl:text>              <!-- ORC-4 Placer Group Number -->
	<xsl:text>|</xsl:text>              <!-- ORC-5 Order Status -->
	<xsl:text>|</xsl:text>              <!-- ORC-6 Response Flag -->
	<xsl:text>|</xsl:text>              <!-- ORC-7 Quantity/Timing -->
	<xsl:text>|</xsl:text>              <!-- ORC-8 Parent -->
	<xsl:text>|</xsl:text>              <!-- ORC-9 Date/Time of Transaction -->
	<xsl:call-template name="hl7Timestamp">
		<xsl:with-param name="date" select="xs:dateTime(header/date_entered)" />
	</xsl:call-template>
	<xsl:text>|</xsl:text>              <!-- ORC-10 Entered By -->
	<xsl:value-of select="header/enterer" />
	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->

	<!-- Observation(s) -->
	<xsl:variable name="obsList" select="obs/*[(@openmrs_concept and value and value/text() != '') or *[@openmrs_concept and text()='true']]|obs/*[not(@openmrs_concept)]/*[(@openmrs_concept and value and value/text() != '') or *[@openmrs_concept and text()='true']]" />
	<xsl:variable name="obsListCount" select="count($obsList)" as="xs:integer" />
	<!-- Observation OBR -->
	<xsl:text>OBR</xsl:text>            <!-- Message type -->
	<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->
	<xsl:text>1</xsl:text>
	<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->
	<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->
	<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->
	<xsl:value-of select="obs/@openmrs_concept" />
	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->

	<!-- observation OBXs -->
	<xsl:for-each select="$obsList">
		<xsl:choose>
			<xsl:when test="value">
				<xsl:call-template name="obsObx">
					<xsl:with-param name="setId" select="position()" />
					<xsl:with-param name="datatype" select="@openmrs_datatype" />
					<xsl:with-param name="units" select="@openmrs_units" />
					<xsl:with-param name="concept" select="@openmrs_concept" />
					<xsl:with-param name="date" select="date/text()" />
					<xsl:with-param name="time" select="time/text()" />
					<xsl:with-param name="value" select="value" />
					<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="setId" select="position()" />
				<xsl:for-each select="*[@openmrs_concept and text() = 'true']">
					<xsl:call-template name="obsObx">
						<xsl:with-param name="setId" select="$setId" />
						<xsl:with-param name="subId" select="concat($setId,position())" />
						<xsl:with-param name="datatype" select="../@openmrs_datatype" />
						<xsl:with-param name="units" select="../@openmrs_units" />
						<xsl:with-param name="concept" select="../@openmrs_concept" />
						<xsl:with-param name="date" select="../date/text()" />
						<xsl:with-param name="time" select="../time/text()" />
						<xsl:with-param name="value" select="@openmrs_concept" />
						<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />
					</xsl:call-template>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:for-each>
	
	<!-- Grouped observation(s) -->
	<xsl:variable name="obsGroupList" select="obs/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != '') or *[@openmrs_concept and text()='true']]]|obs/*[not(@openmrs_concept)]/*[@openmrs_concept and not(date) and *[(@openmrs_concept and value and value/text() != '') or *[@openmrs_concept and text()='true']]]" />
	<xsl:variable name="obsGroupListCount" select="count($obsGroupList)" as="xs:integer" />
	<xsl:for-each select="$obsGroupList">
		<!-- Observation OBR -->
		<xsl:text>OBR</xsl:text>            <!-- Message type -->
		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->
		<xsl:value-of select="$obsListCount + position()" />
		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->
		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->
		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->
		<xsl:value-of select="@openmrs_concept" />
		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->
		
		<!-- Generate OBXs -->
		<xsl:for-each select="*[(@openmrs_concept and value and value/text() != '') or *[@openmrs_concept and text()='true']]">
			<xsl:choose>
				<xsl:when test="value">
					<xsl:call-template name="obsObx">
						<xsl:with-param name="setId" select="position()" />
						<xsl:with-param name="subId" select="1" />
						<xsl:with-param name="datatype" select="@openmrs_datatype" />
						<xsl:with-param name="units" select="@openmrs_units" />
						<xsl:with-param name="concept" select="@openmrs_concept" />
						<xsl:with-param name="date" select="date/text()" />
						<xsl:with-param name="time" select="time/text()" />
						<xsl:with-param name="value" select="value" />
						<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="setId" select="position()" />
					<xsl:for-each select="*[@openmrs_concept and text() = 'true']">
						<xsl:call-template name="obsObx">
							<xsl:with-param name="setId" select="$setId" />
							<xsl:with-param name="subId" select="concat('1.',position())" />
							<xsl:with-param name="datatype" select="../@openmrs_datatype" />
							<xsl:with-param name="units" select="../@openmrs_units" />
							<xsl:with-param name="concept" select="../@openmrs_concept" />
							<xsl:with-param name="date" select="../date/text()" />
							<xsl:with-param name="time" select="../time/text()" />
							<xsl:with-param name="value" select="@openmrs_concept" />
							<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />
						</xsl:call-template>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:for-each>

	<!-- Problem list(s) -->
	<xsl:variable name="problemList" select="problem_list/*[value[text() != '']]" />
	<xsl:variable name="problemListCount" select="count($problemList)" as="xs:integer" />
	<xsl:if test="$problemList">
		<!-- Problem list OBR -->
		<xsl:text>OBR</xsl:text>            <!-- Message type -->
		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->
		<xsl:value-of select="$obsListCount + $obsGroupListCount + 1" />
		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->
		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->
		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->
		<xsl:value-of select="problem_list/@openmrs_concept" />
		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->

		<!-- Problem list OBXs -->
		<xsl:for-each select="$problemList">
			<xsl:call-template name="obsObx">
				<xsl:with-param name="setId" select="position()" />
				<xsl:with-param name="datatype" select="'CWE'" />
				<xsl:with-param name="concept" select="@openmrs_concept" />
				<xsl:with-param name="date" select="date/text()" />
				<xsl:with-param name="time" select="time/text()" />
				<xsl:with-param name="value" select="value" />
				<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />
			</xsl:call-template>		
		</xsl:for-each>
	</xsl:if>
	
	<!-- Orders -->
	<xsl:variable name="orderList" select="orders/*[*[@openmrs_concept and ((value and value/text() != '') or *[@openmrs_concept and text() = 'true'])]]" />
	<xsl:variable name="orderListCount" select="count($orderList)" as="xs:integer" />
	<xsl:for-each select="$orderList">
		<!-- Order section OBR -->
		<xsl:text>OBR</xsl:text>            <!-- Message type -->
		<xsl:text>|</xsl:text>              <!-- OBR-1 Set ID -->
		<xsl:value-of select="$obsListCount + $obsGroupListCount + $problemListCount + 1" />
		<xsl:text>|</xsl:text>              <!-- OBR-2 Placer order number -->
		<xsl:text>|</xsl:text>              <!-- OBR-3 Filler order number -->
		<xsl:text>|</xsl:text>              <!-- OBR-4 OBR concept -->
		<xsl:value-of select="@openmrs_concept" />
		<xsl:text>&#x000d;</xsl:text>       <!-- new line -->
	
		<!-- Order OBXs -->
		<xsl:for-each select="*[@openmrs_concept and ((value and value/text() != '') or *[@openmrs_concept and text() = 'true'])]">
			<xsl:choose>
				<xsl:when test="value">
					<xsl:call-template name="obsObx">
						<xsl:with-param name="setId" select="position()" />
						<xsl:with-param name="datatype" select="@openmrs_datatype" />
						<xsl:with-param name="units" select="@openmrs_units" />
						<xsl:with-param name="concept" select="@openmrs_concept" />
						<xsl:with-param name="date" select="date/text()" />
						<xsl:with-param name="time" select="time/text()" />
						<xsl:with-param name="value" select="value" />
						<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="setId" select="position()" />
					<xsl:for-each select="*[@openmrs_concept and text() = 'true']">
						<xsl:call-template name="obsObx">
							<xsl:with-param name="setId" select="$setId" />
							<xsl:with-param name="subId" select="position()" />
							<xsl:with-param name="datatype" select="../@openmrs_datatype" />
							<xsl:with-param name="units" select="../@openmrs_units" />
							<xsl:with-param name="concept" select="../@openmrs_concept" />
							<xsl:with-param name="date" select="../date/text()" />
							<xsl:with-param name="time" select="../time/text()" />
							<xsl:with-param name="value" select="@openmrs_concept" />
							<xsl:with-param name="encounterTimestamp" select="$encounterTimestamp" />
						</xsl:call-template>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>	
	</xsl:for-each>
	
</xsl:template>

<!-- Patient Identifier (CX) generator -->
<xsl:template name="patient_id">
	<xsl:param name="pid" />
	<xsl:param name="auth" />
	<xsl:param name="type" />
	<xsl:value-of select="$pid" />
	<xsl:text>^</xsl:text>              <!-- Check digit -->
	<xsl:text>^</xsl:text>              <!-- Check Digit Scheme -->
	<xsl:text>^</xsl:text>              <!-- Assigning Authority -->
	<xsl:value-of select="$auth" />
	<xsl:text>^</xsl:text>              <!-- Identifier Type -->
	<xsl:value-of select="$type" />
</xsl:template>

<!-- Next of Kin (NK1) generator -->
<xsl:template name="nk1">
	<xsl:param name="setId" />
	<xsl:param name="relationshipType" />
	<xsl:param name="relationshipSide" />
	<xsl:param name="relationshipDescription" />
	<xsl:param name="relationshipVoided" />
	<xsl:param name="relative" />
	<xsl:text>NK1</xsl:text>       <!-- message type -->
	<xsl:text>|</xsl:text>              <!-- NK1-1  Set Id -->
	<xsl:value-of select="$setId" />
	<xsl:text>|</xsl:text>              <!-- NK1-2  Next of Kin Name -->
	<xsl:value-of select="$relative/relative.family_name" />
	<xsl:text>^</xsl:text>              <!-- Given name -->
	<xsl:value-of select="$relative/relative.given_name" />
	<xsl:text>^</xsl:text>              <!-- Middle name -->
	<xsl:value-of select="$relative/relative.middle_name" />
	<xsl:text>|</xsl:text>              <!-- NK1-3  Next of Kin Relation -->
	<xsl:value-of select="$relationshipType" />
	<xsl:value-of select="$relationshipSide" />
	<xsl:text>^</xsl:text>
	<xsl:value-of select="$relationshipDescription" />
	<xsl:text>^99REL</xsl:text>
	<xsl:text>|</xsl:text>              <!-- NK1-4  Next of Kin Address -->
	<xsl:text>|</xsl:text>              <!-- NK1-5  Next of Kin Phone -->
	<xsl:text>|</xsl:text>              <!-- NK1-6  Business Phone No. -->
	<xsl:text>|</xsl:text>              <!-- NK1-7  Contact Role -->
	<xsl:text>|</xsl:text>              <!-- NK1-8  Start Date -->
	<xsl:text>|</xsl:text>              <!-- NK1-9  End Date -->
	<xsl:text>|</xsl:text>              <!-- NK1-10 Assoc. Party's Job Title -->
	<xsl:text>|</xsl:text>              <!-- NK1-11 Assoc. Party's Job Class -->
	<xsl:text>|</xsl:text>              <!-- NK1-12 Assoc. Party's Employee Number -->
	<xsl:text>|</xsl:text>              <!-- NK1-13 Organization Name -->
	<xsl:text>|</xsl:text>              <!-- NK1-14 Marital Status -->
	<xsl:text>|</xsl:text>              <!-- NK1-15 Sex -->
	<xsl:value-of select="$relative/relative.gender"/>
	<xsl:text>|</xsl:text>              <!-- NK1-16 Date Time of Birth -->
	<xsl:value-of select="$relative/relative.birthdate" />
	<xsl:text>|</xsl:text>              <!-- NK1-17 Living Dependency -->
	<xsl:text>|</xsl:text>              <!-- NK1-18 Ambulatory Status -->
	<xsl:text>|</xsl:text>              <!-- NK1-19 Citizenship -->
	<xsl:text>|</xsl:text>              <!-- NK1-20 Primary Language -->
	<xsl:text>|</xsl:text>              <!-- NK1-21 Living Arrangement -->
	<xsl:text>|</xsl:text>              <!-- NK1-22 Publicity Ind. -->
	<xsl:text>|</xsl:text>              <!-- NK1-23 Protection Ind. -->
	<xsl:text>|</xsl:text>              <!-- NK1-24 Student Ind. -->
	<xsl:text>|</xsl:text>              <!-- NK1-25 Religion -->
	<xsl:text>|</xsl:text>              <!-- NK1-26 Mother's Maiden Name -->
	<xsl:text>|</xsl:text>              <!-- NK1-27 Nationality -->
	<xsl:text>|</xsl:text>              <!-- NK1-28 Ethnic Group -->
	<xsl:text>|</xsl:text>              <!-- NK1-29 Contact Reason -->
	<xsl:text>|</xsl:text>              <!-- NK1-30 Contact Person's Name -->
	<xsl:text>|</xsl:text>              <!-- NK1-31 Contact Person's Phone Number -->
	<xsl:text>|</xsl:text>              <!-- NK1-32 Contact Person's Address -->
	<xsl:text>|</xsl:text>              <!-- NK1-33 Assoc. Party's Identifiers -->
	<xsl:if test="relative/relative.identifier and string-length(relative/relative.identifier) > 0">
		<xsl:call-template name="patient_id">
			<xsl:with-param name="pid" select="$relative/relative.identifier" />
			<xsl:with-param name="auth" select="$relative/relative.identifier_type" />
			<xsl:with-param name="type" select="$EXTERNAL-ID-TYPE"/>
		</xsl:call-template>
	</xsl:if>
	<!-- UUID only if present -->
	<xsl:if test="relative/relative.uuid and string-length(relative/relative.uuid) > 0">
		<xsl:if test="relative/relative.identifier and string-length(relative/relative.identifier) > 0">
			<xsl:text>~</xsl:text>
		</xsl:if>
		<xsl:call-template name="patient_id">
			<xsl:with-param name="pid" select="$relative/relative.uuid" />
			<xsl:with-param name="auth" select="'UUID'" />
			<xsl:with-param name="type" select="$UUID-VERSION"/>
		</xsl:call-template>
	</xsl:if>
	<xsl:text>|</xsl:text>              <!-- NK1-34 Job Status -->
	<xsl:text>|</xsl:text>              <!-- NK1-35 Race -->
	<xsl:text>|</xsl:text>              <!-- NK1-36 Handicap -->
	<xsl:text>|</xsl:text>              <!-- NK1-37 Contact Person's Social Security Number -->
	<xsl:text>&#x000d;</xsl:text>       <!-- new line -->
</xsl:template>

<!-- OBX Generator -->
<xsl:template name="obsObx">
	<xsl:param name="setId" required="no"></xsl:param>
	<xsl:param name="subId" required="no"></xsl:param>
	<xsl:param name="datatype" required="yes" />
	<xsl:param name="concept" required="yes" />
	<xsl:param name="date" required="no"></xsl:param>
	<xsl:param name="time" required="no"></xsl:param>
	<xsl:param name="value" required="no"></xsl:param>
	<xsl:param name="units" required="no"></xsl:param>
	<xsl:param name="encounterTimestamp" required="yes" />
	<xsl:text>OBX</xsl:text>                     <!-- Message type -->
	<xsl:text>|</xsl:text>                       <!-- Set ID -->
	<xsl:value-of select="$setId" />
	<xsl:text>|</xsl:text>                       <!-- Observation datatype -->
	<xsl:choose>
		<xsl:when test="$datatype = 'BIT'">
			<xsl:text>NM</xsl:text>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$datatype" />
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text>|</xsl:text>                       <!-- Concept (what was observed -->
	<xsl:value-of select="$concept" />
	<xsl:text>|</xsl:text>                       <!-- Sub-ID -->
	<xsl:value-of select="$subId" />
	<xsl:text>|</xsl:text>                       <!-- Value -->
	<xsl:choose>
		<xsl:when test="$datatype = 'TS'">
			<xsl:call-template name="hl7Timestamp">
				<xsl:with-param name="date" select="$value" />
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="$datatype = 'DT'">
			<xsl:call-template name="hl7Date">
				<xsl:with-param name="date" select="$value" />
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="$datatype = 'TM'">
			<xsl:call-template name="hl7Time">
				<xsl:with-param name="time" select="$value" />
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="$datatype = 'BIT'">
			<xsl:choose>
				<xsl:when test="$value = '0' or upper-case($value) = 'FALSE'">0</xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>
		</xsl:when>
		<xsl:when test="$datatype = 'ST'">
			<xsl:call-template name="encodeString">
				<xsl:with-param name="value" select="$value" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$value" />
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text>|</xsl:text>                       <!-- Units -->
	<xsl:value-of select="$units" />
	<xsl:text>|</xsl:text>                       <!-- Reference range -->
	<xsl:text>|</xsl:text>                       <!-- Abnormal flags -->
	<xsl:text>|</xsl:text>                       <!-- Probability -->
	<xsl:text>|</xsl:text>                       <!-- Nature of abnormal test -->
	<xsl:text>|</xsl:text>                       <!-- Observation result status -->
	<xsl:text>|</xsl:text>                       <!-- Effective date -->
	<xsl:text>|</xsl:text>                       <!-- User defined access checks -->
	<xsl:text>|</xsl:text>                       <!-- Date time of observation -->
	<xsl:choose>
		<xsl:when test="$date and $time">
			<xsl:call-template name="hl7Timestamp">
				<xsl:with-param name="date" select="dateTime($date,$time)" />
			</xsl:call-template>
		</xsl:when>
		<xsl:when test="$date">
			<xsl:call-template name="hl7Date">
				<xsl:with-param name="date" select="$date" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$encounterTimestamp" />
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text>&#x000d;</xsl:text>
</xsl:template>

<!-- Generate HL7-formatted timestamp -->
<xsl:template name="hl7Timestamp">
	<xsl:param name="date" />
	<xsl:if test="string($date) != ''">
		<xsl:value-of select="concat(year-from-dateTime($date),format-number(month-from-dateTime($date),'00'),format-number(day-from-dateTime($date),'00'),format-number(hours-from-dateTime($date),'00'),format-number(minutes-from-dateTime($date),'00'),format-number(floor(seconds-from-dateTime($date)),'00'))" />
	</xsl:if>
</xsl:template>

<!-- Generate HL7-formatted date -->
<xsl:template name="hl7Date">
	<xsl:param name="date" />
	<xsl:if test="string($date) != ''">
		<xsl:choose>
			<xsl:when test="contains(string($date),'T')">
				<xsl:call-template name="hl7Date">
					<xsl:with-param name="date" select="xs:date(substring-before($date,'T'))" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
					<xsl:value-of select="concat(year-from-date($date),format-number(month-from-date($date),'00'),format-number(day-from-date($date),'00'))" />
			</xsl:otherwise>
		</xsl:choose>				
	</xsl:if>
</xsl:template>

<!-- Generate HL7-formatted time -->
<xsl:template name="hl7Time">
	<xsl:param name="time" />
	<xsl:if test="$time != ''">
		<xsl:value-of select="concat(format-number(hours-from-time($time),'00'),format-number(minutes-from-time($time),'00'),format-number(floor(seconds-from-time($time)),'00'))" />
	</xsl:if>
</xsl:template>

<!-- Escape text according to HL7 encoding rules -->
<xsl:template name="encodeString">
	<xsl:param name="value" />
	<xsl:if test="$value != ''">
		<xsl:value-of  select="replace(replace(replace(replace(replace($value,'\\','\\E\\'),'\|','\\F\\'),'\^','\\S\\'),'&amp;','\\T\\'),'~','\\R\\')" />
	</xsl:if>
</xsl:template>

</xsl:stylesheet>
