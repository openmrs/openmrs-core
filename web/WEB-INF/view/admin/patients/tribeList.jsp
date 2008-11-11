<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Tribes" otherwise="/login.htm" redirect="/admin/patients/tribe.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<spring:message code="Tribe.module.message"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>