<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Programs" otherwise="/login.htm" redirect="/admin/programs/program.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<style>
.statesToRetire, .statesToDelete {
	border: 1px solid #009d8e;
    padding: 0px 7px;
    margin: 0px 2px;
}
</style>
<script type="text/javascript">
	var displayedStates = new Array();
	var idToNameMap = new Array();
	var retiredStates = new Array();
    var deletedStates = new Array();
	var allStates= new Array();
	var activeStates=new Array();
	var isActiveDisplay=true;
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
		if (displayedStates.length != 0) {
			dwr.util.addRows('stateTable', displayedStates, [
					function (st) { return getIdToNameMap(st[0]); },
					function (st) { return '<input type="checkbox" id="initial_' + st[0] + '" ' + (st[1] ? 'checked' : '') + '/>'; },
					function (st) { return '<input type="checkbox" id="terminal_' + st[0] + '" ' + (st[2] ? 'checked' : '') + '/>'; },
					function (st) { return getButton(st[0]); },
                    function (st) { return getDeleteButton(st[0]); }
				], { escapeHtml:false });
		} else {
			dwr.util.addRows('stateTable', ['<openmrs:message code="general.none"/>'], [
					function(s) { return s;}
				], { escapeHtml:false });
		}
	}

	function retireState(conceptId) {
		for (var i = 0; i < activeStates.length; ++i) {
			if (activeStates[i][0] == conceptId) {
				var x=window.confirm("<openmrs:message code='State.retire.confirmation'/>")
				if (x) {
					retiredStates.push(activeStates[i]);
					activeStates.splice(i,1);
					if(isActiveDisplay){
						displayedStates=activeStates;
					}else{
						displayedStates=allStates;
					}

					refreshStateTable();
				}
			}
		}
	}

	function unretireState(conceptId) {
		for (var i = 0; i < retiredStates.length; ++i) {
			if (retiredStates[i][0] == conceptId) {
				var x=window.confirm("<openmrs:message code='State.unretire.confirmation'/>")
				if (x) {
					activeStates.push(retiredStates[i]);
					retiredStates.splice(i,1);
					refreshStateTable();
				}
			}
		}
	}

    function deleteState(conceptId) {
        for (var i = 0; i < activeStates.length; ++i) {
            if (activeStates[i][0] == conceptId) {
                var x=window.confirm("<openmrs:message code='State.delete.confirmation'/>")
                if (x) {
                    deletedStates.push(activeStates[i]);
                    activeStates.splice(i,1);
                    if(isActiveDisplay){
                        displayedStates=activeStates;
                    }else{
                        displayedStates=allStates;
                    }
                    refreshStateTable();
                }
            }
        }
    }

	function getIdToNameMap(conceptId){

		if(isStateRetired(conceptId)){

			return '<strike>'+idToNameMap[conceptId]+'</strike>';
		 } else {
            return idToNameMap[conceptId];
		 }
    }
function getButton(conceptId){
	    if(isStateRetired(conceptId)){

		return '<input type="button" class="statesToRetire" value="<openmrs:message code="general.unretire"/>" onclick="unretireState('+conceptId+')"/>';
	 }else{

	    return '<input type="button" class="statesToRetire" value="<openmrs:message code="general.retire"/>" onclick="retireState('+conceptId+')"/>';

	 }
}


function getDeleteButton(conceptId){
    return '<input type="button" class="statesToDelete" value="<openmrs:message code="general.void"/>" onclick="deleteState('+conceptId+')"/>';
}

function isStateRetired(conceptId){
	for(var i = 0; i < retiredStates.length;++i){
		if(conceptId==retiredStates[i][0]){
			return true;
		}

	}
}
	function toggleStatesVisibility(){
	if(isActiveDisplay){
	activeStates=displayedStates;
	displayedStates=allStates;
	refreshStateTable();
		isActiveDisplay=false;
	}else{
		displayedStates=activeStates;
		refreshStateTable();
		isActiveDisplay=true;
	}

	}

	function initialiseStateTable(){
		isActiveDisplay=false;
		activeStates=displayedStates;
		toggleStatesVisibility();
	}

	function addState(conceptId, isInitial, isTerminal) {
		for (var i = 0; i < allStates.length; ++i)
			if (allStates[i][0] == conceptId) {
				window.alert("<openmrs:message code='State.error.name.duplicate'/>");
				return;
			}
		if(isActiveDisplay){
			displayedStates.push([ conceptId, isInitial, isTerminal ]);
			allStates.push([ conceptId, isInitial, isTerminal ]);

		}else{
			activeStates.push([ conceptId, isInitial, isTerminal ]);
			displayedStates.push([ conceptId, isInitial, isTerminal ]);
		}
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
		displayedStates=activeStates;
		var tmp = "";
        var toDelete = "";
		for (var i = 0; i < displayedStates.length; ++i) {
			var conceptId = displayedStates[i][0];
			var isInitial = jQuery('#initial_' + conceptId).is(':checked');
			var isTerminal = jQuery('#terminal_' + conceptId).is(':checked');
			tmp += conceptId + ",";
			tmp += isInitial + ",";
			tmp += isTerminal + "|";
		}

        for (var i = 0; iLen = deletedStates.length, i < iLen; ++i) {
            var conceptId = deletedStates[i][0];
            toDelete += conceptId;
            toDelete += "|";
        }

		jQuery('#statesToSubmit').val(tmp);
        jQuery('#statesToDelete').val(toDelete);
		jQuery('#theForm').submit();
	}
</script>

<h3>
	<openmrs_tag:concept conceptId="${workflow.program.concept.conceptId}"/>
	-
	<openmrs_tag:concept conceptId="${workflow.concept.conceptId}"/>
</h3>

<spring:hasBindErrors name="workflow">
	<openmrs:message htmlEscape="false" code="fix.error"/>
	<br />
</spring:hasBindErrors>

<b class="boxHeader"> <a style="display: block; float: right"
	href="#" onClick="toggleStatesVisibility();"> <openmrs:message
			code="general.toggle.retired" /> </a> <openmrs:message
		code="State.list.title" /> </b>

<form method="post" id="theForm">
	<table>
		<thead>
			<tr>
				<th><openmrs:message code="State.state"/></th>
				<th><openmrs:message code="State.initial"/>?</th>
				<th><openmrs:message code="State.terminal"/>?</th>
			</tr>
		</thead>
		<tbody id="stateTable">
			<tr><td colspan="3"><openmrs:message code="general.none" /></td></tr>
		</tbody>
		<tbody>
			<tr>
				<td colspan="3" align="center">
					<div dojoType="ConceptSearch" widgetId="cSearch" conceptId="" showVerboseListing="false" conceptClasses="State"></div>
					<div dojoType="OpenmrsPopup" widgetId="conceptSelection" hiddenInputName="conceptId" searchWidget="cSearch" searchTitle='<openmrs:message code="Concept.find" />' changeButtonValue='<openmrs:message code="general.add"/>'></div>
				</td>
			</tr>
		</tbody>
	</table>
	<input type="hidden" id="statesToSubmit" name="newStates" />
    <input type="hidden" id="statesToDelete" name="deleteStates" />
	<input type="button" onClick="handleSave()" value="<openmrs:message code="general.save" />" />
</form>

<script type="text/javascript">
<c:forEach var="state" items="${workflow.states}">
idToNameMap[${state.concept.conceptId}] = '<openmrs:concept conceptId="${state.concept.conceptId}" nameVar="n" var="v" numericVar="nv">${n.name}</openmrs:concept>';
allStates.push([ ${state.concept.conceptId}, ${state.initial}, ${state.terminal} ]);
	<c:if test="${!state.retired}">
	displayedStates.push([ ${state.concept.conceptId}, ${state.initial}, ${state.terminal} ]);
</c:if>
<c:if test="${state.retired}">
var conceptId=${state.concept.conceptId};
retiredStates.push([ ${state.concept.conceptId}, ${state.initial}, ${state.terminal} ]);
</c:if>
</c:forEach>
initialiseStateTable();
refreshStateTable();

</script>

<%@ include file="/WEB-INF/template/footer.jsp" %>
