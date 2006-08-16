<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/formentry/taskpane/concept.htm" />

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	
	function miniObject(c) {
		this.key = c.conceptId;
		this.value = c.name;
	}
	
	function miniConcept(n) {
		this.conceptId = "<%= org.openmrs.util.OpenmrsConstants.PROPOSED_CONCEPT_IDENTIFIER %>";
		if (n == null)
			this.name = $('proposedText').innerHTML;
		else
			this.name = n;
	}
	
	function showProposeConceptForm() {
		$('searchForm').style.display = "none";
		$('proposeConceptForm').style.display = "block";
		txt = $('proposedText');
		txt.value = "";
		insertContent(txt, searchWidget.lastPhraseSearched);
		return false;
	}
	
	function proposeConcept() {
		var box = $('proposedText');
		if (box.text == '')  {
			alert("Proposed Concept text must be entered");
			box.focus();
		}
		else {
			$('proposeConceptForm').style.display = "none";
			$('searchForm').style.display = "";
			DWRConceptService.findProposedConcepts(preProposedConcepts, box.value);
		}
	}
	
	function preProposedConcepts(concepts) {
		if (concepts.length == 0) {
			searchWidget.select({"objs":[new miniConcept()]});
		}
		else {
			//display a box telling them to pick a preposed concept:
			$("preProposedAlert").style.display = "block";
			$('searchForm').style.display = "";
			searchWidget.doObjectsFound(concepts);
		}
	}
	
	/**
	* Inserts text into textarea and places cursor at end of string
	* More steps than needed right now
	* Borrowed from http://www.alexking.org/blog/2004/06/03/js-quicktags-under-lgpl/
	*/
	function insertContent(myField, myValue) {
		//IE support
		if (document.selection) {
			myField.focus();
			sel = document.selection.createRange();
			sel.text = myValue;
			myField.focus();
		}
		//MOZILLA/NETSCAPE support
		else if (myField.selectionStart || myField.selectionStart == '0') {
			var startPos = myField.selectionStart;
			var endPos = myField.selectionEnd;
			var scrollTop = myField.scrollTop;
			myField.value = myField.value.substring(0, startPos)
						+ myValue 
						+ myField.value.substring(endPos, myField.value.length);
			myField.focus();
			myField.selectionStart = startPos + myValue.length;
			myField.selectionEnd = startPos + myValue.length;
			myField.scrollTop = scrollTop;
		} else {
			myField.value += myValue;
			myField.focus();
		}
	}
	
	var searchWidget;
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("cSearch");			
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				for (i=0; i<msg.objs.length; i++) {
					pickProblem('<%= request.getParameter("mode") %>', '//problem_list', new miniObject(msg.objs[i]));
				}
			}
		);
		
		dojo.event.topic.subscribe("cSearch/objectsFound", 
			function(msg) {
				if ($("preProposedAlert").style.display != "block")
					msg.objs.push("<a href='#proposeConcept' onclick='javascript:return showProposeConceptForm();'><spring:message code="ConceptProposal.propose.new"/></a>");
			}
		);
		
		searchWidget.inputNode.focus();
		searchWidget.inputNode.select();
				
	});

</script>

<style>
	#proposeConceptForm { display: none; }
	#preProposedAlert { display: none; }
	.alert { color: red; }
</style>

<h3><spring:message code="diagnosis.title" /></h3>

<div id="preProposedAlert" class="alert">
	<br>
	<spring:message code="ConceptProposal.proposeDuplicate" />
	<br>
</div>

<div id="searchForm">
	<input name="mode" type="hidden" value='${request.mode}'>
	<div dojoType="ConceptSearch" widgetId="cSearch" inputWidth="10em" showVerboseListing="true" includeClasses='<request:existsParameter name="className"><request:parameters id="c" name="className"><request:parameterValues id="names"><jsp:getProperty name="names" property="value"/>;</request:parameterValues></request:parameters></request:existsParameter>' useOnKeyDown="true" allowConceptEdit="false"></div>
	<br />
	<small>
		<em>
			<spring:message code="general.search.hint" />
		</em>
	</small>
</div>

<div id="proposeConceptForm">
	<br />
	<spring:message code="ConceptProposal.proposeInfo" />
	<br /><br />
	<b><spring:message code="ConceptProposal.originalText" /></b><br />
	<textarea name="originalText" id="proposedText" rows="4" cols="20" /></textarea><br />
	<input type="button" onclick="proposeConcept()" value="<spring:message code="ConceptProposal.propose" />" /><br />
	
	<br />
	<span class="alert">
		<spring:message code="ConceptProposal.proposeWarning" />
	</span>
</div>

<br />

<%@ include file="/WEB-INF/template/footer.jsp"%>