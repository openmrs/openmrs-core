/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.FieldSearch");
dojo.require("dojo.widget.openmrs.OpenmrsSearch");

var openmrsSearchBase = djConfig["baseScriptUri"].substring(0, djConfig["baseScriptUri"].indexOf("/", 1));
document.write("<script type='text/javascript' src='" + openmrsSearchBase + "/dwr/interface/DWRFormService.js'></script>");

dojo.widget.tags.addParseTreeHandler("dojo:FieldSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.FieldSearch",
	dojo.widget.openmrs.OpenmrsSearch,
	{
		fieldId: "",
		alsoSearchConcepts: false,
		
		postCreate: function(){
			dojo.debug("postCreate in FieldSearch");
			
			var closure = function(thisObj, method) { return function(obj) { return thisObj[method]({"obj":obj}); }; };
			if (this.fieldId != "")
				DWRFormService.findFields(closure(this, "select"), this.fieldId);
		},
		
		doFindObjects: function(text) {
			if (this.alsoSearchConcepts == true)
				DWRFormService.findFieldsAndConcepts(this.simpleClosure(this, "doObjectsFound"), text);
			else
				DWRFormService.findFields(this.simpleClosure(this, "doObjectsFound"), text);
				
			return false;
		},
		
		getName: function(f) {
			if (typeof f == 'string') return f;
			return f.name;
		},
		
		getType: function(f) {
			if (typeof f == 'string') return this.noCell();
			var span = document.createElement("span");
			span.style.whiteSpace = "nowrap";
			span.innerHTML = f.fieldTypeName;
			return span;
		},
		
		getDesc: function(f) {
			if (typeof f == 'string') return this.noCell();
			return f.description;
		},
		
		getCellFunctions: function() {
			return [this.simpleClosure(this, "getNumber"), 
					this.simpleClosure(this, "getName"), 
					this.simpleClosure(this, "getType"), 
					this.simpleClosure(this, "getDesc")
					];
		},
		
		// TODO: internationalize
		showHeaderRow: true,
		getHeaderCellContent: function() {
			return ['', 'Name', 'Field Type', 'Description'];
		}
		
	},
	"html"
);
