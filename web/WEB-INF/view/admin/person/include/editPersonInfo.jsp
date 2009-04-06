<c:if test="${empty INCLUDE_PERSON_GENDER || (INCLUDE_PERSON_GENDER == 'true')}">
	<tr>
		<td><spring:message code="Person.gender"/></td>
		<td><spring:bind path="gender">
				<openmrs:forEachRecord name="gender">
					<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
						<label for="${record.key}"> <spring:message code="Person.gender.${record.value}"/> </label>
				</openmrs:forEachRecord>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
</c:if>
<tr>
	<td>
		<spring:message code="Person.birthdate"/><br/>
		<i style="font-weight: normal; font-size: .8em;">(<spring:message code="general.format"/>: <openmrs:datePattern />)</i>
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
						ageBox.innerHTML = "(" + age + ' <spring:message code="Person.age.years"/>)';
					else if (age == 1)
						ageBox.innerHTML = '(1 <spring:message code="Person.age.year"/>)';
					else if (age == 0)
						ageBox.innerHTML = '( < 1 <spring:message code="Person.age.year"/>)';
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
					onClick="showCalendar(this)" />
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
		</spring:bind>
		
		<span id="age"></span> &nbsp; 
		
		<span id="birthdateEstimatedCheckbox" class="listItemChecked" style="padding: 5px;">
			<spring:bind path="birthdateEstimated">
				<label for="birthdateEstimatedInput"><spring:message code="Person.birthdateEstimated"/></label>
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

	<tr>
		<td><spring:message code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}" text="${attrType.name}"/></td>
		<td>
			<c:choose>
				<c:when test="${authorized == true}">
					<spring:bind path="attributeMap">
						<openmrs:fieldGen 
							type="${attrType.format}" 
							formFieldName="${attrType.personAttributeTypeId}" 
							val="${status.value[attrType.name].hydratedObject}" 
							parameters="optionHeader=[blank]|showAnswers=${attrType.foreignKey}" />
					</spring:bind>
				</c:when>
				<c:otherwise>
					<spring:bind path="attributeMap">${status.value[attrType.name].hydratedObject}</spring:bind>
				</c:otherwise>
			</c:choose>
		</td>
	</tr>

</openmrs:forEachDisplayAttributeType>

<tr>
	<td><spring:message code="Person.dead"/></td>
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
	<td><spring:message code="Person.deathDate"/></td>
	<td style="white-space: nowrap">
		<spring:bind path="deathDate">
			<input type="text" name="deathDate" size="10" 
				   value="${status.value}" onClick="showCalendar(this)" 
				   id="deathDate" />
			<i style="font-weight: normal; font-size: 0.8em;">(<spring:message code="general.format"/>: <openmrs:datePattern />)</i>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
		</spring:bind>
		&nbsp; &nbsp; 
		<spring:message code="Person.causeOfDeath"/>
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
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${status.value.personName} -
				<openmrs:formatDate path="dateCreated" type="long" />
			</td>
		</tr>
	</c:if>
</spring:bind>

<spring:bind path="changedBy">
	<c:if test="${status.value != null}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td colspan="2">
				${status.value.personName} -
				<openmrs:formatDate path="dateChanged" type="long" />
			</td>
		</tr>
	</c:if>
</spring:bind>

<tr>
	<td><spring:message code="general.voided"/></td>
	<td>
		<spring:bind path="voided">
			<input type="hidden" name="_${status.expression}"/>
			<input type="checkbox" name="${status.expression}"
				   <c:if test="${status.value == true}">checked</c:if> 
				   onClick="toggleLayer('personVoidReasonRow');"
			/>
		</spring:bind>
	</td>
</tr>
<tr  id="personVoidReasonRow" <spring:bind path="voided"><c:if test="${status.value == false}">style="display: none"</c:if></spring:bind> >
	<td><spring:message code="general.voidReason"/></td>
	<spring:bind path="voidReason">
		<td>
			<input type="text" name="${status.expression}" value="${status.value}" size="35"/>
			<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
		</td>
	</spring:bind>
</tr>

<spring:bind path="voidedBy">
	<c:if test="${status.value != null}" >
		<tr>
			<td><spring:message code="general.voidedBy"/></td>
			<td>
				${status.value.personName} -
				<openmrs:formatDate path="dateVoided" type="long" />
			</td>
		</tr>
	</c:if>
</spring:bind>