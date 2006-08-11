/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.ConceptSearch");
dojo.require("dojo.widget.openmrs.OpenmrsSearch");

var openmrsSearchBase = djConfig["baseScriptUri"].substring(0, djConfig["baseScriptUri"].indexOf("/", 1));
document.write("<script type='text/javascript' src='" + openmrsSearchBase + "/dwr/interface/DWRConceptService.js'></script>");

dojo.widget.tags.addParseTreeHandler("dojo:ConceptSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.ConceptSearch",
	dojo.widget.openmrs.OpenmrsSearch,
	{
		conceptId: "",
		
		postCreate: function() {
			dojo.debug("postCreate in conceptsearch");
			
			if (this.conceptId)
				DWRConceptService.getConcept(this.simpleClosure(this, "select"), this.conceptId);
			
			this.inputNode.value = this.searchPhrase
			if (this.searchPhrase)
				DWRConceptService.findConcepts(this.simpleClosure(this, "doObjectsFound"), this.searchPhrase, this.conceptClasses, false);
		},
		
		conceptClasses: [],
		searchPhrase: "",
		
		doFindObjects: function(text) {
			// a javascript closure
			var callback = function(ts) { return function(obj) {ts.doObjectsFound(obj)}};

			var tmpIncludedRetired = (this.showIncludeRetired && this.includeRetired.checked);
			DWRConceptService.findConcepts(callback(this), text, this.conceptClasses, tmpIncludedRetired);
			
			return false;
		},
		
		allowConceptEdit: true,
		
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
		
		showConceptIds: false,
		
		getCellContent: function(conceptHit) {
			if (typeof conceptHit == 'string') {
	    		return conceptHit;
	    	}
	    	
	    	var closure = function(ts, method, conceptId) { return function(obj) {ts[method](conceptId);}}; //a javascript closure
	    	if (conceptHit.drugId != null) {
	    		var a = document.createElement("a");
	    		a.href = "#selectDrug";
	    		a.onclick = function() { return false };
	    		a.ondblclick = closure(this, "editDrug", conceptHit.conceptId);
	    		a.className = "searchHit";
	    		a.innerHTML = conceptHit.fullName;
	    		if (this.showConceptIds)
					a.innerHTML += " (" + conceptHit.conceptId + ")";
				
				return a;
	    	}
		    else {
		    	var a = document.createElement("a");
	    		a.href = "#selectConcept";
				a.onclick = function() { return false };
				a.ondblclick = closure(this, "editConcept", conceptHit.conceptId);
	    		a.title = conceptHit.description;
	    		a.className = "searchHit";
	    		if (conceptHit.synonym != "" && conceptHit.synonym != null) {
	    			var span = document.createElement("span");
	    			span.className = "mainHit";
	    			span.innerHTML = conceptHit.synonym;
	    			
	    			var span2 = document.createElement("span");
	    			span2.className = "additionalHit";
	    			span2.innerHTML = "&rArr; " + conceptHit.name;
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
		}
		
	},
	"html"
);
