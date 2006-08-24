<%@ include file="/WEB-INF/template/include.jsp" %>

<% java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis()); %>

<div id="regimenPortlet">
	<div id="regimenPortletCurrent">
		<div class="boxHeader"><spring:message code="DrugOrder.regimens.current" /></div>
		<div class="box">
			<table cellpadding="3">
				<thead>
					<tr>
						<th> <spring:message code="Order.item.ordered" /> </th>
						<th> <spring:message code="DrugOrder.dose"/>/<spring:message code="DrugOrder.units"/> </th>
						<th> <spring:message code="DrugOrder.frequency"/> </th>
						<th> <spring:message code="general.dateStart"/> </th>
						<th> <spring:message code="DrugOrder.scheduledStopDate"/> </th>
						<th> <spring:message code="DrugOrder.actualStopDate"/> </th>
						<th> <spring:message code="general.instructions" /> </th>
						<th> </th>
						<th> </th>
					</tr>
				</thead>
<c:choose>
	<c:when test="${not empty model.displayDrugSetIds}">
		<c:forTokens var="drugSetId" items="${model.displayDrugSetIds}" delims=",">
				<tbody id="regimenTableCurrent_header_${drugSetId}">
				</tbody>
				<tbody id="regimenTableCurrent_${drugSetId}">
				</tbody>
		</c:forTokens>
	</c:when>
	<c:otherwise>
				<tbody id="regimenTableCurrent">
				</tbody>
	</c:otherwise>
</c:choose>
			</table>
			<span><a href="#" onClick="showHideDiv('regimenPortletAddForm');">(+) <spring:message code="DrugOrder.regimens.add" /></a></span>
			<div id="regimenPortletAddForm" style="display:none">
				<form method="post" id="orderForm">
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
	<div id="regimenPortletCompleted">
		<div class="boxHeader"><spring:message code="DrugOrder.regimens.completed" /></div>
		<div class="box">
			<table cellpadding="3">
				<thead>
					<tr>
						<th> <spring:message code="Order.item.ordered" /> </th>
						<th> <spring:message code="DrugOrder.dose"/>/<spring:message code="DrugOrder.units"/> </th>
						<th> <spring:message code="DrugOrder.frequency"/> </th>
						<th> <spring:message code="general.dateStart"/> </th>
						<th> <spring:message code="DrugOrder.scheduledStopDate"/> </th>
						<th> <spring:message code="DrugOrder.actualStopDate"/> </th>
						<th> <spring:message code="general.instructions" /> </th>
						<th> <spring:message code="general.discontinuedReason" /> </th>
					</tr>
				</thead>
<c:choose>
	<c:when test="${not empty model.displayDrugSetIds}">
		<c:forTokens var="drugSetId" items="${model.displayDrugSetIds}" delims=",">
				<tbody id="regimenTableCompleted_header_${drugSetId}">
				</tbody>
				<tbody id="regimenTableCompleted_${drugSetId}">
				</tbody>
		</c:forTokens>
	</c:when>
	<c:otherwise>
				<tbody id="regimenTableCompleted">
				</tbody>
	</c:otherwise>
</c:choose>
			</table>
		</div>
	</div>
	<openmrs:htmlInclude file="/dwr/interface/DWROrderService.js" />
	<openmrs:htmlInclude file="/dwr/engine.js" />
	<openmrs:htmlInclude file="/dwr/util.js" />
	<script>
		<!-- // begin
		
		var displayDrugSetIds = "${model.displayDrugSetIds}";
		
		var currentRegimenTableCellFuncs = [
			function(data) { return "&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"orderDrug.form?orderId=" + data.orderId + "\">" + data.drugName + "</a>"; },
			function(data) { return data.dose + " " + data.units; },
			function(data) { return "" + data.frequency; },
			function(data) { return "" + data.startDate; },
			function(data) { return "" + data.autoExpireDate; },
			function(data) { return "" + data.discontinuedDate; },
			function(data) { return "" + data.instructions; },
			function(data) {
				var ret = "<input id=\"closebutton_" + data.orderId + "\" type=\"button\" value=\"<spring:message code="general.close" />\" onClick=\"showHideDiv('close_" + data.orderId + "');showHideDiv('closebutton_" + data.orderId + "')\" />";
				ret += "<div id=\"close_" + data.orderId + "\" style=\"display:none\"><form>";
				ret += "<spring:message code="DrugOrder.discontinuedDate" />: ";
				ret += "<input type=\"text\" id=\"close_" + data.orderId + "_date\" size=\"10\" value=\"\" onFocus=\"showCalendar(this)\" />";
				ret += "&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="general.reason" />: ";
				ret += "<input type=\"text\" id=\"close_" + data.orderId + "_reason\" size=\"10\" value=\"\" />";
				ret += "&nbsp;&nbsp;<input type=\"button\" value=\"<spring:message code="general.save" />\" onClick=\"handleDiscontinueDrugOrder(" + data.orderId + ", 'close_" + data.orderId + "_date', 'close_" + data.orderId + "_reason')\" />";
				ret += "</form></div>";
				return ret;
			},
			function(data) {
				var ret = "<input id=\"voidbutton_" + data.orderId + "\" type=\"button\" value=\"<spring:message code="general.void" />\" onClick=\"showHideDiv('void_" + data.orderId + "');showHideDiv('voidbutton_" + data.orderId + "')\" />";
				ret += "<div id=\"void_" + data.orderId + "\" style=\"display:none\"><form>";
				ret += "<spring:message code="general.reason" />: ";
				ret += "<input type=\"text\" id=\"void_" + data.orderId + "_reason\" size=\"10\" value=\"\" />";
				ret += "&nbsp;&nbsp;<input type=\"button\" value=\"<spring:message code="general.save" />\" onClick=\"handleVoidDrugOrder(" + data.orderId + ", 'void_" + data.orderId + "_reason')\" />";
				ret += "</form></div>";
				return ret;
			}
		];

		var completedRegimenTableCellFuncs = [
			function(data) { return "&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"orderDrug.form?orderId=" + data.orderId + "\">" + data.drugName + "</a>"; },
			function(data) { return data.dose + " " + data.units; },
			function(data) { return "" + data.frequency; },
			function(data) { return "" + data.startDate; },
			function(data) { return "" + data.autoExpireDate; },
			function(data) { return "" + data.discontinuedDate; },
			function(data) { return "" + data.instructions; },
			function(data) { return "" + data.discontinueReason; },
			function(data) {
				var ret = "<input id=\"voidcpbutton_" + data.orderId + "\" type=\"button\" value=\"<spring:message code="general.void" />\" onClick=\"showHideDiv('voidcp_" + data.orderId + "');showHideDiv('voidcpbutton_" + data.orderId + "')\" />";
				ret += "<div id=\"voidcp_" + data.orderId + "\" style=\"display:none\"><form>";
				ret += "<spring:message code="general.reason" />: ";
				ret += "<input type=\"text\" id=\"voidcp_" + data.orderId + "_reason\" size=\"10\" value=\"\" />";
				ret += "&nbsp;&nbsp;<input type=\"button\" value=\"<spring:message code="general.save" />\" onClick=\"handleVoidDrugOrder(" + data.orderId + ", 'voidcp_" + data.orderId + "_reason')\" />";
				ret += "</form></div>";
				return ret;
			}
			
		];

		var currentRegimenTableHeaderCells = [
			function(data) { 
				var ret = "<table><tr><td style=\"padding:7px 5px 2px 5px\">" + data.name + "";
				if ( data.drugCount > 0 ) {
					ret += "</td><td style=\"padding:7px 5px 0px 5px\"><input id=\"closegpbutton_" + data.drugSetId + "\" type=\"button\" value=\"<spring:message code="general.closeGroup" />\" onClick=\"showHideDiv('closegp_" + data.drugSetId + "');showHideDiv('closegpbutton_" + data.drugSetId + "')\" />";
					ret += "<div id=\"closegp_" + data.drugSetId + "\" style=\"display:none\"><form>";
					ret += "<spring:message code="DrugOrder.discontinuedDate" />: ";
					ret += "<input type=\"text\" id=\"closegp_" + data.drugSetId + "_date\" size=\"10\" value=\"\" onFocus=\"showCalendar(this)\" />";
					ret += "&nbsp;&nbsp;&nbsp;&nbsp;<spring:message code="general.reason" />: ";
					ret += "<input type=\"text\" id=\"closegp_" + data.drugSetId + "_reason\" size=\"10\" value=\"\" />";
					ret += "&nbsp;&nbsp;<input type=\"button\" value=\"<spring:message code="general.save" />\" onClick=\"handleDiscontinueDrugSet(" + data.drugSetId + ", 'closegp_" + data.drugSetId + "_date', 'closegp_" + data.drugSetId + "_reason')\" />";
					ret += "</form></div>";

					ret += "</td><td style=\"padding:7px 5px 0px 5px\"><input id=\"voidgpbutton_" + data.drugSetId + "\" type=\"button\" value=\"<spring:message code="general.voidGroup" />\" onClick=\"showHideDiv('voidgp_" + data.drugSetId + "');showHideDiv('voidgpbutton_" + data.drugSetId + "')\" />";
					ret += "<div id=\"voidgp_" + data.drugSetId + "\" style=\"display:none\"><form>";
					ret += "<spring:message code="general.reason" />: ";
					ret += "<input type=\"text\" id=\"voidgp_" + data.drugSetId + "_reason\" size=\"10\" value=\"\" />";
					ret += "&nbsp;&nbsp;<input type=\"button\" value=\"<spring:message code="general.save" />\" onClick=\"handleVoidDrugSet(" + data.drugSetId+ ", 'voidgp_" + data.drugSetId + "_reason')\" />";
					ret += "</form></div></td></tr></table>";
					return ret;
				} else {
					ret += "</td><td style=\"padding:7px 5px 2px 5px\"><span class=\"noOrdersMessage\">(<spring:message code="DrugOrder.list.noOrders" />)</span></td></tr></table>";
					return ret;
				}
			}
		];

		var completedRegimenTableHeaderCells = [
			function(data) { 
				var ret = "<table><tr><td style=\"padding:7px 5px 2px 5px\">" + data.name + "";
				if ( data.drugCount == 0 ) {
					ret += "</td><td style=\"padding:7px 5px 2px 5px\"><span class=\"noOrdersMessage\">(<spring:message code="DrugOrder.list.noOrders" />)</span></td></tr></table>";
				} else {
					ret += "</td></tr></table>";
				}
				return ret;
			}
		];

		var gUnitsFieldId = '';

		function handleVoidDrugSet(drugSetId, voidReasonField) {
			var voidReason = DWRUtil.getValue($(voidReasonField));
			DWROrderService.voidDrugSet(${model.patientId}, drugSetId, voidReason, refreshRegimenTables);
		}

		function handleDiscontinueDrugSet(drugSetId, discDateField, discReasonField) {
			var discDate = DWRUtil.getValue($(discDateField));
			var discReason = DWRUtil.getValue($(discReasonField));
			DWROrderService.discontinueDrugSet(${model.patientId}, drugSetId, discReason, discDate, refreshRegimenTables);
		}

		function updateAddFields(drugFieldId, unitsFieldId, frequencyDayFieldId, frequencyWeekFieldId) {
			var drugId = DWRUtil.getValue(drugFieldId);
			gUnitsFieldId = unitsFieldId;
			DWROrderService.getUnitsByDrugId(drugId, setUnitsField);
		}
		
		function setUnitsField(unitsText) {
			DWRUtil.setValue(gUnitsFieldId + "Span", unitsText);
			DWRUtil.setValue(gUnitsFieldId, unitsText);
		}

		function showHideDiv(id) {
			var div = document.getElementById(id);
			if ( div ) {
				if ( div.style.display != "none" ) {
					div.style.display = "none";
				} else { 
					div.style.display = "";
				}
			}
		}
		
		function handleAddDrugOrder(drugField, doseField, unitsField, frequencyDayField, frequencyWeekField, startDateField) {
			var drugId = DWRUtil.getValue($(drugField));
			var dose = DWRUtil.getValue($(doseField));
			var units = DWRUtil.getValue($(unitsField));
			var frequency = DWRUtil.getValue($(frequencyDayField)) + " x " + DWRUtil.getValue($(frequencyWeekField));
			var startDate = DWRUtil.getValue($(startDateField));
			var patientId = ${model.patientId};
			DWRUtil.setValue($(drugField),"");
			DWRUtil.setValue($(doseField),"");
			DWRUtil.setValue($(unitsField),"");
			DWRUtil.setValue($(frequencyDayField),"");
			DWRUtil.setValue($(frequencyWeekField),"");
			DWRUtil.setValue($(startDateField),"");
			DWROrderService.createDrugOrder(patientId, drugId, dose, units, frequency, startDate, refreshRegimenTables);
		}
		
		function handleVoidDrugOrder(orderId, voidReasonField) {
			var voidReason = DWRUtil.getValue($(voidReasonField));
			DWROrderService.voidOrder(orderId, voidReason, refreshRegimenTables);
		}

		function handleDiscontinueDrugOrder(orderId, discDateField, discReasonField) {
			var discDate = DWRUtil.getValue($(discDateField));
			var discReason = DWRUtil.getValue($(discReasonField));
			DWROrderService.discontinueOrder(orderId, discReason, discDate, refreshRegimenTables);
		}

		function refreshRegimenTables() {
			refreshCurrentRegimenTable();
			refreshCompletedRegimenTable();
		}
		
		function refreshCurrentRegimenTable() {
			if ( displayDrugSetIds.length > 0 ) {
				var drugSetIds = displayDrugSetIds.split(",");
				removeDrugOrders('regimenTableCurrent');
				for ( var i = 0; i < drugSetIds.length; i++ ) {
					DWROrderService.getCurrentDrugSet(${model.patientId}, drugSetIds[i], addCurrentDrugSetHeader);
					//alert('added header, now adding rows...');
					DWROrderService.getCurrentDrugOrdersByPatientIdDrugSetId(${model.patientId}, drugSetIds[i], addCurrentDrugOrders);
				}
			} else {
				DWROrderService.getCurrentDrugOrdersByPatientId(${model.patientId}, handleRefreshCurrentRegimenTable);
			}
		}

		function refreshCompletedRegimenTable() {
			if ( displayDrugSetIds.length > 0 ) {
				var drugSetIds = displayDrugSetIds.split(",");
				removeDrugOrders('regimenTableCompleted');
				for ( var i = 0; i < drugSetIds.length; i++ ) {
					DWROrderService.getCompletedDrugSet(${model.patientId}, drugSetIds[i], addCompletedDrugSetHeader);
					//alert('added header, now adding rows...');
					DWROrderService.getCompletedDrugOrdersByPatientIdDrugSetId(${model.patientId}, drugSetIds[i], addCompletedDrugOrders);
				}
			} else {
				DWROrderService.getCompletedDrugOrdersByPatientId(${model.patientId}, handleRefreshCompletedRegimenTable);
			}
		}

		function addCurrentDrugSetHeader(drugSet) {
			var firstSet = drugSet[0];
			var drugSetId = '';
			if ( firstSet ) {
				drugSetId = firstSet.drugSetId;
			}
			DWRUtil.addRows('regimenTableCurrent_header_' + drugSetId, drugSet, currentRegimenTableHeaderCells, {
				cellCreator:function(options) {
				    var td = document.createElement("td");
				    td.setAttribute("colspan", "9");
				    return td;
				}
			});
		}

		function addCompletedDrugSetHeader(drugSet) {
			var firstSet = drugSet[0];
			var drugSetId = '';
			if ( firstSet ) {
				drugSetId = firstSet.drugSetId;
			}
			DWRUtil.addRows('regimenTableCompleted_header_' + drugSetId, drugSet, completedRegimenTableHeaderCells, {
				cellCreator:function(options) {
				    var td = document.createElement("td");
				    td.setAttribute("colspan", "8");
				    return td;
				}
			});
		}
		
		function addCurrentDrugOrders(drugOrders) {
			//alert('in acdo');
			var tableName = 'regimenTableCurrent';
			if ( drugOrders ) {
				var firstOrder = drugOrders[0];
				if ( firstOrder ) { 
					//	alert('firstOrder exists');
					if ( firstOrder.drugSetId ) {
						//alert('firstOrder dsId is ' + firstOrder.drugSetId);
						tableName += "_" + firstOrder.drugSetId;	
					}				
				}
				DWRUtil.addRows(tableName, drugOrders, currentRegimenTableCellFuncs, {
					cellCreator:function(options) {
						//alert("Begin cellCreator");
					    var td = document.createElement("td");
						//alert("End cellCreator");
					    return td;
					}
				});

			}
			//alert('out acdo');
		}

		function addCompletedDrugOrders(drugOrders) {
			var tableName = 'regimenTableCompleted';
			if ( drugOrders ) {
				var firstOrder = drugOrders[0];
				if ( firstOrder ) { 
					//	alert('firstOrder exists');
					if ( firstOrder.drugSetId ) {
						//alert('firstOrder dsId is ' + firstOrder.drugSetId);
						tableName += "_" + firstOrder.drugSetId;	
					}				
				}
				DWRUtil.addRows(tableName, drugOrders, completedRegimenTableCellFuncs, {
					cellCreator:function(options) {
						//alert("Begin cellCreator");
					    var td = document.createElement("td");
						//alert("End cellCreator");
					    return td;
					}
				});

			}
			//alert('out acdo');
		}

		function removeDrugOrders(tableName) {
			if ( displayDrugSetIds.length > 0 ) {
				//alert('length gt 0');
				var drugSetIds = displayDrugSetIds.split(",");
				for ( var i = 0; i < drugSetIds.length; i++ ) {
					DWRUtil.removeAllRows(tableName + '_header_' + drugSetIds[i]);
					DWRUtil.removeAllRows(tableName + '_' + drugSetIds[i]);
				}
			} else {
				//alert('length lt 0');
				DWRUtil.removeAllRows(tableName);
			}
		}

		function handleRefreshCurrentRegimenTable(drugOrders) {
			removeDrugOrders('regimenTableCurrent');
			addCurrentDrugOrders(drugOrders);
		}
		
		function handleRefreshCompletedRegimenTable(drugOrders) {
			removeDrugOrders('regimenTableCompleted');
			addCompletedDrugOrders(drugOrders);
		}

		refreshCurrentRegimenTable();
		refreshCompletedRegimenTable();

		// end -->
		
	</script>
</div>
