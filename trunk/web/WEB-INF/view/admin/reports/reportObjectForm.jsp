<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Report Objects" otherwise="/login.htm" redirect="/admin/reports/reportObject.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/openmrsPopup.js" />
<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<h2><spring:message code="ReportObject.title"/></h2>

<spring:hasBindErrors name="reportObject">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post" id="reportObjectForm">
<table>
	<tr>
		<td valign="top"><spring:message code="general.type"/></td>
		<td valign="top" colspan="5">
			<c:if test="${reportObject.reportObjectId == null}" >
				<spring:bind path="reportObject.type">
					<select name="${status.expression}" onChange="document.getElementById('reportObjectForm').submit();">
						<option value=""><spring:message code="ReportObject.type.chooseone"/></option>
						<c:forEach items="${availableTypes}" var="availableType">
							<option <c:if test="${status.value == availableType}">selected</c:if> value="<spring:transform value="${availableType}"/>"><c:out value="${availableType}"/></option>
						</c:forEach>
					</select>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</c:if>
			<c:if test="${!(reportObject.reportObjectId == null)}" >
				<c:out value="${reportObject.type}" />
				<input type="hidden" name="type" value="${reportObject.type}" />			
			</c:if>
		</td>
	</tr>
	<c:if test="${not empty reportObject.type}">
		<tr>
			<td valign="top"><spring:message code="general.subType"/></td>
			<td valign="top" colspan="5">
				<c:if test="${reportObject.reportObjectId == null}" >
					<spring:bind path="reportObject.subType">
						<select name="${status.expression}" onChange="document.getElementById('reportObjectForm').submit();">
							<option value=""><spring:message code="ReportObject.subtype.chooseone"/></option>
							<c:forEach items="${availableSubTypes}" var="availableSubType">
								<option <c:if test="${status.value == availableSubType}">selected</c:if> value="<spring:transform value="${availableSubType}"/>"><c:out value="${availableSubType}"/></option>
							</c:forEach>
						</select>
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</c:if>
				<c:if test="${!(reportObject.reportObjectId == null)}" >
					<c:out value="${reportObject.subType}" />
					<input type="hidden" name="subType" value="${reportObject.subType}" />			
				</c:if>
			</td>
		</tr>
		<c:if test="${not empty reportObject.subType || not empty reportObject.reportObjectId}">		
			<tr>
				<td><spring:message code="general.name"/></td>
				<td colspan="5">
					<spring:bind path="reportObject.name">
						<input type="text" name="name" value="${status.value}" size="35" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<tr>
				<td valign="top"><spring:message code="general.description"/></td>
				<td valign="top" colspan="5">
					<spring:bind path="reportObject.description">
						<textarea name="description" rows="3" cols="40">${status.value}</textarea>
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<c:forEach items="${reportObject.class.declaredFields}" var="field">
				<c:if test="${empty transientObjects[field.name]}">
					<spring:bind path="reportObject.${field.name}">
						<tr>
							<td valign="top"><c:out value="${field.name}"/></td>
							<c:if test="${!field.type.enum}">
								<td valign="top" colspan="5"><openmrs:fieldGen type="${field.genericType}" formFieldName="${status.expression}" val="${status.editor.value}" /></td>
							</c:if>
							<c:if test="${field.type.enum}">
								<td valign="top" colspan="5"><openmrs:fieldGen type="${field.genericType}" formFieldName="${status.expression}" val="${status.value}" /></td>
							</c:if>
						</tr>
					</spring:bind>	
				</c:if>
			</c:forEach>
		</c:if>
	</c:if>
	<c:if test="${!(reportObject.reportObjectId == null)}" >
		<input type="hidden" name="reportObjectId:int" value="${reportObject.reportObjectId}">
		<%--
		<tr>
			<td><spring:message code="general.creator"/></td>
			<td>
				<spring:bind path="reportObject.creator">
					<c:out value="${reportObject.creator.username}" />
					<input type="hidden" name="${status.expression}" value="${status.value}" />
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="general.dateCreated"/></td>
			<td>
				<spring:bind path="reportObject.dateCreated">
					<openmrs:formatDate date="${reportObject.dateCreated}" type="long"/>
					<input type="hidden" name="${status.expression}" value="${status.value}" />
				</spring:bind>
			</td>
		</tr>
		<spring:bind path="reportObject.voided">
			<input type="hidden" name="${status.expression}" value="${status.value}">
		</spring:bind>
		--%>
	</c:if>
</table>
<c:if test="${not empty reportObject.subType}">		
<br />
<input type="submit" name="submitted" value="<spring:message code="ReportObject.save"/>">
</c:if>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>