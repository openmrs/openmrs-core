<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="" otherwise="/login.htm" redirect="/options.htm" />

<h2><spring:message code="options.title"/></h2>

<form method="post" class="box">
	<table>
		<tr>
			<td>Default Location</td>
			<td><select name="location">
					<openmrs:forEachRecord name="location">
						<option value="${record.locationId}">${record.name}</option>
					</openmrs:forEachRecord>
				</select>
			</td>
		</tr>
		<tr>
			<td>Default Language</td>
			<td><select name="language">
					<option value="en">English</option>
					<option value="fr">Français</option>
					<option value="de">Deutsch</option>
				</select>
			</td>
		</tr>
	</table>
	
	<input type="submit" value="<spring:message code="options.save"/>">
</form>
 

<%@ include file="/WEB-INF/template/footer.jsp" %>