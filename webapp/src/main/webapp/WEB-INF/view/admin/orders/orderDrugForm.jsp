<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.htm" redirect="/admin/orders/orderDrug.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Order.drug.title"/></h2>

<spring:hasBindErrors name="order">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<c:if test="${order.voided}">
	<form method="post">
		<div class="retiredMessage">
			<div>
				<spring:message code="general.voidedBy"/>
				${order.voidedBy.personName}
				<openmrs:formatDate date="${order.dateVoided}" type="medium" />
				-
				${order.voidReason}
				<input type="submit" name="unvoidOrder" value='<spring:message code="Order.unvoidOrder"/>'/>
			</div>
		</div>
	</form>
</c:if>

<form method="post" class="box">
	<table>
		<tr>
			<td valign="top"><spring:message code="Order.patient"/></td>
			<td valign="top">
				<spring:bind path="order.patient">
					<openmrs:fieldGen type="org.openmrs.Patient" formFieldName="${status.expression}" val="${status.editor.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
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
					<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.value}" parameters="fieldLength=40" />
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
		<c:if test="${order.discontinued}">	
			<tr id="discontinuedBy">
				<td valign="top"><spring:message code="general.discontinuedBy"/></td>
				<td valign="top">${order.discontinuedBy.personName}</td>
			</tr>
			<tr id="dateDiscontinued">
				<td valign="top"><spring:message code="general.dateDiscontinued"/></td>
				<td valign="top">
					<openmrs:formatDate date="${order.discontinuedDate}" type="long" />
				</td>
			</tr>
			<tr id="discontinuedReason">
				<td valign="top"><spring:message code="general.discontinuedReason"/></td>
				<td valign="top">${order.discontinuedReason.name}</td>
			</tr>
		</c:if>
		<c:if test="${order.creator != null}">
			<tr>
				<td><spring:message code="general.createdBy" /></td>
				<td>
					${order.creator.personName} - <openmrs:formatDate date="${order.dateCreated}" type="long" />
				</td>
			</tr>
		</c:if>
		<%-- DrugOrder extensions to Order: --%>
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
					<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="DrugOrder.frequency"/></td>
			<td valign="top">
				<spring:bind path="order.frequency">
					<openmrs:fieldGen type="java.lang.String" formFieldName="${status.expression}" val="${status.value}" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="DrugOrder.prn"/></td>
			<td valign="top">
				<spring:bind path="order.prn">
					<openmrs:fieldGen type="java.lang.Boolean" formFieldName="${status.expression}" val="${status.editor.value}" parameters="isNullable=false" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="DrugOrder.complex"/></td>
			<td valign="top">
				<spring:bind path="order.complex">
					<openmrs:fieldGen type="java.lang.Boolean" formFieldName="${status.expression}" val="${status.editor.value}" parameters="isNullable=false" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="DrugOrder.quantity"/></td>
			<td valign="top">
				<spring:bind path="order.quantity">
					<openmrs:fieldGen type="java.lang.Integer" formFieldName="${status.expression}" val="${status.editor.value}" />
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
	</table>
	<br />
	<input type="submit" name="saveOrder" value="<spring:message code="Order.save"/>">
</form>

<c:if test="${order.discontinued}">
	<form method="post" class="box">
		<input type="submit" value='<spring:message code="Order.undiscontinueOrder"/>' name="undiscontinueOrder"/>
	</form>
</c:if>

<c:if test="${not order.discontinued and not empty order.orderId}">
	<form method="post" class="box">
		<table>
			<tr id="dateDiscontinued">
				<td valign="top"><spring:message code="general.dateDiscontinued"/></td>
				<td valign="top">
					<spring:bind path="order.discontinuedDate">
						<openmrs:fieldGen type="java.util.Date" formFieldName="${status.expression}" val="${status.editor.value}" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
			<tr id="discontinuedReason">
				<td valign="top"><spring:message code="general.discontinuedReason"/></td>
				<td valign="top">
					<spring:bind path="order.discontinuedReason">
						<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="${status.expression}" val="${status.editor.value}" />
						<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
					</spring:bind>
				</td>
			</tr>
		</table>
		<input type="submit" name="discontinueOrder" value='<spring:message code="Order.discontinueOrder"/>'/>
	</form>
</c:if>

<c:if test="${not order.voided and not empty order.orderId}">
	<form method="post" class="box">
		<spring:message code="general.voidReason"/>
		<input type="text" value="" size="40" name="voidReason" />
		<spring:hasBindErrors name="order">
			<c:forEach items="${errors.allErrors}" var="error">
				<c:if test="${error.code == 'voidReason'}"><span class="error"><spring:message code="${error.defaultMessage}" text="${error.defaultMessage}"/></span></c:if>
			</c:forEach>
		</spring:hasBindErrors>
		<input type="submit" name="voidOrder" value='<spring:message code="Order.voidOrder"/>'/>
	</form>
</c:if>

<%@ include file="/WEB-INF/template/footer.jsp" %>