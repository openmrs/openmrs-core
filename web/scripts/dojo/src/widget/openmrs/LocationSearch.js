/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.LocationSearch");
dojo.require("dojo.widget.openmrs.OpenmrsSearch");

var openmrsSearchBase = djConfig["baseScriptUri"].substring(0, djConfig["baseScriptUri"].indexOf("/", 1));
importJavascriptFile(openmrsSearchBase + "/dwr/interface/DWREncounterService.js");

dojo.widget.tags.addParseTreeHandler("dojo:LocationSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.LocationSearch",
	dojo.widget.openmrs.OpenmrsSearch,
	{
		locationId: "",
		
		postCreate: function(){
			dojo.debug("postCreate in LocationSearch");
			if (this.locationId)
				DWREncounterService.getLocation(this.locationId, this.simpleClosure(this, "select"));
		},
		
		showAll: function() {
			DWREncounterService.getAllLocations(this.simpleClosure(this, "doObjectsFound"));
		},
		
		doFindObjects: function(text) {
			DWREncounterService.findLocations(text, this.simpleClosure(this, "doObjectsFound"));
			return false;
		},
		
		getName: function(loc) {
			if (typeof loc == 'string') return this.noCell();
			return loc.name;
		},
		
		getCellFunctions: function() {
			return [this.simpleClosure(this, "getNumber"), 
					this.simpleClosure(this, "getName")
					];
			
		},
			
		// TODO: internationalize
		showHeaderRow: true,
		getHeaderCellContent: function() {
			return ['', 'Location Name'];
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