<%@ include file="/WEB-INF/template/include.jsp" %>

<% java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis()); %>

<div class="boxHeader">Regimens</div>
<div class="box">
	<c:if test="${not empty model.patientDrugOrders}">
	<table cellpadding="3">
		<tr>
			<th> <spring:message code="Order.item.ordered" /> </th>
			<th> <spring:message code="DrugOrder.dose"/>/<spring:message code="DrugOrder.units"/> </th>
			<th> <spring:message code="DrugOrder.frequency"/> </th>
			<th> <spring:message code="general.dateStart"/> </th>
			<th> <spring:message code="DrugOrder.actualStopDate"/> </th>
			<th> <spring:message code="DrugOrder.scheduledStopDate"/> </th>
			<th> <spring:message code="general.instructions" /> </th>
		</tr>
		<openmrs:forEachDrugOrder drugOrders="${model.patientDrugOrders}" sortBy="startDate" descending="false" var="order">
			<tr>
				<td>
					<a href="orderDrug.form?orderId=${order.orderId}">
						${order.drug.name}
					</a>
				</td>
				<td>${order.dose} ${order.units}</td>
				<td>${order.frequency}</td>
				<td>
					<openmrs:formatDate date="${order.startDate}" format="dd/MM/yyyy" />
				</td>
				<td>
					<openmrs:formatDate date="${order.discontinuedDate}" format="dd/MM/yyyy" />
				</td>
				<td>
					<c:if test="${order.discontinuedDate == null && order.autoExpireDate > now}">
						<i><openmrs:formatDate date="${order.autoExpireDate}" format="dd/MM/yyyy" /></i>
					</c:if>
				</td>
				<td>${order.instructions}</td>
			</tr>
		</openmrs:forEachDrugOrder>
	</table>
	</c:if>
	<c:if test="${empty model.patientDrugOrders}">
		<spring:message code="Order.drug.list.empty"/>
	</c:if>
</div>