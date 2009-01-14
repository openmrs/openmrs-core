<%@ include file="/WEB-INF/template/include.jsp" %>

<% java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis()); %>

<div id="regimenPortlet">
	<div id="regimenPortletCurrent">
		<div class="boxHeader${model.patientVariation}"><spring:message code="DrugOrder.regimens.current" /></div>
		<div class="box${model.patientVariation}">

			<openmrs:portlet url="patientRegimenCurrent" id="patientRegimenCurrent" patientId="${patient.patientId}" parameters="displayDrugSetIds=${model.displayDrugSetIds},*|displayFutureRegimens=true" />
			
			<span><input type="button" onclick="showHideDiv('regimenPortletAddForm');" value="(+) <spring:message code="DrugOrder.regimens.addOrChange" />"></span>
			<div id="regimenPortletAddForm" style="display:none; border: 1px dashed black; padding: 10px;">
				<table width="100%">
					<tr>
						<c:if test="${not empty model.standardRegimens}">
							<td valign="top">								
								<table>								
									<tr>
										<td colspan="2"><strong><spring:message code="DrugOrder.regimens.addStandard"/></strong></td>
									</tr>
									<c:forEach var="standardRegimen" items="${model.standardRegimens}">
										<tr id="row${standardRegimen.codeName}">
											<form onSubmit="addStandard${standardRegimen.codeName}();">
												<td><a href="javascript:selectStandard('${standardRegimen.codeName}');">${standardRegimen.displayName}</a></td>
												<td><div id="stDtLabel${standardRegimen.codeName}" style="display:none"><spring:message code="general.dateStart"/></div></td>
												<td><div id="stDt${standardRegimen.codeName}" style="display:none"><openmrs:fieldGen type="java.util.Date" formFieldName="startDate${standardRegimen.codeName}" val="" parameters="noBind=true" /></div></td>
												<td><div id="action${standardRegimen.codeName}" style="display:none">
													<select id="actionSelect${standardRegimen.codeName}" onChange="handleStandardActionChange('${standardRegimen.codeName}');">
														<option value=""><spring:message code="DrugOrder.regimen.action.choose" /></option>
														<option value="add"><spring:message code="DrugOrder.regimen.action.addToCurrent" /></option>
														<c:if test="${not empty standardRegimen.canReplace && not empty model.currentDrugOrders}">
															<option value="discontinue"><spring:message code="DrugOrder.regimen.action.discontinue" arguments="${standardRegimen.canReplace}" /></option>
															<option value="void"><spring:message code="DrugOrder.regimen.action.void" arguments="${standardRegimen.canReplace}" /></option>
														</c:if>
													</select>
												</td>
												<td><div id="reas${standardRegimen.codeName}" style="display:none">
													<spring:message code="general.reason" />: 
														<select name="reason${standardRegimen.codeName}" id="reason${standardRegimen.codeName}"></select>
												</div></td>
												<td><div id="replace${standardRegimen.codeName}" style="display:none"><input type="button" value="<spring:message code="DrugOrder.regimen.addAndReplace" />" onClick="addStandard${standardRegimen.codeName}(true);"></div></td>
												<td><div id="add${standardRegimen.codeName}" style="display:none"><input type="button" value="<spring:message code="general.add" />" onClick="addStandard${standardRegimen.codeName}(true);"></div></td>
											</form>
										</tr>
									</c:forEach>
								</table>
							</td>							
						</c:if>
						
						
						<td valign="top" align="right">
							<div id="regimenPortletAddFlexible">
								<form method="post" id="orderForm" onSubmit="handleAddDrugOrder(${model.patientId}, 'drug', 'dose', 'units', 'frequencyDay', 'frequencyWeek', 'startDate')">
								<table>
									<tr>
										<td colspan="2"><strong><spring:message code="DrugOrder.regimens.addCustom"/></strong></td>
									</tr>								
									<tr>
										<td><spring:message code="DrugOrder.drug"/></td>						
										<td>
											<openmrs:fieldGen type="org.openmrs.Drug" formFieldName="drug" val="" parameters="includeVoided=false|noBind=true|optionHeader=[blank]|onChange=updateAddFields('drug','units','frequency')" />
										</td>
									</tr>
									<tr>
										<td><spring:message code="DrugOrder.dose"/></td>
										<td>
											<openmrs:fieldGen type="java.lang.Integer" formFieldName="dose" val="" parameters="noBind=true" />
											<span id="unitsSpan"></span>
											<input type="hidden" id="units" name="units" value="" />
										</td>
									</tr>
									<tr>
										<td><spring:message code="DrugOrder.frequency"/></td>
										<td>
											<%--<openmrs:fieldGen type="java.lang.String" formFieldName="frequency" val="" parameters="noBind=true|fieldLength=8" />--%>
											<select name="frequencyDay" id="frequencyDay">
												<% for ( int i = 1; i <= 10; i++ ) { %>
													<option value="<%= i %>/<spring:message code="DrugOrder.frequency.day" />"><%= i %>/<spring:message code="DrugOrder.frequency.day" /></option>
												<% } %>
											</select>
											<span> x </span>
											<select name="frequencyWeek" id="frequencyWeek">
												<%--<option value="<spring:message code="DrugOrder.frequency.everyDay" />"><spring:message code="DrugOrder.frequency.everyDay" /></option>--%>
												<% for ( int i = 7; i >= 1; i-- ) { %>
													<option value="<%= i %> <spring:message code="DrugOrder.frequency.days" />/<spring:message code="DrugOrder.frequency.week" />"><%= i %> <spring:message code="DrugOrder.frequency.days" />/<spring:message code="DrugOrder.frequency.week" /></option>
												<% } %>
											</select>
										</td>
									</tr>
									<tr>
										<td><spring:message code="general.dateStart"/></td>
										<td>
											<openmrs:fieldGen type="java.util.Date" formFieldName="startDate" val="" parameters="noBind=true" />
										</td>
									</tr>
									<tr>
										<td></td>
										<td>
											<div id="actionNew" style="display:none;">
												<select id="actionSelectNew" onChange="handleStandardActionChangeNew();">
													<option value=""><spring:message code="DrugOrder.regimen.action.choose" /></option>
													<option value="add"><spring:message code="DrugOrder.regimen.action.addToCurrent" /></option>
													<option value="discontinue"><spring:message code="DrugOrder.regimen.action.discontinue.allCurrent" /></option>
													<option value="void"><spring:message code="DrugOrder.regimen.action.void.allCurrent" /></option>
												</select>
											</div>
										</td>
									</tr>
									<tr id="reasNew" style="display:none">
										<td>
												<spring:message code="general.reason" />: 
										</td>
										<td>
												<select name="reasonNew" id="reasonNew"></select>
										</td>
									</tr>
									<tr>
										<td colspan="2" align="center">
											<span id="replaceNew" style="display:none"><input type="button" value="<spring:message code="DrugOrder.regimen.addAndReplace" />" onClick="addNewDrugOrder();"></span>
											<span id="addNew" style="display:none"><input type="button" value="<spring:message code="general.add" />" onClick="addNewDrugOrder();"></span>
											<span id="cancelNew" style="display:none"><input type="button" value="<spring:message code="general.cancel" />" onClick="cancelNewOrder();"></span>
											<%--<td><input type="button" value="<spring:message code="general.add"/>" onClick="handleAddDrugOrder(${model.patientId}, 'drug', 'dose', 'units', 'frequencyDay', 'frequencyWeek', 'startDate')"></td>--%>									
										</td>
									</tr>
								</table>
								</form>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</div>			
	</div>
	<br />
	<div id="regimenPortletCompleted">
		<div class="boxHeader${model.patientVariation}"><spring:message code="DrugOrder.regimens.completed" /></div>
		<div class="box${model.patientVariation}">

			<openmrs:portlet url="patientRegimenCompleted" id="patientRegimenCompleted" patientId="${patient.patientId}" parameters="displayDrugSetIds=${model.displayDrugSetIds}" />
		
		</div>
	</div>
	<openmrs:htmlInclude file="/dwr/interface/DWROrderService.js" />
	<openmrs:htmlInclude file="/dwr/engine.js" />
	<openmrs:htmlInclude file="/dwr/util.js" />
	<script>
		<!-- // begin
		
		var hasOrders = ${fn:length(model.currentDrugOrders)};
		//alert("hasOrders starting as " + hasOrders);
		
		function updateAddFields(drugFieldId, unitsFieldId, frequencyDayFieldId, frequencyWeekFieldId) {
			var drugId = dwr.util.getValue(drugFieldId);
			gUnitsFieldId = unitsFieldId;
			DWROrderService.getUnitsByDrugId(drugId, setUnitsField);
		}
		
		function setUnitsField(unitsText) {
			dwr.util.setValue(gUnitsFieldId + "Span", unitsText);
			dwr.util.setValue(gUnitsFieldId, unitsText);
			hideOtherStandards("New");
			showAppropriateActions("New");
		}
		
		function showAppropriateActions(codeName) {
			//alert("hasOrders is " + hasOrders + " in showAppropriateActions");
			if ( hasOrders > 0 ) {
				hideDiv('add' + codeName);
				showDiv('action' + codeName);
			} else {
				hideDiv('action' + codeName);
				dwr.util.setValue('actionSelect' + codeName, 'add');
				showDiv('add' + codeName);
			}
			showDiv('cancel' + codeName);
		}

		function cancelNewOrder() {
			blankAddNewOrder('drug', 'dose', 'units', 'frequencyDay', 'frequencyWeek', 'startDate');
			hideDiv("addNew");
			hideDiv("actionNew");
			hideDiv("reasNew");
			hideDiv("replaceNew");
			hideDiv("cancelNew");
			showHideOtherStandards("New");
		}
		
		<c:if test="${not empty model.standardRegimens}">

			function selectStandard(codeName) {
				showHideOtherStandards(codeName);
				showFullStandard(codeName);
				// if you've selected a standard regimen, show appropriate actions
				// how do we know if you've selected, vs. unselected?  we check to see if the addNew row is showing (it won't be if you've selected a standard)
				var addNewDiv = document.getElementById("regimenPortletAddFlexible");
				if ( addNewDiv ) {
					if ( addNewDiv.style.display == 'none' ) {
						showAppropriateActions(codeName);
					}
				}
			}
			
			function showHideOtherStandards(codeName) {
				<c:forEach var="standardRegimen" items="${model.standardRegimens}">
					if ( codeName != '${standardRegimen.codeName}' ) showHideDiv('row${standardRegimen.codeName}');
				</c:forEach>
			}

			function hideOtherStandards(codeName) {
				<c:forEach var="standardRegimen" items="${model.standardRegimens}">
					if ( codeName != '${standardRegimen.codeName}' ) hideDiv('row${standardRegimen.codeName}');
				</c:forEach>
			}

			function showFullStandard(codeName) {
				showHideDiv('regimenPortletAddFlexible');
				//alert("should have just showed/hid addNew");
				showHideDiv('stDtLabel' + codeName);
				showHideDiv('stDt' + codeName);
				showHideDiv('submit' + codeName);
				hideDiv('action' + codeName);
				dwr.util.setValue('actionSelect' + codeName, '');
				hideDiv('reas' + codeName);
				hideDiv('replace' + codeName);
				hideDiv('add' + codeName);
			}

			<c:forEach var="standardRegimen" items="${model.standardRegimens}">

				function addStandard${standardRegimen.codeName}(canReplace) {
					var startDate = dwr.util.getValue('startDate${standardRegimen.codeName}');
					if ( startDate && startDate != '' ) {
						if ( canReplace ) {
							var action = dwr.util.getValue('actionSelect${standardRegimen.codeName}');
							var reason = dwr.util.getValue('reason${standardRegimen.codeName}');
							dwr.util.setValue('actionSelect${standardRegimen.codeName}', '');
							dwr.util.setValue('reason${standardRegimen.codeName}', '');
							if ( action == 'void' ) {
								//alert('voiding with [${model.patientId}] [${standardRegimen.canReplace}] [' + reason + ']');
								DWROrderService.voidCurrentDrugSet(${model.patientId}, '${standardRegimen.canReplace}', reason, addComponents${standardRegimen.codeName});
								showHideDiv('reas${standardRegimen.codeName}');
								showHideDiv('replace${standardRegimen.codeName}');
							} else if ( action == 'discontinue' ) {
								//alert('discontinuing with [${model.patientId}] [${standardRegimen.canReplace}] [' + reason + ']');
								DWROrderService.discontinueDrugSet(${model.patientId}, '${standardRegimen.canReplace}', reason, startDate, addComponents${standardRegimen.codeName});
								showHideDiv('reas${standardRegimen.codeName}');
								showHideDiv('replace${standardRegimen.codeName}');
							} else if ( action == 'add') {
								showHideDiv('add${standardRegimen.codeName}');
								addComponents${standardRegimen.codeName}();
							}
						}
					} else {
						alert("<spring:message code="DrugOrder.add.error.missingStartDate" />");
					}
				}
				
				function addComponents${standardRegimen.codeName}() {
					var startDate = dwr.util.getValue('startDate${standardRegimen.codeName}');
					dwr.util.setValue('startDate${standardRegimen.codeName}', '');
					<c:forEach var="drugComponent" items="${standardRegimen.drugComponents}">
						addStack.push("DWROrderService.createDrugOrder(${drugComponent.drugId})");
					</c:forEach>
					<c:forEach var="drugComponent" items="${standardRegimen.drugComponents}">
						addStandardDrug('${drugComponent.drugId}', '${drugComponent.dose}', '${drugComponent.units}', '${drugComponent.frequency}', '${drugComponent.instructions}', startDate);
					</c:forEach>
					//alert('and now here');
					//waitToRefreshRegimenTables();
					selectStandard('${standardRegimen.codeName}');
				}
			</c:forEach>

			function addStandardDrug(drugId, dose, units, frequency, instructions, startDate) {
				DWROrderService.createDrugOrder(${model.patientId}, drugId, dose, units, frequency, startDate, instructions, dwrOrderNotifyComplete);
			}

			function doNothing() {
				// dummy method
			}
			
			function handleStandardActionChange(codeName) {
				var action = dwr.util.getValue('actionSelect' + codeName);
				if ( action == 'void' || action == 'discontinue' ) {
					showDiv('reas' + codeName);
					showDiv('replace' + codeName);
					hideDiv('add' + codeName);
					if ( action == 'void' ) {
						dwr.util.removeAllOptions('reason' + codeName);
						dwr.util.addOptions('reason' + codeName, voidReasons, 'val', 'display');
					} else if ( action == 'discontinue') {
						dwr.util.removeAllOptions('reason' + codeName);
						dwr.util.addOptions('reason' + codeName, discReasons, 'val', 'display');
					}
				} else if ( action == 'add' ) {
					hideDiv('reas' + codeName);
					hideDiv('replace' + codeName);
					showHideDiv('add' + codeName);
				}
			}

		</c:if>

		function handleStandardActionChangeNew() {
			handleStandardActionChange("New");
		}

		function addNewDrugOrder() {
			var action = dwr.util.getValue('actionSelectNew');
			var reason = dwr.util.getValue('reasonNew');
			var startDate = dwr.util.getValue('startDate');
			var drugId = dwr.util.getValue('drug');
			var dose = dwr.util.getValue('dose');
			var units = dwr.util.getValue('units');
			var freqDay = dwr.util.getValue('frequencyDay');
			var freqWeek = dwr.util.getValue('frequencyWeek');
			if ( validateNewOrder(drugId, dose, units, freqDay, freqWeek, startDate) ) {
				dwr.util.setValue('actionSelectNew', '');
				dwr.util.setValue('reasonNew', '');
				if ( action == 'void' ) {
					DWROrderService.voidCurrentDrugOrders(${model.patientId}, reason, addNewComponent);
					showHideDiv('reasNew');
					showHideDiv('replaceNew');
				} else if ( action == 'discontinue' ) {
					DWROrderService.discontinueCurrentDrugOrders(${model.patientId}, reason, startDate, addNewComponent);
					showHideDiv('reasNew');
					showHideDiv('replaceNew');
				} else if ( action == 'add') {
					showHideDiv('addNew');
					addNewComponent();
				}
				hideDiv('cancelNew');
				hideDiv('actionNew');
				showHideOtherStandards("New");
			} else {
				if ( drugId == '' ) alert("<spring:message code="DrugOrder.add.error.missingDrug" />");
				else if ( dose == '' ) alert("<spring:message code="DrugOrder.add.error.missingDose" />");
				else if ( units == '' ) alert("<spring:message code="DrugOrder.add.error.missingUnits" />");
				else if ( freqDay == '' ) alert("<spring:message code="DrugOrder.add.error.missingFrequency" />");
				else if ( freqWeek == '' ) alert("<spring:message code="DrugOrder.add.error.missingFrequency" />");
				else if ( startDate == '' ) alert("<spring:message code="DrugOrder.add.error.missingStartDate" />");
			}
		}

		function validateNewOrder(drug, dose, units, freqDay, freqWeek, startDate) {
			if ( drug == '' || dose == '' || units == '' || freqDay == '' || freqWeek == '' || startDate == '' ) {
				return false;
			} else {
				return true;
			}
		}
		
		function addNewComponent() {
			handleAddDrugOrder(${model.patientId}, 'drug', 'dose', 'units', 'frequencyDay', 'frequencyWeek', 'startDate');
		}

		// end -->
		
	</script>
</div>
