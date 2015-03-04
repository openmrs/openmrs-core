/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.PatientSearch");
dojo.require("dojo.widget.openmrs.PersonSearch");

importJavascriptFile(openmrsContextPath + "/dwr/interface/DWRPatientService.js");

dojo.widget.tags.addParseTreeHandler("dojo:PatientSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.PatientSearch",
	dojo.widget.openmrs.PersonSearch,
	{
		patientId: "",
		showAddPatientLink: true,
		searchPhrase: "",
		
		initializer: function(){
			dojo.debug("initializing patientsearch");
			
			dojo.event.connect("before", this, "doObjectsFound", this, "preDoObjectsFound");
		},
		
		postCreate: function() {
			if (this.patientId != "") {
				this.selectPatient(this.patientId);
			}
			else if (this.searchPhrase) {
				DWRPatientService.findPatients(this.searchPhrase, false, this.simpleClosure(this, "doObjectsFound"));
			}
		},
		
		selectPatient: function(patientId) {
			DWRPatientService.getPatient(patientId, this.simpleClosure(this, "select"));
		},
		
		doFindObjects: function(text) {
		    dojo.debug("finding patient with: " + text);

			var tmpIncludedVoided = (this.showIncludeVoided && this.includeVoided.checked);
			DWRPatientService.findPatients(text, tmpIncludedVoided, this.simpleClosure(this, "doObjectsFound"));
			
			return false;
		},
		
		preDoObjectsFound: function(patients) {
			if (patients == null) { return; }
			// if no hits
			if (patients.length < 1) {
				if (this.text && this.text.match(/\d/)) {
					patients.push(this.noPatientsFoundText);
					patients.push(this.searchOnPatientNameText);
					if (this.showAddPatientLink)
						patients.push(this.addPatientLink);
				}
				else {
					// the user put in a text search
					patients.push(this.noPatientsFoundText);
					if (this.showAddPatientLink)
						patients.push(this.addPatientLink);
				}
			}
			// if hits
			else{
				if (this.showAddPatientLink)
					patients.push(this.addPatientLink);	//setup links for appending to the end
			}
		},
		
		searchOnPatientNameText: omsgs.searchOnName,
		noPatientsFoundText: omsgs.noPatientsFound + " <br/> ",
		addPatientLink: "<a href='" + openmrsContextPath + "/admin/person/addPerson.htm?personType=patient&viewType=shortEdit'>" + omsgs.addNewPatient + "</a>",
		
		getId: function(p) {
			var td = document.createElement("td");
			
			if (p.patientId)
				td.onmouseover = function() { window.status = "Patient Id: " + p.patientId; };
			td.onmouseout = function() { window.status = "" };
			
			if (typeof p == 'string') {
				td.colSpan = this.getHeaderCellContent().length;
				td.innerHTML = p;
			}
			else {
				td.className = "patientIdentifier";
				var obj = document.createElement("a");
				obj.appendChild(document.createTextNode(p.identifier + " "));
				td.appendChild(obj);
				if (p.voided) {
					td.className += " retired";
				}
			}
			
			return td;
		},
		
		getCellFunctions: function() {
			var arr = new Array();
			arr.push(this.simpleClosure(this, "getNumber"));
			arr.push(this.simpleClosure(this, "getId"));
			arr.push(this.simpleClosure(this, "getGiven"));
			arr.push(this.simpleClosure(this, "getMiddle"));
			arr.push(this.simpleClosure(this, "getFamily"));
			arr.push(this.simpleClosure(this, "getAge"));
			arr.push(this.simpleClosure(this, "getGender"));
			arr.push(this.simpleClosure(this, "getBirthdayEstimated"));
			arr.push(this.simpleClosure(this, "getBirthday"));
			/* patientListingAttrs var from openmrsmessages.js */
			for (var i = 0; i < omsgs.patientListingAttrs.length; i++) {
				arr.push(this.simpleClosure(this, "getAttribute", omsgs.patientListingAttrs[i]));
			}
			return arr;
		},
		
		showHeaderRow: true,
		getHeaderCellContent: function() {
			var arr = new Array();
			arr.push('');
			arr.push(omsgs.identifier);
			arr.push(omsgs.givenName);
			arr.push(omsgs.middleName);
			arr.push(omsgs.familyName);
			arr.push(omsgs.age);
			arr.push(omsgs.gender);
			arr.push('');
			arr.push(omsgs.birthdate);
			/* patientListingHeaders var from openmrsmessages.js */
			for (var i = 0; i < omsgs.patientListingHeaders.length; i++) {
				arr.push(omsgs.patientListingHeaders[i]);
			}
			
			return arr;
		},
		
		getRowHeight: function() {
			return 23;
		},
		
		rowMouseOver: function() {
			if (this.className.indexOf("searchHighlight") == -1) {
				this.className = "searchHighlight " + this.className;
				
				var other = this.nextSibling;
				if (other == null || other.firstChild.id != this.firstChild.id) {
					other = this.previousSibling;
					if (other == null || other.firstChild.id != this.firstChild.id)
						other = null;
				}
				
				if (other != null)
					other.className = "searchHighlight " + other.className;
			}
		},
		
		rowMouseOut: function() {
			var c = this.className;
			this.className = c.substring(c.indexOf(" ") + 1, c.length);
			
			var other = this.nextSibling;
			if (other == null || other.firstChild.id != this.firstChild.id) {
				other = this.previousSibling;
				if (other == null || other.firstChild.id != this.firstChild.id)
					other = null;
			}
			
			if (other != null) {
				c = other.className;
				other.className = c.substring(c.indexOf(" ") + 1, c.length);
			}
		},
				
		stripCharsInBag: function(s, bag){
			var i;
		    var returnString = "";
		    // Search through string's characters one by one.
		    // If character is not in bag, append to returnString.
		    for (i = 0; i < s.length; i++){
		        var c = s.charAt(i);
		        if (bag.indexOf(c) == -1) returnString += c;
		    }
		    return returnString;
		},
		
		autoJump: true,
		allowAutoJump: function() {
			if (this.autoJump == false) {
				this.autoJump = true;
				return false;
			}
			//	only allow the first item to be automatically selected if:
			//		the entered text is a string or the entered text is a valid identifier
			return (this.text && (this.text.match(/\d/) == false));
		}
		
	},
	"html"
);