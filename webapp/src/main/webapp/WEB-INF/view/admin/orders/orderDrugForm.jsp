<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/orderDrug.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<c:if test="${order.patient != null}">
	<a href="../../patientDashboard.form?patientId=<c:out value="${order.patient.patientId}" />"><openmrs:message code="patientDashboard.viewDashboard"/></a>
</c:if>


<h2><openmrs:message code="Order.drug.title"/></h2>

<spring:hasBindErrors name="order">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>

<c:if test="${order.voided}">
	<form method="post">
		<div class="retiredMessage">
			<div>
				<openmrs:message code="general.voidedBy"/>
				<c:out value="${order.voidedBy.personName}" />
				<openmrs:formatDate date="${order.dateVoided}" type="medium" />
				-
				${order.voidReason}
				<input type="submit" name="unvoidOrder" value='<openmrs:message code="Order.unvoidOrder"/>'/>
			</div>
		</div>
	</form>
</c:if>

<b class="boxHeader"><openmrs:message code="Order.drug.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<td valign="top"><openmrs:message code="Order.patient"/></td>
			<td valign="top">
				<spring:bind path="order.patient">
					<openmrs:fieldGen type="org.openmrs.Patient" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Order.concept"/></td>
			<td valign="top">
				<spring:bind path="order.concept">
					<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.instructions"/></td>
			<td valign="top">
				<spring:bind path="order.instructions">
					<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.value}" parameters="fieldLength=40" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.dateStart"/></td>
			<td valign="top">
				<spring:bind path="order.startDate">
					<openmrs:fieldGen type="java.util.Date" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.dateAutoExpire"/></td>
			<td valign="top">
				<spring:bind path="order.autoExpireDate">
					<openmrs:fieldGen type="java.util.Date" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Order.encounter"/></td>
			<td valign="top">
				<spring:bind path="order.encounter">
					<openmrs:fieldGen type="org.openmrs.Encounter" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Order.orderer"/></td>
			<td valign="top">
				<spring:bind path="order.orderer">
					<openmrs:fieldGen type="org.openmrs.Provider" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<c:if test="${order.discontinued}">	
			<tr id="discontinuedBy">
				<td valign="top"><openmrs:message code="general.discontinuedBy"/></td>
				<td valign="top"><c:out value="${order.discontinuedBy.personName}" /></td>
			</tr>
			<tr id="dateDiscontinued">
				<td valign="top"><openmrs:message code="general.dateDiscontinued"/></td>
				<td valign="top">
					<openmrs:formatDate date="${order.discontinuedDate}" type="long" />
				</td>
			</tr>
			<tr id="discontinuedReason">
				<td valign="top"><openmrs:message code="general.discontinuedReason"/></td>
				<td valign="top">${order.discontinuedReason.name}</td>
			</tr>
		</c:if>
		<c:if test="${order.creator != null}">
			<tr>
				<td><openmrs:message code="general.createdBy" /></td>
				<td>
					<c:out value="${order.creator.personName}" /> - <openmrs:formatDate date="${order.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
		<%-- DrugOrder extensions to Order: --%>
		<tr>
			<td valign="top"><openmrs:message code="DrugOrder.dose"/></td>
			<td valign="top">
				<spring:bind path="order.dose">
					<openmrs:fieldGen type="java.lang.Integer" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="DrugOrder.units"/></td>
			<td valign="top">
				<spring:bind path="order.doseUnits">
					<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="DrugOrder.frequency"/></td>
			<td valign="top">
				<spring:bind path="order.frequency">
					<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="DrugOrder.prn"/></td>
			<td valign="top">
				<spring:bind path="order.asNeeded">
					<openmrs:fieldGen type="java.lang.Boolean" formFieldName="${status.expression}" val="${status.editor.value}" parameters="isNullable=false" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="DrugOrder.quantity"/></td>
			<td valign="top">
				<spring:bind path="order.quantity">
					<openmrs:fieldGen type="java.lang.Integer" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="DrugOrder.quantityUnits"/></td>
			<td valign="top">
				<spring:bind path="order.quantityUnits">
					<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="DrugOrder.drug"/></td>
			<td valign="top">
				<spring:bind path="order.drug">
					<openmrs:fieldGen type="org.openmrs.Drug" formFieldName="${status.expression}" val="${status.editor.value}" parameters="optionHeader=[blank]" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="DrugOrder.brandName"/></td>
			<td valign="top">
				<spring:bind path="order.brandName">
					<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.value}" parameters="fieldLength=100|isNullable=false" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
	</table>
	<br />
	<input type="submit" name="saveOrder" value="<openmrs:message code="Order.save"/>">
</form>

<c:if test="${order.discontinued}">
	<br/>
	<b class="boxHeader"><openmrs:message code="Order.undiscontinueOrder"/></b>
	<form method="post" class="box">
		<input type="submit" value='<openmrs:message code="Order.undiscontinueOrder"/>' name="undiscontinueOrder"/>
	</form>
</c:if>

<c:if test="${not order.discontinued and not empty order.orderId}">
	<br/>
	<b class="boxHeader"><openmrs:message code="Order.discontinueOrder"/></b>
	<form method="post" class="box">
		<table>
			<tr id="dateDiscontinued">
				<td valign="top"><openmrs:message code="general.dateDiscontinued"/></td>
				<td valign="top">
					<spring:bind path="order.discontinuedDate">
						<openmrs:fieldGen type="java.util.Date" formFieldName="${status.expression}" val="${status.editor.value}" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<tr id="discontinuedReason">
				<td valign="top"><openmrs:message code="general.discontinuedReason"/></td>
				<td valign="top">
					<spring:bind path="order.discontinuedReason">
						<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="${status.expression}" val="${status.editor.value}" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
		</table>
		<input type="submit" name="discontinueOrder" value='<openmrs:message code="Order.discontinueOrder"/>'/>
	</form>
</c:if>

<c:if test="${not order.voided and not empty order.orderId}">
	<br/>
	<b class="boxHeader"><openmrs:message code="Order.voidOrder"/></b>
	<form method="post" class="box">
		<openmrs:message code="general.voidReason"/>
		<input type="text" value="" size="40" name="voidReason" />
		<spring:hasBindErrors name="order">
			<c:forEach items="${errors.allErrors}" var="error">
				<c:if test="${error.code == 'voidReason'}"><span class="error"><openmrs:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
			</c:forEach>
		</spring:hasBindErrors>
		<input type="submit" name="voidOrder" value='<openmrs:message code="Order.voidOrder"/>'/>
	</form>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>