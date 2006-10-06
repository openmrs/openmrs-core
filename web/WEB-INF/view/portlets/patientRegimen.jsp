<%@ include file="/WEB-INF/template/include.jsp" %>

<% java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis()); %>

<div id="regimenPortlet">
	<div id="regimenPortletCurrent">
		<div class="boxHeader"><spring:message code="DrugOrder.regimens.current" /></div>
		<div class="box">

			<openmrs:portlet url="patientRegimenCurrent" id="patientRegimenCurrent" patientId="${patient.patientId}" parameters="displayDrugSetIds=ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS,*" />
			
			<span><a href="javascript:void();" onClick="showHideDiv('regimenPortletAddForm');">(+) <spring:message code="DrugOrder.regimens.addOrChange" /></a></span>
			<div id="regimenPortletAddForm" style="display:none">
				<c:if test="${not empty model.standardRegimens}">
					<table>
						<c:forEach var="standardRegimen" items="${model.standardRegimens}">
							<tr id="row${standardRegimen.codeName}">
								<form onSubmit="addStandard${standardRegimen.codeName}();">
									<td><a href="javascript:void();" onClick="selectStandard('${standardRegimen.codeName}')">${standardRegimen.displayName}</a></td>
									<td><div id="stDtLabel${standardRegimen.codeName}" style="display:none"><spring:message code="general.dateStart"/></div></td>
									<td><div id="stDt${standardRegimen.codeName}" style="display:none"><openmrs:fieldGen type="java.util.Date" formFieldName="startDate${standardRegimen.codeName}" val="" parameters="noBind=true" /></div></td>
									<c:choose>
										<c:when test="${not empty standardRegimen.canReplace}">
											<td><div id="action${standardRegimen.codeName}" style="display:none">
												<select id="actionSelect${standardRegimen.codeName}" onChange="handleStandardActionChange('${standardRegimen.codeName}');">
													<option value=""><spring:message code="DrugOrder.regimen.action.choose" /></option>
													<option value="add"><spring:message code="DrugOrder.regimen.action.addToCurrent" /></option>
													<option value="discontinue"><spring:message code="DrugOrder.regimen.action.discontinue" arguments="${standardRegimen.canReplace}" /></option>
													<option value="void"><spring:message code="DrugOrder.regimen.action.void" arguments="${standardRegimen.canReplace}" /></option>
												</select>
											</td>
											<td><div id="reas${standardRegimen.codeName}" style="display:none">
												<spring:message code="general.reason" />: <input id="reason${standardRegimen.codeName}" name="reason${standardRegimen.codeName}" size="14" value="" />
											</div></td>
											<td><div id="replace${standardRegimen.codeName}" style="display:none"><input type="button" value="<spring:message code="DrugOrder.regimen.addAndReplace" />" onClick="addStandard${standardRegimen.codeName}(true);"></div></td>
											<td><div id="add${standardRegimen.codeName}" style="display:none"><input type="button" value="<spring:message code="general.add" />" onClick="addStandard${standardRegimen.codeName}(true);"></div></td>
										</c:when>
										<c:otherwise>
											<td><div id="submit${standardRegimen.codeName}" style="display:none"><input type="button" value="<spring:message code="general.add" />" onClick="addStandard${standardRegimen.codeName}(false);"></div></td>
										</c:otherwise>
									</c:choose>
								</form>
							</tr>
						</c:forEach>
					</table>
				</c:if>
				<div id="regimenPortletAddFlexible">
					<form method="post" id="orderForm" onSubmit="handleAddDrugOrder('drug', 'dose', 'units', 'frequencyDay', 'frequencyWeek', 'startDate')">
					<input type="hidden" name="patientId" value="${model.patientId}" />
					<table>
						<tr>
							<td><spring:message code="DrugOrder.drug"/></td>
							<td>
								<openmrs:fieldGen type="org.openmrs.Drug" formFieldName="drug" val="" parameters="noBind=true|optionHeader=[blank]|onChange=updateAddFields('drug','units','frequency')" />
							</td>
							<td><spring:message code="DrugOrder.dose"/></td>
							<td>
								<openmrs:fieldGen type="java.lang.Integer" formFieldName="dose" val="" parameters="noBind=true" />
							</td>
							<td>
								<span id="unitsSpan"></span>
								<input type="hidden" id="units" name="units" value="" />
								<%--<openmrs:fieldGen type="java.lang.String" formFieldName="units" val="" parameters="noBind=true|fieldLength=12" />--%>
							</td>
							<td><spring:message code="DrugOrder.frequency"/></td>
							<td>
								<%--<openmrs:fieldGen type="java.lang.String" formFieldName="frequency" val="" parameters="noBind=true|fieldLength=8" />--%>
								<select name="frequencyDay" id="frequencyDay">
									<% for ( int i = 1; i <= 10; i++ ) { %>
										<option value="<%= i %>/<spring:message code="DrugOrder.frequency.day" />"><%= i %>/<spring:message code="DrugOrder.frequency.day" /></option>
									<% } %>
								</select>
							</td>
							<td>
								<span> x </span>
							</td>
							<td>
								<select name="frequencyWeek" id="frequencyWeek">
									<option value="<spring:message code="DrugOrder.frequency.everyDay" />"><spring:message code="DrugOrder.frequency.everyDay" /></option>
									<% for ( int i = 1; i <= 6; i++ ) { %>
										<option value="<%= i %> <spring:message code="DrugOrder.frequency.days" />/<spring:message code="DrugOrder.frequency.week" />"><%= i %> <spring:message code="DrugOrder.frequency.days" />/<spring:message code="DrugOrder.frequency.week" /></option>
									<% } %>
								</select>
							</td>
							<td><spring:message code="general.dateStart"/></td>
							<td>
								<openmrs:fieldGen type="java.util.Date" formFieldName="startDate" val="" parameters="noBind=true" />
							</td>
							<td><input type="button" value="<spring:message code="general.add"/>" onClick="handleAddDrugOrder('drug', 'dose', 'units', 'frequencyDay', 'frequencyWeek', 'startDate')"></td>
						</tr>
					</table>
					</form>
				</div>
			</div>
		</div>			
	</div>
	<div id="regimenPortletCompleted">
		<div class="boxHeader"><spring:message code="DrugOrder.regimens.completed" /></div>
		<div class="box">

			<openmrs:portlet url="patientRegimenCompleted" id="patientRegimenCompleted" patientId="${patient.patientId}" parameters="displayDrugSetIds=ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS,*" />
		
		</div>
	</div>
	<openmrs:htmlInclude file="/dwr/interface/DWROrderService.js" />
	<openmrs:htmlInclude file="/dwr/engine.js" />
	<openmrs:htmlInclude file="/dwr/util.js" />
	<script>
		<!-- // begin
		
		<c:if test="${not empty model.standardRegimens}">

			function selectStandard(codeName) {
				hideOtherStandards(codeName);
				showFullStandard(codeName);
			}
			
			function hideOtherStandards(codeName) {
				<c:forEach var="standardRegimen" items="${model.standardRegimens}">
					if ( codeName != '${standardRegimen.codeName}' ) showHideDiv('row${standardRegimen.codeName}');
				</c:forEach>
			}

			function showFullStandard(codeName) {
				showHideDiv('regimenPortletAddFlexible');
				showHideDiv('stDtLabel' + codeName);
				showHideDiv('stDt' + codeName);
				showHideDiv('submit' + codeName);
				showHideDiv('action' + codeName);
			}

			<c:forEach var="standardRegimen" items="${model.standardRegimens}">

				function addStandard${standardRegimen.codeName}(canReplace) {
					var startDate = DWRUtil.getValue('startDate${standardRegimen.codeName}');
					DWRUtil.setValue('startDate${standardRegimen.codeName}', '');
					if ( canReplace ) {
						var action = DWRUtil.getValue('actionSelect${standardRegimen.codeName}');
						var reason = DWRUtil.getValue('reason${standardRegimen.codeName}');
						DWRUtil.setValue('actionSelect${standardRegimen.codeName}', '');
						DWRUtil.setValue('reason${standardRegimen.codeName}', '');
						if ( action == 'void' ) {
							//alert('voiding with [${model.patientId}] [${standardRegimen.canReplace}] [' + reason + ']');
							DWROrderService.voidDrugSet(${model.patientId}, '${standardRegimen.canReplace}', reason, doNothing);
							showHideDiv('reas${standardRegimen.codeName}');
							showHideDiv('replace${standardRegimen.codeName}');
						} else if ( action == 'discontinue' ) {
							//alert('discontinuing with [${model.patientId}] [${standardRegimen.canReplace}] [' + reason + ']');
							DWROrderService.discontinueDrugSet(${model.patientId}, '${standardRegimen.canReplace}', reason, startDate, doNothing);
							showHideDiv('reas${standardRegimen.codeName}');
							showHideDiv('replace${standardRegimen.codeName}');
						} else if ( action == 'add') {
							showHideDiv('add${standardRegimen.codeName}');
						}
					}
					//alert('here');
					<c:forEach var="drugComponent" items="${standardRegimen.drugComponents}">
						addStandardDrug('${drugComponent.drugId}', '${drugComponent.dose}', '${drugComponent.units}', '${drugComponent.frequency}', '${drugComponent.instructions}', startDate);
					</c:forEach>
					//alert('and now here');
					refreshRegimenTables();
					selectStandard('${standardRegimen.codeName}');
				}
			</c:forEach>

			function addStandardDrug(drugId, dose, units, frequency, instructions, startDate) {
				DWROrderService.createDrugOrder(${model.patientId}, drugId, dose, units, frequency, startDate, instructions, doNothing);
			}

			function doNothing() {
				// dummy method
			}

			function handleStandardActionChange(codeName) {
				var action = DWRUtil.getValue('actionSelect' + codeName);
				if ( action == 'void' || action == 'discontinue' ) {
					showHideDiv('reas' + codeName);
					showHideDiv('replace' + codeName);
				} else if ( action == 'add' ) {
					showHideDiv('add' + codeName);
				}
			}

		</c:if>

		// end -->
		
	</script>
</div>
