<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/order.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="Order.list.patient.choose" /></h2>

<br />

<openmrs:portlet id="choosePatient" url="findPatient" parameters="size=full|postURL=orderDrug.list|hideAddNewPatient=true" />

<br />

<center>
	<form>
		<input type="hidden" name="showAll" value="true" />
		<input type="submit" value="<openmrs:message code="DrugOrder.list.showAll" />" />
	</form>
</center>

<%@ include file="/WEB-INF/template/footer.jsp" %>