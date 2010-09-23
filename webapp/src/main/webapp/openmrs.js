
function markAlertRead(self, alertId) {
	DWRAlertService.markAlertRead(alertId, null);
	var parent = self.parentNode;
	parent.style.display = "none";
	var unreadAlertSizeBox = document.getElementById('unreadAlertSize');
	var unreadAlertSize = parseInt(unreadAlertSizeBox.innerHTML);
	if (unreadAlertSize == 1) {
		// hide the entire alert outer div because they read the last alert
		parent = parent.parentNode.parentNode;
		parent.style.display = "none";
	}
	else {
		unreadAlertSize = unreadAlertSize - 1;
		unreadAlertSizeBox.innerHTML = unreadAlertSize;
	}
		
	return false;
}

function addClass(obj, c) {
	if (obj.className.indexOf(c) == -1)
		obj.className = c + " " + obj.className;
}

function removeClass(obj, newClassName) {
	var className = obj.className;
	var startIndex = className.indexOf(newClassName);
	if (startIndex != -1) {
		var endIndex = obj.className.indexOf(" ", startIndex);
		if (endIndex == -1)
			endIndex = className.length;
		obj.className = className.substring(0, startIndex, endIndex) + " " + className.substring(endIndex, className.length);
	}
	//alert("class: '" + className + "' startIndex: " + startIndex + " endIndex: " + endIndex + " final class: '" + obj.className + "'");
}

function hasClass(obj, className) {
	var classes = obj.className.split(" ");
	for (var i = 0; i<classes.length; i++) {
		if (classes[i] == className)
			return true;
	}
	return false;
}

function manipulateClass(operation, obj, c1, c2) {
	switch (operation){
		case 'swap':
			obj.className=!manipulateClass('check',obj,c1)?obj.className.replace(c2,c1): obj.className.replace(c1,c2);
			break;
		case 'add':
			if(!manipulateClass('check',obj,c1)){obj.className+=obj.className?' '+c1:c1;}
			break;
		case 'remove':
			var rep=obj.className.match(' '+c1)?' '+c1:c1;
			obj.className=obj.className.replace(rep,'');
			break;
		case 'check':
			return new RegExp('\\b'+c1+'\\b').test(obj.className)
			break;
	}
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

function toggleLayer(layerId, linkObj, showText, hideText) {
    var style = document.getElementById(layerId).style;
 	if (style.display == "none") {
        style.display = "";
        if (linkObj != null)
        	linkObj.innerHTML = hideText;
    } else {
        style.display = "none";
        if (linkObj != null)
        	linkObj.innerHTML = showText;
    }
    
    return false;
}

function showLayer(id) {
	var div = document.getElementById(id);
	if ( div ) { div.style.display = ""; }
}

function hideLayer(id) {
	var div = document.getElementById(id);
	if ( div ) { div.style.display = "none"; }
}

function showDiv(id) {
	var div = document.getElementById(id);
	if ( div ) { div.style.display = ""; }
}

function hideDiv(id) {
	var div = document.getElementById(id);
	if ( div ) { div.style.display = "none"; }
}

function refreshPage() {
	window.location.reload();
}

function addEvent(obj, eventType, fn) {
	if (obj.addEventListener) {
		obj.addEventListener(eventType, fn, true);
		return true;
	} else if (obj.attachEvent) {
		var r = obj.attachEvent("on"+eventType, fn);
		return r;
	} else {
		return false;
	}
}

useLoadingMessage = function(message) {
	var loadingMessage;
	if (message) loadingMessage = message;
	else loadingMessage = dwrLoadingMessage; // to internationalize message

	dwr.engine.setPreHook(function() {
		var disabledZone = $('disabledZone');
		if (!disabledZone) {
			disabledZone = document.createElement('div');
			disabledZone.setAttribute('id', 'disabledZone');
			document.body.appendChild(disabledZone);
			var messageZone = document.createElement('div');
			messageZone.setAttribute('id', 'messageZone');
			disabledZone.appendChild(messageZone);
			var text = document.createTextNode(loadingMessage);
			messageZone.appendChild(text);
		}
		else {
			$('messageZone').innerHTML = loadingMessage;
			disabledZone.style.display = '';
		}
	});

	dwr.engine.setPostHook(function() {
		$('disabledZone').style.display = 'none';
	});
}

var tagNameVisibility = new Array();
function toggleVisibility(element, childrenTagNames, className) {
	var key = element + "." + childrenTagNames;
	
	if (tagNameVisibility[key] == "none")
		tagNameVisibility[key] = "";
	else
		tagNameVisibility[key] = "none";
		
	var items = element.getElementsByTagName(childrenTagNames);
	for (var i=0; i < items.length; i++) {
		var classes = items[i].className.split(" ");
		for (x=0; x<classes.length; x++) {
			if (classes[x] == className)
				items[i].style.display = tagNameVisibility[key];
		}
	}
	
	return false;
}

function toggleRowVisibilityForClass(elementId, className, hasDescriptionRow) {
	var el = document.getElementById(elementId);
	
	if (el) {
		toggleVisibility(el, "tr", className);
	
		var rows = el.rows;
		var oddRow = true;
		
		for (var i=1; i<rows.length; i++) {
			if (rows[i].style.display == "") {
				var c = "";
				if (rows[i].className.substr(0, className.length) == className)
					c = className + " ";
				if (oddRow)
					c = c + "oddRow";
				else
					c = c + "evenRow";
				oddRow = !oddRow;
				
				rows[i].className = c;
				if (hasDescriptionRow)
					rows[++i].className = c;
			}
		}
	}
	
	return false;
}

function gotoUser(select, userId) {
	if (userId == null)
		userId = $(select).value;
	if (userId != "")
		window.location = openmrsContextPath + "/admin/users/user.form?userId=" + userId;
	return false;
}

/**
 * Writes a <script src=... > tag to the current body
 * element.  This precludes the need for document.write(<script...)  
 * 
 * This won't add the script file import to the header if one exists in <head> already.
 * 
 * @param filename the full path to the file to include
 */
 function importJavascriptFile(filename) {
	var scriptElements = document.getElementsByTagName('script');
	var foundMatchingScript = false;
	for (var i = 0; i < scriptElements.length && !foundMatchingScript; i++) {
		var src = scriptElements[i].src;
		
		// strip out the ?v=... part of the src url
		var indexOfQuestionMark = src.indexOf("?");
		if (indexOfQuestionMark != -1)
			src = src.substring(0, indexOfQuestionMark);
		
		// check to see if src ends with filename
		if (src.length >= filename.length && src.indexOf(filename)==(src.length - filename.length)) {
        	foundMatchingScript = true;
		}
    }
    
    // only append the new script if one wasn't found already
    if (!foundMatchingScript) {
    	var headElement = document.getElementsByTagName('head').item(0);
    	script = document.createElement('script');
		script.src = filename;
		script.type = 'text/javascript';
		headElement.appendChild(script);
    }
 }
 
 /**
  * This parses a string into a js date object.  This only works on numbered dates, not
  * dates that have strings in them: (ie. 05-06-2009 will work, but 55-JUN-2009 will not work).
  * 
  * @param birthdate the string to parse 
  * @param datePattern the pattern that will be parse M-D-Y, Y-M-D, or D-M-Y
  * @return a javascript date object for the given string in the given pattern 
  */
 function parseSimpleDate(birthdate, datePattern) {
	var datePatternStart = datePattern.substr(0,1).toLowerCase(); 
	var year, month, day; 
	
	if (datePatternStart == 'm') { /* M-D-Y */ 
		year = birthdate.substr(6, 4); 
		month = birthdate.substr(0, 2); 
		day = birthdate.substr(3, 2); 
	} 
	else if (datePatternStart == 'y') { /* Y-M-D */ 
		year = birthdate.substr(0, 4); 
		month = birthdate.substr(3, 2); 
		day = birthdate.substr(8, 2); 
	} 
	else { /* (datePatternStart == 'd') D-M-Y */ 
		year = birthdate.substr(6, 4); 
		month = birthdate.substr(3, 2); 
		day = birthdate.substr(0, 2); 
	} 
	
	var localeBirthDate = new Date(); 
	localeBirthDate.setYear(year); 
	localeBirthDate.setMonth(month - 1); 
	localeBirthDate.setDate(day);
	
	return localeBirthDate;
 }

/**
 * @param sFormat :String - the format to use (ex: dd-mm-yyyy)
 * @param sDate :String - the javascript Date object to use
 * @returns javascript Date object
 */
function parseDateFromStringToJs(sFormat, sDate) {
	if(sDate == null) {
		return new Date();
	}
	
	var idx;
	var date = new Date();
	if((idx = sFormat.search(/mm/)) != -1) {
		date.setMonth(sDate.substring(idx, idx+2)-1);//0-11
	}
	if((idx = sFormat.search(/dd/)) != -1) {
		date.setDate(sDate.substring(idx, idx+2));//1-31
	}
	if((idx = sFormat.search(/yyyy/)) != -1) {
		date.setYear(sDate.substring(idx, idx+4));
	}
	
	return date;
}

function tenrule(n) {
	return ((n < 10) ? "0" : "") + n;
}

/**
 * @param sFormat :String - the format of the date to use (ex: dd-mm-yyyy)
 * @param jsDate :Date - a javascript Date object
 * @returns a string in the proper format
 */
function parseDateFromJsToString(sFormat, jsDate) {
 	if(jsDate == null) {
 		jsDate = new Date();
 	}
 	
 	return sFormat.replace(/mm/, tenrule(jsDate.getMonth()+1)).replace(/dd/, tenrule(jsDate.getDate())).replace(/yyyy/, (1900+jsDate.getYear()));
}

/**
 * Takes something like "param[start]" and returns "param\\[start\\]"
 * @param partialSelector the part of a jquery selector after the # or the .
 * @return the input, with the following characters escaped: #;&,.+*~':"!^$[]()=>|/@
 */
function escapeJquerySelector(partialSelector) {
	return partialSelector.replace('#', '\\#').replace(';', '\\;').replace('&', '\\&').replace(',', '\\,').replace('.', '\\.').replace('+', '\\+').replace('*', '\\*').replace('~', '\\~').replace("'", "\\'").replace(':', '\\:').replace('"', '\\"').replace('!', '\\!').replace('^', '\\^').replace('$', '\\$').replace('[', '\\[').replace(']', '\\]').replace('(', '\\(').replace(')', '\\)').replace('=', '\\=').replace('>', '\\>').replace('|', '\\|').replace('/', '\\/').replace('@', '\\@');
}

/**
 * DatePicker class
 * @param dateFormat :String date format to use (ex: dd-mm-yyyy)
 * @param id :Element the html element (when id is not present)
 *           :String the id of the text box to use as the calendar
 * @param opts :Map additional options for the jquery datepicker widget (included are dateFormat, appendText, gotoCurrent)
 */
function DatePicker(dateFormat, id, opts) {
	var jq;
	if(typeof id == 'string') {
		id = escapeJquerySelector(id);
		jq = jQuery('#' + id);
	}
	else {
		jq = jQuery(id);
	}
 	
 	if(opts == null) {
 		opts = {};
 	}
 	setOptions(opts, 'dateFormat', dateFormat.replace("yyyy", "yy"));//have to do the replace here because the datepicker only required 'yy' for 4-number year
 	setOptions(opts, 'appendText', "(" + dateFormat + ")");
 	setOptions(opts, 'gotoCurrent', true);
 	setOptions(opts, 'changeMonth', true);
 	setOptions(opts, 'changeYear', true);
 	setOptions(opts, 'showOtherMonths', true);
 	setOptions(opts, 'selectOtherMonths', true);

 	jq.datepicker(opts);
 		
 	this.setDate = function(date) {
 		var jsDate = date;
 		if(typeof date == 'string') {
 			jsDate = parseDateFromStringToJs(dateFormat, date);
 		}
 		
 		jq.datepicker("setDate", jsDate);
 	};
 	
 	this.getDate = function() {
 		return jq.datepicker("getDate");
 	};
 	
 	this.getDateAsString = function() {
 		return parseDateFromJsToString(dateFormat, this.getDate());
 	};
 	
 	this.show = function() {
 		jq.datepicker("show");
 	}
}

/**
 * AutoComplete class
 * @param id :Element the html element (when id is not present)
 * 			 :String the id of the text box
 * @param callback a function with 2 params (query - the text in the box, and response - use when the data is returned and takes an array as a param)
 * @param opts :Map addtional options (included are: minLength, delay, source)
 */
function AutoComplete(id, callback, opts) {
	var jq;
	if(typeof id == 'string') {
		id = escapeJquerySelector(id);
		jq = jQuery('#' + id);
	}
	else {
		jq = jQuery(id);
	}

 	if(opts == null) {
 		opts = {};
 	}
 	setOptions(opts, 'minLength', 2);
 	setOptions(opts, 'delay', 0);
 	setOptions(opts, 'source', function(request, response) {
 		callback(request.term, response);
 	});
 	
 	jq.autocomplete(opts);
}

/**
 * Simple utility method to set a map value if the key doesnt exist
 * @param opts :Map
 * @param name :Object the key
 * @param value :Object the value (can also be a function)
 */
function setOptions(opts, name, value) {
	if(opts[name]) return;
 	opts[name] = value;
}

function colorVisibleTableRows(tableId, oddColorClass, evenColorClass, includeHeader) {
 	var rows = jQuery('#' + tableId + ' tr');
 	var odd = true;
 	for(var i=(includeHeader ? 0 : 1); i < rows.length; i++) {
 		if(!jQuery(rows[i]).is(':visible')) continue;
 		
 		jQuery(rows[i]).css("backgroundColor", (odd ? oddColorClass : evenColorClass));
 		odd = !odd;
 	}
}
