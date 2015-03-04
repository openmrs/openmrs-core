<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Patients" otherwise="/login.htm" redirect="/admin/maintenance/mrnGenerator.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	function toggle(link, id) {
		var trace = document.getElementById(id);
		if (link.innerHTML == '<openmrs:message code="MRNGenerator.log.view"/>') {
			link.innerHTML = '<openmrs:message code="MRNGenerator.log.hide"/>';
			trace.style.display = "block";
		}
		else {
			link.innerHTML = '<openmrs:message code="MRNGenerator.log.view"/>';
			trace.style.display = "none";
		}
		return false;
	}
</script>

<style>
	#mrnLog {
		display: none;
	}
</style>

<br />
<h2><openmrs:message code="MRNGenerator.title"/></h2>
<br />

<form method="post" action="${pageContext.request.contextPath}/mrnGenerator">
	<table border="0" cellspacing="2" cellpadding="2">
		<tr>
			<td rowspan="4" align="left" valign="top">
				<label for="site"><openmrs:message code="MRNGenerator.select.site"/></label><br/>
				<select name="site" size="24">
					<optgroup label="Group A sites">
						<option value="BF">BF - Burnt Forest</option>
						<option value="MO">MO - Mosoriot</option>
						<option value="MT">MT - MTRH Adult</option>
						<option value="MP">MP - MTRH Pediatric</option>
						<option value="TU">TU - Turbo</option>
					</optgroup>
					<optgroup label="Group B sites">
						<option value="AM">AM - Amukura</option>
						<option value="AN">AN - Anderson</option>
						<option value="BS">BS - Busia</option>
						<option value="CH">CH - Chulaimbo</option>
						<option value="EG">EG - Mt. Elgon Clinic</option>
						<option value="KB">KB - Kabarnet</option>
						<option value="KH">KH - Khuyangu</option>
						<option value="KP">KP - Kapenguria</option>
						<option value="KT">KT - Kitale</option>
						<option value="NT">NT - Naitiri</option>
						<option value="PV">PV - Port Victoria</option>
						<option value="TE">TE - Iten</option>
						<option value="TS">TS - Teso</option>
						<option value="WB">WB - Webuye</option>
						<option value="ZW">ZW - Ziwa</option>
					</optgroup>
					<optgroup label="Other sites">
						<option value="HW">HW - Highway Clinic</option>
						<option value="PM">PM - PMTCT</option>
						<option value="VC">VC - OVC</option>
					</optgroup>
				</select>
			</td>
			<td align="left" valign="top">
				<label for="mrn_first"><openmrs:message code="MRNGenerator.starting.number"/></label><br>
				<input name="mrn_first" size="10" type="text">
			</td>
		</tr>
		<tr>
			<td align="left" valign="top">
				<label for="mrn_prefix"><openmrs:message code="MRNGenerator.prefix.number"/></label><br>
				<input type="text" size="4" name="mrn_prefix"/>
			</td>
		</tr>
		<tr>
			<td align="left" valign="top">
				<label for="mrn_count"><openmrs:message code="MRNGenerator.generate.number"/></label><br>
				<select name="mrn_count" size="4">
					  <option>100</option>
					  <option>500</option>
					  <option selected="true">1000</option>
					  <option>2000</option>
				</select>
			</td>
		</tr>
		<tr>
			<td>
				<input type="submit" value="<openmrs:message code="general.submit"/>">
			</td>
		</tr>
	</table>
</form>

<br/>
<a href="#toggle" onClick="return toggle(this, 'mrnLog')"><openmrs:message code="MRNGenerator.log.view"/></a>
<div id="mrnLog">
	<table cellpadding="4" cellspacing="0">
		<tr>
			<th><openmrs:message code="MRNGenerator.date"/></th>
			<th><openmrs:message code="MRNGenerator.generator"/></th>
			<th><openmrs:message code="MRNGenerator.site"/></th>
			<th><openmrs:message code="MRNGenerator.first"/></th>
			<th><openmrs:message code="MRNGenerator.count"/></th>
		</tr>
		<%
			pageContext.setAttribute("rows", org.openmrs.api.context.Context.getAdministrationService().getMRNGeneratorLog());
		%>
		<c:forEach items="${rows}" var="row" varStatus="status">
			<tr class="${status.index % 2 == 0 ? 'evenRow' : 'oddRow'}">
				<td>${row.date}</td>
				<td>${row.user}</td>
				<td>${row.site}</td>
				<td>${row.first}</td>
				<td>${row.count}</td>
			</tr>
		</c:forEach>
	</table>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>