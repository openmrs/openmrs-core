<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:require privilege="" otherwise="/login.htm" redirect="/admin/index.htm" />

<style>
	#menu li {
		display: list-item;
		border-left-width: 0px;
		
	}
	#menu li.first {
		display: none;
	}
	#menu {
		list-style: none;
		margin-left: 10px;
		margin-top: 0;
	}
	h4 {
		margin-bottom: 0;
	}
</style>

<h2><spring:message code="admin.title"/></h2>

<h4>Users</h4>
	<%@ include file="users/localHeader.jsp" %>

<h4>Patients</h4>
	<%@ include file="patients/localHeader.jsp" %>

<h4>Encounters</h4>
	<%@ include file="encounters/localHeader.jsp" %>
	
<h4>Observations</h4>
	<%@ include file="observations/localHeader.jsp" %>

<h4>Orders</h4>
	<%@ include file="orders/localHeader.jsp" %>

<h4>Concepts</h4>
	<%@ include file="concepts/localHeader.jsp" %>

<h4>Forms</h4> 
	<%@ include file="forms/localHeader.jsp" %>

<h4>Reports</h4>
	<%@ include file="reports/localHeader.jsp" %>

<%@ include file="/WEB-INF/template/footer.jsp" %>