<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'compact'}">
			<form method=get action="${model.postURL}">
				<spring:message code="Navigation.findPatient" />
				<input type="text" name="phrase" value="<request:parameter name="phrase"/>"/>
				<input type="submit" value="<spring:message code="general.searchButton" />" />
			</form>
		</c:when>
		<c:when test="${model.size == 'slowConnection'}">

			<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />

			<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" ></openmrs:htmlInclude>
			<openmrs:htmlInclude file="/dwr/engine.js" ></openmrs:htmlInclude>
			<openmrs:htmlInclude file="/dwr/util.js" ></openmrs:htmlInclude>
			
			<div id="findPatient">
				<b class="boxHeader"><spring:message code="Patient.find"/></b>
				<div class="box">
					<form>
						<span style="white-space: nowrap">
							<span><spring:message code="PatientSearch.searchOnName"/></span>
							<input type="text" value="" id="pSearch" name="pSearch" autocomplete="off" />
							<input type="button" id="searchButton" value="Search" />
						</span>
						<span class="openmrsSearchDiv">
							<table class="openmrsSearchTable" cellpadding="2" cellspacing="0" style="width: 100%">
								<thead id="openmrsSearchTableHead">
									<tr></tr>
								</thead>
								<tbody id="objHitsTableBody" style="vertical-align: top">
									<tr>
										<td class="searchIndex"></td>
										<td></td>
									</tr>
								</tbody>
							</table>
						</span>
					</form>
				</div>
			</div>

			<script>
			
				var patient;
				var autoJump = true;
				<request:existsParameter name="autoJump">
					autoJump = <request:parameter name="autoJump"/>;
				</request:existsParameter>
				
				function showSearch() {
					findPatient.style.display = "";
					patientListing.style.display = "none";
					savedText = "";
					searchBox.focus();
				}
				
				function onSelect(arr) {
					if (arr[0].patientId != null) {
						document.location = "${model.postURL}?patientId=" + arr[0].patientId + "&phrase=" + savedText;
					}
					else if (arr[0].href != null) {
						document.location = arr[0].href;
					}
				}
				
				function findObjects(text) {
					if (text.length > 2) {
						savedText = text;
						DWRPatientService.findPatients(text, includeRetired, preFillTable);
					}
					else {
						var msg = new Array();
						msg.push("Invalid number of search characters");
						fillTable(msg, [getNumber, getString]);
					}
					patientListing.style.display = "";
					return false;
				}
				
				function allowAutoJump() {
					if (autoJump == false) {
						autoJump = true;
						return false;
					}
					//	only allow the first item to be automatically selected if:
					//		the entered text is a string or the entered text is a valid identifier
					//return (savedText.match(/\d/) == false || isValidCheckDigit(savedText));
					
					//don't autojump anymore.  keeping the above functionality
					//would require adding a field to PatientListItem, and this inelegant solution
					//doesn't seem worth the minimal functionality conferred above.
					return false;
				}
				
			</script>
			
			<script>
				
				var patientListing= document.getElementById("patientListing");
				var findPatient   = document.getElementById("findPatient");
				var searchBox		= document.getElementById("searchBox");
				var findPatientForm = document.getElementById("findPatientForm");
				
				function init() {
					dwr.util.useLoadingMessage();
				
					<request:existsParameter name="patientId">
						<!-- User has 'patientId' in the request params -- selecting that patient -->
						var pats = new Array();
						pats.push(new Object());
						pats[0].patientId = '<request:parameter name="patientId"/>';
						onSelect(pats);
					</request:existsParameter>
					
					<request:existsParameter name="phrase">
						<!-- User has 'phrase' in the request params -- searching on that -->
						searchBox.value = '<request:parameter name="phrase"/>';
					</request:existsParameter>
				
					showSearch();
			
					// creates back button functionality
					if (searchBox.value != "")
						search(searchBox, null, false, 0);
						
					changeClassProperty("description", "display", "none");
				}
					
				window.onload=init;
			</script>

		</c:when>
		<c:when test="${model.size == 'full'}">
			
			<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/index.htm" />

			<openmrs:htmlInclude file="/scripts/dojoConfig.js"></openmrs:htmlInclude>
			<openmrs:htmlInclude file="/scripts/dojo/dojo.js"></openmrs:htmlInclude>

			<script type="text/javascript">
				dojo.require("dojo.widget.openmrs.PatientSearch");
				
				dojo.addOnLoad( function() {
					
					searchWidget = dojo.widget.manager.getWidgetById("pSearch");
					dojo.event.topic.subscribe("pSearch/select", 
						function(msg) {
							if (msg.objs[0].patientId)
								document.location = "${model.postURL}?patientId=" + msg.objs[0].patientId + "&phrase=" + searchWidget.text;
							else if (msg.objs[0].href)
								document.location = msg.objs[0].href;
						}
					);
					
					<c:if test="${empty hideAddNewPatient}">
						searchWidget.addPatientLink = '<a href="#" onClick="return jumpToAddNew()"><spring:message javaScriptEscape="true" code="Patient.addNew"/></a>';
					</c:if>
					searchWidget.inputNode.select();
					changeClassProperty("description", "display", "none");
				});
				
			</script>
			
			<div id="findPatient">
				<b class="boxHeader"><spring:message code="Patient.find"/></b>
				<div class="box">
					<div dojoType="PatientSearch" widgetId="pSearch" <c:if test="${model.showIncludeVoided == 'true'}">showIncludeVoided="true"</c:if> searchLabel="<spring:message code="Patient.searchBox" htmlEscape="true"/>" showVerboseListing="true" patientId='<request:parameter name="patientId"/>' searchPhrase='<request:parameter name="phrase"/>' <c:if test="${not empty hideAddNewPatient}">showAddPatientLink='false'</c:if>></div>
				</div>
			</div>
			
			<c:if test="${empty model.hideAddNewPatient}">
				<openmrs:hasPrivilege privilege="Add Patients">
					<br/> &nbsp; <spring:message code="general.or"/><br/><br/>
					<openmrs:portlet id="addPersonForm" url="addPersonForm" parameters="personType=patient|postURL=admin/person/addPerson.htm|viewType=${model.viewType}" />
				</openmrs:hasPrivilege>
				
				<script type="text/javascript">
					function jumpToAddNew() {
						var searchWidget = dojo.widget.manager.getWidgetById("pSearch");
						searchWidget.clearSearch();
						document.getElementById("personName").focus();
						return false;
					}
				</script>
			</c:if>

		</c:when>
		<c:otherwise>
			<spring:message code="Portlet.findPatient.error" arguments="${model.size}"/>
		</c:otherwise>
	</c:choose>

	<p/>
	<openmrs:extensionPoint pointId="org.openmrs.findPatientPortlet.linksAtBottom" type="html"
		requiredClass="org.openmrs.module.web.extension.LinkExt">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
			<a href="${extension.url}"><spring:message code="${extension.label}"/></a>
			<br/>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>


	<p/>
	<openmrs:extensionPoint pointId="org.openmrs.findPatientPortlet.linksAtBottom" type="html"
		requiredClass="org.openmrs.module.web.extension.LinkExt">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
			<a href="${extension.url}"><spring:message code="${extension.label}"/></a>
			<br/>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>

</c:if>
