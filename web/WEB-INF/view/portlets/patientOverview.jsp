<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<openmrs:globalProperty var="importantIdentifiers" key="patient_identifier.importantTypes" />
<openmrs:globalProperty key="use_patient_attribute.healthCenter" defaultValue="false" var="showHealthCenter"/>

<c:if test="${not empty importantIdentifiers}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="Patient.identifiers" /></div>
	<div class="box${model.patientVariation}">
		<openmrs:portlet url="patientIdentifiers" size="normal" patientId="${model.patientId}" parameters="showIfSet=true|showIfMissing=true|highlightIfMissing=false" />
	</div>
	<p>
</c:if>

<c:if test="${showHealthCenter == 'true'}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="Patient.healthCenter"/></div>
	<div class="box${model.patientVariation}">
		<table>
			<tr>
				<td><spring:message code="Patient.healthCenter" /></td>
				<td>
					<openmrs:fieldGen type="org.openmrs.Location" formFieldName="healthCenter" val="${patient.healthCenter}" parameters="optionHeader=[blank]|onChange=changeHealthCenter()" />
				</td>
				<td><input id="healthCenterSave" type="button" value="<spring:message code="general.save" />" onClick="saveHealthCenter();" disabled="true" /></td>
			</tr>
		</table>
		<script language="javascript">
			function changeHealthCenter() {
				var hcVal = DWRUtil.getValue("healthCenter");
				
				if ( hcVal ) {
					if ( hcVal != "" ) {
						//alert("healthCenterId is " + healthCenterId);
						document.getElementById("healthCenterSave").disabled = false;
					} else {
						//alert("healthCenterId is blank");
						document.getElementById("healthCenterSave").disabled = true;
					}
				} else {
					//alert("healthCenterId is null");
					document.getElementById("healthCenterSave").disabled = true;
				}	
			}
			
			function saveHealthCenter() {
				//alert("saving health center");

				var hcVal = DWRUtil.getValue("healthCenter");
				DWRPatientService.changeHealthCenter( ${model.patient.patientId}, hcVal, refreshPage );
			}
		</script>
	</div>
	<br/>
</c:if>


<div class="boxHeader${model.patientVariation}"><spring:message code="Program.title"/></div>
<div class="box${model.patientVariation}">
	<openmrs:portlet url="patientPrograms" id="patientPrograms" patientId="${patient.patientId}" parameters="allowEdits=true"/>
</div>
<br/>

<openmrs:globalProperty var="conceptIdsToUse" key="dashboard.overview.showConcepts" />
<c:if test="${not empty conceptIdsToUse}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="Patient.groups"/></div>
	<div class="box${model.patientVariation}">
		<openmrs:portlet url="customMostRecentObs" size="normal" patientId="${patient.patientId}" parameters="conceptIds=${conceptIdsToUse}|allowNew=true" />
	</div>
	
	<br/>
</c:if>

<openmrs:globalProperty var="relationshipTypesToShow" key="dashboard.relationships.show_types" defaultValue=""/>
<c:if test="${not empty relationshipTypesToShow}">
	<div class="boxHeader${model.patientVariation}"><spring:message code="Relationship.patient.providers" /></div>
	<div class="box${model.patientVariation}">
		<openmrs:portlet url="patientRelationships" size="normal" patientId="${patient.patientId}" parameters="allowEditShownTypes=true|allowAddShownTypes=false|allowAddOtherTypes=true|allowVoid=true|showFrom=false|showTo=true|showTypes=${relationshipTypesToShow}|showOtherTypes=false"/>
	</div>
</c:if>