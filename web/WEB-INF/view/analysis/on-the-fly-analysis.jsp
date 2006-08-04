<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View Patient Sets" otherwise="/login.htm" redirect="analysis.list" />

<style>
	#actionBox {
		background-color: #e0e0e0;
		padding: 4px;
	}
	#patientSetBox {
		padding: 4px;
		margin: 4px 20px 4px 0px;
		border: 1px black dashed;
		background-color: #f4f4f4;
	}
	#filterBox {
		width: 33%;
		float: right;
		border: 2px black solid;
		padding: 4px;
		background-color: #e0e0ff;
	}
	#activeFilterBox {
		width: 95%;
		border: 1px black solid;
		padding: 3px;
		spacing: 3px;
		background-color: #f0f0f0;
	}
	#suggestedFilterBox {
		border: 1px solid black;
		margin-bottom: 15px;
		background-color: #ffe0e0;
		position: absolute;
		z-index: 1;
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
	.shortcutBarButton {
		border: 1px black solid;
		background-color: #e0e0e0;
		padding: 2px;
	}
	#shortcutBox {
		border: 1px solid black;
		margin-bottom: 15px;
		background-color: #ffe0e0;
		position: absolute;
		z-index: 1;
	}
	.oneShortcutBox {
		border: 1px black solid;
		background-color: #e0e0ff;
		padding: 4px 2px;
		position: absolute;
		left: 20;
		top: -10;
		z-index: 2;
	}
</style>

<script language="JavaScript">
<!--
	function toggleLayer(whichLayer) {
		if (document.getElementById) {
            var style2 = document.getElementById(whichLayer).style;
         	if (style2.display == "none") {
                style2.display = "";
            } else {
                style2.display = "none";
            }
        } else {
            window.alert("Your browser doesn't support document.getElementById");
        }
	}
	
	function showLayer(whichLayer) {
        var style2 = document.getElementById(whichLayer).style;
        style2.display = "";
	}
	
	function hideLayer(whichLayer) {
        var style2 = document.getElementById(whichLayer).style;
        style2.display = "none";
	}

	var menuNames = [ "shortcutMenu", "linkMenu" ];
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
-->
</script>

<%@ include file="/WEB-INF/template/header.jsp" %>

<h3 align="center"><spring:message code="Analysis.title"/></h3>

<openmrs:portlet url="activeFilters" id="filterBox" parameterMap="${model.filterPortletParams}" />

<span style="position: relative">
	<a class="shortcutBarButton" href="javascript:menuHelper('shortcutMenu')"><spring:message code="Analysis.shortcutButton"/></a>
	<div id="shortcutMenu" style="border: 1px solid black; background-color: #ffe0e0; position: absolute; left: 0px; z-index: 1; display: none">
		<div align=right><a href="javascript:menuHelper()">[X]</a></div>
		<ul>
		<c:forEach var="item" items="${model.shortcuts}">
			<li>
			<c:if test="${!empty item.currentFilter}">
				<b>
			</c:if>
			<spring:message code="Analysis.shortcut.${item.label}"/>
			<c:if test="${!empty item.currentFilter}">
				</b>
			</c:if>
			<ul>
			<c:forEach var="shortcutOption" items="${item.list}"> <%-- The items are Map.Entry<String, ShortcutOptionSpec> --%>
				<li>
					<c:set var="method" value="addFilter"/>
					<c:if test="${shortcutOption.value.remove}">
						<c:set var="method" value="removeFilter"/>
					</c:if>
					<c:set var="isSelected" value="false"/>
					<c:if test="${item.currentFilter == shortcutOption.value}">
						<c:set var="isSelected" value="true"/>
					</c:if>
					
					<c:if test="${isSelected == true}">
						<b>
					</c:if>
					<c:choose>
						<c:when test="${shortcutOption.value.concrete}">
							<a href="analysis.form?method=${method}&patient_filter_name=<c:out value="${shortcutOption.value.value}"/>&patient_filter_key=${item.label}">
								<spring:message code="Analysis.shortcut.${shortcutOption.key}"/>
							</a>		
						</c:when>
						<c:otherwise>
							<form method="post" action="analysis.form" id="form_${item.label}_${shortcutOption.key}">
								<input type="hidden" name="method" value="${method}"/>
								<input type="hidden" name="patient_filter_key" value="${item.label}"/>
								<input type="hidden" name="patient_filter_name" value="${shortcutOption.value.value}"/>
								<c:forEach var="arg" items="${shortcutOption.value.hiddenArgs}">
									${arg}
								</c:forEach>
								<c:choose>
									<c:when test="${shortcutOption.value.promptArgs}">
										<c:forEach var="arg" items="${shortcutOption.value.args}">
											<spring:message code="Analysis.shortcut.${item.label}.${arg}"/>
											<input type="text" name="${arg}"/>
										</c:forEach>
										<input type="submit" value="<spring:message code="Analysis.shortcut.go"/>"/>
									</c:when>
									<c:otherwise>
										<a href="javascript:document.getElementById('form_${item.label}_${shortcutOption.key}').submit()">
											<spring:message code="Analysis.shortcut.${shortcutOption.key}"/>
										</a>
									</c:otherwise>
								</c:choose>
							</form>
						</c:otherwise>
					</c:choose>
					<c:if test="${isSelected == true}">
						</b>
					</c:if>
				</li>
			</c:forEach>
			</ul>
			</li>
		</c:forEach>
		</ul>
	</div>
</span>

<span style="position: relative">
	<a class="shortcutBarButton" href="javascript:menuHelper('linkMenu')"><spring:message code="Analysis.linkButton"/></a>
	<div id="linkMenu" style="border: 1px solid black; background-color: #ffe0e0; position: absolute; left: 0px; width: 250px; z-index: 1; display: none">
		<div align=right><a href="javascript:menuHelper()">[X]</a></div>
		<ul>
			<c:forEach var="item" items="${model.links}" varStatus="loopStatus">
				<li>
					<form method="post" action="${item.url}" id="link_${loopStatus.index}_form">
						<input type="hidden" name="patientSet" value="${model.patient_set_for_links}"/>
						<c:forEach var="arg" items="${item.arguments}">
							<input type="hidden" name="${arg.name}" value="${arg.value}"/>
						</c:forEach>
						<a href="javascript:void(0)" onClick="menuHelper(); document.getElementById('link_${loopStatus.index}_form').submit()">
							<spring:message code="${item.label}"/>
						</a>
					</form>
				</li>
			</c:forEach>
		</ul>		
	</div>
</span>

<span style="position: relative">
	<form method="post" action="patientSet.form" id="goToFormEntry" style="display: inline">
		<input type="hidden" name="method" value="setPatientSet"/>
		<input type="hidden" name="patientIds" value="${model.patient_set_for_links}"/>
		<input type="hidden" name="url" value="formentry"/>
		<a class="shortcutBarButton" href="javascript:document.getElementById('goToFormEntry').submit()">FormEntry for this group</a>
	</form>
</span>

<br/>

<openmrs:portlet url="patientSetList" id="patientSetBox" patientIds="${model.result.commaSeparatedPatientIds}" parameters="view=${model.viewMethod}|limit=25" size="full"/>

<%@ include file="/WEB-INF/template/footer.jsp" %> 