<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/orderDrug.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsPopup.js"></script>

<h2><spring:message code="Order.drug.title"/></h2>

<spring:hasBindErrors name="order">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post" id="orderForm">
<input type="hidden" name="patientId" value="${patientId}" />
<table>
	<tr>
		<td valign="top"><spring:message code="Order.encounter"/></td>
		<td valign="top">
			<spring:bind path="order.encounter">
				<openmrs:fieldGen type="org.openmrs.Encounter" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="DrugOrder.drug"/></td>
		<td valign="top">
			<spring:bind path="order.drug">
				<openmrs:fieldGen type="org.openmrs.Drug" formFieldName="${status.expression}" val="${status.editor.value}" parameters="optionHeader=[blank]" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="DrugOrder.dose"/></td>
		<td valign="top">
			<spring:bind path="order.dose">
				<openmrs:fieldGen type="java.lang.Integer" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="DrugOrder.units"/></td>
		<td valign="top">
			<spring:bind path="order.units">
				<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="DrugOrder.frequency"/></td>
		<td valign="top">
			<spring:bind path="order.frequency">
				<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.instructions"/></td>
		<td valign="top">
			<spring:bind path="order.instructions">
				<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.editor.value}" parameters="fieldLength=40" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.dateStart"/></td>
		<td valign="top">
			<spring:bind path="order.startDate">
				<openmrs:fieldGen type="java.util.Date" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="Order.orderer"/></td>
		<td valign="top">
			<spring:bind path="order.orderer">
				<openmrs:fieldGen type="org.openmrs.User" formFieldName="${status.expression}" val="${status.editor.value}" parameters="roles=Clinician" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<br />
<input type="submit" value="<spring:message code="Order.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>