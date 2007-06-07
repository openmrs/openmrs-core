<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientSetService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<c:if test="${not empty model.title}">
	<h3 align="center">${model.title}</h3>
</c:if>
<c:if test="${not empty model.titleCode}">
	<h3 align="center"><spring:message code="${model.titleCode}"/></h3>
</c:if>

<b><u><spring:message code="Analysis.activeFilters"/></u></b>
<c:choose>
	<c:when test="${fn:length(model.patientAnalysis.patientFilters) == 0}">
		<br/>
		<spring:message code="Analysis.noFiltersSelected"/>
	</c:when>
	<c:otherwise>
		<table>
		<c:forEach var="item" varStatus="stat" items="${model.patientAnalysis.patientFilters}">
			<tr><td>
				<div class="activeFilter">
					<c:choose>
						<c:when test="${item.value.name != null}">
							${item.value.name}
						</c:when>
						<c:otherwise>
							${item.value.description}
						</c:otherwise>
					</c:choose>
					<a href="javascript:DWRPatientSetService.removeFilterFromMyAnalysis('${item.key}', refreshPage);">[X]</a>
				</div>
			</td></tr>
		</c:forEach>
		</table>
	</c:otherwise>
</c:choose>

<c:if test="${model.addURL != null}">
	<p>
	<b><u><spring:message code="Analysis.addFilter"/></u></b>
	<ul style="padding-left: 20px">
			<c:forEach var="item" items="${model.shortcuts}">
			<li>
			<c:if test="${!empty item.currentFilter}">
				<b>
			</c:if>
			<spring:message code="Analysis.shortcut.${item.label}"/>
			<c:if test="${!empty item.currentFilter}">
				</b>
			</c:if>
			<ul>
			<c:if test="${fn:length(model.suggestedFilters) == 0 && fn:length(item.list) == null}">
				<li><spring:message code="Analysis.noFiltersAvailable"/></li>
			</c:if>
			<c:forEach var="shortcutOption" items="${item.list}"> <%-- The items are Map.Entry<String, ShortcutOptionSpec> --%>
				<li>
					<c:set var="method" value="addFilter"/>
					<c:if test="${shortcutOption.value.remove}">
						<c:set var="method" value="removeFilter"/>
					</c:if>
					<c:set var="isSelected" value="false"/>
					<c:if test="${item.currentFilter == shortcutOption.value}">
						<c:set var="isSelected" value="true"/>
					</c:if>
					
					<c:if test="${isSelected == true}">
						<b>
					</c:if>
					<c:choose>
						<c:when test="${shortcutOption.value.concrete}">
							<a href="analysis.form?method=${method}&patient_filter_name=<c:out value="${shortcutOption.value.value}"/><c:if test="${!item.allowMultiple}">&patient_filter_key=${item.label}</c:if>">
								<spring:message code="Analysis.shortcut.${shortcutOption.key}"/>
							</a>
						</c:when>
						<c:otherwise>
							<form method="post" action="analysis.form" id="form_${item.label}_${shortcutOption.key}" style="display: inline">
								<input type="hidden" name="viewMethod" value="${model.viewMethod}"/>
								<input type="hidden" name="method" value="${method}"/>
								<c:if test="${!item.allowMultiple}">
									<input type="hidden" name="patient_filter_key" value="${item.label}"/>
								</c:if>
								<input type="hidden" name="patient_filter_name" value="${shortcutOption.value.value}"/>
								<c:forEach var="arg" items="${shortcutOption.value.hiddenArgs}">
									${arg}
								</c:forEach>
								<c:choose>
									<c:when test="${shortcutOption.value.promptArgs}">
										<c:forEach var="arg" items="${shortcutOption.value.args}">
											<c:choose>
												<c:when test="${arg.label}">
													<spring:message code="${arg.name}"/>
												</c:when>
												<c:otherwise>
													<spring:message code="Analysis.shortcut.${item.label}.${arg.name}"/>
													<c:if test="${arg.name == 'treatmentGroup'}">
														<openmrs:fieldGen type="${arg.fieldClass}" formFieldName="${arg.name}" val="" parameters="optionHeader=[blank]|fieldLength=10|answerSet=${model.arvGroups}" />
													</c:if>
													<c:if test="${arg.name != 'treatmentGroup'}">
														<openmrs:fieldGen type="${arg.fieldClass}" formFieldName="${arg.name}" val="" parameters="optionHeader=[blank]|fieldLength=10" />
													</c:if>
												</c:otherwise>
											</c:choose>
										</c:forEach>
										<input type="submit" value="<spring:message code="general.add"/>"/>
									</c:when>
									<c:otherwise>
										<a href="javascript:document.getElementById('form_${item.label}_${shortcutOption.key}').submit()">
											<spring:message code="Analysis.shortcut.${shortcutOption.key}"/>
										</a>
									</c:otherwise>
								</c:choose>
							</form>
						</c:otherwise>
					</c:choose>
					<c:if test="${isSelected == true}">
						</b>
					</c:if>
				</li>
			</c:forEach>
			</ul>
			</li>
		</c:forEach>
		<c:forEach var="item" items="${model.suggestedFilters}">
			<li>
				<a href="javascript:DWRPatientSetService.addFilterToMyAnalysis(${item.reportObjectId}, refreshPage)">
					${item.name}
					<%-- <small><i>(${item.description})</i></small> --%>
				</a>
			</li>
		</c:forEach>
	</ul>
</c:if>