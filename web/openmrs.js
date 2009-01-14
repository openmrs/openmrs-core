
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
	else loadingMessage = "Loading";

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