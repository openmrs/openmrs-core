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

<h2><spring:message code="Order.list.patient.title" arguments="${patient.patientId},${personName.familyName},${personName.givenName},${personName.middleName}" /></h2>

<a href="orderDrug.form?patientId=${patient.patientId}"><spring:message code="Order.drug.place.patient"/></a>
<br />

<span class="boxHeader">
	<span style="float: right">
		<a href="#" id="showVoided" onClick="return toggleRowVisibilityForClass('orderDrugTable', 'voided', false);"><spring:message code="general.toggle.voided"/></a>
	</span>
	<spring:message code="Order.drug.list.title"/>
</span>
<c:if test="${not empty orderDrugList}">
<div class="box">
	<form method="post">
		<input type="hidden" name="patientId" value="${patient.patientId}" />
		<table id="orderDrugTable" cellpadding="5" cellspacing="0">
			<tr>
				<th> </th>
				<th> <spring:message code="Order.item.ordered" /> </th>
				<th> <spring:message code="DrugOrder.dose"/> </th>
				<th> <spring:message code="DrugOrder.units"/> </th>
				<th> <spring:message code="DrugOrder.frequency"/> </th>
				<th> <spring:message code="general.dateStart"/> </th>
				<th> <spring:message code="general.instructions" /> </th>
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
							${order.drug.name}
						</a>
					</td>
					<td valign="top">
					   ${order.dose}
					</td>
					<td valign="top">
					   ${order.units}
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
		<b><spring:message code="general.voidReason" />:</b>
		<input type="text" value="" size="40" name="voidReason" /><br/>
		<input type="submit" value="<spring:message code="Order.void"/>" name="action">

	</form>
</div>
</c:if>
<c:if test="${empty orderDrugList}"><spring:message code="Order.list.empty"/></c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>