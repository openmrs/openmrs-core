<%@ include file="/WEB-INF/template/include.jsp" %>

<% java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis()); %>

<openmrs:htmlInclude file="/dwr/interface/DWROrderService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/scripts/drugOrder.js" appendLocale="true" />

		<div id="regimenPortletCompleted">
			<table class="regimenCompletedTable">
				<thead>
					<tr class="regimenCompletedHeaderRow">
						<th style="nowrap: true;" class="regimenCompletedDrugOrderedHeader"> <spring:message code="Order.item.ordered" /> </th>
						<th class="regimenCompletedDrugDoseHeader"> <spring:message code="DrugOrder.dose"/>/<spring:message code="DrugOrder.units"/> </th>
						<th class="regimenCompletedDrugFrequencyHeader"> <spring:message code="DrugOrder.frequency"/> </th>
						<th class="regimenCompletedDrugDateStartHeader"> <spring:message code="general.dateStart"/> </th>
						<th class="regimenCompletedDrugScheduledStopDateHeader"> <spring:message code="DrugOrder.scheduledStopDate"/> </th>
						<th class="regimenCompletedDrugActualStopDateHeader"> <spring:message code="DrugOrder.actualStopDate"/> </th>
						<th class="regimenCompletedDrugInstructionHeader"> <spring:message code="general.instructions" /> </th>
						<th class="regimenCompletedDrugDiscontinuedReasonHeader"> <spring:message code="general.discontinuedReason" /> </th>
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
					<tr class="regimenCompletedHeaderOtherRow">
						<c:choose>
							<c:when test="${model.completedRegimenMode == 'view'}">
								<td colspan="6" class="regimenCompletedViewModeData"><table class="regimenCompletedViewModeInnerTable"><tr class="regimenCompletedViewModeInnerRow"><td class="regimenCompletedViewModeInnerData"><spring:message code="DrugOrder.header.otherRegimens" /></td></tr></table></td>
							</c:when>
							<c:otherwise>
								<td colspan="8" class="regimenCompletedOtherModeData"><table class="regimenCompletedOtherModeInnerTable"><tr class="regimenCompletedOtherModeInnerRow"><td class="regimenCompletedOtherModeInnerData"><spring:message code="DrugOrder.header.otherRegimens" /></td></tr></table></td>
							</c:otherwise>
						</c:choose>
					</tr>
				</tbody>
				<tbody id="regimenTableCompleted___other__">
					<c:if test="${not empty model.completedDrugOrderSets['*']}">
						<c:forEach items="${model.completedDrugOrderSets['*']}" var="drugOrder">
							<tr class="regimenCompletedOtherRow">
								<td nowrap class="regimenCompletedOtherData">&nbsp;&nbsp;&nbsp;&nbsp;
									<c:if test="${!empty drugOrder.drug}">
										<a class="patientRegimenDrugName" href="${pageContext.request.contextPath}/admin/orders/orderDrug.form?orderId=${drugOrder.orderId}">${drugOrder.drug.name}</a>
									</c:if>
									<c:if test="${empty drugOrder.drug}">
										<a class="patientRegimenDrugName" href="${pageContext.request.contextPath}/admin/orders/orderDrug.form?orderId=${drugOrder.orderId}">${drugOrder.concept.name.name}</a>
									</c:if>
								</td>
								<td class="regimenCompletedOtherDrugOrder" >${drugOrder.dose} ${drugOrder.units}</td>
								<td class="regimenCompletedOtherDrugFrequency">${drugOrder.frequency}</td>
								<td class="regimenCompletedOtherStartDate"><openmrs:formatDate date="${drugOrder.startDate}" type="medium" /></td>
								<td class="regimenCompletedOtherAutoExpireDate"><openmrs:formatDate date="${drugOrder.autoExpireDate}" type="medium" /></td>
								<td class="regimenCompletedOtherDiscontinuedDate"><openmrs:formatDate date="${drugOrder.discontinuedDate}" type="medium" /></td>
								<td class="regimenCompletedOtherInstructions">${drugOrder.instructions}</td>
								<td class="regimenCompletedOtherDiscontinuedReason">
									<c:if test="${not empty drugOrder.discontinuedReason}">
										<openmrs_tag:concept conceptId="${drugOrder.discontinuedReason.conceptId}" />
									</c:if>
								</td>
								<c:if test="${model.completedRegimenMode != 'view'}">
									<td class="regimenCompletedOtherModeData">
										<input id="voidbutton_${drugOrder.orderId}" type="button" value="<spring:message code="DrugOrder.void" />" onClick="showHideDiv('void_${drugOrder.orderId}');showHideDiv('voidbutton_${drugOrder.orderId}')" />
										<div id="void_${drugOrder.orderId}" style="display:none" class="dashedAndHighlighted">
											<form class="regimenCompletedReasonForm">
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
						<tr class="completedDrugOrderSetsRow">
							<c:choose>
								<c:when test="${model.completedRegimenMode == 'view'}">
									<td colspan="8" class="completedRegimenViewModeData"><span class="noOrdersMessage">&nbsp;&nbsp;&nbsp;&nbsp;(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:when>
								<c:otherwise>
									<td colspan="9" class="completedRegimenOtherModeData"><span class="noOrdersMessage">&nbsp;&nbsp;&nbsp;&nbsp;(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:if>
				</tbody>
			</c:if>
			<c:if test="${drugSetId != '*'}" >
				<tbody id="regimenTableCompleted_header_${fn:replace(drugSetId, " ", "_")}">
					<tr class="regimenTableCompletedRow">
						<c:choose>
							<c:when test="${model.completedRegimenMode == 'view'}">
								<td colspan="8" class="regimenTableCompletedViewModeData">
							</c:when>
							<c:otherwise>
								<td colspan="9" class="regimenTableCompletedOtherModeData">
							</c:otherwise>
						</c:choose>
							<table class="drugOrderTable">
								<tr class="drugOrderHeadersRow">
									<td class="drugOrderHeadersData"><openmrs_tag:concept conceptId="${model.drugOrderHeaders[drugSetId].conceptId}" /></td>
								</tr>
							</table>
						</td>
					</tr>
				</tbody>
				<tbody id="regimenTableCompleted_${fn:replace(drugSetId, " ", "_")}">
					<c:if test="${not empty model.completedDrugOrderSets[drugSetId]}">
						<c:forEach items="${model.completedDrugOrderSets[drugSetId]}" var="drugOrder">
							<tr class="patientRegimenDrugOrderRow">
								<td class="patientRegimenDrugNameData">&nbsp;&nbsp;&nbsp;&nbsp;
									<c:if test="${!empty drugOrder.drug}">
										<a class="patientRegimenDrugName" href="${pageContext.request.contextPath}/admin/orders/orderDrug.form?orderId=${drugOrder.orderId}">${drugOrder.drug.name}</a>
									</c:if>
									<c:if test="${empty drugOrder.drug}">
										<a class="patientRegimenDrugName" href="${pageContext.request.contextPath}/admin/orders/orderDrug.form?orderId=${drugOrder.orderId}">${drugOrder.concept.name.name}</a>
									</c:if>
								</td>	
								<td class="patientRegimenDrugDoseData">${drugOrder.dose} ${drugOrder.units}</td>
								<td class="patientRegimenDrugFrequencyData">${drugOrder.frequency}</td>
								<td class="patientRegimenDrugStartDateData"><openmrs:formatDate date="${drugOrder.startDate}" type="medium" /></td>
								<td class="patientRegimenDrugAutoExpireDateData"><openmrs:formatDate date="${drugOrder.autoExpireDate}" type="medium" /></td>
								<td class="patientRegimenDrugDiscontinuedDateData"><openmrs:formatDate date="${drugOrder.discontinuedDate}" type="medium" /></td>
								<td class="patientRegimenDrugInstrucionData">${drugOrder.instructions}</td>
								<td class="patientRegimenDrugDiscontinuedReasonData">
									<c:if test="${not empty drugOrder.discontinuedReason}">
										<openmrs_tag:concept conceptId="${drugOrder.discontinuedReason.conceptId}" />
									</c:if>
								</td>
								<c:if test="${model.completedRegimenMode != 'view'}">
									<td class="patientRegimenDrugButtonData">
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
						<tr class="noDrugsOrderRow">
							<c:choose>
								<c:when test="${model.completedRegimenMode == 'view'}">
									<td colspan="8" class="noDrugsOrderViewModeData"><span class="noOrdersMessage">&nbsp;&nbsp;&nbsp;&nbsp;(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
								</c:when>
								<c:otherwise>
									<td colspan="9" class="noDrugsOrderOtherModeData"><span class="noOrdersMessage">&nbsp;&nbsp;&nbsp;&nbsp;(<spring:message code="DrugOrder.list.noOrders" />)</span></td>
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
							<tr id="regimenTableCompletedRow">
								<td class="patientRegimenDrugNameData">
									<c:if test="${!empty drugOrder.drug}">
										<span class="patientRegimenDrugName">${drugOrder.drug.name}</span>
									</c:if>
									<c:if test="${empty drugOrder.drug}">
										<span class="patientRegimenDrugName">${drugOrder.concept.name.name}</span>
									</c:if>
								</td>
								<td class="patientRegimenDrugDoseData">${drugOrder.dose} ${drugOrder.units}</td>
								<td class="patientRegimenDrugFrequencyData">${drugOrder.frequency}</td>
								<td class="patientRegimenDrugStartDateData"><openmrs:formatDate date="${drugOrder.startDate}" type="medium" /></td>
								<td class="patientRegimenDrugAutoExpireDateData"><openmrs:formatDate date="${drugOrder.autoExpireDate}" type="medium" /></td>
								<td class="patientRegimenDrugDiscontinuedDateData"><openmrs:formatDate date="${drugOrder.discontinuedDate}" type="medium" /></td>
								<td class="patientRegimenDrugInstrucionData">${drugOrder.instructions}</td>
								<td class="patientRegimenDrugDiscontinuedReasonData">
									<c:if test="${not empty drugOrder.discontinuedReason}">
										<openmrs_tag:concept conceptId="${drugOrder.discontinuedReason.conceptId}" />
									</c:if>
								</td>
								<c:if test="${model.completedRegimenMode != 'view'}">
									<td class="patientRegimenDrugButtonData">
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
						<tr class="patientRegimenDrugsNoOrderRow">
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

