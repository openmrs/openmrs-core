/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.EncounterSearch");
dojo.require("dojo.widget.openmrs.OpenmrsSearch");

document.write("<script type='text/javascript' src='" + openmrsSearchBase + "/dwr/interface/DWREncounterService.js'></script>");

dojo.debug("before parse");
dojo.widget.tags.addParseTreeHandler("dojo:EncounterSearch");
dojo.debug("after parse");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.EncounterSearch",
	dojo.widget.openmrs.OpenmrsSearch,
	{
		initializer: function(){
			dojo.debug("initializing encountersearch");
		},
		
		doFindObjects: function(text) {

			// a javascript closure
			var callback = function(ts) { return function(obj) {ts.fillTable(obj)}};
			
			var tmpIncludedVoided = (this.showIncludeVoided && this.includeVoided.checked);
			DWREncounterService.findEncounters(callback(this), text, tmpIncludedVoided);
			
			return false;
		},
		
		
		getPatient: function(enc) {
			if (typeof enc == 'string') {
				var td = document.createElement("td");
				td.colSpan = 6;
				td.innerHTML = enc;
				return td;
			}
			return enc.patientName;
		},
		
		
		getType: function(enc) {
			if (typeof enc == 'string') return this.noCell();
			return enc.encounterType;
		},
		
		
		getForm: function(enc) {
			if (typeof enc == 'string') return this.noCell();
			return enc.formName;
		},
		
		
		getProvider: function(enc) {
			if (typeof enc == 'string') return this.noCell();
			return enc.providerName;
		},
		
		
		getLocation: function(enc) {
			if (typeof enc == 'string') return this.noCell();
			return enc.location;
		},
		
		
		getDateTime: function(enc) {
			if (typeof p == 'string') return this.noCell();
			return this.getDateString(enc.encounterDateTime);
		},
		
		
		getCellFunctions: function() {
			var tmp = function(ths, method) { return function(obj) { return ths[method](obj); }; };
			return [tmp(this, "getNumber"), 
					tmp(this, "getPatient"), 
					tmp(this, "getType"), 
					tmp(this, "getForm"), 
					tmp(this, "getProvider"),
					tmp(this, "getLocation"), 
					tmp(this, "getDateTime")
					];
			
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
