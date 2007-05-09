<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">

	<openmrs:require privilege="Add Patients" otherwise="/login.htm" redirect="/index.htm" />
	
	<div id="createPatient">
		<b class="boxHeader"><spring:message code="Person.create"/></b>
		<div class="box">
			<spring:message code="Person.search.instructions"/> <br/>
			
			<form method="get" action="${model.postURL}" onSubmit="return validateForm()">
				<table>
					<tr>
						<td><spring:message code="Person.name"/></td>
						<td>
							<input type="text" name="name" id="personName" size="40" onKeyUp="clearError('name')" />
							<span class="error" id="nameError"><spring:message code="Person.name.required"/></span>
						</td>
					</tr>
					<tr>
						<td><spring:message code="Person.birthyear"/></td>
						<td>
							<input type="text" name="birthyear" id="birthyear" size="5" value="" onKeyUp="clearError('birthyear')" />
							<spring:message code="Person.age.or"/>
							<input type="text" name="age" id="age" size="5" value="" onKeyUp="clearError('birthyear')" />
							<span class="error" id="birthyearError"><spring:message code="Person.birthyear.required"/></span>
						</td>
					</tr>
					<tr>
						<td><spring:message code="Person.gender"/></td>
						<td>
							<openmrs:forEachRecord name="gender">
								<input type="radio" name="gndr" id="gender-${record.key}" value="${record.key}"  onClick="clearError('gender')" /><label for="gender-${record.key}"> <spring:message code="Person.gender.${record.value}"/> </label>
							</openmrs:forEachRecord>
							<span class="error" id="genderError"><spring:message code="Person.gender.required"/></span>
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="submit" value='<spring:message code="Person.create"/>'/>
						</td>
					</tr>
				</table>
				<input type="hidden" name="personType" value="${model.personType}"/>
				<input type="hidden" name="viewType" value="${model.viewType}"/>
			</form>
		</div>
		
		<script type="text/javascript">
			clearError("name");
			clearError("birthyear");
			clearError("gender");
			
			function validateForm() {
				var name = document.getElementById("personName");
				var birthyear = document.getElementById("birthyear");
				var age = document.getElementById("age");
				var male = document.getElementById("gender-M");
				var female = document.getElementById("gender-F");
				var year = new Date().getFullYear();
				
				var result = true;
				if (name.value == "") {
					document.getElementById("nameError").style.display = "";
					result = false;
				}
				if ((birthyear.value == "" || birthyear.value.length < 4 || birthyear.value < (year-120) || birthyear.value > year) && age.value == "") {
					document.getElementById("birthyearError").style.display = "";
					result = false;
				}
				if (male.checked == false && female.checked == false) {
					document.getElementById("genderError").style.display = "";
					result = false;
				} 
				
				return result;
			}
			
			function clearError(errorName) {
				document.getElementById(errorName + "Error").style.display = "none";
			}
		</script>
		
	</div>

</c:if>
