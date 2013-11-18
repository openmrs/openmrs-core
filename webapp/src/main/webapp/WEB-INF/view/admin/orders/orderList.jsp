<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/order.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.addOnLoad( function() {
		toggleRowVisibilityForClass("orderTable", "voided", false);
	})
</script>

<h2><openmrs:message code="Order.manage.title"/></h2>	
		
<a href="order.form"><openmrs:message code="Order.add"/></a> 
<a href="orderDrug.form"><openmrs:message code="Order.drug.add"/></a><br />

<br />

<c:forEach items="${errors.allErrors}" var="error">
	<span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span><
</c:forEach>

<span class="boxHeader">
	<span style="float: right">
		<a href="#" id="showVoided" onClick="return toggleRowVisibilityForClass('orderTable', 'voided', false);"><openmrs:message code="general.toggle.voided"/></a>
	</span>
	<openmrs:message code="Order.list.title"/>
</span>
<c:if test="${not empty orderList}">
<div class="box">
	<form method="post">
		<table id="orderTable" cellpadding="5" cellspacing="0">
			<tr>
				<th> </th>
				<th> <openmrs:message code="Order.orderType" /> </th>
				<th> <openmrs:message code="Patient.names" /> </th>
				<th> <openmrs:message code="Order.item.ordered" /> </th>
				<th> <openmrs:message code="general.instructions" /> </th>
				<th> </th>
			</tr>
			<c:forEach var="order" items="${orderList}">
				<tr class="<c:if test="${order.voided}">voided </c:if>">
					<td valign="top">
						<c:if test="${not order.voided}">
							<input type="checkbox" name="orderId" value="${order.orderId}">
						</c:if>
					</td>
					<td valign="top">${order.orderType.name}</td>
					<c:if test="${order.encounter == null}">
						<td></td>
					</c:if>
					<c:if test="${order.encounter != null}">
						<td valign="top">
							<c:forEach var="name" items="${order.encounter.patient.names}">
								<c:out value="${name.familyName}" />, <c:out value="${name.givenName}" /><br />
							</c:forEach>
						</td>
					</c:if>
					<td valign="top"><c:out value="${conceptNames[order.concept.conceptId]}" /></td>
					<td valign="top">
					   <c:out value="${order.instructions}" />
					</td>
					<td valign="top">
						<a href="<c:choose><c:when test="${order.class.name=='org.openmrs.DrugOrder'}">orderDrug</c:when><c:otherwise>order</c:otherwise></c:choose>.form?orderId=${order.orderId}">
							<openmrs:message code="general.edit" />
						</a>
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
<c:if test="${empty orderList}"><openmrs:message code="Order.list.empty"/></c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>