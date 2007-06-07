<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/order.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsPopup.js"></script>

<h2><spring:message code="Order.title"/></h2>

<spring:hasBindErrors name="order">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post" id="orderForm">
<table>
	<tr>
		<td><spring:message code="Order.orderType"/></td>
		<td>
			<spring:bind path="order.orderType">
				<openmrs:fieldGen type="org.openmrs.OrderType" formFieldName="${status.expression}" val="${status.editor.value}" parameters="optionHeader=[blank]" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="Order.concept"/></td>
		<td valign="top">
			<spring:bind path="order.concept">
				<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="${status.expression}" val="${status.editor.value}" />
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
		<td valign="top"><spring:message code="general.dateAutoExpire"/></td>
		<td valign="top">
			<spring:bind path="order.autoExpireDate">
				<openmrs:fieldGen type="java.util.Date" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
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
		<td valign="top"><spring:message code="Order.orderer"/></td>
		<td valign="top">
			<spring:bind path="order.orderer">
				<openmrs:fieldGen type="org.openmrs.User" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.discontinued"/></td>
		<td valign="top">
			<spring:bind path="order.discontinued">
				<openmrs:fieldGen type="java.lang.Boolean" formFieldName="${status.expression}" val="${status.editor.value}" parameters="isNullable=false" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.discontinuedBy"/></td>
		<td valign="top">
			<spring:bind path="order.discontinuedBy">
				<openmrs:fieldGen type="org.openmrs.User" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.dateDiscontinued"/></td>
		<td valign="top">
			<spring:bind path="order.discontinuedDate">
				<openmrs:fieldGen type="java.util.Date" formFieldName="${status.expression}" val="${status.editor.value}" />
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(order.creator == null)}">
		<tr>
			<td><spring:message code="general.createdBy" /></td>
			<td>
				${order.creator.personName} - <openmrs:formatDate date="${order.dateCreated}" type="long" />
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.voided"/></td>
			<td valign="top">
				<spring:bind path="order.voided">
					<openmrs:fieldGen type="java.lang.Boolean" formFieldName="${status.expression}" val="${status.editor.value}" parameters="isNullable=false" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.voidedBy"/></td>
			<td valign="top">
				<spring:bind path="order.voidedBy">
					<openmrs:fieldGen type="org.openmrs.User" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.dateVoided"/></td>
			<td valign="top">
				<spring:bind path="order.dateVoided">
					<openmrs:fieldGen type="java.util.Date" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
	</c:if>
</table>
<br />
<input type="submit" value="<spring:message code="Order.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>