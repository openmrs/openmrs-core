<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/dwr/util.js" />

<script type="text/javascript">
	var states = new Array();
	var idToNameMap = new Array();
	
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");

	dojo.addOnLoad( function() {
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				idToNameMap[msg.objs[0].conceptId] = msg.objs[0].name;
				addState(msg.objs[0].conceptId, false, false);
			}
		);
	});

	function refreshStateTable() {
		dwr.util.removeAllRows('stateTable');
		if (states.length != 0) {
			dwr.util.addRows('stateTable', states, [
					function (st) { return idToNameMap[st[0]]; },
					function (st) { return '<input type="checkbox" id="initial_' + st[0] + '" ' + (st[1] ? 'checked' : '') + '/>'; },
					function (st) { return '<input type="checkbox" id="terminal_' + st[0] + '" ' + (st[2] ? 'checked' : '') + '/>'; }
				], { escapeHtml:false });
		} else {
			dwr.util.addRows('stateTable', ['<spring:message code="general.none"/>'], [
					function(s) { return s;}
				], { escapeHtml:false });
		}
	}
	
	function addState(conceptId, isInitial, isTerminal) {
		for (var i = 0; i < states.length; ++i)
			if (states[i][0] == conceptId) {
				window.alert('State already exists');
				return;
			}
		states.push([ conceptId, isInitial, isTerminal ]);
		refreshStateTable();	
	}
	
	function handleAddState() {
		var popup = dojo.widget.manager.getWidgetById("conceptSelection")
		var conceptId = popup.hiddenInputNode.value;
		popup.hiddenInputNode.value = '';
		popup.displayNode.innerHTML = '';
		addState(conceptId, false, false);
	}
	
	function handleSave() {
		var tmp = "";
		for (var i = 0; i < states.length; ++i) {
			var conceptId = states[i][0];
			var isInitial = $('initial_' + conceptId).checked;
			var isTerminal = $('terminal_' + conceptId).checked;
			tmp += conceptId + ",";
			tmp += isInitial + ",";
			tmp += isTerminal + "|";
		}
		$('statesToSubmit').value = tmp;
		$('theForm').submit();
	}
</script>

<h3>
	<openmrs_tag:concept conceptId="${workflow.program.concept.conceptId}"/>
	-
	<openmrs_tag:concept conceptId="${workflow.concept.conceptId}"/>
</h3>

<spring:hasBindErrors name="workflow">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>

<form method="post" id="theForm">
	<table>
		<thead>
			<tr>
				<th><spring:message code="State.state"/></th>
				<th><spring:message code="State.initial"/>?</th>
				<th><spring:message code="State.terminal"/>?</th>
			</tr>
		</thead>
		<tbody id="stateTable">
			<tr><td colspan="3"><spring:message code="general.none" /></td></tr>
		</tbody>
		<tbody>
			<tr>
				<td colspan="3" align="center">
					<div dojoType="ConceptSearch" widgetId="cSearch" conceptId="" showVerboseListing="false" conceptClasses="State"></div>
					<div dojoType="OpenmrsPopup" widgetId="conceptSelection" hiddenInputName="conceptId" searchWidget="cSearch" searchTitle='<spring:message code="Concept.find" />' changeButtonValue='<spring:message code="general.add"/>'></div>
				</td>
			</tr>
		</tbody>
	</table>
	<input type="hidden" id="statesToSubmit" name="newStates" />
	<input type="button" onClick="handleSave()" value="<spring:message code="general.save" />" />
</form>

<script type="text/javascript">
	<c:forEach var="state" items="${workflow.states}">
		idToNameMap[${state.concept.conceptId}] = '<openmrs:concept conceptId="${state.concept.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept>';
		<c:if test="${!state.retired}">
			states.push([ ${state.concept.conceptId}, ${state.initial}, ${state.terminal} ]);
		</c:if>
	</c:forEach>
	refreshStateTable();
</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
