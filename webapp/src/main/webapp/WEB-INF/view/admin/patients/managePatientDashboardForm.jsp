<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/patients/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	$j(document).ready(function() {
		<c:forEach var="entry" items="${ajaxProperties}">
			var radioId = "${entry.key}" + "${entry.value}" + "Radio";
			document.getElementById(radioId).checked = true;
		</c:forEach>
		<c:forEach var="dynamicEntry" items="${dynamicModuleAjaxProperties}">
			var radioId = "${dynamicEntry.key}" + "${dynamicEntry.value}" + "Radio";
			document.getElementById(radioId).checked = true;
		</c:forEach>
	})


	function changeStatusClicked(button) {
	    var form = document.createElement("form");
	    form.setAttribute("method", "post");
	    form.setAttribute("action", "savePatientDashboard.form");
		var params = {};
		
		var tabId = button.id.replace("Button","");
		var onclickRadio = tabId + "OnclickRadio";
		var backgroundRadio = tabId + "BackgroundRadio";
		var preloadRadio = tabId + "PreloadRadio";

		params["tabId"] = tabId;
		if(document.getElementById(onclickRadio).checked) {
			params["status"] = "Onclick"
		} else if(document.getElementById(backgroundRadio).checked) {
			params["status"] = "Background";
		} else if(document.getElementById(preloadRadio).checked) {
			params["status"] = "Preload";
		}
	    for(var key in params) {
	        if(params.hasOwnProperty(key)) {
	            var hiddenField = document.createElement("input");
	            hiddenField.setAttribute("type", "hidden");
	            hiddenField.setAttribute("name", key);
	            hiddenField.setAttribute("value", params[key]);

	            form.appendChild(hiddenField);
	         }
	    }

	    document.body.appendChild(form);
	    form.submit();
	}

</script>

<div>
	<b class="boxHeader"><spring:message code="PatientDashboard.box.header"/></b>
	<div class="box">
		<table id="manageDashboardTable" cellpadding="2" cellspacing="0" style="width: 50%">
			<thead id="manageDashboardTableHeader">
				<tr>
					<th><span><spring:message code="PatientDashboard.tab.header"/></span></th>
					<th><span><spring:message code="PatientDashboard.current.header"/></span></th>
					<th><span><spring:message code="PatientDashboard.change.header"/></span></th>
				</tr>
			</thead>
			<tbody>
				<c:set var="count" value="0" scope="page" />
				<c:forEach var="entry" items="${ajaxProperties}" varStatus="status">
					<tr class="${status.index % 2 == 0 ? 'odd' : 'even'}">
						<td><spring:message code="patientDashboard.${entry.key}"/></td>
						<td>${ajaxLabelProperties[entry.key]}</td>
						<td>
							<input id="${entry.key}OnclickRadio" type="radio" name="${entry.key}" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
						    <input id="${entry.key}BackgroundRadio" type="radio" name="${entry.key}" value="background"/><spring:message code="PatientDashboard.status.background"/>
						    <input id="${entry.key}PreloadRadio" type="radio" name="${entry.key}" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
							<button id="${entry.key}Button" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
						</td>
					</tr>
					<c:set var="count" value="${count + 1}" scope="page"/>
				</c:forEach>
				<openmrs:extensionPoint pointId="org.openmrs.patientDashboardTab" type="html">
					<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
						<c:set var="keyValue" value="ajax${extension.tabId}Extension" scope="page"/>
						<tr class="${count % 2 == 0 ? 'odd' : 'even'}">
							<td><spring:message code="${extension.tabName}"/></td>
							<c:choose>
		    					<c:when test="${ajaxLabelProperties[keyValue] == null}">
		        					<td><spring:message code="PatientDashboard.status.preload"/></td>
		    					</c:when>
		    					<c:otherwise>
							       <td>${ajaxLabelProperties[keyValue]}</td>
							    </c:otherwise>
							</c:choose>
							<td>
								<input id="ajax${extension.tabId}ExtensionOnclickRadio" type="radio" name="${extension.tabId}" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
							    <input id="ajax${extension.tabId}ExtensionBackgroundRadio" type="radio" name="${extension.tabId}" value="background"/><spring:message code="PatientDashboard.status.background"/>
							    <c:choose>
			    					<c:when test="${ajaxLabelProperties[entry.key] == null}">
			        					 <input id="ajax${extension.tabId}ExtensionPreloadRadio" type="radio" name="${extension.tabId}" value="preload" checked="true"/><spring:message code="PatientDashboard.status.preload"/>
			    					</c:when>
			    					<c:otherwise>
								        <input id="ajax${extension.tabId}ExtensionPreloadRadio" type="radio" name="${extension.tabId}" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
								    </c:otherwise>
								</c:choose>
								<button id="ajax${extension.tabId}ExtensionButton" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
							</td>
						</tr>
						<c:set var="count" value="${count + 1}" scope="page"/>
					</openmrs:hasPrivilege>
				</openmrs:extensionPoint>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>