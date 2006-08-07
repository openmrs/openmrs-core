/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.PatientSearch");
dojo.require("dojo.widget.openmrs.OpenmrsSearch");

document.write("<script type='text/javascript' src='" + openmrsSearchBase + "/dwr/interface/DWRPatientService.js'></script>");

dojo.widget.tags.addParseTreeHandler("dojo:PatientSearch");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.PatientSearch",
	dojo.widget.openmrs.OpenmrsSearch,
	{
		initializer: function(){
			dojo.debug("initializing patientsearch");
			
			dojo.event.topic.subscribe(this.widgetId + "/findObjects",
				function(phrase) {
					this.savedText = phrase;
				}
			);
			
			dojo.event.topic.subscribe(this.widgetId + "/fillTable", 
				function(msg) {
					if (msg) {
						patients = msg.objects;
						// if no hits
						if (patients.length < 1) {
							if (this.savedText.match(/\d/)) {
								if (isValidCheckDigit(this.savedText) == false) {
									//the user didn't input an identifier with a valid check digit
									if (patientTableHead)
										patientTableHead.style.display = "none";
									var img = getProblemImage();
									var tmp = " <img src='" + img.src + "' title='" + img.title + "' /> " + invalidCheckDigitText + this.savedText;
									patients.push(tmp);
									patients.push(this.noPatientsFoundText);
									patients.push(this.searchOnPatientNameText);
								}
								else {
									//the user did input a valid identifier, but we don't have it
									patients.push(this.noPatientsFoundText);
									patients.push(this.searchOnPatientNameText);
									patients.push(this.addPatientLink);
								}
							}
							else {
								// the user put in a text search
								patients.push(this.noPatientsFoundText);
								patients.push(this.addPatientLink);
							}
							fillTable([]);	//this call sets up the table/info bar
						}
						// if hits
						else if (patients.length > 1 || this.isValidCheckDigit(savedText) == false) {
							patients.push(this.addPatientLink);	//setup links for appending to the end
						}
					}
				});
			
		},
		
		doFindObjects: function(text) {

			// a javascript closure
			var callback = function(ts) { return function(obj) {ts.fillTable(obj)}};
			
			var tmpIncludedVoided = (this.showIncludeVoided && this.includeVoided.checked);
			DWRPatientService.findPatients(callback(this), text, tmpIncludedVoided);
			
			return false;
		},
		
		savedText: "",
		invalidCheckDigitText: "Invalid check digit for MRN: ",
		searchOnPatientNameText: "Please search on part of the patient's name. ",
		noPatientsFoundText: "No patients found. <br/> ",
		addPatientLink: "<a href='/@WEBAPP.NAME@/admin/patients/addPatient.htm'>Add a new patient</a>",
		
		getId: function(p) {
			var td = document.createElement("td");
			if (typeof p == 'string') {
				td.colSpan = 9;
				td.innerHTML = p;
			}
			else {
				td.className = "patientIdentifier";
				var obj = document.createElement("a");
				obj.appendChild(document.createTextNode(p.identifier + " "));
				td.appendChild(obj);
				if (p.identifierCheckDigit)
					if (this.isValidCheckDigit(p.identifier)==false) {
						td.appendChild(getProblemImage());
					}
				if (p.voided) {
					td.className += " retired";
				}
			}
			
			return td;
		},
		getGiven : function(p) { return p.givenName == null ? this.noCell() : p.givenName;  },
		getMiddle: function(p) { return p.middleName == null ? this.noCell() : p.middleName; },
		getFamily: function(p) { return p.familyName == null ? this.noCell() : p.familyName; },
		getTribe : function(p) { return p.tribe == null ? this.noCell() : p.tribe; },
		getGender: function(p) {
				if (typeof p == 'string') return this.noCell();
				
				var td = document.createElement("td");
				td.className = "patientGender";
				var src = "/@WEBAPP.NAME@/images/";
				if (p.gender.toUpperCase() == "F")
					src += "female.gif";
				else
					src += "male.gif";
				var img = document.createElement("img");
				td.innerHTML = "<img src='" + src + "'>";
				return td;
		},
		
		getBirthdayEstimated: function(p) {
				if (typeof p == 'string') return this.noCell();
				if (p.birthdateEstimated)
					return "&asymp;";
				else
					return "";
		},
		
		getBirthday: function(p) { 
				if (typeof p == 'string') return this.noCell();
				str = getDateString(p.birthdate);
				return str;
		},
		
		getAge: function(p) { 
				if (typeof p == 'string') return this.noCell();
				if (p.age == null) return "";
				var td = document.createElement("td");
				td.className = 'patientAge';
				var age = p.age;
				if (age < 1)
					age = "<1";
				td.innerHTML = age;
				return td;
		},
		
		getMother: function(p) { return p.mothersName == null ? this.noCell() : p.mothersName; },
		
		getCellFunctions: function() {
			var tmp = function(ths, method) { return function(obj) { return ths[method](obj); }; };
			return [tmp(this, "getNumber"), 
					tmp(this, "getId"), 
					tmp(this, "getGiven"), 
					tmp(this, "getMiddle"), 
					tmp(this, "getFamily"),
					tmp(this, "getAge"), 
					tmp(this, "getGender"),
					tmp(this, "getTribe"),
					tmp(this, "getBirthdayEstimated"),
					tmp(this, "getBirthday")
					];
			
		},
		
		getProblemImage: function() {
			var img = document.createElement("img");
			img.src = "/@WEBAPP.NAME@/images/problem.gif";
			img.title="The check digit on this identifier is invalid.  Please double check this patient";
			return img;
		},
		
		getRowHeight: function() {
			return 23;
		},
		
		rowMouseOver: function() {
			if (this.className.indexOf("searchHighlight") == -1) {
				this.className = "searchHighlight " + this.className;
				var other = dojo.widget.openmrs.PatientSearch.getOtherRow(this);
				if (other != null)
					other.className = "searchHighlight " + other.className;
			}
		},
		
		rowMouseOut: function() {
			var c = this.className;
			this.className = c.substring(c.indexOf(" ") + 1, c.length);
			var other = dojo.widget.openmrs.PatientSearch.getOtherRow(this);
			if (other != null) {
				c = other.className;
				other.className = c.substring(c.indexOf(" ") + 1, c.length);
			}
		},
		
		getOtherRow: function(row) {
			var other = row.nextSibling;
			if (other != null && other.firstChild.id == row.firstChild.id)
				return other;
			other = row.previousSibling;
			if (other != null && other.firstChild.id == row.firstChild.id)
				return other;
			return null;
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
