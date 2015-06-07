<%@ include file="/WEB-INF/template/include.jsp"%>
<openmrs:require privilege="Manage Global Properties"
	otherwise="/login.htm" redirect="/admin/maintenance/settings.list" />
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2>
	<openmrs:message code="Settings.title" />
</h2>
<table style="width: 98%;">
	<tr style="vertical-align: top;">
		<td style="width: 20%; padding: 0px;"><c:forEach
				items="${sections}" var="item">
				<c:choose>
					<c:when test="${settingsForm.section eq item}">
						<div class="boxHeader" style="padding: 0px">${item}</div>
					</c:when>
					<c:otherwise>
						<div>
							<a href="?show=${item}">${item}</a>
						</div>
					</c:otherwise>
				</c:choose>
			</c:forEach></td>
		<td class="box"><form:form method="post"
				commandName="settingsForm">
				<form:hidden path="section"/>
				<table>
					<c:forEach items="${ settingsForm.settings }" var="item"
						varStatus="status">
						<tr>
							<td style="width: 70%;">${ item.name } <br /> <span
								class="description">${ item.globalProperty.description }</span>
							</td>
							<td>
								<c:choose>
									<c:when test="${ not empty item.globalProperty.datatypeClassname }">
										<input type="hidden" name="originalValue[${ status.index }]" value='<c:out escapeXml="true" value="${ item.globalProperty.propertyValue }" />'/>
										<openmrs_tag:singleCustomValue
											formFieldName="settings[${ status.index }].globalProperty.propertyValue"
											value="${ item.globalProperty }" />
									</c:when>
									<c:otherwise>
										<c:choose>
											<c:when test="${fn:length(item.globalProperty.propertyValue) > 20}">
												<form:textarea
													path="settings[${status.index}].globalProperty.propertyValue"
													cols="50"/></td>
										    </c:when>
										    <c:otherwise>
												<form:input
													path="settings[${status.index}].globalProperty.propertyValue"
													size="50" maxlength="4000" /></td>
										    </c:otherwise>
										</c:choose>
									</c:otherwise>
								</c:choose>
								<form:errors path="settings[${status.index}].globalProperty.propertyValue" cssClass="error"/>
						</tr>
					</c:forEach>
					<tr>
						<td colspan="2"><p>
								<input id="saveButton" type="submit"
									value="<openmrs:message code="general.save"/>" />
								<input id="cancelButton" type="button"
									value="<openmrs:message code="general.cancel"/>"
									onclick="window.location=''" />
							</p></td>
					</tr>
				</table>
			</form:form></td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>