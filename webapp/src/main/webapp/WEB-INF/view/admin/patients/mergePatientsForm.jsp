<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Patients" otherwise="/login.htm" redirect="/admin/patients/mergePatients.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PatientSearch");
	
	function changePrimary(dir) {
		var left = document.getElementById("left");
		var right= document.getElementById("right");
		
		if (dir == "left") {
			left.className = "preferred";
			right.className = "notPreferred";
		}
		else {
			left.className = "notPreferred";
			right.className = "preferred";
		}
		
		var cell = document.getElementById("patientDivider");
		cell.innerHTML = "";
		var img = document.createElement("img");
		var src = "${pageContext.request.contextPath}/images/" + dir + "Arrow.gif";
		img.src = src;
		cell.appendChild(img);
	}
	
	dojo.addOnLoad( function() {
	
		dojo.event.topic.subscribe("pSearch/select", 
			function(msg) {
				var patient = msg.objs[0];
				if (patient.patientId != "${patient1.patientId}") {
					var query = "?patientId=${patient1.patientId}&patientId=" + patient.patientId;
					document.location = "mergePatients.form" + query;
				}
			}
		);
	});
	
</script>

<style>
	#patientDivider {
		border-left: 1px solid black;
		border-right: 1px solid black;
	}
	.notPreferred {
		color: gray;
	}
	.preferred {
		background-color: lightgreen;
	}
</style>

<spring:hasBindErrors name="patient">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<h2><spring:message code="Patient.merge.title"/></h2>

<spring:message code="Patient.merge.warning" />

<br/><br/>

<form method="post">
	<table width="100%" id="patientTable" cellpadding="1" cellspacing="0">
		<colgroup>
			<col width="46%" id="left" class="preferred">
			<col width="22">
			<col id="right" <c:if test="${patient2.patientId != null}">class="notPreferred"</c:if>>
		</colgroup>
		<tr>
			<td width="46%"></td>
			<td align="center" valign="middle" rowspan="8" id="patientDivider">
				<img src="${pageContext.request.contextPath}/images/leftArrow.gif"/>
			</td>
			<td></td>
		</tr>
		<c:if test="${patient2.patientId != null}">
			<tr>
				<td valign="top">
					<h4>
						<input type="radio" name="preferred" id="${patient1.patientId}preferred" value="${patient1.patientId}" onclick="if (this.checked) changePrimary('left')" checked />
						<label for="${patient1.patientId}preferred"><spring:message code="Patient.merge.preferred" /></label>
					</h4>
					<c:if test="${patient1.voided}">
						<div class="retiredMessage">
							<div><spring:message code="Patient.voided"/></div>
						</div>
					</c:if>
				</td>
				<td valign="top">
					<h4>
						<input type="radio" name="preferred" id="${patient2.patientId}preferred" value="${patient2.patientId}" onclick="if (this.checked) changePrimary('right')"/>
						<label for="${patient2.patientId}preferred"><spring:message code="Patient.merge.preferred" /></label>
					</h4>
						<c:if test="${patient2.voided}">
							<div class="retiredMessage">
								<div><spring:message code="Patient.voided"/></div>
							</div>
						</c:if>
				</td>
			</tr>
		</c:if>
		<tr>
			<td valign="top">
				<h4><spring:message code="Patient.names"/></h4>
				<ol>
					<c:forEach items="${patient1.names}" var="name">
						<li>${name.givenName} ${name.middleName} ${name.familyName}
					</c:forEach>
				</ol>
			</td>
			<c:if test="${patient2.patientId == null}">
				<td rowspan="6" valign="top">
					<h4><spring:message code="Patient.select"/></h4>
					<div dojoType="PatientSearch" widgetId="pSearch"></div>
				</td>
			</c:if>
			<c:if test="${patient2.patientId != null}">
				<td valign="top">
					<h4><spring:message code="Patient.names"/></h4>
					<ol>
						<c:forEach items="${patient2.names}" var="name">
							<li>${name.givenName} ${name.middleName} ${name.familyName}
						</c:forEach>
					</ol>
				</td>
			</c:if>
		</tr>
		<tr>
			<td valign="top">
				<h4><spring:message code="Patient.identifiers"/></h4>
				<ol>
					<c:forEach items="${patient1.identifiers}" var="identifier">
						<li>${identifier.identifier} ${identifier.identifierType.name}
					</c:forEach>
				</ol>
			</td>
			<c:if test="${patient2.patientId != null}">
				<td valign="top">
					<h4><spring:message code="Patient.identifiers"/></h4>
					<ol>
						<c:forEach items="${patient2.identifiers}" var="identifier">
							<li>${identifier.identifier} ${identifier.identifierType.name}
						</c:forEach>
					</ol>
				</td>
			</c:if>
		</tr>
		<tr>
			<td valign="top">
				<h4><spring:message code="Patient.addresses"/></h4>
				<ol>
					<c:forEach items="${patient1.addresses}" var="address">
						<li>${address.address1} ${address.address2} ${address.cityVillage}
					</c:forEach>
				</ol>
			</td>
			<c:if test="${patient2.patientId != null}">
				<td valign="top">
					<h4><spring:message code="Patient.addresses"/></h4>
					<ol>
						<c:forEach items="${patient2.addresses}" var="address">
							<li>${address.address1} ${address.address2} ${address.cityVillage}
						</c:forEach>
					</ol>
				</td>
			</c:if>
		</tr>
		<tr>
			<td valign="top">
				<h4><spring:message code="Patient.information"/></h4>
				<c:set var="patient" value="${patient1}" />
				<%@ include file="../person/include/showPersonInfo.jsp" %>
			</td>
			<c:if test="${patient2.patientId != null}">
				<td valign="top">
					<h4><spring:message code="Patient.information"/></h4>
					<c:set var="patient" value="${patient2}" />
					<%@ include file="../person/include/showPersonInfo.jsp" %>
				</td>
			</c:if>
		</tr>
		<tr>
			<td valign="top">
				<h4><spring:message code="Patient.encounters"/></h4>
				<ol>
					<c:forEach items="${patient1Encounters}" var="encounter">
						<li>
							${encounter.encounterType.name}
							${encounter.location.name}
							<openmrs:formatDate date="${encounter.encounterDatetime}" type="short" />
							<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${encounter.encounterId}">
								<spring:message code="general.view"/>
							</a>
					</c:forEach>
				</ol>
			</td>
			<c:if test="${patient2.patientId != null}">
				<td valign="top">
					<h4><spring:message code="Patient.encounters"/></h4>
					<ol>
						<c:forEach items="${patient2Encounters}" var="encounter">
							<li>
								${encounter.encounterType.name}
								${encounter.location.name}
								<openmrs:formatDate date="${encounter.encounterDatetime}" type="short" />
								<a href="${pageContext.request.contextPath}/admin/encounters/encounter.form?encounterId=${encounter.encounterId}">
									<spring:message code="general.view"/>
								</a>
						</c:forEach>
					</ol>
				</td>
			</c:if>
		</tr>
		<tr>
			<td>
				<a href="patient.form?patientId=${patient1.patientId}"><spring:message code="Patient.edit"/></a>
			</td>
			<c:if test="${patient2.patientId != null}">
				<td>
					<a href="patient.form?patientId=${patient2.patientId}"><spring:message code="Patient.edit"/></a>
				</td>
			</c:if>
	</table>

	<c:if test="${patient2.patientId != null}">
		<br />
		<input type="submit" name="action" value='<spring:message code="Patient.merge"/>' onclick="return confirm('Are you sure you want to merge these patients?')" >
		<input type="hidden" name="patient1" value="${patient1.patientId}"/>
		<input type="hidden" name="patient2" value="${patient2.patientId}"/>
	</c:if>
</form>

<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>