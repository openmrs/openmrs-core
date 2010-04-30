<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Location Tags" otherwise="/login.htm" redirect="/admin/locations/locationTag.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="LocationTag.edit.title"/></h2>

<c:if test="${locationTag.retired}">
	<form method="post" action="locationTagUnretire.form">
		<input type="hidden" name="id" value="${locationTag.id}"/>
		<div class="retiredMessage">
			<div>
				<spring:message code="general.retiredBy"/>
				${locationTag.retiredBy.personName}
				<openmrs:formatDate date="${locationTag.dateRetired}" type="medium" />
				-
				${locationTag.retireReason}
				<input type="submit" value='<spring:message code="general.unretire"/>'/>
			</div>
		</div>
	</form>
</c:if>

<div class="boxHeader">
	<spring:message code="general.properties"/>
</div>
<div class="box">
	<form:form modelAttribute="locationTag">
		<table>
			<tr>
				<td>
					<span class="required">*</span>
					<spring:message code="LocationTag.name"/>
				</td>
				<td>
					<form:input path="name"/> <form:errors path="name" cssClass="error"/>
				</td>
			</tr>
			<tr>
				<td>
					<spring:message code="LocationTag.description"/>
				</td>
				<td>
					<form:textarea path="description" rows="3" cols="72"/> <form:errors path="description" cssClass="error"/>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.createdBy"/></td>
				<td>
					<openmrs:format user="${locationTag.creator}"/>
					<openmrs:formatDate date="${locationTag.dateCreated}"/>
				</td>
			</tr>
			<tr>
				<td></td>
				<td>
					<input type="submit" value="<spring:message code="general.save"/>" />
					<input type="button" value="<spring:message code="general.cancel"/>" onClick="window.location = 'locationTag.list'"/>
				</td>
			</tr>
		</table>
	</form:form>
</div>

<br/>
<div class="boxHeader">
	<spring:message code="LocationTag.purgeRetire"/>
</div>
<div class="box">
	<%-- Purge --%>
	<c:choose>
		<c:when test="${empty locations}">
			<form method="post" action="locationTagPurge.form">
				<input type="hidden" name="id" value="${locationTag.id}"/>
				<spring:message code="LocationTag.purge.allowed"/>:
				<input type="submit" value="<spring:message code="general.purge"/>"/>
			</form>
		</c:when>
		<c:otherwise>
			<spring:message code="LocationTag.cannot.purge.in.use"/>
		</c:otherwise>
	</c:choose>
	
	<%-- Retire --%>
	<c:if test="${not locationTag.retired && not empty locationTag.id}">
		<br/>
		<form method="post" action="locationTagRetire.form">
			<input type="hidden" name="id" value="${locationTag.id}"/>
			
			<b><spring:message code="general.retire"/></b>
			<br/>
			<spring:message code="general.reason"/>:
			<input type="text" name="retireReason" size="40"/>
			<input type="submit" value='<spring:message code="general.retire"/>'/>
		</form>
	</c:if>
</div>



<c:if test="${not empty locations}">
	<br/>
	<div class="boxHeader">
		<spring:message code="LocationTag.locationsWithTag"/>
	</div>
	<div class="box">
		<ul>
			<c:forEach var="l" items="${locations}">
				<li><openmrs:format location="${l}"/></li>
			</c:forEach>
		</ul>
	</div>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp"%>
