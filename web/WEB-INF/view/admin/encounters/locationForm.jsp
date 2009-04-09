<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Locations" otherwise="/login.htm" redirect="/admin/encounters/location.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Location.title"/></h2>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationForm.afterTitle" type="html" parameters="locationId=${location.locationId}" />

<c:if test="${location.retired}">
	<form action="" method="post">
		<div class="retiredMessage">
			<div>
				<spring:message code="general.retiredBy"/>
				${location.retiredBy.personName}
				<openmrs:formatDate date="${location.dateRetired}" type="medium" />
				-
				${location.retireReason}
				<input type="submit" value='<spring:message code="Location.unretireLocation"/>' name="unretireLocation"/>
			</div>
		</div>
	</form>
</c:if>

<spring:hasBindErrors name="location">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.globalErrors}" var="error">
			<spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
	<br />
</spring:hasBindErrors>
<form method="post">
<fieldset>
	<table>
		<tr>
			<td><spring:message code="general.name"/></td>
			<td colspan="5">
				<spring:bind path="location.name">
					<input type="text" name="name" value="${status.value}" size="35" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.description"/></td>
			<td valign="top" colspan="5">
				<spring:bind path="location.description">
					<textarea name="description" rows="3" cols="40">${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<spring:nestedPath path="location">
			<openmrs:portlet url="addressLayout" id="addressPortlet" size="full" parameters="layoutShowTable=false|layoutShowExtended=false|layoutShowErrors=false" />
		</spring:nestedPath>
		<c:if test="${!(location.creator == null)}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${location.creator.personName} -
					<openmrs:formatDate date="${location.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
	</table>
	<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationForm.inForm" type="html" parameters="locationId=${location.locationId}" />
	<br />
	<input type="submit" value="<spring:message code="Location.save"/>" name="saveLocation">
</fieldset>
</form>

<br/>
<br/>

<c:if test="${not location.retired && not empty location.locationId}">
	<form action="" method="post">
		<fieldset>
			<h4><spring:message code="Location.retireLocation"/></h4>

			<b><spring:message code="general.reason"/></b>
			<input type="text" value="" size="40" name="retireReason" />
			<spring:hasBindErrors name="location">
				<c:forEach items="${errors.allErrors}" var="error">
					<c:if test="${error.code == 'retireReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
				</c:forEach>
			</spring:hasBindErrors>
			<br/>
			<input type="submit" value='<spring:message code="Location.retireLocation"/>' name="retireLocation"/>
		</fieldset>
	</form>
</c:if>

<openmrs:extensionPoint pointId="org.openmrs.admin.encounters.locationForm.footer" type="html" parameters="locationId=${location.locationId}" />

<%@ include file="/WEB-INF/template/footer.jsp" %>