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
						<th> <spring:message code="DrugOrder.actualStopDate"/> </th>
						<th> <spring:message code="DrugOrder.scheduledStopDate"/> </th>
						<th> <spring:message code="general.instructions" /> </th>
						<th> </th>
						<th> </th>
					</tr>
				</thead>
				<tbody id="regimenTableCurrent">
					<c:if test="${empty model.patientDrugOrders}">
						<td colspan="9"><spring:message code="Order.drug.list.empty"/></td>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>
	<div id="regimenPortletAdd">
		<div class="boxHeader"><a class="boxHeader" href="#" onClick="showRegimenPortletItem('regimenPortletAddForm');"><spring:message code="DrugOrder.regimens.add" /></a></div>
		<div class="box" id="regimenPortletAddForm" style="display:none">
			<form method="post" id="orderForm">
			<input type="hidden" name="patientId" value="${model.patientId}" />
			<table>
				<tr>
					<td><spring:message code="DrugOrder.drug"/></td>
					<td>
						<openmrs:fieldGen type="org.openmrs.Drug" formFieldName="drug" val="" parameters="noBind=true" />
					</td>
					<td><spring:message code="DrugOrder.dose"/></td>
					<td>
						<openmrs:fieldGen type="java.lang.Integer" formFieldName="dose" val="" parameters="noBind=true" />
					</td>
					<td><spring:message code="DrugOrder.units"/></td>
					<td>
						<openmrs:fieldGen type="java.lang.String" formFieldName="units" val="" parameters="noBind=true|fieldLength=12" />
					</td>
					<td><spring:message code="DrugOrder.frequency"/></td>
					<td>
						<openmrs:fieldGen type="java.lang.String" formFieldName="frequency" val="" parameters="noBind=true|fieldLength=8" />
					</td>
					<td><spring:message code="general.dateStart"/></td>
					<td>
						<openmrs:fieldGen type="java.util.Date" formFieldName="startDate" val="" parameters="noBind=true" />
					</td>
					<td><input type="button" value="<spring:message code="general.add"/>" onClick="handleAddDrugOrder('drug', 'dose', 'units', 'frequency', 'startDate')"></td>
				</tr>
			</table>
			</form>
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
						<th> <spring:message code="DrugOrder.actualStopDate"/> </th>
						<th> <spring:message code="DrugOrder.scheduledStopDate"/> </th>
						<th> <spring:message code="general.instructions" /> </th>
						<th> <spring:message code="general.discontinuedReason" /> </th>
					</tr>
				</thead>
				<tbody id="regimenTableCompleted">
					<c:if test="${empty model.patientDrugOrders}">
						<td colspan="8"><spring:message code="Order.drug.list.empty"/></td>
					</c:if>
				</tbody>
			</table>
		</div>
	</div>
	<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWROrderService.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/engine.js"></script>
	<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/util.js"></script>
	<script>
		<!-- // begin
		
		var currentRegimenTableCellFuncs = [
			function(data) { return "<a href=\"orderDrug.form?orderId=" + data.orderId + "\">" + data.drugName + "</a>"; },
			function(data) { return data.dose + " " + data.units; },
			function(data) { return "" + data.frequency; },
			function(data) { return "" + data.startDate; },
			function(data) { return "" + data.discontinuedDate; },
			function(data) { return "" + data.autoExpireDate; },
			function(data) { return "" + data.instructions; },
			function(data) {
				var ret = "<input id=\"closebutton_" + data.orderId + "\" type=\"button\" value=\"<spring:message code="general.close" />\" onClick=\"showRegimenPortletItem('close_" + data.orderId + "');hideRegimenPortletItem('closebutton_" + data.orderId + "')\" />";
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
				var ret = "<input id=\"voidbutton_" + data.orderId + "\" type=\"button\" value=\"<spring:message code="general.void" />\" onClick=\"showRegimenPortletItem('void_" + data.orderId + "');hideRegimenPortletItem('voidbutton_" + data.orderId + "')\" />";
				ret += "<div id=\"void_" + data.orderId + "\" style=\"display:none\"><form>";
				ret += "<spring:message code="general.reason" />: ";
				ret += "<input type=\"text\" id=\"void_" + data.orderId + "_reason\" size=\"10\" value=\"\" />";
				ret += "&nbsp;&nbsp;<input type=\"button\" value=\"<spring:message code="general.save" />\" onClick=\"handleVoidDrugOrder(" + data.orderId + ", 'void_" + data.orderId + "_reason')\" />";
				ret += "</form></div>";
				return ret;
			}
		];

		var completedRegimenTableCellFuncs = [
			function(data) { return "<a href=\"orderDrug.form?orderId=" + data.orderId + "\">" + data.drugName + "</a>"; },
			function(data) { return data.dose + " " + data.units; },
			function(data) { return "" + data.frequency; },
			function(data) { return "" + data.startDate; },
			function(data) { return "" + data.discontinuedDate; },
			function(data) { return "" + data.autoExpireDate; },
			function(data) { return "" + data.instructions; },
			function(data) { return "" + data.discontinueReason; }
		];

		function showRegimenPortletItem(itemId) {
			var addForm = document.getElementById(itemId);
			if ( addForm ) {
				addForm.style.display = "";
			}
		}
		
		function hideRegimenPortletItem(itemId) {
			var addForm = document.getElementById(itemId);
			if ( addForm ) {
				addForm.style.display = "none";
			}
		}

		function handleAddDrugOrder(drugField, doseField, unitsField, frequencyField, startDateField) {
			var drugId = DWRUtil.getValue($(drugField));
			var dose = DWRUtil.getValue($(doseField));
			var units = DWRUtil.getValue($(unitsField));
			var frequency = DWRUtil.getValue($(frequencyField));
			var startDate = DWRUtil.getValue($(startDateField));
			var patientId = ${model.patientId};
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
			//alert("Begin refreshCurrent");
			DWROrderService.getCurrentDrugOrdersByPatientId(${model.patientId}, handleRefreshCurrentRegimenTable);
			//alert("End refreshCurrent");
		}
		
		function refreshCompletedRegimenTable() {
			//alert("Begin refreshCompleted");
			DWROrderService.getCompletedDrugOrdersByPatientId(${model.patientId}, handleRefreshCompletedRegimenTable);
			//alert("End refreshCompleted");
		}

		function handleRefreshCurrentRegimenTable(drugOrders) {
			//alert("Begin handleRefreshCurrentRegimenTable");
			DWRUtil.removeAllRows('regimenTableCurrent');
			//alert("Removed all rows");
			DWRUtil.addRows('regimenTableCurrent', drugOrders, currentRegimenTableCellFuncs, {
				cellCreator:function(options) {
					//alert("Begin cellCreator");
				    var td = document.createElement("td");
					//alert("End cellCreator");
				    return td;
				}
			});
			//alert("End handleRefreshCurrentRegimenTable");
		}
		
		function handleRefreshCompletedRegimenTable(drugOrders) {
			DWRUtil.removeAllRows('regimenTableCompleted');
			DWRUtil.addRows('regimenTableCompleted', drugOrders, completedRegimenTableCellFuncs, {
				cellCreator:function(options) {
				    var td = document.createElement("td");
				    return td;
				}
			});
		}

		refreshCurrentRegimenTable();
		refreshCompletedRegimenTable();

		// end -->
		
	</script>
</div>
