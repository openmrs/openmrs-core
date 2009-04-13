<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'compact'}">

		</c:when>
		<c:when test="${model.size == 'full'}">
			
			<openmrs:require privilege="Manage Observations" otherwise="/login.htm" redirect="/index.htm" />

			<openmrs:htmlInclude file="/scripts/obs.js" ></openmrs:htmlInclude>
			<openmrs:htmlInclude file="/dwr/interface/DWRObsService.js" ></openmrs:htmlInclude>
			
			<div id="findObservation">
				<b class="boxHeader"><spring:message code="ObsSearch.findOrEdit"/></b>
				<div class="box">
					<form>
						<spring:message code="general.search"/>
						<input type="radio" checked name="selectSearchStyle" value="byPatientAndConcept" onClick="hideDiv('searchByEncounter');showDiv('searchByPatientConcept');" />
						<spring:message code="ObsSearch.byPersonAndConcept" />
						&nbsp;&nbsp;&nbsp;&nbsp;
						<input type="radio" name="selectSearchStyle" value="byEncounter" onClick="hideDiv('searchByPatientConcept');showDiv('searchByEncounter');" />
						<spring:message code="ObsSearch.byEncounter" />
						<p />
						<div id="searchByPatientConcept">
							<table>
								<tr class="searchObsByPatient">
									<td><span><spring:message code="Obs.person" /></span>:</td>
									<!-- <spring:message code="ObsSearch.patientLabel" var="patientLabel" /> -->
									<td><openmrs_tag:personField formFieldName="personId" searchLabel="${personLabel}" initialValue="" linkUrl="" callback="" /></td>
								</tr>
								<tr class="searchObsByConcept">
									<td><span><spring:message code="Obs.concept" /></span> <span class="instructions">(<spring:message code="general.optional" />)</span>:</td>
									<!-- <spring:message code="ObsSearch.conceptLabel" var="conceptLabel" /> -->
									<td><openmrs_tag:conceptField formFieldName="conceptId" searchLabel="${conceptLabel}" initialValue="" /></td>
								</tr>
								<tr>
									<td colspan="2">
										<p />
										<input type="button" value="<spring:message code="general.cancel" />" onClick="obsSearchClear('personId', 'conceptId', '');" />
										<input type="button" value="<spring:message code="general.searchButton" />" onClick="obsSearch('personId', 'conceptId', '', 'obsTable', 'observationList');" />
									</td>
								</tr>
							</table>
						</div>
						<div id="searchByEncounter" style="display:none;">
							<table>
								<tr class="searchObsByEncounter">
									<td><span><spring:message code="Obs.encounter" /></span>:</td>
									<!-- <spring:message code="ObsSearch.encounterLabel" var="encounterLabel" /> -->
									<td><openmrs_tag:encounterField formFieldName="encounterId" searchLabel="${encounterLabel}" initialValue="" linkUrl="" callback="" /></td>
								</tr>
								<tr>
									<td colspan="2">
										<p />
										<input type="button" value="<spring:message code="general.cancel" />" onClick="obsSearchClear('', '', 'encounterId');" />
										<input type="button" value="<spring:message code="general.searchButton" />" onClick="obsSearch('', '', 'encounterId', 'obsTable', 'observationList');" />
									</td>
								</tr>
							</table>
						</div>
					</form>
				</div>
			</div>

			<div id="observationList" style="display:none;">
				<b class="boxHeader"><spring:message code="ObsSearch.results"/></b>
				<div class="box">
					<table width="100%">
						<thead>
							<tr>
								<th class="obsPersonHeader"><spring:message code="Obs.person" /></th>
								<th class="obsFormHeader"><spring:message code="Obs.form" /> (<spring:message code="Obs.encounterDate" />)</th>
								<th class="obsConceptHeader"><spring:message code="Obs.concept"/></th>
								<th class="obsValueHeader"><spring:message code="Obs.value"/></th>
								<th class="obsDateHeader"><spring:message code="Obs.date"/></th>
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
