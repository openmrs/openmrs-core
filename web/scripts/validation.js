
function showError(isValid, obj, msgNode, btn) {
	if (isValid) {
		// value is valid; hide the error message
		obj.className = "";	// might need to save classname and re-apply eventually
		var sibling = obj.nextSibling;
		//if next sibling isn't textnode, its error tag and we have to hide it
		if (sibling != null && sibling.tagName != null) {
			sibling.style.display = "none";
		}
		return true;
	}
	else {
		// value is not valid; show the error msg
		obj.className = "error";
		//add a child (?) and insert msg here...
		var sibling = obj.nextSibling;
		//if next sibling is textnode, create error tag
		if (sibling == null || sibling.tagName == null) {
			var errorTag = document.createElement("span");
			errorTag.className = "error";
			obj.parentNode.insertBefore(errorTag, sibling);
			sibling = errorTag;
		}
		sibling.style.display = "inline";
		if (typeof msgNode == "string")
			msgNode = document.createTextNode(msgNode);
		removeChildren(sibling);
		sibling.appendChild(msgNode);
		if (btn != null)
			sibling.appendChild(btn);
		return false;
	}
}

function isValidCheckDigit(value) {
	if (value == null) return false;
	
	if (value.length < 3 || value.indexOf('-') != value.length - 2)
		return false;
	
	var checkDigit = value.charAt(value.length - 1).valueOf();
	
	var valueWithoutCheckDigit = value.substr(0, value.length - 2);
	
	return (checkDigit == getCheckDigit(valueWithoutCheckDigit));
}

function getCheckDigit(value) {

	// allowable characters within identifier
	var validChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVYWXZ_";
	
	if (stripCharsInBag(value, validChars) != "") {
		//Invalid character in string
	}
	
	// remove whitespace
	value = value.replace([ 	], "");
	// convert to uppercase
	value = value.toUpperCase();
	
	// running total
	var sum = 0;
	
	//alert("sum: " + sum + " ch: " + ch + " digit: " + digit + " weight: " + weight);
	
	// loop through digits from right to left
	for (var i = 0; i < value.length; i++) {
		//set ch to "current" character to be processed
		var ch = value.charCodeAt(value.length - i - 1);
		var digit = ch - 48;
		var weight;
		if (i % 2 == 0) {
			// for alternating digits starting with the rightmost, we use our formula
			// this is the same as multiplying x 2 and adding digits together for values
			// 0 to 9.  Using the following formula allows us to gracefully calculate a
			// weight for non-numeric "digits" as well (from their ASCII value - 48).
			weight = (2 * digit) - Math.floor(digit / 5) * 9;
		}
		else {
			// even-positioned digits just contribute their ascii value minus 48
			weight = digit;
		}
		// keep a running total of weights
		sum = sum + weight;
	}	
 	// avoid sum less than 10 (if characters below "0" allowed, this could happen)
 	sum = Math.abs(sum) + 10;
	// check digit is amount needed to reach next number divisible by ten 
	return (10 - (sum % 10)) % 10;
	
}

function isValidInteger(s){
	var i;
    for (i = 0; i < s.length; i++){   
        // Check that current character is number.
        var c = s.charAt(i);
        if (((c < "0") || (c > "9"))) return false;
    }
    // All characters are numbers.
    return true;
}

function stripCharsInBag(s, bag){
	var i;
    var returnString = "";
    // Search through string's characters one by one.
    // If character is not in bag, append to returnString.
    for (i = 0; i < s.length; i++){   
        var c = s.charAt(i);
        if (bag.indexOf(c) == -1) returnString += c;
    }
    return returnString;
}

function daysInFebruary (year){
	// February has 29 days in any year evenly divisible by four,
    // EXCEPT for centurial years which are not also divisible by 400.
    return (((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28 );
}

function DaysArray(n) {
	for (var i = 1; i <= n; i++) {
		this[i] = 31
		if (i==4 || i==6 || i==9 || i==11) {this[i] = 30}
		if (i==2) {this[i] = 29}
   } 
   return this
}

function isValidDate(obj, dtStr){
	var format = "mm/dd/yyyy";
	var delimiter = "/";
	var minYear=1900;
	var maxYear=2100;
	var daysInMonth = DaysArray(12)
	var pos1=dtStr.indexOf(delimiter)
	var pos2=dtStr.indexOf(delimiter,pos1+1)
	var strMonth=dtStr.substring(0,pos1)
	var strDay=dtStr.substring(pos1+1,pos2)
	var strYear=dtStr.substring(pos2+1)
	strYr=strYear
	if (strDay.charAt(0)=="0" && strDay.length>1) strDay=strDay.substring(1)
	if (strMonth.charAt(0)=="0" && strMonth.length>1) strMonth=strMonth.substring(1)
	for (var i = 1; i <= 3; i++) {
		if (strYr.charAt(0)=="0" && strYr.length>1) strYr=strYr.substring(1)
	}
	month=parseInt(strMonth)
	day=parseInt(strDay)
	year=parseInt(strYr)
	if (pos1==-1 || pos2==-1){
		alert("The date format should be : " + format)
		return false
	}
	if (strMonth.length<1 || month<1 || month>12){
		alert("Please enter a valid month")
		return false
	}
	if (strDay.length<1 || day<1 || day>31 || (month==2 && day>daysInFebruary(year)) || day > daysInMonth[month]){
		alert("Please enter a valid day")
		return false
	}
	if (strYear.length != 4 || year==0 || year<minYear || year>maxYear){
		alert("Please enter a valid 4 digit year between "+minYear+" and "+maxYear)
		return false
	}
	if (dtStr.indexOf(delimiter,pos2+1)!=-1 || isValidInteger(stripCharsInBag(dtStr, delimiter))==false){
		alert("Please enter a valid date")
		return false
	}
return true;
}

var validateObj = null;
var btnObj = null;

function validateIdentifier(obj, btn, text) {
	validateObj = obj;
	var textNode = document.createTextNode(text);
	var button = document.createElement("input");
	button.type = "button";
	button.value = "Fix Check Digit";
	button.className = "smallButton";
	button.onclick = fixCheckDigit;
	if (btn != null) 
		btnObj = document.getElementById(btn);
	var hasError = showError(isValidCheckDigit(obj.value), obj, textNode, button);
	if (hasError) {
		btnObj.disabled = false;
	} else {
		btnObj.disabled = true;
	}
	obj.focus();
}
function fixCheckDigit() {
	var obj = validateObj;
	if (obj != null) {
		var val = obj.value;
		var index = val.lastIndexOf("-");
		if (index != -1)
			val = val.substr(0, index);
		obj.value = val + "-" + getCheckDigit(val);
		validateIdentifier(obj);
	}
}
	
function removeChildren(obj) {
	var child = obj.firstChild;
	while (child != null) {
		next = child.nextSibling;
		obj.removeChild(child);
		child = next;
	}
}
		