<%@ page import="org.openmrs.web.Util" %>
<table>
	<tr>
		<td>Gender</td>
		<td>
			<select name="gender">
				<option value="M" <%= patient.getGender().equals("M") ? "selected" : "" %>>Male</option>
				<option value="F" <%= patient.getGender().equals("F") ? "selected" : "" %>>Female</option>
			</select>
		</td>
	</tr>
	<tr>
		<td>Race</td>
		<td><input type="text" name="race" id="race" size="10" value="${patient.race}" /></td>
	</tr>
	<tr>
		<td>Birthdate</td>
		<td colspan="3">
			<input type="text" name="birthdate" id="birthdate" size="10" 
				   value="<%= Util.formatTextBoxDate(patient.getBirthdate()) %>" />
			Estimated
			<input type="checkbox" name="birthdateEstimated" 
				   id="birthdateEstimated" value="true" 
				   <%= patient.isBirthdateEstimated().booleanValue() ? "checked" : "" %>/>
		</td>
	</tr>
	<tr>
		<td>Birthplace</td>
		<td><input type="text" name="birthplace" id="birthplace" value="${patient.birthplace}" /></td>
	<tr>
		<td>Tribe</td>
		<td>
			<select name="tribe">
					<openmrs:forEachRecord name="Tribe" select="${patient.tribe}">
						<option value="${record.tribeId}" ${selected}>
							${record.name}
						</option>
					</openmrs:forEachRecord>
			</select>
		</td>
	</tr>
	<tr>
		<td>Citizenship</td>
		<td><input type="text" name="citizenship" id="citizenship" value="${patient.citizenship}" /></td>
	</tr>
	<tr>
		<td>Mother's Name</td>
		<td><input type="text" name="mothersName" id="mothersName" value="${patient.mothersName}" /></td>
	</tr>
	<tr>
		<td>Civil Status</td>
		<td>
			<select name="civilStatus">
				<%	java.util.HashMap opts = new java.util.HashMap();
					opts.put("1", "Single");
					opts.put("2", "Married");
					opts.put("3", "Divorced");
					opts.put("4", "Widowed"); 
					pageContext.setAttribute("opts", opts); 
				%>
				<c:forEach var="opt" items="${opts}">
					<option value="<c:out value="${opt.key}" />" 
						<c:if test="${opt.key == patient.civilStatus}">selected</c:if>>
						<c:out value="${opt.value}" />
					</option> 
				</c:forEach>
			</select>
		</td>
	</tr>
	<tr>
		<td>Death Date</td>
		<td><input type="text" name="deathDate" id="deathDate" size="10" 
			value="<%= Util.formatTextBoxDate(patient.getDeathDate()) %>"/></td>
		<td>Cause of Death</td>
		<td><input type="text" name="causeOfDeath" id="causeOfDeath" value="${patient.causeOfDeath}" /></td>
	</tr>
	<tr>
		<td>Health District</td>
		<td><input type="text" name="healthDistrict" id="healthDistrict" value="${patient.healthDistrict}" /></td>
	</tr>
	<tr>
		<td>Health Center</td>
		<td><input type="text" name="healthCenter" id="healthCenter" value="${patient.healthCenter}" /></td>
	</tr>
</table>