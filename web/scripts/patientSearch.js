function search(obj, event, retired, delay) {
	searchBoxChange("patientTableBody", obj, event, retired, delay);
	return false;
}

var getId		= function(p) {
		if (typeof p == 'string') {
			var td = document.createElement("td");
			td.colspan = 0;
			td.innerHTML = "<div style='float: left;'>" + p + "</div>";
			return td;
		}
		div = document.createElement("div");
		var obj = document.createElement("a");
		obj.href = "#" + searchIndex;
		obj.className = "searchHit";
		obj.onclick = function() { return selectObject(parseInt(this.href.substring(this.href.indexOf('#')+1, this.href.length))); };
		obj.appendChild(document.createTextNode(p.identifier + " "));
		div.appendChild(obj);
		if (typeof isValidCheckDigit != 'undefined' && isValidCheckDigit(p.identifier)==false) {
			div.appendChild(getProblemImage());
		}
		if (p.voided) {
			div.className = "retired";
		}
		return div;
	};
var getGiven	= function(p) { return p.givenName == null ? "" : p.givenName;  };
var getMiddle	= function(p) { return p.middleName == null ? "" : p.middleName; };
var getFamily	= function(p) { return p.familyName == null ? "" : p.familyName; };
var getTribe	= function(p) { return p.tribe == null ? "" : p.tribe; };
var getGender	= function(p) {
		if (typeof p == 'string') return "";
		
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
		if (typeof p == 'string') return "";
		
		var str = '';
		if (p.birthdate != null) {
			str += p.birthdate.getDate() + '-';
			str += p.birthdate.getMonth() + 1 + '-';
			str += (p.birthdate.getYear() + 1900);
		}
		if (p.birthdateEstimated)
			str += " (?)";
		return str;
	};
var getMother  = function(p) { return p.mothersName == null ? "" : p.mothersName;  };

var customCellFunctions = [getNumber, getId, getFamily, getGiven, getMiddle, getGender, getTribe, getBirthday, getMother];

function getProblemImage() {
	var img = document.createElement("img");
	img.src = "/@WEBAPP.NAME@/images/problem.gif";
	if (typeof invalidCheckDigit != 'undefined') img.onclick=invalidCheckDigit;
	img.title="The check digit on this identifier is invalid.  Please double check this patient";
	return img;
}