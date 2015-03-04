<c:if test="${empty INCLUDE_PERSON_GENDER || (INCLUDE_PERSON_GENDER == 'true')}">
	<tr>
		<td><openmrs:message code="Person.gender"/></td>
		<td><spring:bind path="gender">
				<openmrs:forEachRecord name="gender">
					<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
						<label for="${record.key}"> <openmrs:message code="Person.gender.${record.value}"/> </label>
				</openmrs:forEachRecord>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
</c:if>
<tr>
	<td>
		<openmrs:message code="Person.birthdate"/><br/>
		<i style="font-weight: normal; font-size: .8em;">(<openmrs:message code="general.format"/>: <openmrs:datePattern />)</i>
	</td>
	<td colspan="3">
		<script type="text/javascript">
			function updateEstimated(txtbox) {
				var input = document.getElementById("birthdateEstimatedInput");
				if (input) {
					input.checked = false;
					input.parentNode.className = "";
				}
				else if (txtbox)
					txtbox.parentNode.className = "listItemChecked";
			}
			
			function updateAge() {
				var birthdateBox = document.getElementById('birthdate');
				var ageBox = document.getElementById('age');
				try {
					var birthdate = parseSimpleDate(birthdateBox.value, '<openmrs:datePattern />');
					var age = getAge(birthdate);
					if (age > 0)
						ageBox.innerHTML = "(" + age + ' <openmrs:message code="Person.age.years"/>)';
					else if (age == 1)
						ageBox.innerHTML = '(1 <openmrs:message code="Person.age.year"/>)';
					else if (age == 0)
						ageBox.innerHTML = '( < 1 <openmrs:message code="Person.age.year"/>)';
					else
						ageBox.innerHTML = '( ? )';
					ageBox.style.display = "";
				} catch (err) {
					ageBox.innerHTML = "";
					ageBox.style.display = "none";
				}
			}
		</script>
		<spring:bind path="birthdate">			
			<input type="text" 
					name="birthdate" size="10" id="birthdate"
					value="${status.value}"
					onChange="updateAge(); updateEstimated(this);"
					onfocus="showCalendar(this,60)" />
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
		</spring:bind>
		
		<span id="age"></span> &nbsp; 
		
		<span id="birthdateEstimatedCheckbox" class="listItemChecked" style="padding: 5px;">
			<spring:bind path="birthdateEstimated">
				<label for="birthdateEstimatedInput"><openmrs:message code="Person.birthdateEstimated"/></label>
				<input type="hidden" name="_birthdateEstimated">
				<input type="checkbox" name="birthdateEstimated" value="true" 
					   <c:if test="${status.value == true}">checked</c:if> 
					   id="birthdateEstimatedInput" 
					   onclick="if (!this.checked) updateEstimated()" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</span>
		
		<script type="text/javascript">
			if (document.getElementById("birthdateEstimatedInput").checked == false)
				updateEstimated();
			updateAge();
		</script>
	</td>
</tr>

<openmrs:forEachDisplayAttributeType personType="" displayType="all" var="attrType">
	<c:set var="authorized" value="false" />
	<c:choose>
		<c:when test="${not empty attrType.editPrivilege}">
			<openmrs:hasPrivilege privilege="${attrType.editPrivilege.privilege}">
				<c:set var="authorized" value="true" />
			</openmrs:hasPrivilege>
		</c:when>
		<c:otherwise>
			<c:set var="authorized" value="true" />
		</c:otherwise>
	</c:choose>
	<c:choose>
	<c:when test="${attrType.retired == true}"></c:when>
	<c:otherwise>
	<tr>
		<td><openmrs:message code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}" text="${attrType.name}"/></td>
		<td>
			<c:choose>
				<c:when test="${authorized == true}">
					<spring:bind path="allAttributeMap">
						<openmrs:fieldGen 
							type="${attrType.format}" 
							formFieldName="${attrType.personAttributeTypeId}" 
							val="${status.value[attrType.name].hydratedObject}" 
							parameters="optionHeader=[blank]|showAnswers=${attrType.foreignKey}|isNullable=false" /> <%-- isNullable=false so booleans don't have 'unknown' radiobox --%>
					</spring:bind>
				</c:when>
				<c:otherwise>
					<spring:bind path="attributeMap">${status.value[attrType.name]}</spring:bind>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>
	</c:otherwise>
	</c:choose>

</openmrs:forEachDisplayAttributeType>

<tr>
	<td><openmrs:message code="Person.dead"/></td>
	<td>
		<spring:bind path="dead">
			<input type="hidden" name="_${status.expression}"/>
			<input type="checkbox" name="${status.expression}" 
				   <c:if test="${status.value == true}">checked</c:if>
				   onclick="personDeadClicked(this)" id="personDead"
			/>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
		</spring:bind>
		<script type="text/javascript">
			function personDeadClicked(input) {
				if (input.checked) {
					document.getElementById("deathInformation").style.display = "";
				}
				else {
					document.getElementById("deathInformation").style.display = "none";
					document.getElementById("deathDate").value = "";
					if (document.getElementById("causeOfDeath"))
						document.getElementById("causeOfDeath").value = "";
				}
			}
		</script>
	</td>
</tr>
<tr id="deathInformation">
	<td><openmrs:message code="Person.deathDate"/></td>
	<td style="white-space: nowrap">
		<spring:bind path="deathDate">
			<input type="text" name="deathDate" size="10" 
				   value="${status.value}" onfocus="showCalendar(this)"
				   id="deathDate" />
			<i style="font-weight: normal; font-size: 0.8em;">(<openmrs:message code="general.format"/>: <openmrs:datePattern />)</i>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
		</spring:bind>
		&nbsp; &nbsp;

    	<openmrs:message code="Person.deathdateEstimated"/>
			<spring:bind path="deathdateEstimated">
				<input type="hidden" name="_${status.expression}"> 
                   <input type="checkbox" name="${status.expression}" value="true" 
					<c:if test="${status.value == true}">checked</c:if> 
					   id="deathdateEstimatedInput" 
				 />					
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
			&nbsp; 
		 
		<openmrs:message code="Person.causeOfDeath"/>
		<openmrs:globalProperty key="concept.causeOfDeath" var="conceptCauseOfDeath" />
		<openmrs:globalProperty key="concept.otherNonCoded" var="conceptOther" />
		<spring:bind path="causeOfDeath">
			<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="causeOfDeath" val="${status.value}" parameters="showAnswers=${conceptCauseOfDeath}|showOther=${conceptOther}|otherValue=${causeOfDeathOther}" />
			<%--<input type="text" name="causeOfDeath" value="${status.value}" id="causeOfDeath"/>--%>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
		</spring:bind>
		<script type="text/javascript">				
			//set up death info fields
			personDeadClicked(document.getElementById("personDead"));
		</script>
	</td>
</tr>

<spring:bind path="creator">
	<c:if test="${status.value != null}">
		<tr>
			<td><openmrs:message code="general.createdBy" /></td>
			<td>
				<c:out value="${status.value.personName}" /> -
				<openmrs:formatDate path="dateCreated" type="long" />
			</td>
		</tr>
	</c:if>
</spring:bind>

<spring:bind path="changedBy">
	<c:if test="${status.value != null}">
		<tr>
			<td><openmrs:message code="general.changedBy" /></td>
			<td colspan="2">
				<c:out value="${status.value.personName}" /> -
				<openmrs:formatDate path="dateChanged" type="long" />
			</td>
		</tr>
	</c:if>
</spring:bind>

<c:choose>
    <c:when test="${not empty patient}">
        <c:set var="voided" value="${patient.voided}" />
        <c:set var="voidReason" value="${patient.voidReason}" />
    </c:when>
    <c:otherwise>
        <c:set var="voided" value="${person.voided}" />
        <c:set var="voidReason" value="${person.voidReason}" />
    </c:otherwise>
</c:choose>

<tr>
	<td><openmrs:message code="general.voided"/></td>
	<td>${voided}</td>
</tr>

<tr id="personVoidReasonRow" <c:if test="${patient.voided == false}">style="display: none"</c:if> >
	<td><openmrs:message code="general.voidReason"/></td>
	<td>${voidReason}</td>
</tr>
<tr>
  	  <td><font color="#D0D0D0"><sub><openmrs:message code="general.uuid"/></sub></font></td>
      <td colspan="${fn:length(locales)}">
      <font color="#D0D0D0"><sub>
       <spring:bind path="person.uuid">
           <c:out value="${status.value}"></c:out>
       </spring:bind>
       </sub></font>
     </td>
    </tr>
<spring:bind path="voidedBy">
	<c:if test="${status.value != null}" >
		<tr>
			<td><openmrs:message code="general.voidedBy"/></td>
			<td>
				<c:out value="${status.value.personName}" /> -
				<openmrs:formatDate path="dateVoided" type="long" />
			</td>
		</tr>
	</c:if>
</spring:bind>