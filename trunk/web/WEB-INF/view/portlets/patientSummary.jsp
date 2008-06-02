<%@ include file="/WEB-INF/template/include.jsp" %>

<div style="width: 100%;">

	<div id="pihHeader" align="center">
		<hr/>
			<b><spring:message code="summary.title"/></b>
		<hr/>
	</div>
	
	<c:if test="${empty model.showHeader || model.showHeader == 'true'}">
		<table id="patientHeaderGeneralTable" width="100%">
			<tr valign="bottom">
				<td id="patientHeaderPatientName" width="50%"><span class="patientPatientName">${model.patient.personName}</span>&nbsp;&nbsp;</td>
				<td id="summaryHeaderPatientGender" width="25%">
					<c:if test="${model.patient.gender == 'M'}">
						<table><tr><td><img src="${pageContext.request.contextPath}/images/male.gif" alt='<spring:message code="Patient.gender.male"/>'/></td>
						<td><spring:message code="Patient.gender.male"/></td></tr></table>
					</c:if>
					<c:if test="${model.patient.gender == 'F'}">
						<table><tr><td><img src="${pageContext.request.contextPath}/images/female.gif" alt='<spring:message code="Patient.gender.female"/>'/></td>
						<td><spring:message code="Patient.gender.female"/></td></tr></table>
					</c:if>
				</td>
				<td id="summaryHeaderPatientAge" width="25%">
					<c:if test="${model.patient.age > 0}">${model.patient.age} <spring:message code="Person.age.years"/></c:if>
					<c:if test="${model.patient.age == 0}">< 1 <spring:message code="Person.age.year"/></c:if>
					(<c:if test="${model.patient.birthdateEstimated}">~</c:if><openmrs:formatDate date="${model.patient.birthdate}" type="medium" />)
				</td>
			</tr>
		</table>
	</c:if>

	<table width="100%">
		<tr>
			<td>
				<spring:message code="Patient.lastEncounter"/>:
				<c:forEach items='${openmrs:sort(model.patientEncounters, "encounterDatetime", true)}' var="lastEncounter" varStatus="lastEncounterStatus" end="0">
					${lastEncounter.encounterType.name}
					<spring:message code="general.atLocation"/> ${lastEncounter.location.name}
					<spring:message code="general.onDate"/> <openmrs:formatDate date="${lastEncounter.encounterDatetime}" type="medium" />
					<c:if test="${not empty lastEncounter.provider}">
						<spring:message code="general.byPerson"/> ${lastEncounter.provider}
					</c:if>
				</c:forEach>
				<c:if test="${fn:length(model.patientEncounters) == 0}">
					<spring:message code="Encounter.no.previous"/>
				</c:if>
			</td>
			<td></td>
			<td id="summaryHeaderPatientIdentifiers">
				<c:forEach var="identifier" items="${model.patient.identifiers}" varStatus="status">
					<c:if test="${!identifier.preferred}">
						&nbsp;&nbsp;&nbsp;&nbsp;<span class="patientHeaderPatientIdentifier">${identifier.identifierType.name}: ${identifier.identifier}</span>
					</c:if>
				</c:forEach>
			</td>
			<%--
			<c:forEach var="identifier" items="${model.patient.identifiers}" varStatus="status">
				<c:if test="${!identifier.preferred}">
					<td align="center">
						<span class="patientHeaderPatientIdentifier">${identifier.identifierType.name}: ${identifier.identifier}</span>
					</td>
				</c:if>
			</c:forEach>
			--%>
		</tr>
	</table>
	
	<hr/>
	
	<table width="100%">
	<tr valign="top"><td>
		<c:forEach var="specElement" items="${model.patientSummarySpecification.specification}">
			<openmrs:summaryTest var="toShow" observations="${model.patientObs}" encounters="${model.patientEncounters}"
				ifTrue="${specElement.showIfTrue}"
				ifFalse="${specElement.showIfFalse}"
			/>
			<c:if test="${toShow}">
			
				<c:if test="${not empty specElement.headingCode}">
					<b><u><spring:message code="${specElement.headingCode}" /></u></b>
					<br/>
				</c:if>
				<c:if test="${not empty specElement.headingText}">
					<b><u>${specElement.headingText}</u></b>
					<br/>
				</c:if>
				<c:choose>
					<c:when test="${specElement.type == 'NEWCOLUMN'}">
						</td><td>
					</c:when>
					<c:when test="${specElement.type == 'NEWROW'}">
						</td></tr><tr valign="top"><td>
					</c:when>
					<c:when test="${specElement.type == 'NEWTABLE'}">
						</td></tr></table>
						<table width="100%"><tr valign="top"><td>
					</c:when>
					<c:when test="${specElement.type == 'newlines'}">
						<c:forEach begin="0" end="${specElement.count}"><br/></c:forEach>
					</c:when>
					<c:when test="${specElement.type == 'heading'}">
						<b><u><spring:message code="${specElement.code}"/></u></b><br/>
					</c:when>
					<c:when test="${specElement.type == 'label'}">
						<spring:message code="${specElement.code}"/>
					</c:when>
					<c:when test="${specElement.type == 'output'}">
						${specElement.output}
					</c:when>
					<c:when test="${specElement.type == 'activeList'}">
						<openmrs:activeList observations="${model.patientObs}"
							onDate="${specElement.onDate}"
							addConcept="${specElement.addConcept}"
							removeConcept="${specElement.removeConcept}"
							otherGroupedConcepts="${specElement.otherGroupedConcepts}"
							showDate="${specElement.showDate}"
							displayStyle="${specElement.displayStyle}"
						/>
					</c:when>
					<c:when test="${specElement.type == 'obsTable'}">
						<openmrs:obsTable observations="${model.patientObs}"
							concepts="${specElement.concepts}"
							id="${specElement.id}"
							cssClass="${specElement.cssClass}"
							showEmptyConcepts="${specElement.showEmptyConcepts}"
							showConceptHeader="${specElement.showConceptHeader}"
							showDateHeader="${specElement.showDateHeader}"
							fromDate="${specElement.fromDate}"
							toDate="${specElement.toDate}"
							limit="${specElement.limit}"
							sort="${specElement.sort}"
							orientation="${specElement.orientation}"
						/>
					</c:when>
					<c:when test="${specElement.type == 'currentDrugOrders'}">
						<openmrs:portlet url="patientRegimenCurrent" id="patientRegimenCurrent" patientId="${model.patientId}"
							parameters="displayDrugSetIds=${specElement.whichSets}|currentRegimenMode=view" />
					</c:when>
					<c:when test="${specElement.type == 'pastDrugOrders'}">
						<openmrs:portlet url="patientRegimenCompleted" id="patientRegimenCompleted" patientId="${model.patientId}"
							parameters="displayDrugSetIds=${specElement.whichSets}|completedRegimenMode=view" />
					</c:when>
					<c:when test="${specElement.type == 'graph'}">
						<c:choose>
							<c:when test="${not empty specElement.conceptId}">
								<span align="center" id="conceptBox-${specElement.conceptId}">
									<spring:message code="general.loading"/>
								</span>
								<script type="text/javascript">
									function loadGraphs${specElement.conceptId}() {
										<openmrs:concept conceptId="${specElement.conceptId}" var="concept" nameVar="name" numericVar="num">
											document.getElementById('conceptBox-${specElement.conceptId}').innerHTML = '<img src="${pageContext.request.contextPath}/showGraphServlet?patientId=${patient.patientId}&conceptId=${specElement.conceptId}&width=350&height=250&minRange=<c:out value="${num.lowAbsolute}" default="0.0"/>&maxRange=<c:out value="${num.hiAbsolute}" default="200.0"/>" />';
										</openmrs:concept>
									}
									window.setTimeout(loadGraphs${specElement.conceptId}, 1000);		
								</script>
							</c:when>
							<c:when test="${not empty specElement.globalKey}">
								<openmrs:globalProperty key="${specElement.globalKey}" defaultValue="" var="conceptId" />
								<span align="center" id="conceptBox-${conceptId}">
									<spring:message code="general.loading"/>
								</span>
								<script type="text/javascript">
									function loadGraphs${conceptId}() {
										<openmrs:concept conceptId="${conceptId}" var="concept" nameVar="name" numericVar="num">
											<c:choose>
												<c:when test="${not empty specElement.maxRange}">
													document.getElementById('conceptBox-${conceptId}').innerHTML = '<img src="${pageContext.request.contextPath}/showGraphServlet?patientId=${model.patientId}&conceptId=${conceptId}&width=275&height=200&minRange=<c:out value="${num.lowAbsolute}" default="0.0"/>&maxRange=${specElement.maxRange}';
												</c:when>
												<c:otherwise>
													document.getElementById('conceptBox-${conceptId}').innerHTML = '<img src="${pageContext.request.contextPath}/showGraphServlet?patientId=${model.patientId}&conceptId=${conceptId}&width=275&height=200&minRange=<c:out value="${num.lowAbsolute}" default="0.0"/>&maxRange=<c:out value="${num.hiAbsolute}" default="200.0"/>" />';
												</c:otherwise>
											</c:choose>
										</openmrs:concept>
									}
									window.setTimeout(loadGraphs${conceptId}, 1000);		
								</script>
							</c:when>
						</c:choose>
					</c:when>
					<%--
					<c:when test="${}">
					</c:when>
					<c:when test="${specElement.type == 'obsGroupTable'}">
						<openmrs:obsTable observations="${model.patientObs}"
							primaryConcepts="${specElement.primaryConcepts}"
							otherConcepts="${specElement.otherConcepts}"
							orientation="${specElement.orientation}"
						/>
					</c:when>
					--%>
					<c:otherwise>
						DON'T KNOW HOW TO HANDLE: ${specElement}
					</c:otherwise>
				</c:choose>
				
			</c:if>
		</c:forEach>
	</td></tr></table>
	
	<hr/>
	<spring:message code="general.printedOn"/> <openmrs:formatDate date="${model.now}"/>
</div>