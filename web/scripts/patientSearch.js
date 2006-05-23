function search(obj, event, retired, delay) {
	searchBoxChange("patientTableBody", obj, event, retired, delay);
	return false;
}

var getId = function(p) {
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
			if (typeof isValidCheckDigit != 'undefined' && isValidCheckDigit(p.identifier)==false) {
				td.appendChild(getProblemImage());
			}
			if (p.voided) {
				td.className += " retired";
			}
		}
		
		return td;
	};
var getGiven	= function(p) { return p.givenName == null ? noCell() : p.givenName;  };
var getMiddle	= function(p) { return p.middleName == null ? noCell() : p.middleName; };
var getFamily	= function(p) { return p.familyName == null ? noCell() : p.familyName; };
var getTribe	= function(p) { return p.tribe == null ? noCell() : p.tribe; };
var getGender	= function(p) {
		if (typeof p == 'string') return noCell();
		
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
	};
var getBirthdayEstimated = function(p) {
		if (typeof p == 'string') return noCell();
		if (p.birthdateEstimated)
			return "&asymp;";
		else
			return "";
	};
var getBirthday	= function(p) { 
		if (typeof p == 'string') return noCell();
		str = getDateString(p.birthdate);
		return str;
	};
var getAge = function(p) { 
		if (typeof p == 'string') return noCell();
		if (p.age == null) return "";
		var td = document.createElement("td");
		td.className = 'patientAge';
		var age = p.age;
		if (age < 1)
			age = "<1";
		td.innerHTML = age;
		return td;
}

var getMother  = function(p) { return p.mothersName == null ? noCell() : p.mothersName; };

var customCellFunctions = [getNumber, getId, getGiven, getMiddle, getFamily, getAge, getGender, getTribe, getBirthdayEstimated, getBirthday];

function getProblemImage() {
	var img = document.createElement("img");
	img.src = "/@WEBAPP.NAME@/images/problem.gif";
	if (typeof invalidCheckDigit != 'undefined') img.onclick=invalidCheckDigit;
	img.title="The check digit on this identifier is invalid.  Please double check this patient";
	return img;
}

function customGetRowHeight() {
	return 23;
}

function allowAutoListWithNumber() {
	return true;
}

function rowMouseOver() {
	if (this.className.indexOf("searchHighlight") == -1) {
		this.className = "searchHighlight " + this.className;
		var other = getOtherRow(this);
		if (other != null)
			other.className = "searchHighlight " + other.className;
	}
}

function rowMouseOut() {
	var c = this.className;
	this.className = c.substring(c.indexOf(" ") + 1, c.length);
	var other = getOtherRow(this);
	if (other != null) {
		c = other.className;
		other.className = c.substring(c.indexOf(" ") + 1, c.length);
	}
}

function getOtherRow(row) {
	var other = row.nextSibling;
	if (other != null && other.firstChild.id == row.firstChild.id)
		return other;
	other = row.previousSibling;
	if (other != null && other.firstChild.id == row.firstChild.id)
		return other;
	return null;
}

var savedText="";
var invalidCheckDigitText   = "Invalid check digit for MRN: ";
var searchOnPatientNameText = "Please search on part of the patient's name. ";
var noPatientsFoundText     = "No patients found. <br/> ";
var addPatientLink = "<a href='/@WEBAPP.NAME@/admin/patients/addPatient.htm'>Add a new patient</a>";
function preFillTable(patients) {
	var patientTableHead = $('patientTableHead');
	if (patientTableHead)
		patientTableHead.style.display = "";
	
	// if no hits
	if (patients.length < 1) {
		if (savedText.match(/\d/)) {
			if (isValidCheckDigit(savedText) == false) {
				//the user didn't input an identifier with a valid check digit
				if (patientTableHead)
					patientTableHead.style.display = "none";
				var img = getProblemImage();
				var tmp = " <img src='" + img.src + "' title='" + img.title + "' /> " + invalidCheckDigitText + savedText;
				patients.push(tmp);
				patients.push(noPatientsFoundText);
				patients.push(searchOnPatientNameText);
			}
			else {
				//the user did input a valid identifier, but we don't have it
				patients.push(noPatientsFoundText);
				patients.push(searchOnPatientNameText);
				patients.push(addPatientLink);
			}
		}
		else {
			// the user put in a text search
			patients.push(noPatientsFoundText);
			patients.push(addPatientLink);
		}
		fillTable([]);	//this call sets up the table/info bar
	}
	// if hits
	else if (patients.length > 1 || isValidCheckDigit(savedText) == false) {
		patients.push(addPatientLink);	//setup links for appending to the end
	}
	
	fillTable(patients);		//continue as normal
	
	return false;
};

var postFillTable = function(patients) {
	var tableHead = $('patientTableHead');
	if (tableHead != null) {
		var numObjects = patients.length;
		if (numObjects > 0)
			tableHead.style.display = '';
		else
			tableHead.style.display = 'none';
	}
	
	var rows = $('patientTableBody').getElementsByTagName("tr");
	for (var i=0; i<rows.length; i++) {
		var row = rows[i];
		var index = row.firstChild.id;
		var tr = duplicateRow(row, patients[index-1]);
		if (tr) {
			if (row.nextSibling != null)
				row.parentNode.insertBefore(tr, row.nextSibling);
			else
				row.parentNode.appendChild(tr);
			i = i + 1;
		}
	}
}

function duplicateRow(row, obj) {
	if (obj == null) return false;
	var div = document.createElement("div");
	div.innerHTML = "";
	if (obj.otherIdentifiers != "")
		div.innerHTML = 'Other Identifiers: ' + obj.otherIdentifiers;
	if (obj.otherNames != "")
		div.innerHTML += ' &nbsp; Other Names: ' + obj.otherNames;
	// return false if we shouldn't have a row
	if (!div.innerHTML != "")
		return false;
	
	var newRow = row.cloneNode(true);
	var cellCount = 0;
	while (newRow.childNodes.length > 1) {
		cellCount = cellCount + 1;
		newRow.removeChild(newRow.childNodes[1]);
	}
	newRow.firstChild.innerHTML = "";
	newRow.onmouseover = row.onmouseover;
	newRow.onmouseout = row.onmouseout;
	newRow.onclick = row.onclick;
	
	var td = document.createElement("td");
	td.colSpan = cellCount;
	div.className = "description";
	td.appendChild(div);
	newRow.appendChild(td);
	return newRow;
}

function toggleVerbose(input, tagName, className) {
	var display = "none";
	if (input.checked == true)
		display = "";
		
	changeClassProperty(className, "display", display);
}

function changeClassProperty(sClassName,sProperty,sValue) {
	sClassName="."+sClassName;
	var sheets = document.styleSheets;
	var rules;
	var styleObj;
	
	for (var i=sheets.length-1; i >= 0; i--) {
		rules=sheets[i].cssRules || sheets[1].rules;
		
		for (var j=0; j<rules.length; j++) {
			if (rules[j].selectorText &&
				rules[j].selectorText==sClassName) {
					styleObj=rules[j].style;
					break;
			}
		}
	}
	
	styleObj[sProperty]=sValue;
}