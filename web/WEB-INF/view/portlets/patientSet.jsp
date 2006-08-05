<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/easyAjax.js"></script>

<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRPatientSetService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/engine.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/util.js"></script>

<c:choose>
	<c:when test="${model.bodyId != null}">
		<c:set var="tableBodyId" value="${model.bodyId}"/>
	</c:when>
	<c:otherwise>
		<c:set var="tableBodyId" value="patientSetTableBody"/>
	</c:otherwise>
</c:choose>

<script language="JavaScript">
	hideLayer('${model.id}');

	var localList = null;

	if (${model.patientSet != null}) {
		localList = "${model.patientSet.commaSeparatedPatientIds}".split(",");
		<c:if test="${model.varToSet != null}">
			<%-- TODO: refresh this variable on changes to localList --%>
			${model.varToSet} = "${model.patientSet.commaSeparatedPatientIds}";
		</c:if>
	}

	var PS_fromIndex = 0;
	var PS_pageSize = 15;
	<c:if test="${model.pageSize != null}">PS_pageSize = ${model.pageSize}</c:if>
	var PS_totalPatients = 0;

	function remove(patientId) {
		if (localList != null) {
			for (var i = 0; i < localList.length; ++i) {
				if (localList[i] == patientId) {
					localList.splice(i, 1);
					--i;
				}
			}
			refreshList();
		} else {
			DWRPatientSetService.removeFromMyPatientSet(patientId, function() { refreshList() });
		}
	}
	
	function clearThis() {
		if (localList != null) {
			localList = new Array();
			refreshList();
			hideLayer('${model.id}');
		} else {
			DWRPatientSetService.clearMyPatientSet(<c:if test="${model.id != null}">function() { refreshList(); hideLayer('${model.id}')}</c:if>);
		}
	}
	
	function goToStart() {
		PS_fromIndex = 0;
		refreshList();
	}
	
	function pageBackwards() {
		PS_fromIndex -= PS_pageSize;
		if (PS_fromIndex < 0) {
			PS_fromIndex = 0;
		}
		refreshList();
	}

	function pageForwards() {
		PS_fromIndex += PS_pageSize;
		if (localList != null && PS_fromIndex >= PS_totalPatients) {
			PS_fromIndex -= PS_pageSize;
			return;
		}
		refreshList();
	}
	
	function goToEnd() {
		PS_fromIndex = PS_totalPatients - PS_pageSize;
		if (PS_fromIndex < 0) {
			PS_fromIndex = 0;
		}
		refreshList();
	}
	
	function refreshList() {
		if (localList != null) {
			PS_totalPatients = localList.length;
			document.getElementById("PS_totalNumber").innerHTML = localList.length;
		} else {
			DWRPatientSetService.getMyPatientSetSize(function(sz) { PS_totalPatients = sz; document.getElementById("PS_totalNumber").innerHTML = sz; });
		}
		<%-- TODO: this is commented out because it breaks the page in IE. Find the right place to put it.
		DWRUtil.useLoadingMessage();
		--%>
		if (localList != null) {
			var ptIds = new Array();
			var n = PS_fromIndex + PS_pageSize;
			if (n > localList.length) {
				n = localList.length;
			}
			for (var i = PS_fromIndex; i < n; ++i) {
				ptIds.push(localList[i]);
			}
			var commaSeparated = ptIds.join();
			DWRPatientSetService.getPatients(commaSeparated, handleRefreshList);
		} else {
			DWRPatientSetService.getFromMyPatientSet(PS_fromIndex, PS_pageSize, handleRefreshList);
		}
	}
	
	function handleRefreshList(patients) {
		<c:if test="${model.id != null}">
			if (patients.length == 0) {
				<c:if test="${model.droppable}">
					hideLayer('${model.id}');
				</c:if>
				return;
			} else {
				showLayer('${model.id}');
			}
		</c:if>
		DWRUtil.removeAllRows("${tableBodyId}");
		if (PS_fromIndex > 0) {
			var row = document.getElementById("${tableBodyId}").insertRow(0);
			var cell = row.insertCell(0);
			cell.colSpan = 5;
			cell.innerHTML = "...";
		}
		var count = PS_fromIndex + 1;
		// TODO: highlight the currently-selected patient
		var cellFuncs = [
			function(patient) { return count++ + "."; },
			function(patient) {
			<c:choose>
				<c:when test="${model.linkUrl == null}">
					return patient.givenName + " " + patient.middleName + " " + patient.familyName;
				</c:when>
				<c:otherwise>
					return '<a href="${model.linkUrl}?patientId=' + patient.patientId + '">' + patient.givenName + ' ' + patient.middleName + ' ' + patient.familyName + '</a>';
				</c:otherwise>
			</c:choose>
			},
			function(patient) { return patient.gender; },
			function(patient) { return patient.age; },
			function(patient) { return '<c:if test="${model.mutable}"><a href="javascript:remove(' + patient.patientId + ')" style="color: red">[x]<a></c:if>'; }
		];
		DWRUtil.addRows("${tableBodyId}", patients, cellFuncs);
		if (PS_fromIndex + PS_pageSize < PS_totalPatients) {
			var tbl = document.getElementById("${tableBodyId}");
			var row = tbl.insertRow(tbl.rows.length);
			var cell = row.insertCell(0);
			cell.colSpan = 5;
			cell.innerHTML = "...";
		}
	}
</script>

<c:if test="${model.size != 'full'}">
<span onMouseOver="javascript:showLayer('_patientSetBox')">
	<span style="border: 1px black dashed; background-color: yellow; display: block">
		<a href="javascript:toggleLayer('_patientSetBox')"><spring:message code="PatientSet.yours"/></a>
		<c:if test="${model.droppable}">
			<a href="javascript:clearThis()" style="color: red">
				[<spring:message code="general.drop"/>]
			</a>
		</c:if>
	</span>
	<div id="_patientSetBox"
		 style="border: 1px solid black;
		 		background-color: #ffffaa;
		 		position: absolute;
		 		z-index: 1;
		 		<c:if test="${model.size == 'compact'}">display: none;</c:if>
		 		right: 0px;
		 		width: 400px;">
</c:if>
		
		<table id="${model.tableId}">
			<thead id="${model.headId}">
				<tr>
					<th colspan="5">
						<a href="javascript:goToStart()">|&lt;</a>
							&nbsp;&nbsp;&nbsp;
						<a href="javascript:pageBackwards()">&lt;</a>
							&nbsp;&nbsp;&nbsp;
						<spring:message code="PatientSet.setOfN" arguments='<span id="PS_totalNumber">?</span>'/>
							&nbsp;&nbsp;&nbsp;
						<a href="javascript:pageForwards()">&gt;</a>
							&nbsp;&nbsp;&nbsp;
						<a href="javascript:goToEnd()">&gt;|</a>
					</th>
				</tr>
				<tr>
					<th>#</th>
					<th><spring:message code="Patient.name"/></th>
					<th><spring:message code="Patient.gender"/></th>
					<th><spring:message code="Patient.age"/></th>
					<th></th>
				</tr>
			</thead>
			<tbody id="${tableBodyId}">
			</tbody>
		</table>

		<script language="JavaScript">
			refreshList();
		</script>

<c:if test="${model.size != 'full'}">
	</div>
</span>
</c:if>