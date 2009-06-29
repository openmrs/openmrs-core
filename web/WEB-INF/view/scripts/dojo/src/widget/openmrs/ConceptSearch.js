/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.ConceptSearch");
dojo.require("dojo.widget.openmrs.OpenmrsSearch");

var openmrsSearchBase = djConfig["baseScriptUri"].substring(0, djConfig["baseScriptUri"].indexOf("/", 1));
importJavascriptFile(openmrsSearchBase + "/dwr/interface/DWRConceptService.js");

dojo.widget.tags.addParseTreeHandler("dojo:ConceptSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.ConceptSearch",
	dojo.widget.openmrs.OpenmrsSearch,
	{
		includeClasses: [],
		excludeClasses: [],
		includeDatatypes: [],
		excludeDatatypes: [],
		includeDrugConcepts: false,
		allowConceptEdit: true,
		showConceptIds: false,
		showAnswers: "",
		
		searchPhrase: "",
		conceptId: "",
		drugId: "",
		performInitialSearch: false,
		
		postCreate: function() {
			dojo.debug("postCreate in conceptsearch");
			
			if (this.drugId) {
				DWRConceptService.getDrug(this.drugId, this.simpleClosure(this, "select"));
			}
			else if (this.conceptId) {
				DWRConceptService.getConcept(this.conceptId, this.simpleClosure(this, "select", true));
			}
			
			this.inputNode.value = this.searchPhrase;
			dojo.debug("searchPhrase, before inserting to inputNode is " + this.searchPhrase);
			
			if ( this.showAnswers && (this.searchPhrase || this.performInitialSearch) ) {
				DWRConceptService.findConceptAnswers(this.searchPhrase, this.showAnswers, false, this.includeDrugConcepts, this.simpleClosure(this, "doObjectsFound"));
			}
			else if (this.searchPhrase){
				DWRConceptService.findConcepts(this.searchPhrase, false, this.includeClasses, this.excludeClasses, this.includeDatatypes, this.excludeDatatypes, this.includeDrugConcepts, this.simpleClosure(this, "doObjectsFound"));
			}
		},
		
		doFindObjects: function(text) {
			dojo.debug("starting doFindObjects with text = " + text);
			var tmpIncludedRetired = (this.showIncludeRetired && this.includeRetired.checked);
			
			if ( this.showAnswers != "" ) {
				DWRConceptService.findConceptAnswers(text, this.showAnswers, tmpIncludedRetired, this.includeDrugConcepts, this.simpleClosure(this, "doObjectsFound"));
			} else {
				DWRConceptService.findConcepts(text, tmpIncludedRetired, this.includeClasses, this.excludeClasses, this.includeDatatypes, this.excludeDatatypes, this.includeDrugConcepts, this.simpleClosure(this, "doObjectsFound"));
			}
			
			//DWRConceptService.findConcepts(text, tmpIncludedRetired, this.includeClasses, this.excludeClasses, this.includeDatatypes, this.excludeDatatypes, this.includeDrugConcepts, this.simpleClosure(this, "doObjectsFound"));
			
			return false;
		},
		
		editConcept: function(conceptId) {
			if (this.allowConceptEdit) {
				if (this.event.ctrlKey) {
					var win = window.open();
					win.location.href = "concept.form?conceptId=" + conceptId;
					}
				else {
					location.href = "concept.form?conceptId=" + conceptId;
				}
			}
		},
		
		editDrug: function(conceptId) {
			if (this.allowConceptEdit) {
				// TODO complete this function after completing drug forms
			}
		},
		
		getCellContent: function(conceptHit) {
			if (typeof conceptHit == 'string') {
	    		return conceptHit;
	    	}
	    	
	    	var closure = function(ts, method, conceptId) { return function(obj) {ts[method](conceptId);};}; //a javascript closure
	    	if (conceptHit.drugId != null) {
	    		var a = document.createElement("a");
	    		a.href = "#selectDrug";
	    		a.onclick = function() { return false };
	    		a.ondblclick = closure(this, "editDrug", conceptHit.conceptId);
	    		a.className = "searchHit";
	    		a.innerHTML = conceptHit.fullName;
	    		if (this.showConceptIds)
					a.innerHTML += " (" + conceptHit.conceptId + ")";
				
				var span = document.createElement("span");
				span.innerHTML = " &nbsp; ->";
				span.appendChild(a);
				return span;
	    	}
		    else {
		    	var a = document.createElement("a");
	    		a.href = "#selectConcept (conceptId = " + conceptHit.conceptId + ")";
				a.onclick = function() { return false };
				a.ondblclick = closure(this, "editConcept", conceptHit.conceptId);
	    		a.title = conceptHit.description;
	    		a.className = "searchHit";
	    		if (conceptHit.preferredName != null) {
	    			var span = document.createElement("span");
	    			span.className = "mainHit";
	    			span.innerHTML = conceptHit.name;
	    			
	    			var span2 = document.createElement("span");
	    			span2.className = "additionalHit";
	    			span2.innerHTML = "&rArr; " + conceptHit.preferredName;
	    			if (this.showConceptIds)
						span2.innerHTML += " (" + conceptHit.conceptId + ")";	
					a.appendChild(span);
					a.appendChild(span2);
				}
				else {
					var span = document.createElement("span");
					span.className = "mainHit";
					span.innerHTML = conceptHit.name;
					if (this.showConceptIds)
						span.innerHTML += " (" + conceptHit.conceptId + ")";
					a.appendChild(span);
				}
				
				var obj = a;
				
				if (conceptHit.retired) {
					var div = document.createElement("div");
					div.className = "retired";
					div.appendChild(a);
					obj = div;
				}
				
				if (this.showVerboseListing && this.verboseListing.checked) {
					var verboseDiv = document.createElement("div");
					verboseDiv.className="description";
					verboseDiv.innerHTML = "#" + conceptHit.conceptId + ": " + conceptHit.description;
					var span = document.createElement("span");
					span.appendChild(a);
					span.appendChild(verboseDiv);
					obj = span;
				}
				
				return obj;
			}
		},
		
		getRowHeight: function() {
			var height = dojo.widget.openmrs.ConceptSearch.superclass.getRowHeight.call(this);
			if (this.showVerboseListing && this.verboseListing.checked)
				return parseInt(height * 3);
			else
				return height;
		},
		
		autoJump: true,
		allowAutoJump: function() {
			if (this.autoJump == false) {
				this.autoJump = true;
				return false;
			}
			return true;	
		},
		
		searchCleared: function() {
			if ( this.showAnswers )
				DWRConceptService.findConceptAnswers("", this.showAnswers, false, this.includeDrugConcepts, this.simpleClosure(this, "doObjectsFound"));
		}
		
	},
	"html"
);
