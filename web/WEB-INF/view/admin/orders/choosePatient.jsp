<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/order.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Order.list.patient.choose" /></h2>

<br />

<openmrs:portlet id="choosePatient" url="findPatient" parameters="size=full|postURL=orderByPatient.list" />

<%@ include file="/WEB-INF/template/footer.jsp" %>