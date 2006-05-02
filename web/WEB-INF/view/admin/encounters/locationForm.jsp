<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Locations" otherwise="/login.htm" redirect="/admin/encounters/location.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Location.title"/></h2>

<spring:hasBindErrors name="location">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">
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
	<tr>
		<td><spring:message code="Location.address1"/></td>
		<td>
			<spring:bind path="location.address1">
				<input type="text" name="${status.expression}" id="address1" value="${status.value}"/>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Location.address2"/></td>
		<td>
			<spring:bind path="location.address2">
				<input type="text" name="${status.expression}" id="address2" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="Location.cityVillage"/></td>
			<spring:bind path="location.cityVillage">
				<td>
					<input type="text" name="${status.expression}" id="cityVillage" value="${status.value}" onKeyUp="modifyTab(this, this.value, 0);"/>
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td><spring:message code="Location.stateProvince"/></td>
			<spring:bind path="location.stateProvince">
				<td>
					<input type="text" name="${status.expression}" id="stateProvince" size="10" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td><spring:message code="Location.country"/></td>
			<spring:bind path="location.country">
				<td>
					<input type="text" name="${status.expression}" id="country" size="15" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td><spring:message code="Location.postalCode"/></td>
			<spring:bind path="location.postalCode">
				<td>
					<input type="text" name="${status.expression}" id="postalCode" size="5" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
	</tr>
	<tr>
		<td><spring:message code="Location.latitude"/></td>
			<spring:bind path="location.latitude">
				<td>
					<input type="text" name="${status.expression}" id="latitude" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		<td>Longitude</td>
			<spring:bind path="location.longitude">
				<td>
					<input type="text" name="${status.expression}" id="longitude" value="${status.value}" />
				</td>
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
	</tr>
	<c:if test="${!(location.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${location.creator.firstName} ${location.creator.lastName} -
				<openmrs:formatDate date="${location.dateCreated}" type="long" />
			</td>
		</tr>
	</c:if>
</table>
<br />
<input type="submit" value="<spring:message code="Location.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>