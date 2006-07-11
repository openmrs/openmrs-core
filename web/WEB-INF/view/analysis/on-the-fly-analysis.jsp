<%@ include file="/WEB-INF/template/include.jsp" %>

<style>
<%--
	* {
		padding: 0;
		margin: 0;
	}

	ul {
		list-style: none;
		width: 125px;
	}

	ul a {
		color: #FFF;
		text-decoration: none;
		display: block;
		padding: 5px 5px 5px 10px;
		width: 125px;
		background: #CCCCCC;
	}
	
	ul a:hover {
		color: #4BD8FF;
		text-decoration: none;
		background: #C3C3C3;
	}
	
	ul li {
		border-bottom: 1px solid #FFF;
		float: left;
		position: relative;
		background: #CCCCCC;
	}
	
	ul li ul {
		list-style: none;
		position: absolute;
		left: 140px;
		top: 0;
		display: none;
		width: 125px;
		border-left: 1px solid #FFF;
	}
	
	ul li:hover ul { display: block; }
	
	ul li:hover ul li ul { display: none; }
	
	ul li ul li:hover ul { display: block; }
--%>
	#actionBox {
		background-color: #e0e0e0;
		padding: 4px;
	}
	#filterBox {
		width: 33%;
		float: right;
		border: 2px black solid;
		padding: 4px;
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
		padding: 4px 2px;
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
	#patientSetBox {
		padding: 4px;
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
	

<div id="filterBox">
	<div id="activeFilterBox">
		<center><b><u><spring:message code="Analysis.activeFilters"/></u></b></center>
		<p>
		<c:if test="${model.no_filters}">
			<spring:message code="Analysis.noFiltersSelected"/>
		</c:if>
		<table>
		<c:forEach var="item" varStatus="stat" items="${model.active_filters}">
			<tr><td>
				<div class="activeFilter">
					${item.value.description}
					<a href="analysis.form?method=removeFilter&patient_filter_key=${item.key}">[X]</a>
				</div>
			</td></tr>
		</c:forEach>
		</table>
	</div>

	<p>
	<a href="javascript:toggleLayer('suggestedFilterBox')"><spring:message code="Analysis.addFilter"/></a>
	<div id="suggestedFilterBox">
		<div style="float:right"><a href="javascript:toggleLayer('suggestedFilterBox')">[X]</a></div>
		<c:forEach var="item" items="${model.suggested_filters}">
			<div class="inactiveFilter">
				<a href="analysis.form?method=addFilter&patient_filter_id=<c:out value="${item.reportObjectId}"/>">${item.description}</a>
			</div>
		</c:forEach>
	</div>
	<script language="JavaScript">
	<!--
		document.getElementById("suggestedFilterBox").style.display = "none";
	-->
	</script>
</div>

<div id="patientSetBox">
	<center><b><u><spring:message code="Analysis.currentPatientSet"/></u></b></center>
	<p>
	<i><spring:message code="Analysis.numPatients" arguments="${model.number_of_results}"/></i>
	<p>
	<c:if test="${!empty model.xml_debug}">
		<pre><c:out value="${model.xml_debug}" escapeXml="true"/></pre>
	</c:if>
	
	<pre>${model.analysis_results}</pre>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %> 