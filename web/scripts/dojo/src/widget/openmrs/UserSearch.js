/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.UserSearch");
dojo.require("dojo.widget.openmrs.OpenmrsSearch");

document.write("<script type='text/javascript' src='" + openmrsSearchBase + "/dwr/interface/DWRUserService.js'></script>");

dojo.widget.tags.addParseTreeHandler("dojo:UserSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.UserSearch",
	dojo.widget.openmrs.OpenmrsSearch,
	{
		roles: [],
		
		userId: "",
		
		postCreate: function(){
			dojo.debug("postCreate in UserSearch");
			
			var closure = function(thisObj, method) { return function(obj) { return thisObj[method]({"obj":obj}); }; };
			if (this.userId != "")
				DWRUserService.getUser(closure(this, "select"), this.userId);
		},
		
		doFindObjects: function(text) {

			// a javascript closure
			var callback = function(ts) { return function(obj) {ts.doObjectsFound(obj)}};
			
			var tmpIncludedVoided = (this.showIncludeVoided && this.includeVoided.checked);
			DWRUserService.findUsers(callback(this), text, this.roles, tmpIncludedVoided);
			
			return false;
		},
		
		getCellContent: function(user) {
				if (typeof user == 'string') return this.noCell();
				var txt = "";
				if (user.firstName)
					txt = user.firstName;
				if (user.middleName)
					txt += (txt.length ? " " : "") + user.middleName;
				if (user.lastName)
					txt += (txt.length ? " " : "") + user.lastName;
				
				return txt;
		}
		
	},
	"html"
);
