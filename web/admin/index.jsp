<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<style>
	#menu li {
		display: list-item;
		border-left-width: 0px;
	}
	
	#menu li.first {
		display: none;
	}
</style>

<br>
<h2>Administration</h2>

<h4>Users</h4>
<ul>
	<li><a href="users/users.jsp">Manage users</a></li>
	<li><a href="users/roles.jsp">Manage roles</a></li>
	<li><a href="users/privileges.jsp">Manage privileges</a></li>
</ul>

<h4>Patients</h4>
	<%@ include file="patients/localHeader.jsp" %>

<h4>Encounters</h4>
<ul>
	<li><a href="encounters/encounters.jsp">Void/Unvoid an encounter</a></li>
	<li><a href="encounters/locations.jsp">Manage the locations</a></li>
	<li><a href="encounters/encounterTypes.jsp">Manage encounter types</a></li>
</ul>

<h4>Observations</h4>
<ul>
	<li><a href="observations/observations.jsp">Void/Unvoid an observation</a></li>
	<li><a href="observations/mimeTypes.jsp">Manage mime types</a></li>
</ul>

<h4>Orders</h4>
	<%@ include file="orders/localHeader.jsp" %>

<h4>Forms</h4> 
<ul>
	<li><a href="forms/forms.jsp">Manage forms</a></li>
	<li><a href="forms/fields.jsp">Manage fields</a></li>
	<li><a href="forms/fieldsAnswers.jsp">Manage field answers</a></li>
	<li><a href="forms/fieldTypes.jsp">Manage field types</a></li>
</ul>

<%@ include file="/WEB-INF/template/footer.jsp" %>