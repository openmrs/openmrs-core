<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage RESTWS" otherwise="/login.htm" redirect="/module/webservices/rest/settings.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="localHeader.jsp"%>

<style type="text/css">
.settingRow {
	padding-top: 1em;
	clear: both;
}
.settingName {
	padding: 0.25em;
	background-color: #e0e0e0;
}
.settingValue {
	float: left;
	width: 40%;
}
.settingDescription {
	font-size: 0.8em;
	float: left;
	width: 55%;
}
.saveButtons {
	padding-left: 0.5em;
	background-color: #e0e0e0;
	clear: both;
}
</style>

<h2><spring:message code="webservices.rest.manage.settings.title" /></h2>

<spring:hasBindErrors name="globalPropertiesModel">
	<spring:message code="fix.error"/>
</spring:hasBindErrors>

<form:form method="post" modelAttribute="globalPropertiesModel">
	<c:forEach var="prop" items="${globalPropertiesModel.properties}" varStatus="varStatus">
		<spring:nestedPath path="properties[${varStatus.index}]">
			<div class="settingRow">
				<h4 class="settingName"><spring:message code="${prop.property}.label" /></h4>
				<span class="settingValue">
					<spring:bind path="propertyValue">
						<c:set var="inputSize" value="50" scope="page" />
						<c:if test="${prop.property == 'webservices.rest.maxResultsDefault' || prop.property == 'webservices.rest.maxResultsAbsolute' }">
                             <c:set var="inputSize" value="3" />
                        </c:if>
						<input type="text" name="${status.expression}" value="${status.value}" size="${inputSize}">
						<form:errors cssClass="error"/>
					</spring:bind>
				</span>
				<span class="settingDescription">
					${prop.description}
				</span>
			</div>
		</spring:nestedPath>
	</c:forEach>

	<div class="settingRow">
		<div class="saveButtons">
			<input type="submit" value='<spring:message code="general.save"/>' /> &nbsp;&nbsp; 
			<input type="button" value='<spring:message code="general.cancel"/>' onclick="javascript:window.location='<openmrs:contextPath />/admin'" />
		</div>
	</div>
</form:form>

<%@ include file="/WEB-INF/template/footer.jsp"%>