<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Order Types" otherwise="/login.htm" redirect="/admin/orders/orderType.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="OrderType.title"/></h2>

<spring:hasBindErrors name="orderType">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
<table>
	<spring:nestedPath path="orderType">
		<openmrs:portlet url="localizedName" id="localizedNameLayout" /> 
		<openmrs:portlet url="localizedDescription" id="localizedDescriptionLayout" /> 
	</spring:nestedPath>
	<c:if test="${!(orderType.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${orderType.creator.personName} -
				<openmrs:formatDate date="${orderType.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<br />
<input type="submit" value="<spring:message code="OrderType.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>