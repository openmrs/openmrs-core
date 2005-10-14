<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.EncounterService" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.Location" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Encounters" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	EncounterService encounterService = context.getEncounterService();
	Location location = encounterService.getLocation(Integer.valueOf(request.getParameter("locationId")));

	if (request.getParameter("name") != null) {
		location.setName(request.getParameter("name"));
		location.setDescription(request.getParameter("description"));
		location.setAddress1(request.getParameter("address1"));
		location.setAddress2(request.getParameter("address2"));
		location.setCityVillage(request.getParameter("cityVillage"));
		location.setStateProvince(request.getParameter("stateProvince"));
		location.setCountry(request.getParameter("country"));
		location.setPostalCode(request.getParameter("postalCode"));
		location.setLatitude(request.getParameter("latitude"));
		location.setLongitude(request.getParameter("longitude"));
		try {
			adminService.updateLocation(location);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Location updated");
			response.sendRedirect("locations.jsp");
			return;
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to update location " + e.getMessage());
		}
	}
	pageContext.setAttribute("location", location);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Editing Location</h2>

<form method="post">
<table>
	<tr>
		<td>Name</td>
		<td><input type="text" name="name" value="${location.name}" size="35" /></td>
	</tr>
	<tr>
		<td valign="top">Description</td>
		<td><textarea name="description" rows="2" cols="40">${location.description}</textarea></td>
	</tr>
	<tr>
		<td>Address</td>
		<td><input type="text" name="address1" value="${location.address1}"></td>
	</tr>
	<tr>
		<td>Address</td>
		<td><input type="text" name="address2" value="${location.address2}"></td>
	</tr>
	<tr>
		<td>City or Village</td>
		<td><input type="text" name="cityVillage" value="${location.cityVillage}"></td>
	</tr>
	<tr>
		<td>State or Province</td>
		<td><input type="text" name="stateProvince" value="${location.stateProvince}"></td>
	</tr>
	<tr>
		<td>Country</td>
		<td><input type="text" name="country" value="${location.country}"></td>
	</tr>
	<tr>
		<td>Postal Code</td>
		<td><input type="text" name="postalCode" value="${location.postalCode}"></td>
	</tr>
	<tr>
		<td>Latitude</td>
		<td><input type="text" name="latitude" value="${location.latitude}"></td>
	</tr>
	<tr>
		<td>Longitude</td>
		<td><input type="text" name="longitude" value="${location.longitude}"></td>
	</tr>
	<tr>
		<td>Creator</td>
		<td>${location.creator}</td>
	</tr>
	<tr>
		<td>Date Created</td>
		<td>${location.dateCreated}</td>
	</tr>
</table>
<input type="hidden" name="locationId" value="<c:out value="${location.locationId}"/>">
<br />
<input type="submit" value="Save Location">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>