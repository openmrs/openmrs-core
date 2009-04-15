<%@ include file="/WEB-INF/template/include.jsp" %>

<% java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis()); %>

<openmrs:htmlInclude file="/dwr/interface/DWROrderService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/scripts/drugOrder.js" />

		<div id="regimenPortletCompleted">
			<table>
				<thead>
					<tr>
						<th style="nowrap: true;"> <spring:message code="Order.item.ordered" /> </th>
						<th> <spring:message code="DrugOrder.dose"/>/<spring:message code="DrugOrder.units"/> </th>
						<th> <spring:message code="DrugOrder.frequency"/> </th>
						<th> <spring:message code="general.dateStart"/> </th>
						<th> <spring:message code="DrugOrder.scheduledStopDate"/> </th>
						<th> <spring:message code="DrugOrder.actualStopDate"/> </th>
						<th> <spring:message code="general.instructions" /> </th>
						<th> <spring:message code="general.discontinuedReason" /> </th>
						<c:if test="${model.completedRegimenMode != 'view'}">
							<th> </th>
						</c:if>
					</tr>
				</thead>
<c:choose>
	<c:when test="${not empty model.displayDrugSetIds}">
		<c:forTokens var="drugSetId" items="${model.displayDrugSetIds}" delims=",">
			<c:if test="${drugSetId == '*'}" >
				<tbody id="regimenTableCompleted_header___other__">
					<tr>
						<c:choose>
							<c:when test="${model.completedRegimenMode == 'view'}">
								<td colspan="6"><table><tr><td><spring:message code="DrugOrder.header.otherRegimens" /></td></tr></table></td>
							</c:when>
							<c:otherwise>
								<td colspan="8"><table><tr><td><spring:message code="DrugOrder.header.otherRegimens" /></td></tr></table></td>
							</c:otherwise>
						</c:choose>
					</tr>
				</tbody>
				<tbody id="regimenTableCompleted___other__">
					<c:if test="${not empty model.completedDrugOrderSets['*']}">
						<c:forEach items="${model.completedDrugOrderSets['*']}" var="drugOrder">
							<tr>
								<td nowrap>&nbsp;&nbsp;&nbsp;&nbsp;
									<c:if test="${!empty drugOrder.drug}">
										<a class="patientRegimenDrugName" href="${pageContext.request.contextPath}/admin/orders/orderDrug.form?orderId=${drugOrder.orderId}">${drugOrder.drug.name}</a>
									</c:if>
									<c:if test="${empty drugOrder.drug}">
										<a class="patientRegimenDrugName" href="${pageContext.request.contextPath}/admin/orders/orderDrug.form?orderId=${drugOrder.orderId}">${drugOrder.concept.name.name}</a>
									</c:if>
								</td>
								<td>${drugOrder.dose} ${drugOrder.units}</td>
								<td>${drugOrder.frequency}</td>
								<td><openmrs:formatDate date="${drugOrder.startDate}" type="medium" /></td>
								<td><openmrs:formatDate date="${drugOrder.autoExpireDate}" type="medium" /></td>
								<td><openmrs:formatDate date="${drugOrder.discontinuedDate}" type="medium" /></td>
								<td>${drugOrder.instructions}</td>
								<td>
									<c:if test="${not empty drugOrder.discontinuedReason}">
										<openmrs_tag:concept conceptId="${drugOrder.discontinuedReason.conceptId}" />
									</c:if>
								</td>
								<c:if test="${model.completedRegimenMode != 'view'}">
									<td>
										<input id="voidbutton_${drugOrder.orderId}" type="button" value="<spring:message code="DrugOrder.void" />" onClick="showHideDiv('void_${drugOrder.orderId}');showHideDiv('voidbutton_${drugOrder.orderId}')" />
										<div id="void_${drugOrder.orderId}" style="display:none" class="dashedAndHighlighted">
											<form>
												<spring:message code="general.reason" />: 
													<select name="void_${drugOrder.orderId}_reason" id="void_${drugOrder.orderId}_reason">
														<option value=""></option>
														<option value="DrugOrder.void.reason.dateError"><spring:message code="DrugOrder.void.reason.dateError" /></option>
														<option value="DrugOrder.void.reason.error"><spring:message code="DrugOrder.void.reason.error" /></option>
														<option value="DrugOrder.void.reason.other"><spring:message code="DrugOrder.void.reason.other" /></option>
													</select>
												&nbsp;&nbsp;
												<input type="button" value="<spring:message code="DrugOrder.void" />" onClick="handleVoidCompletedDrugOrder('${drugOrder.orderId}', 'void_${drugOrder.orderId}_reason')" />
												<input type="button" value="<spring:message code="general.cancel" />" onClick="showHideDiv('void_${drugOrder.orderId}');showHideDiv('voidbutton_${drugOrder.orderId}')" />
											</form>
										</div>
									</td>
								</c:if>
							</tr>
						</c:forEach>
					</c:if>
					<c:if test="${empty model.completedDrugOrderSets['*']}">
						<tr>
							<c:choose>
								<c:when test="${model.completedRegimenMode == 'view'}">
									<td colspan="8"><span class="noOrdersMessage">&nbsp;&nbsp;&nbsp;&nbsp;(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:when>
								<c:otherwise>
									<td colspan="9"><span class="noOrdersMessage">&nbsp;&nbsp;&nbsp;&nbsp;(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:if>
				</tbody>
			</c:if>
			<c:if test="${drugSetId != '*'}" >
				<tbody id="regimenTableCompleted_header_${fn:replace(drugSetId, " ", "_")}">
					<tr>
						<c:choose>
							<c:when test="${model.completedRegimenMode == 'view'}">
								<td colspan="8">
							</c:when>
							<c:otherwise>
								<td colspan="9">
							</c:otherwise>
						</c:choose>
							<table>
								<tr>
									<td><openmrs_tag:concept conceptId="${model.drugOrderHeaders[drugSetId].conceptId}" /></td>
								</tr>
							</table>
						</td>
					</tr>
				</tbody>
				<tbody id="regimenTableCompleted_${fn:replace(drugSetId, " ", "_")}">
					<c:if test="${not empty model.completedDrugOrderSets[drugSetId]}">
						<c:forEach items="${model.completedDrugOrderSets[drugSetId]}" var="drugOrder">
							<tr>
								<td>&nbsp;&nbsp;&nbsp;&nbsp;
									<c:if test="${!empty drugOrder.drug}">
										<a class="patientRegimenDrugName" href="${pageContext.request.contextPath}/admin/orders/orderDrug.form?orderId=${drugOrder.orderId}">${drugOrder.drug.name}</a>
									</c:if>
									<c:if test="${empty drugOrder.drug}">
										<a class="patientRegimenDrugName" href="${pageContext.request.contextPath}/admin/orders/orderDrug.form?orderId=${drugOrder.orderId}">${drugOrder.concept.name.name}</a>
									</c:if>
								</td>	
								<td>${drugOrder.dose} ${drugOrder.units}</td>
								<td>${drugOrder.frequency}</td>
								<td><openmrs:formatDate date="${drugOrder.startDate}" type="medium" /></td>
								<td><openmrs:formatDate date="${drugOrder.autoExpireDate}" type="medium" /></td>
								<td><openmrs:formatDate date="${drugOrder.discontinuedDate}" type="medium" /></td>
								<td>${drugOrder.instructions}</td>
								<td>
									<c:if test="${not empty drugOrder.discontinuedReason}">
										<openmrs_tag:concept conceptId="${drugOrder.discontinuedReason.conceptId}" />
									</c:if>
								</td>
								<c:if test="${model.completedRegimenMode != 'view'}">
									<td>
										<input id="voidbutton_${drugOrder.orderId}" type="button" value="<spring:message code="DrugOrder.void" />" onClick="showHideDiv('void_${drugOrder.orderId}');showHideDiv('voidbutton_${drugOrder.orderId}')" />
										<div id="void_${drugOrder.orderId}" style="display:none" class="dashedAndHighlighted">
											<form>
												<spring:message code="general.reason" />: 
													<select name="void_${drugOrder.orderId}_reason" id="void_${drugOrder.orderId}_reason">
														<option value=""></option>
														<option value="DrugOrder.void.reason.dateError"><spring:message code="DrugOrder.void.reason.dateError" /></option>
														<option value="DrugOrder.void.reason.error"><spring:message code="DrugOrder.void.reason.error" /></option>
														<option value="DrugOrder.void.reason.other"><spring:message code="DrugOrder.void.reason.other" /></option>
													</select>
												&nbsp;&nbsp;
												<input type="button" value="<spring:message code="DrugOrder.void" />" onClick="handleVoidCompletedDrugOrder('${drugOrder.orderId}', 'void_${drugOrder.orderId}_reason')" />
												<input type="button" value="<spring:message code="general.cancel" />" onClick="showHideDiv('void_${drugOrder.orderId}');showHideDiv('voidbutton_${drugOrder.orderId}')" />
											</form>
										</div>
									</td>
								</c:if>
							</tr>
						</c:forEach>
					</c:if>
					<c:if test="${empty model.completedDrugOrderSets[drugSetId]}">
						<tr>
							<c:choose>
								<c:when test="${model.completedRegimenMode == 'view'}">
									<td colspan="8"><span class="noOrdersMessage">&nbsp;&nbsp;&nbsp;&nbsp;(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:when>
								<c:otherwise>
									<td colspan="9"><span class="noOrdersMessage">&nbsp;&nbsp;&nbsp;&nbsp;(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:if>
				</tbody>
			</c:if>
		</c:forTokens>
	</c:when>
	<c:otherwise>
				<tbody id="regimenTableCompleted">
					<c:if test="${not empty model.completedDrugOrders}">
						<c:forEach items="${model.completedDrugOrders}" var="drugOrder">
							<tr>
								<td>
									<c:if test="${!empty drugOrder.drug}">
										<span class="patientRegimenDrugName">${drugOrder.drug.name}</span>
									</c:if>
									<c:if test="${empty drugOrder.drug}">
										<span class="patientRegimenDrugName">${drugOrder.concept.name.name}</span>
									</c:if>
								</td>
								<td>${drugOrder.dose} ${drugOrder.units}</td>
								<td>${drugOrder.frequency}</td>
								<td><openmrs:formatDate date="${drugOrder.startDate}" type="medium" /></td>
								<td><openmrs:formatDate date="${drugOrder.autoExpireDate}" type="medium" /></td>
								<td><openmrs:formatDate date="${drugOrder.discontinuedDate}" type="medium" /></td>
								<td>${drugOrder.instructions}</td>
								<td>
									<c:if test="${not empty drugOrder.discontinuedReason}">
										<openmrs_tag:concept conceptId="${drugOrder.discontinuedReason.conceptId}" />
									</c:if>
								</td>
								<c:if test="${model.completedRegimenMode != 'view'}">
									<td>
										<input id="voidbutton_${drugOrder.orderId}" type="button" value="<spring:message code="DrugOrder.void" />" onClick="showHideDiv('void_${drugOrder.orderId}');showHideDiv('voidbutton_${drugOrder.orderId}')" />
										<div id="void_${drugOrder.orderId}" style="display:none" class="dashedAndHighlighted">
											<form>
												<spring:message code="general.reason" />: 
													<select name="void_${drugOrder.orderId}_reason" id="void_${drugOrder.orderId}_reason">
														<option value=""></option>
														<option value="DrugOrder.void.reason.dateError"><spring:message code="DrugOrder.void.reason.dateError" /></option>
														<option value="DrugOrder.void.reason.error"><spring:message code="DrugOrder.void.reason.error" /></option>
														<option value="DrugOrder.void.reason.other"><spring:message code="DrugOrder.void.reason.other" /></option>
													</select>
												&nbsp;&nbsp;
												<input type="button" value="<spring:message code="DrugOrder.void" />" onClick="handleVoidCompletedDrugOrder('${drugOrder.orderId}', 'void_${drugOrder.orderId}_reason')" />
												<input type="button" value="<spring:message code="general.cancel" />" onClick="showHideDiv('void_${drugOrder.orderId}');showHideDiv('voidbutton_${drugOrder.orderId}')" />
											</form>
										</div>
									</td>
								</c:if>
							</tr>
						</c:forEach>
					</c:if>
					<c:if test="${empty model.completedDrugOrders}">
						<tr>
							<c:choose>
								<c:when test="${model.currentRegimenMode == 'view'}">
									<td colspan="8"><span class="noOrdersMessage">(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:when>
								<c:otherwise>
									<td colspan="9"><span class="noOrdersMessage">(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:if>
				</tbody>
	</c:otherwise>
</c:choose>
			</table>
			<script>
				setPatientId("${model.patientId}");
				setDisplayDrugSetIds("${model.displayDrugSetIds}");
				setRegimenMode("${model.completedRegimenMode}");
			</script>
		</div>

