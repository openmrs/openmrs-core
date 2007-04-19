<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Add Patients" otherwise="/login.htm" redirect="/admin/patients/newPatient.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<script type="text/javascript">
	function addIdentifier(id, type, location, pref, oldIdentifier) {
		var tbody = document.getElementById('identifiersTbody');
		var row = document.getElementById('identifierRow');
		var newrow = row.cloneNode(true);
		newrow.style.display = "";
		newrow.id = tbody.childNodes.length;
		tbody.appendChild(newrow);
		var inputs = newrow.getElementsByTagName("input");
		var selects = newrow.getElementsByTagName("select");
		if (id) {
			for (var i in inputs) {
				if (inputs[i] && inputs[i].name == "identifier") {
					inputs[i].value = id;
					if (oldIdentifier && 1 == 0) {
						inputs[i].parentNode.appendChild(document.createTextNode(id));
						inputs[i].parentNode.removeChild(inputs[i]);
					}
				}
			}	
		}
		if (type) {
			for (var i in selects)
				if (selects[i] && selects[i].name == "identifierType") {
					var selectedOpt;
					for (o in selects[i].options) {
						if (selects[i].options[o].value == type) {
							selectedOpt = selects[i].options[o];
							selectedOpt.selected = true;
						}
						else
							selects[i].options[o].selected = false;
					}
					if (oldIdentifier && 1 == 0) {
						selects[i].parentNode.appendChild(document.createTextNode(selectedOpt.text));
						selects[i].parentNode.removeChild(selects[i]);
					}
				}
		}
		if (location) {
			for (var i in selects)
				if (selects[i] && selects[i].name == "location") {
					var selectedOpt;
					for (o in selects[i].options) {
						if (selects[i].options[o].value == location) {
							selectedOpt = selects[i].options[o];
							selectedOpt.selected = true;
						}
						else
							selects[i].options[o].selected = false;
					}
					if (oldIdentifier && 1 == 0) {
						selects[i].parentNode.appendChild(document.createTextNode(selectedOpt.text));
						selects[i].parentNode.removeChild(selects[i]);
					}
				}	
		}
		
		for (var i in inputs)
			if (inputs[i] && inputs[i].name == "preferred") {
				inputs[i].checked = (pref == true);
				inputs[i].value = id + type;
			}
		
		/*
		if (oldIdentifier) {
			for (var i in inputs) {
				if(inputs[i] && inputs[i].name == "closeButton")
					inputs[i].style.display = "none";
			}
		}
		*/

	}
	
	function updateAge() {
		var birthdateBox = document.getElementById('birthdate');
		var ageBox = document.getElementById('age');
		try {
			var birthdate = new Date(birthdateBox.value);
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
	
	function updateEstimated() {
		var input = document.getElementById("birthdateEstimatedInput");
		if (input) {
			input.checked = false;
			input.parentNode.className = "";
		}
		else
			input.parentNode.className = "listItemChecked";
	}
	
	// age function borrowed from http://anotherdan.com/2006/02/simple-javascript-age-function/
	function getAge(d, now) {
		var age = -1;
		if (typeof(now) == 'undefined') now = new Date();
		while (now >= d) {
			age++;
			d.setFullYear(d.getFullYear() + 1);
		}
		return age;
	}
	
	function removeRow(btn) {
		var parent = btn.parentNode;
		while (parent.tagName.toLowerCase() != "tr")
			parent = parent.parentNode;
		
		parent.style.display = "none";
	}
	
	function removeHiddenRows() {
		var rows = document.getElementsByTagName("TR");
		var i = 0;
		while (i < rows.length) {
			if (rows[i].style.display == "none") {
				rows[i].parentNode.removeChild(rows[i]);
			}
			else {
				i = i + 1;
			}
		}
	}
	
	function identifierOrTypeChanged(input) {
		var parent = input.parentNode;
		while (parent.tagName.toLowerCase() != "tr")
			parent = parent.parentNode;
		
		var inputs = parent.getElementsByTagName("input");
		var prefInput;
		var idInput;
		var typeInput;
		for (var i in inputs) {
			if (inputs[i] && inputs[i].name == "preferred")
				prefInput = inputs[i];
			else if (inputs[i] && inputs[i].name == "identifier")
				idInput = inputs[i];
		}
		inputs = parent.getElementsByTagName("select");
		for (var i in inputs)
			if (inputs[i] && inputs[i].name == "identifierType")
				typeInput = inputs[i];
		
		if (idInput && typeInput)
			prefInput.value = idInput.value + typeInput.value;
	}
	
</script>

<style>
	th { text-align: left; }
</style>

<openmrs:globalProperty key="use_patient_attribute.tribe" defaultValue="false" var="showTribe"/>
<openmrs:globalProperty key="use_patient_attribute.mothersName" defaultValue="false" var="showMothersName"/>

<spring:hasBindErrors name="patient">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${fn:replace(error, '--', '\\-\\-')} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post" action="newPatient.form" onSubmit="removeHiddenRows()">
	<c:if test="${patient.patientId == null}"><h2><spring:message code="Patient.create"/></h2></c:if>
	<c:if test="${patient.patientId != null}"><h2><spring:message code="Patient.edit"/></h2></c:if>

	<c:if test="${patient.patientId != null}">
		<a href="${pageContext.request.contextPath}/patientDashboard.form?patientId=${patient.patientId}">
			<spring:message code="patientDashboard.viewDashboard"/>
		</a>
		<p/>
	</c:if>
	
	<table cellspacing="2">
		<thead>
			<openmrs:portlet url="nameLayout" id="namePortlet" size="columnHeaders" parameters="layoutShowTable=false|layoutShowExtended=false" />
		</thead>
		<spring:nestedPath path="patient.name">
			<openmrs:portlet url="nameLayout" id="namePortlet" size="inOneRow" parameters="layoutMode=edit|layoutShowTable=false|layoutShowExtended=false" />
		</spring:nestedPath>
	</table>
	<br/>
	<table id="identifiers" cellspacing="2">
		<tr>
			<th><spring:message code="PatientIdentifier.identifier"/></th>
			<th><spring:message code="PatientIdentifier.identifierType"/></th>
			<th><spring:message code="PatientIdentifier.location.identifier"/></th>
			<th><spring:message code="general.preferred"/></th>
			<th></th>
		</tr>
		<tbody id="identifiersTbody">
			<tr id="identifierRow">
				<td valign="top">
					<input type="text" size="30" name="identifier" onmouseup="identifierOrTypeChanged(this)" />
				</td>
				<td valign="top">
					<select name="identifierType" onclick="identifierOrTypeChanged(this)">
						<openmrs:forEachRecord name="patientIdentifierType">
							<option value="${record.patientIdentifierTypeId}">
								${record.name}
							</option>
						</openmrs:forEachRecord>
					</select>
				</td>
				<td valign="top">
					<select name="location">
						<option value=""></option>
						<openmrs:forEachRecord name="location">
							<option value="${record.locationId}">
								${record.name}
							</option>
						</openmrs:forEachRecord>
					</select>
				</td>
				<td valign="middle" align="center">
					<input type="radio" name="preferred" value="" onclick="identifierOrTypeChanged(this)" />
				</td>
				<td valign="middle" align="center">
					<input type="button" name="closeButton" onClick="return removeRow(this);" class="closeButton" value='<spring:message code="general.remove"/>'/>
				</td>
			</tr>
		</tbody>
	</table>
	<script type="text/javascript">
		<c:forEach items="${identifiers}" var="id">
			addIdentifier("${id.identifier}", "${id.identifierType.patientIdentifierTypeId}", "${id.location.locationId}", ${id.preferred}, ${id.dateCreated != null});
		</c:forEach>
	</script>
	<input type="button" class="smallButton" onclick="addIdentifier()" value="<spring:message code="PatientIdentifier.add" />" hidefocus />
	
	<!-- TEMPORARY HACK.  RELATIONSHIPS NEED TO BE COMPLETELY REDONE USING PORTLETS -->
	<c:if test="${showMothersName == 'true'}">
		<br/><br/><br />
	
		<table id="relationships" cellspacing="2">
			<tr>
				<th><spring:message code="Relationship.relative"/></th>
				<th><spring:message code="Relationship.relationshipType"/></th>
				<th></th>
			</tr>
			<tbody id="relationshipsTbody">
				<tr id="relationshipRow">
					<td valign="top" style="width:230px">
						<c:choose>
							<c:when test="${patient.relationships[0].personA.personId != patient.patientId}">
								<openmrs_tag:personField formFieldName="personA" initialValue="${patient.relationships[0].personA.personId}" searchLabelCode="Relationship.instructions.select" searchLabelArguments="${relType}" />
							</c:when>
							<c:otherwise>
								<openmrs_tag:personField formFieldName="personA" initialValue="" searchLabelCode="Relationship.instructions.select" searchLabelArguments="${relType}" />
							</c:otherwise>
						</c:choose>
					</td>
					<td valign="top">
						<select name="relationshipType">
							<openmrs:forEachRecord name="relationshipType" select="${patient.relationships[0].relationshipType}">
								<option value="${record.relationshipTypeId}" ${selected}>
									${record.aIsToB}
								</option>
							</openmrs:forEachRecord>
						</select>
					</td>
				</tr>
			</tbody>
		</table>
	</c:if>
	
	<br/><br/>
	
	<table>
		<tr>
			<th>
				<spring:message code="Person.birthdate"/><br/>
				<i style="font-weight: normal; font-size: .8em;">(<spring:message code="general.format"/>: ${datePattern})</i>
			</th>
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
							var birthdate = new Date(birthdateBox.value);
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
				<spring:bind path="patient.birthdate">			
					<input type="text" 
							name="birthdate" size="10" id="birthdate"
							value="${status.value}"
							onChange="updateAge(); updateEstimated(this);"
							onClick="showCalendar(this)" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if> 
				</spring:bind>
				
				<span id="age"></span> &nbsp; 
				
				<span id="birthdateEstimatedCheckbox" class="listItemChecked" style="padding: 5px;">
					<spring:bind path="patient.birthdateEstimated">
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
		<tr>
			<th><spring:message code="Person.gender"/></th>
			<td>
				<spring:bind path="patient.gender">
						<openmrs:forEachRecord name="gender">
							<input type="radio" name="gender" id="${record.key}" value="${record.key}" <c:if test="${record.key == status.value}">checked</c:if> />
								<label for="${record.key}"> <spring:message code="Person.gender.${record.value}"/> </label>
						</openmrs:forEachRecord>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<c:if test="${showTribe == 'true'}">
			<tr>
				<th><spring:message code="Patient.tribe"/></th>
				<td>
					<spring:bind path="patient.tribe">
						<select name="tribe">
							<option value=""></option>
							<openmrs:forEachRecord name="tribe">
								<option value="${record.tribeId}" <c:catch><c:if test="${record.name == status.value || status.value == record.tribeId}">selected</c:if></c:catch>>
									${record.name}
								</option>
							</openmrs:forEachRecord>
						</select>
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
		</c:if>
		<tr valign="top">
			<th><spring:message code="Person.address"/></th>
			<td>
				<spring:nestedPath path="patient.address">
					<openmrs:portlet url="addressLayout" id="addressPortlet" size="full" parameters="layoutShowTable=true|layoutShowExtended=false" />
				</spring:nestedPath>
			</td>
		</tr>
		<openmrs:forEachDisplayAttributeType personType="patient" displayType="viewing" var="attrType">
			<tr>
				<th><spring:message code="PersonAttributeType.${fn:replace(attrType.name, ' ', '')}"/></th>
				<td>
					<openmrs:fieldGen 
						type="${attrType.format}" 
						formFieldName="${attrType.personAttributeTypeId}" 
						val="${patient.attributeMap[attrType.name].hydratedObject}" 
						parameters="optionHeader=[blank]|showAnswers=${attrType.foreignKey}" />
				</td>
			</tr>
		</openmrs:forEachDisplayAttributeType>
		<tr>
			<th><spring:message code="Person.dead"/></th>
			<td>
				<spring:bind path="patient.dead">
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
							var cause = document.getElementById("causeOfDeath");
							if (cause != null)
								cause.value = "";
						}
					}
				</script>
			</td>
		</tr>
		<tr id="deathInformation">
			<th><spring:message code="Person.deathDate"/></th>
			<td style="white-space: nowrap">
				<spring:bind path="patient.deathDate">
					<input type="text" name="deathDate" size="10" 
						   value="${status.value}" onClick="showCalendar(this)" 
						   id="deathDate" />
					<i style="font-weight: normal; font-size: 0.8em;">(<spring:message code="general.format"/>: ${datePattern})</i>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
				&nbsp; &nbsp; 
				<spring:message code="Person.causeOfDeath"/>
				<openmrs:globalProperty key="concept.causeOfDeath" var="conceptCauseOfDeath" />
				<openmrs:globalProperty key="concept.otherNonCoded" var="conceptOther" />
				<spring:bind path="patient.causeOfDeath">
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
	</table>
	
	<input type="hidden" name="patientId" value="${param.patientId}" />
	
	<br />
	<input type="submit" value="<spring:message code="general.save" />" name="action" id="addButton"> &nbsp; &nbsp; 
	<input type="button" value="<spring:message code="general.back" />" onclick="history.go(-1);">
</form>

<script type="text/javascript">
	document.forms[0].elements[0].focus();
	document.getElementById("identifierRow").style.display = "none";
	addIdentifier();
	updateAge();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
