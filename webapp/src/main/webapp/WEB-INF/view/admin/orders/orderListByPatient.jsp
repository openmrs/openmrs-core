<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/orderDrug.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.addOnLoad( function() {
		toggleRowVisibilityForClass("orderDrugTable", "voided", false);
	})
</script>

<h2><openmrs:message code="Order.list.patient.title" arguments="${patient.patientId},${personName.familyName},${personName.givenName},${personName.middleName}" /></h2>

<a href="orderDrug.form?patientId=<c:out value="${patient.patientId}" />"><openmrs:message code="Order.drug.place.patient"/></a>
<br />

<span class="boxHeader">
	<span style="float: right">
		<a href="#" id="showVoided" onClick="return toggleRowVisibilityForClass('orderDrugTable', 'voided', false);"><openmrs:message code="general.toggle.voided"/></a>
	</span>
	<openmrs:message code="Order.drug.list.title"/>
</span>
<c:if test="${not empty orderDrugList}">
<div class="box">
	<form method="post">
		<input type="hidden" name="patientId" value="<c:out value="${patient.patientId}" />" />
		<table id="orderDrugTable" cellpadding="5" cellspacing="0">
			<tr>
				<th> </th>
				<th> <openmrs:message code="Order.item.ordered" /> </th>
				<th> <openmrs:message code="DrugOrder.dose"/> </th>
				<th> <openmrs:message code="DrugOrder.units"/> </th>
				<th> <openmrs:message code="DrugOrder.frequency"/> </th>
				<th> <openmrs:message code="general.dateStart"/> </th>
				<th> <openmrs:message code="general.instructions" /> </th>
			</tr>
			<c:forEach var="order" items="${orderDrugList}">
				<tr class="<c:if test="${order.voided}">voided </c:if>">
					<td valign="top">
						<c:if test="${not order.voided}">
							<input type="checkbox" name="orderId" value="${order.orderId}">
						</c:if>
					</td>
					<td valign="top">
						<a href="orderDrug.form?orderId=${order.orderId}">
							<c:out value="${order.drug.name}" />
						</a>
					</td>
					<td valign="top">
					   ${order.dose}
					</td>
					<td valign="top">
					   ${order.doseUnits}
					</td>
					<td valign="top">
					   ${order.frequency}
					</td>
					<td valign="top">
						<openmrs:formatDate date="${order.startDate}" format="dd/MM/yyyy" />
					</td>
					<td valign="top">
					   ${order.instructions}
					</td>
				</tr>
			</c:forEach>
		</table>
		<br/>
		<b><openmrs:message code="general.voidReason" />:</b>
		<input type="text" value="" size="40" name="voidReason" /><br/>
		<input type="submit" value="<openmrs:message code="Order.void"/>" name="action">

	</form>
</div>
</c:if>
<c:if test="${empty orderDrugList}"><openmrs:message code="Order.list.empty"/></c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>