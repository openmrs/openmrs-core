<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<br>
<h2>Administration</h2>

<h4>Users</h4>
<ul>
	<li><a href="users/addUser.jsp">Add a new user</a></li>
	<li><a href="users/users.jsp">Manage the current users</a></li>
	<li><a href="users/roles.jsp">Manage the roles</a></li>
	<li><a href="users/privileges.jsp">Manage the privileges</a></li>
</ul>

<h4>Patients</h4>
<ul>
	<li><a href="patients/addUser.jsp">Void/Unvoid a patient</a></li>
	<li><a href="patients/tribes.jsp">Manage the tribes</a></li>
	<li><a href="patients/identifierTypes.jsp">Manage the patient identifier types</a></li>
</ul>

<%@ include file="/WEB-INF/template/footer.jsp" %>