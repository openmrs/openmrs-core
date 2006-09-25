<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patient Sets" otherwise="/login.htm" redirect="/analysis.list" />

<c:set var="OPENMRS_DO_NOT_SHOW_PATIENT_SET" scope="request" value="true"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />

<style>
	#actionBox {
		background-color: #e0e0e0;
		padding: 4px;
	}
	#titleBox {
		text-align: center;
		height: 30px;
		margin-bottom: 10px;
	}
	#filterBox {
		float: left;
		width: 290px;
		border: 2px black solid;
		padding: 4px;
		background-color: #e0e0ff;
	}
	#contentBox {
		float: left;
		margin-left: 10px;
	}
	.activeFilter {
		border: 1px black solid;
		background-color: #e0ffe0;
		padding: 3px 2px;
		margin: 0px 4px;
	}
	.inactiveFilter {
		border: 1px black solid;
		background-color: #e0e0ff;
		padding: 4px 2px;		
	}
	#analysisSetHeader {
		background-color: #e0e0e0;
	}
	#analysisSetTable {
		border-spacing: 0px;
	}
</style>

<script language="JavaScript">
	var menuNames = [ "_linkMenu", "_viewMenu" ];
	function menuHelper(idClicked) {
		for (var i = 0; i < menuNames.length; ++i) {
	        if (menuNames[i] == idClicked) {
			    toggleLayer(idClicked);
			} else {
			    var style = document.getElementById(menuNames[i]).style;
			    style.display = "none";
			}
		}
	}
</script>

<openmrs:portlet url="activeFilters" id="filterBox" parameterMap="${model.filterPortletParams}" parameters="titleCode=Analysis.pickSet|viewMethod=${model.viewMethod}" />

<script language="JavaScript">
	var patientIds = null; // a comma-separated list of patientIds, set from the included portlet once it's loaded
	function submitHelper(idPrefix) {
		if (patientIds != null) {
			document.getElementById(idPrefix + "_ptIds").value = patientIds;
			document.getElementById(idPrefix + "_form").submit();
		} else if (document.getElementById("hiddenPatientIds")) {
			document.getElementById(idPrefix + "_ptIds").value = document.getElementById("hiddenPatientIds").value;
			document.getElementById(idPrefix + "_form").submit();
		} else {
			window.alert("<spring:message code="PatientSet.stillLoading"/>");
		}
	}
</script>

<div id="contentBox">

<c:if test="${fn:length(model.links) > 0}">
	<span style="position: relative" onMouseOver="javascript:showLayer('_linkMenu')" onMouseOut="javascript:hideLayer('_linkMenu')">
		<a class="analysisShortcutBarButton"><spring:message code="Analysis.linkButton"/></a>
		<div id="_linkMenu" class="analysisShortcutMenu" style="display: none">
			<ul>
				<c:forEach var="item" items="${model.links}" varStatus="loopStatus">
					<li>
						<form method="post" action="${item.url}" id="link_${loopStatus.index}_form" style="display: inline" <c:if test="${model.linkTarget != null}">target="${model.linkTarget}"</c:if>>
							<input type="hidden" name="patientIds" id="link_${loopStatus.index}_ptIds" value=""/>
							<c:forEach var="arg" items="${item.arguments}">
								<input type="hidden" name="${arg.name}" value="${arg.value}"/>
							</c:forEach>
							<a href="javascript:submitHelper('link_${loopStatus.index}')">
								<spring:message code="${item.label}"/>
							</a>
						</form>
					</li>
				</c:forEach>
			</ul>
		</div>
	</span>
</c:if>

<span style="position: relative" onMouseOver="javascript:showLayer('_viewMenu')" onMouseOut="javascript:hideLayer('_viewMenu')">
		<a class="analysisShortcutBarButton"><spring:message code="Analysis.viewButton"/></a>
		<div id="_viewMenu" class="analysisShortcutMenu" style="display: none">
			<ul>
				<li>
					<c:if test="${(not empty model.viewMethod) && (model.viewMethod != 'list')}">
						<a href="?viewMethod=list">
					</c:if>
					<spring:message code="Analysis.listView"/>
					<c:if test="${(not empty model.viewMethod) && (model.viewMethod != 'list')}">
						</a>
					</c:if>
				</li>
				<li>
					<c:if test="${model.viewMethod != 'overview'}">
						<a href="?viewMethod=overview">
					</c:if>
					<spring:message code="Analysis.overviewView"/>
					<c:if test="${model.viewMethod != 'overview'}">
						</a>
					</c:if>
				</li>
			</ul>
		</div>
	</span>

<c:if test="${model.firstPatientId != null}">
	<span style="position: relative">
		<form method="post" action="patientSet.form" id="goToFormEntry" style="display: inline">
			<input type="hidden" name="method" value="setPatientSet"/>
			<input type="hidden" name="patientIds" value="${model.patient_set_for_links}"/>
			<input type="hidden" name="url" value="patientDashboard.form?patientId=${model.firstPatientId}"/>
			<a class="shortcutBarButton" href="javascript:document.getElementById('goToFormEntry').submit()"><spring:message code="Analysis.formEntryButton"/></a>
		</form>
	</span>
</c:if>

<br/>

<c:choose>
	<c:when test="${model.viewMethod == 'overview'}">
		<div id="analysisContentPane"></div>
		<script ype="text/javascript">
			loadInto("Loading...", "cohortSummary.list", "analysisContentPane");
		</script>
	</c:when>
	<c:otherwise>
		<openmrs:portlet url="patientSet" id="analysisPatientSetBox" size="full" parameters="myAnalysis=inProgress|headId=analysisSetHeader|tableId=analysisSetTable|pageSize=20|varToSet=patientIds|linkUrl=patientDashboard.form"/>
	</c:otherwise>
</c:choose>

</div>

<%@ include file="/WEB-INF/template/footer.jsp" %> 