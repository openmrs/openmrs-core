<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/dwr/util.js" />

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'compact'}">

		</c:when>
		<c:when test="${model.size == 'full'}">
			
			<openmrs:require privilege="View Observations" otherwise="/login.htm" redirect="/index.htm" />

			<openmrs:htmlInclude file="/scripts/obs.js" ></openmrs:htmlInclude>
			<openmrs:htmlInclude file="/dwr/interface/DWRObsService.js" ></openmrs:htmlInclude>
			
			<div id="findObservation">
				<b class="boxHeader"><openmrs:message code="ObsSearch.findOrEdit"/></b>
				<div class="box">
					<form>
						<openmrs:message code="general.search"/>
						<input type="radio" checked name="selectSearchStyle" value="byPatientAndConcept" onClick="hideDiv('searchByEncounter');showDiv('searchByPatientConcept');" />
						<openmrs:message code="ObsSearch.byPersonAndConcept" />
						&nbsp;&nbsp;&nbsp;&nbsp;
						<input type="radio" name="selectSearchStyle" value="byEncounter" onClick="hideDiv('searchByPatientConcept');showDiv('searchByEncounter');" />
						<openmrs:message code="ObsSearch.byEncounter" />
						<p />
						<div id="searchByPatientConcept">
							<table>
								<tr class="searchObsByPatient">
									<td><span><openmrs:message code="Obs.person" /></span>:</td>
									<!-- <openmrs:message code="ObsSearch.patientLabel" var="patientLabel" /> -->
									<td><openmrs_tag:personField formFieldName="personId" formFieldId="personId" /></td>
								</tr>
								<tr class="searchObsByConcept">
									<td><span><openmrs:message code="Obs.concept" /></span> <span class="instructions">(<openmrs:message code="general.optional" />)</span>:</td>
									<td><openmrs_tag:conceptField formFieldName="conceptId" formFieldId="conceptId"/></td>
								</tr>
								<tr>
									<td colspan="2">
										<p />
										<input type="button" value="<openmrs:message code="general.cancel" />" onClick="obsSearchClear('personId', 'conceptId', '');" />
										<input type="button" value="<openmrs:message code="general.searchButton" />" onClick="obsSearch('personId', 'conceptId', '', 'obsTable', 'observationList');" />
									</td>
								</tr>
							</table>
						</div>
						<div id="searchByEncounter" style="display:none;">
							<table>
								<tr class="searchObsByEncounter">
									<td><span><openmrs:message code="Obs.encounter" /></span>:</td>
									<!-- <openmrs:message code="ObsSearch.encounterLabel" var="encounterLabel" /> -->
									<td><openmrs_tag:encounterField formFieldName="encounterId" formFieldId="encounterId" searchLabel="${encounterLabel}" initialValue="" linkUrl="" callback="" /></td>
								</tr>
								<tr>
									<td colspan="2">
										<p />
										<input type="button" value="<openmrs:message code="general.cancel" />" onClick="obsSearchClear('', '', 'encounterId');" />
										<input type="button" value="<openmrs:message code="general.searchButton" />" onClick="obsSearch('', '', 'encounterId', 'obsTable', 'observationList');" />
									</td>
								</tr>
							</table>
						</div>
					</form>
				</div>
			</div>

			<div id="observationList" style="display:none;">
				<b class="boxHeader"><openmrs:message code="ObsSearch.results"/></b>
				<div class="box">
					<table width="100%">
						<thead>
							<tr>
								<th class="obsPersonHeader"><openmrs:message code="Obs.person" /></th>
								<th class="obsFormHeader"><openmrs:message code="Obs.form" /> (<openmrs:message code="Obs.encounterDate" />)</th>
								<th class="obsConceptHeader"><openmrs:message code="Obs.concept"/></th>
								<th class="obsValueHeader"><openmrs:message code="Obs.value"/></th>
								<th class="obsDateHeader"><openmrs:message code="Obs.date"/></th>
							</tr>
						</thead>
						<tbody id="obsTable">
						</tbody>
					</table>
				</div>
			</div>
		</c:when>
		<c:otherwise>
			ERROR! unknown size '${model.size}' in FindObsWidget
		</c:otherwise>
	</c:choose>
</c:if>
