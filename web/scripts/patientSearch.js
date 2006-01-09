function search(obj, event, retired, delay) {
	searchBoxChange("patientTableBody", obj, event, retired, delay);
	return false;
}

if (!findObjects) {
	var findObjects = function (text) {
		if (text.length > 2) {
			DWRPatientService.findPatients(fillTable, text, includeRetired);
		}
		else {
			var msg = new Array();
			msg.push("Invalid number of search characters");
			fillTable(msg, [getNumber, getString]);
		}
		patientListing.style.display = "";
		return false;
	};
}

var getId		= function(p) { 
		var obj = document.createElement("a");
		obj.href = "#" + searchIndex;
		obj.className = "searchHit";
		obj.onclick = function() { return selectObject(parseInt(this.href.substring(this.href.indexOf('#')+1, this.href.length))); };
		obj.innerHTML = p.identifier;
		if (p.voided) {
			div = document.createElement("div");
			div.className = "retired";
			div.appendChild(obj);
			obj = div;
		}
		return obj;
	};
var getGiven	= function(p) { return p.givenName;  };
var getMiddle	= function(p) { return p.middleName; };
var getFamily	= function(p) { return p.familyName; };
var getRace		= function(p) { return p.race; };
var getGender	= function(p) {
		var src = "/@WEBAPP.NAME@/images/";
		if (p.gender.toUpperCase() == "F")
			src += "female.gif";
		else
			src += "male.gif";
		var img = document.createElement("img");
		img.src = src;
		return img;
	};
var getBirthday	= function(p) { 
		var str = '';
		if (p.birthdate != null) {
			str += p.birthdate.getMonth() + 1 + '-';
			str += p.birthdate.getDate() + '-';
			str += (p.birthdate.getYear() + 1900);
		}
		
		if (p.birthdateEstimated)
			str += " (?)";
		
		return str;
	};
var getMother  = function(p) { return p.mothersName;  };

var customCellFunctions = [getNumber, getId, getFamily, getGiven, getMiddle, getGender, getRace, getBirthday, getMother];
