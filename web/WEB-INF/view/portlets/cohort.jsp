<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientSetService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRCohortBuilderService.js" />
<openmrs:htmlInclude file="/dwr/engine.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">
	var cohort_method = '';
	var cohort_index = -1;
	var cohort_patientIds = new Array();
	var cohort_startIndex = 0;
	<c:if test="${empty model.patientsPerPage}">
		var cohort_pageSize = 15;
	</c:if>
	<c:if test="${not empty model.patientsPerPage}">
		var cohort_pageSize = ${model.patientsPerPage};
	</c:if>
	var cohort_endIndex = cohort_pageSize;
	
	function cohort_setIdsHelper(commaSeparatedIds) {
		if (commaSeparatedIds != null)
			cohort_patientIds = commaSeparatedIds.split(',');
		else
			cohort_patientIds = new Array();
		cohort_setStartIndex(0);
	}
	
	function cohort_setStartIndex(i) {
		if (i > cohort_patientIds.length)
			i = cohort_patientIds.length - 1;
		if (i < 0)
			i = 0;
		cohort_startIndex = i;
		cohort_endIndex = cohort_startIndex + cohort_pageSize;
		if (cohort_endIndex > cohort_patientIds.length)
			cohort_endIndex = cohort_patientIds.length;
		if (cohort_endIndex < 0)
			cohort_endIndex = 0;
		cohort_refreshDisplay();
	}
		
	function cohort_pageForwards() {
		cohort_setStartIndex(cohort_startIndex + cohort_pageSize);
	}
	
	function cohort_pageBack() {
		cohort_setStartIndex(cohort_startIndex - cohort_pageSize);
	}
	
	function cohort_goToStart() {
		cohort_setStartIndex(0);
	}

	function cohort_goToEnd() {
		cohort_setStartIndex(cohort_patientIds.length - cohort_pageSize);
	}

	function cohort_setPatientIds(commaSeparatedIds) {
		cohort_method = 'ids';
		cohort_index = -1;
		cohort_setIdsHelper(commaSeparatedIds);
	}
	
	function cohort_setCohortId(cohortId) {
		cohort_method = 'cohort';
		cohort_index = cohortId;
		DWRCohortBuilderService.getCohortAsCommaSeparatedIds(cohortId, cohort_setIdsHelper);
	}
	
	function cohort_setFilterId(filterId) {
		cohort_method = 'filter';
		cohort_index = filterId;
		DWRCohortBuilderService.getFilterResultAsCommaSeparatedIds(filterId, cohort_setIdsHelper);
	}
	
	function cohort_refreshDisplay() {
		$('cohort_fromNumber').innerHTML = cohort_startIndex + 1;
		$('cohort_toNumber').innerHTML = cohort_endIndex;
		$('cohort_ofNumber').innerHTML = cohort_patientIds.length;
		$('cohort_contents').innerHTML = '<spring:message code="general.loading"/>';
		var str = '';
		for (var i = cohort_startIndex; i < cohort_endIndex; ++i)
			str += cohort_patientIds[i] + ',';
		DWRPatientSetService.getPatients(str, cohort_showContentHelper);
	}
	
	function cohort_showContentHelper(patientList) {
		var str = '';
		if (cohort_startIndex > 0)
			str += '...<br/>';
		for (var i = 0; i < patientList.length; ++i) {
			var pli = patientList[i];
			str += (cohort_startIndex + i + 1) + '. ';
			<c:choose>
			<c:when test="${not empty model.linkUrl && model.target != '_blank'}">
				str += '<a href="${model.linkUrl}?patientId=' + pli.patientId + '">';
                str += pli.givenName + ' ' + pli.familyName + ' (' + pli.age + ' year old ' + (pli.gender == 'M' ? 'Male' : 'Female') + ')';
                str += '</a>';
			</c:when>
			<c:when test="${not empty model.linkUrl && model.target == '_blank'}">
                str += '<a href="${model.linkUrl}?patientId=' + pli.patientId + '" ' + 'target="_blank"' + '>';
                str += pli.givenName + ' ' + pli.familyName + ' (' + pli.age + ' year old ' + (pli.gender == 'M' ? 'Male' : 'Female') + ')';
                str += '</a>';
			</c:when>
            <c:otherwise>
                str += pli.givenName + ' ' + pli.familyName + ' (' + pli.age + ' year old ' + (pli.gender == 'M' ? 'Male' : 'Female') + ')';			
			</c:otherwise>
			</c:choose>
			str += '<br/>';
		}
		if (cohort_endIndex < cohort_patientIds.length)
			str += '...<br/>';
		$('cohort_contents').innerHTML = str;
	}

	<c:choose>
		<c:when test="${patientIds != null}">
			cohort_setPatientIds('${patientIds}');
		</c:when>
		<c:when test="${model.cohortId != null}">
			cohort_setCohortId(${model.cohortId});
		</c:when>
		<c:when test="${model.filterId != null}">
			cohort_setFilterId(${model.filterId});
		</c:when>
	</c:choose>
</script>

<div>
	<div id="cohort_nameDiv">
		<b><u><span id="cohort_name">${model.cohortTitle}</span></u></b>
	</div>
	<div id="cohort_viewMethodDiv">
	<%--
		View method:
			<b>List</b>
			&nbsp;&nbsp; <small>Other methods are not yet implemented</small>
    --%>
	</div>
	<div id="cohort_navButtons">
		<b><u>
		<a href="javascript:cohort_goToStart()">|&lt;</a>
		&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="javascript:cohort_pageBack()">&lt;</a>
		&nbsp;&nbsp;&nbsp;&nbsp;
		<spring:message code="general.displayingXtoYofZ" arguments='<span id="cohort_fromNumber"></span>,<span id="cohort_toNumber"></span>,<span id="cohort_ofNumber"></span>' />
		&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="javascript:cohort_pageForwards()">&gt;</a>
		&nbsp;&nbsp;&nbsp;&nbsp;
		<a href="javascript:cohort_goToEnd()">&gt;|</a>
		</u></b>
	</div>
	<div id="cohort_contents">
	</div>
</div>