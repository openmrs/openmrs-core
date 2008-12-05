<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Reports" otherwise="/login.htm" redirect="/admin/reports/reportSchemaXml.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<openmrs:htmlInclude file="/dwr/interface/DWRCohortService.js" />
<openmrs:htmlInclude file="/scripts/jquery/jquery-1.2.6.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/jquery.ui-1.5/ui/packed/ui.core.packed.js" />
<openmrs:htmlInclude file="/scripts/jquery/jquery.ui-1.5/ui/packed/ui.tabs.packed.js" />
<openmrs:htmlInclude file="/scripts/jquery/jquery.ui-1.5/ui/packed/ui.draggable.packed.js" />
<openmrs:htmlInclude file="/scripts/jquery/jquery.ui-1.5/themes/flora/flora.tabs.css" />

<script type="text/javascript">

	var $j = jQuery.noConflict();
	
	$j(document).ready(function() {
		$j("#shortcuts").draggable();
		setResizingTextArea(".rowQuery", 1, 60, 5, 60);
		$j("#reportTabs > ul").tabs();
 	});
 	
 	function setResizingTextArea(el, blurRows, blurCols, focusRows, focusCols) {
 		$j(el)
 			.each(function() { $(this).rows = blurRows; $(this).cols = blurCols; })
 			.focus(function() { $(this).rows = focusRows; $(this).cols = focusCols; })
 			.blur(function() { $(this).rows = blurRows; $(this).cols = blurCols; });
 	} 
	
	function deleteTableRow(tableRow) {
		tableRow.parentNode.removeChild(tableRow);
	}
	
	function addAnotherParameter() {
		var row = document.createElement("tr");
		var cell = document.createElement("td");
		var input = document.createElement("input");
		var toFocus = input;
		input.setAttribute("type", "text");
		input.setAttribute("name", "parameterName");
		cell.appendChild(input);
		row.appendChild(cell);
		
		cell = document.createElement("td");
		input = document.createElement("input");
		input.setAttribute("type", "text");
		input.setAttribute("size", "40");
		input.setAttribute("name", "parameterLabel");
		cell.appendChild(input);
		row.appendChild(cell);
		
		cell = document.createElement("td");
		var sel = document.createElement("select");
		sel.setAttribute("name", "parameterClass");
		var opt = document.createElement("option");
		sel.appendChild(opt);
		<c:forEach var="clazz" items="${parameterClasses}">
			opt = document.createElement("option");
			<c:if test="${param.clazz == clazz}">
				opt.setAttribute("selected", "true");
			</c:if>
			opt.setAttribute("value", "${clazz.name}");
			opt.innerHTML = "${clazz.simpleName}";
			sel.appendChild(opt);
		</c:forEach>
		cell.appendChild(sel);
		row.appendChild(cell);
		
		cell = document.createElement("td");
		var link = document.createElement("span");
		link.className = "voidButton";
		link.setAttribute("onClick", "deleteTableRow(this.parentNode.parentNode)");
		link.innerHTML = "X";
		cell.appendChild(link); 
		row.appendChild(cell);
		
		document.getElementById("parametersTable").appendChild(row);
		toFocus.focus();
	}
	
	function addAnotherRow() {
		var row = document.createElement("tr");
		row.setAttribute("valign", "top");
		var cell = document.createElement("td");
		var input = document.createElement("input");
		var toFocus = input;
		input.setAttribute("type", "text");
		input.setAttribute("name", "rowName");
		cell.appendChild(input);
		row.appendChild(cell);
		
		cell = document.createElement("td");
		input = document.createElement("input");
		input.setAttribute("type", "text");
		input.setAttribute("size", "40");
		input.setAttribute("name", "rowDescription");
		cell.appendChild(input);
		row.appendChild(cell);
		
		cell = document.createElement("td");
		input = document.createElement("textarea");
		input.setAttribute("class", "rowQuery");
		input.setAttribute("name", "rowQuery");
		input.setAttribute("onFocus", "showShortcuts(this)");
		setResizingTextArea(input, 1, 60, 5, 60);
		cell.appendChild(input);
		row.appendChild(cell);
		
		cell = document.createElement("td");
		var link = document.createElement("span");
		link.className = "voidButton";
		link.setAttribute("onClick", "deleteTableRow(this.parentNode.parentNode)");
		link.innerHTML = "X";
		cell.appendChild(link); 
		row.appendChild(cell);
		
		document.getElementById("rowsTable").appendChild(row);
		toFocus.focus();
	}
	
	var shortcutTarget = null;
	
	function showShortcuts(target) {
		shortcutTarget = target;
	}
	
	function handleShortcut(text) {
		if (shortcutTarget != null) {
			shortcutTarget.focus();
			if (shortcutTarget.value != '')
				shortcutTarget.value += ' ';
			shortcutTarget.value += text;
		}
	}
	
	function testQuery(button) {
		$j(button.parentNode.parentNode).find("textarea").each(
			function() {
				var el = document.getElementById('cohortResult');
				cohort_setPatientIds(null);
				showDiv('cohortResult');
				DWRCohortService.evaluateCohortDefinition($(this).value, null, showCohortResult);
			}
		);
	}
	
	function showCohortResult(cohort) {
		var el = document.getElementById('cohortResult');
		cohort_setPatientIds(cohort.commaSeparatedPatientIds);
		showDiv('cohortResult');
	}
</script>
<h2>
	<c:if test="${empty command.reportId}"><spring:message code="Report.new" /></c:if>
	<c:if test="${!empty command.reportId}"><spring:message code="Report.edit" />: ${command.name}</c:if>
</h2>

<spring:hasBindErrors name="command">
	<spring:message code="fix.error"/>
</spring:hasBindErrors>
<br/>


<div id="reportTabs">
	<ul>
		<li><a href="#reportTab1"><span style="color:white;"><spring:message code="Report.details" /></span></a></li>
		<c:if test="${!empty command.reportId}">
			<li><a href="#reportTab2"><span style="color:white;"><spring:message code="Report.parameters" /></span></a></li>
			<li><a href="#reportTab3"><span style="color:white;"><spring:message code="Report.indicators" /></span></a></li>
			<openmrs:extensionPoint pointId="org.openmrs.report.cohortReportFormTab" type="html">
				<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
					<li>
						<a href="#reportExtensionTab${extension.tabId}">
							<span style="color:white;"><spring:message code="${extension.tabName}"/></span>
						</a>
					</li>
				</openmrs:hasPrivilege>
			</openmrs:extensionPoint>
		</c:if>
	</ul>
	
	<form method="post">
		
		<div id="reportTab1">
			<span style="color:blue;">
				<c:if test="${empty command.reportId}"><spring:message code="Report.cohortReport.help.newDetails" /></c:if>
				<c:if test="${!empty command.reportId}"><spring:message code="Report.cohortReport.help.existingDetails" /></c:if>
			</span><br/><br/>
			<table>
				<spring:bind path="command.reportId">
					<tr>
						<th><spring:message code="Report.id" /></th>
						<td>
							<c:if test="${empty status.value}">
								(<spring:message code="Report.new" />)
							</c:if>
							${status.value}
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</td>
					</tr>
				</spring:bind>
				<spring:bind path="command.name">
					<tr>
						<th><spring:message code="Report.name" /></th>
						<td>
							<input type="text" size="40" name="${status.expression}" value="${status.value}"/>
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</td>
					</tr>
				</spring:bind>
				<spring:bind path="command.description">
					<tr valign="top">
						<th><spring:message code="Report.description" /></th>
						<td>
							<textarea rows="3" cols="60" name="${status.expression}">${status.value}</textarea>
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</td>
					</tr>
				</spring:bind>
			</table>
			<br/>
			<input type="submit" value="<spring:message code="general.save"/>"/>
		</div>
		
		<div id="reportTab2">
			<spring:bind path="command.parameters">
				<c:if test="${status.errorMessage != ''}"><div class="error">${status.errorMessage}</div></c:if>
			</spring:bind>
			<span style="color:blue;"><spring:message code="Report.cohortReport.help.parameters" /></span><br/><br/>
			<table id="parametersTable">
				<tr>
					<th><spring:message code="Report.parameter.name" /></th>
					<th><spring:message code="Report.parameter.label" /></th>
					<th><spring:message code="Report.parameter.type" /></th>
				</tr>
				<c:forEach var="parameter" items="${command.parameters}">
					<tr>
						<td><input type="text" name="parameterName" value="${parameter.name}"/></td>
						<td><input type="text" size="40" name="parameterLabel" value="${parameter.label}"/></td>
						<td>
							<select name="parameterClass">
								<option value=""></option>
								<c:forEach var="clazz" items="${parameterClasses}">
									<option <c:if test="${parameter.clazz == clazz}">selected="true"</c:if> value="${clazz.name}">
										${clazz.simpleName}
									</option>
								</c:forEach>
							</select>
						</td>
						<td>
							<span class="voidButton" onClick="deleteTableRow(this.parentNode.parentNode)">X</span>
						</td>
					</tr>
				</c:forEach>
			</table>
			<a onClick="addAnotherParameter()"><spring:message code="Report.parameter.add" /></a>
			<br/><br/>
			<input type="submit" value="<spring:message code="general.save"/>"/>
		</div>
	
		<div id="reportTab3">
			<div id="cohortResult" style="display: none; position: absolute; z-index: 5; border: 2px black solid; background-color: #e0f0d0">
				<div style="float: right">
					<a onClick="hideDiv('cohortResult')">[X]</a>
				</div>
				<b><u><spring:message code="Report.cohortReport.preview" /></u></b>
				<br/>
				<openmrs:portlet url="cohort" parameters="linkUrl="/>
			</div>
			<span style="color:blue;"><spring:message code="Report.cohortReport.help.indicators" /></span><br/><br/>
			<table><tr valign="top">
				<td>
					<spring:bind path="command.rows">
						<c:if test="${status.errorMessage != ''}"><div class="error">${status.errorMessage}</div></c:if>
					</spring:bind>
					
					<table id="rowsTable">
						<tr>
							<th nowrap>
								<spring:message code="Report.cohortReport.indicatorName" />
								(<spring:message code="Report.cohortReport.indicatorName.example" />)
							</th>
							<th nowrap>
								<spring:message code="Report.cohortReport.indicatorDescription" />
								(<spring:message code="Report.cohortReport.indicatorDescription.example" />)
							</th>
							<th nowrap>
								<spring:message code="Report.cohortReport.indicatorSpecification" />
								(<spring:message code="Report.cohortReport.indicatorSpecification.example" />)
							</th>
							<th colspan="2">&nbsp;</th>
						</tr>
						<c:forEach var="row" items="${command.rows}">
							<tr valign="top">
								<td><input type="text" name="rowName" value="${row.name}"/></td>
								<td><input type="text" size="40" name="rowDescription" value="${row.description}"/></td>
								<td><textarea name="rowQuery" class="rowQuery" onFocus="showShortcuts(this)">${row.query}</textarea></td>
								<td><input type="button" value="Test" onClick="testQuery(this)"/></td>
								<td><span class="voidButton" onClick="deleteTableRow(this.parentNode.parentNode)">X</span></td>
							</tr>
						</c:forEach>
					</table>
					<a onClick="addAnotherRow()"><spring:message code="Report.cohortReport.indicator.add" /></a>
				</td>
				<td width="*">
		
					<div id="shortcuts" style="border: 1px black solid; padding: 5px; background-color: #f0f0f0">
						<span style="font-weight:bold;"><spring:message code="Report.cohortReport.operators" />:<br/></span><br/>
						<span class="button" onClick="handleShortcut('(')">&nbsp;(&nbsp;</span>
						<span class="button" onClick="handleShortcut(')')">&nbsp;)&nbsp;</span>
						<span class="button" onClick="handleShortcut('AND')"><spring:message code="Report.cohortReport.operator.and" /></span>
						<span class="button" onClick="handleShortcut('OR')"><spring:message code="Report.cohortReport.operator.or" /></span>
						<span class="button" onClick="handleShortcut('NOT')"><spring:message code="Report.cohortReport.operator.not" /></span>
						<br/>
							
						<c:if test="${fn:length(macros) > 0}">
							<br/>
							<b><spring:message code="Report.macros" />:</b>
							<c:forEach var="macro" items="${macros}">
								<br/><a class="shortcut" onClick="handleShortcut('${macroPrefix}${macro.key}${macroSuffix}')" title="${macro.value}">${macro.key}</a>
							</c:forEach>
							<br/>
						</c:if>
				
						<c:if test="${fn:length(patientSearches) > 0}">
							<br/>
							<b><spring:message code="Report.cohortReport.savedPatientSearches" />:</b>
							<c:forEach var="search" items="${patientSearches}">
								<br/><a class="shortcut" onClick="handleShortcut('[${search.key}]')" title="${search.value}">${search.key}</a>
							</c:forEach>
							<br/>
						</c:if>
					</div>
				</td>
			</tr></table>
			<input type="submit" value="<spring:message code="general.save"/>"/>
		</div>
	</form>

	<openmrs:extensionPoint pointId="org.openmrs.report.cohortReportFormTab" type="html">
		<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
			<div id="reportExtensionTab${extension.tabId}">
				<c:choose>
					<c:when test="${extension.portletUrl == '' || extension.portletUrl == null}">
						portletId is null: '${extension.extensionId}'
					</c:when>
					<c:otherwise>
						<openmrs:portlet url="${extension.portletUrl}" id="${extension.tabId}" moduleId="${extension.moduleId}" parameters="reportId=${command.reportId}" />
					</c:otherwise>
				</c:choose>
			</div>
		</openmrs:hasPrivilege>
	</openmrs:extensionPoint>

</div>
<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>