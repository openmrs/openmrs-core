/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.UserSearch");
dojo.require("dojo.widget.openmrs.PersonSearch");

importJavascriptFile(openmrsContextPath + "/dwr/interface/DWRUserService.js");

dojo.widget.tags.addParseTreeHandler("dojo:UserSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.UserSearch",
	dojo.widget.openmrs.PersonSearch,
	{
		roles: [],
		
		userId: "",
		
		showRoles: false,
		
		showUsername: true,
		
		includeRetiredLabel: omsgs.includeDisabled,
		
		postCreate: function(){
			dojo.debug("postCreate in UserSearch");
			
			var closure = function(thisObj, method) { return function(obj) { return thisObj[method]({"obj":obj}); }; };
			if (this.userId != "") {
				DWRUserService.getUser(this.userId, closure(this, "select"));
			}
		},
		
		doFindObjects: function(text) {
			var tmpIncludeRetired = (this.showIncludeRetired && this.includeRetired.checked);
			DWRUserService.findUsers(text, this.roles, tmpIncludeRetired, this.simpleClosure(this, "doObjectsFound"));
			
			return false;
		},
		
		getUsername: function(u) {
			if (u == null) return "";
			return u.username;
		},
		
		getSystemId: function(u) {
			if (u == null) return "";
			return u.systemId;
		},
		
		showAll: function() {
			var tmpIncludeRetired = (this.showIncludeRetired && this.includeRetired.checked);
			DWRUserService.getAllUsers(this.roles, tmpIncludeRetired, this.simpleClosure(this, "doObjectsFound"));
		},
		
		getCellFunctions: function() {
			var arr = new Array();
			arr.push(this.simpleClosure(this, "getNumber"));
			arr.push(this.getSystemId);
			if (this.showUsername)
				arr.push(this.getUsername);
			arr.push(this.simpleClosure(this, "getGiven"));
			arr.push(this.simpleClosure(this, "getMiddle"));
			arr.push(this.simpleClosure(this, "getFamily"));
			if (this.showRoles)
				arr.push(getRoles);
			
			/* userListingAttrs var from openmrsmessages.js */
			for (var i = 0; i < omsgs.userListingAttrs.length; i++) {
				arr.push(this.simpleClosure(this, "getAttribute", omsgs.userListingAttrs[i]));
			}
			
			return arr;
		},
		
		showHeaderRow: true,
		getHeaderCellContent: function() {
			var arr = new Array();
			arr.push('');
			arr.push(omsgs.systemId);
			if (this.showUsername)
				arr.push(omsgs.username);
			arr.push(omsgs.givenName);
			arr.push(omsgs.middleName);
			arr.push(omsgs.familyName);
			if (this.showRoles)
				arr.push(omsgs.userRoles);
			
			/* userListingHeaders var from openmrsmessages.js */
			for (var i = 0; i < omsgs.userListingHeaders.length; i++) {
				arr.push(omsgs.userListingHeaders[i]);
			}
			return arr;
		},
		
		searchCleared: function() {
			this.showAll();
		}
		
	},
	"html"
);
