<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patients" otherwise="/login.htm" redirect="/admin/patients/index.htm" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />

<script type="text/javascript">
	var ajaxProperties = {};
	
	$j(document).ready(function() {
		<c:forEach var="entry" items="${ajaxProperties}">
			ajaxProperties["${entry.key}"] = "${entry.value}";
			var divId = "${entry.key}" + "${entry.value}" + "Radio";
			document.getElementById(divId).checked = true;
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
				<c:forEach var="entry" items="${ajaxProperties}" varStatus="status">
					<c:choose>
    					<c:when test="${status.count % 2 ne 0}">
        					<tr class="odd">
    					</c:when>
    					<c:otherwise>
					        <tr class="even">
					    </c:otherwise>
					</c:choose>
						<td><spring:message code="patientDashboard.${entry.key}"/></td>
						<td>${ajaxLabelProperties[entry.key]}</td>
						<td>
							<input id="${entry.key}OnclickRadio" type="radio" name="${entry.key}" value="onclick"/><spring:message code="PatientDashboard.status.onclick"/>
						    <input id="${entry.key}BackgroundRadio" type="radio" name="${entry.key}" value="background"/><spring:message code="PatientDashboard.status.background"/>
						    <input id="${entry.key}PreloadRadio" type="radio" name="${entry.key}" value="preload"/><spring:message code="PatientDashboard.status.preload"/>
							<button id="${entry.key}Button" onclick="return changeStatusClicked(this);"><spring:message code="PatientDashboard.button.save"/></button>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>