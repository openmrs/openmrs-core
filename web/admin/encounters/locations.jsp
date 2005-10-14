<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.Location" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Encounters" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	pageContext.setAttribute("encounterService", context.getEncounterService());

	//deleting locations
	String[] locations = request.getParameterValues("locationId");
	if (locations != null) {
		Location tmpLocation = null;
		for(int x = 0; x < locations.length; x++) {
				tmpLocation = context.getEncounterService().getLocation(Integer.valueOf(locations[x]));
				try {
					adminService.deleteLocation(tmpLocation);
				}
				catch (APIException e)
				{
					session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Location cannot be deleted - " + e.getMessage());
				}
		}
		if (locations.length == 1)
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Location '" + tmpLocation.getName() + "' deleted");
		else
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, locations.length + " locations deleted");
	}
	
	//adding a location
	String locationName = request.getParameter("name");
	if (locationName != "" && locationName != null) {
		Location location = new Location();
		location.setName(locationName);
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
			adminService.createLocation(location);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Location added");
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to add location " + e.getMessage());
		}
	}
%>
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Location Management</h2>	
<br />

<b class="boxHeader">Add a New Location</b>
<form method="post" class="box">
	<table>
		<tr>
			<td>Name</td>
			<td><input type="text" name="name"></td>
		</tr>
		<tr>
			<td>Description</td>
			<td><textarea name="description" rows="2" cols="50"></textarea></td>
		</tr>
		<tr>
			<td>Address</td>
			<td><input type="text" name="address1"></td>
		</tr>
		<tr>
			<td>Address</td>
			<td><input type="text" name="address2"></td>
		</tr>
		<tr>
			<td>City or Village</td>
			<td><input type="text" name="cityVillage"></td>
		</tr>
		<tr>
			<td>State or Province</td>
			<td><input type="text" name="stateProvince"></td>
		</tr>
		<tr>
			<td>Country</td>
			<td><input type="text" name="country"></td>
		</tr>
		<tr>
			<td>Postal Code</td>
			<td><input type="text" name="postalCode"></td>
		</tr>
		<tr>
			<td>Latitude</td>
			<td><input type="text" name="latitude"></td>
		</tr>
		<tr>
			<td>Longitude</td>
			<td><input type="text" name="longitude"></td>
		</tr>
	</table>
	<input type="submit" value="Add Location">
</form>

<br />

<b class="boxHeader">
	Current Locations
</b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> Name </th>
			<th> Location </th>
			<th> Description </th>
		</tr>
		<c:forEach var="location" items="${encounterService.locations}">
			<jsp:useBean id="location" type="org.openmrs.Location" scope="page"/>
			<tr>
				<td valign="top"><input type="checkbox" name="locationId" value="${location.locationId}"></td>
				<td valign="top"><a href="editLocation.jsp?locationId=${location.locationId}">
					   ${location.name}
					</a>
				</td>
				<td valign="top">${location.cityVillage} ${location.stateProvince}, ${location.country}</td>
				<td valign="top">${location.description}</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="Delete Selected Locations" name="action">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>