
// General OpenMRS searching display algorithm
// Author: Ben Wolfe
// Version: 1.5
//
// To use, import this file in the html and create (at least) two methods similar to these:
//
//	findObjects(textPhrase) {
//	  DWRConceptService.findConcepts(fillTable, text, includeRetired);
//	}
//	var getCellContent = function(obj) {
//	  return "<a href='#' onClick=selectObject('" + searchIndex + "')>" + obj.name + "</a>";
//	};
//  var onSelect(array) {
//    alert(array[0].id)'
//  }
//
//	OR for more complicated displays:
//
//	(same as above)
//	findObjects(textPhrase) {
//	  DWRService.findConcepts(fillTable, text, includeRetired);
// 
//	}
//  var onSelect(array) {
//    alert(array[0].id)'
//  }
//	var getCellContent = function(obj) {
//	  return "<a href='#' onClick=selectObject('" + searchIndex + "')>" + obj.name + "</a>";
//	};
//	var customCellFunctions = [getNumber, getCellContent, getCell2Content, getCell3Content];
//	Note: getNumber increments the searchIndex
//	var getCell2Content = function(obj) {
//	  return obj.age;
//	  };
//	var getCell3Content = function(obj) {
//	  return obj.group;
//	};



var searchTimeout;
var searchIndex;
var objectsFound;
var allObjectsFound;
var objectHitsTableBody;
var text;
var textbox;
var includeRetired;
var keyCode;
var lastPhraseSearched;
var numItemsDisplayed;
var firstItemDisplayed;
var allowAutoListWithNumber;
var previousHit;

var debugBox;

var ENTERKEY = 13;

// clears variables. Equivalent to reloading the page.
//  (useful for pages with multiple search boxes)
function resetForm() {
	hideHighlight();
	lastPhraseSearched = null;
	objectsFound = new Array();
	allObjectsFound = new Array();
	text = null;
	textbox = null;
	includeRetired = false;
	searchIndex = 0;
	firstItemDisplayed = 1;
	numItemsDisplayed = 0;
	window.onkeypress = hotkey;
	clearPagingBars();
	allowAutoListWithNumber = false;
	debugBox = $("debugBox");
	if (debugBox) debugBox.innerHTML = "";
	keyCode = null;
	previousHit = null;
}

function searchBoxChange(bodyElementId, obj, event, retired, delay, onkeydownused) {
	if (debugBox) debugBox.innerHTML += '<br>---- delay: ' + delay;
	objectHitsTableBody = $(bodyElementId);
	textbox = obj;
	text = textbox.value.toString();
	text = text.replace(/^\s+/, '')
	text = text.replace(/\s+$/, '');
	
	if (!delay)  { delay = 400; }
	clearTimeout(searchTimeout);
		
	key = 0;
	if (event == null) { 
		// if onSubmit function called
		key = ENTERKEY;	//mimic user hitting enter key
	}
	else {
		if (!event.altKey && (!event.ctrlKey || event.keyCode == 86)) {
			// this if statement cancels the search on alt and control keys, except for ctrl-v
			key = event.keyCode;
			if (debugBox) debugBox.innerHTML += '<br>event.type : ' + event.type;
			var type = null;
			try {
				if ((key==0 || key==null) && (event.type == "click" || event.type == "change" || event.type == "submit")) {
					//if non-key event like clicking checkbox or changing dropdown list
					key = 1;
				}
				else if (key == 0 && event.charCode != null) {
					key = event.charCode;
				}
			}
			catch (err) {
				// event.type gave an error
				if (key == 0 && event.charCode != null) {
					key = event.charCode;
				}
			}
		}
	}
	
	// infopath hack since it doesn't let us use onkeyup or onkeypress	
	if (onkeydownused == true) {
		// only add if the key is a letter and no modifier key was pressed
		if (key >= 48 && key <= 90 && !event.altKey && !event.ctrlKey) {
			var newKey = String.fromCharCode(key).toLowerCase();
			// IE interprets all char codes as upper case.  
			// Only leave in uppercase if the previous char is uppercase (hack #2)
			if (text.length > 0) {
				var lastKey = text.substring(text.length - 1, text.length);
				if (lastKey >= 'A' && lastKey <= 'Z')
					newKey = newKey.toUpperCase();
			}
			text = text + newKey;
		}
		if (key == 8 && text.length > 1) {//backspace
			text = text.substring(0, text.length - 1);
		}
	}
	
	if (typeof allowAutoListWithNumber == 'function' && allowAutoListWithNumber()) {
		allowAutoListWithNumber = allowAutoListWithNumber();
	}
	
	//reset textbox for mouse events
	if (key == 1 && (text == "" || text == lastPhraseSearched)) {
		if (event.type == 'submit') return false; //exit searching if user hit enter on empty box
		text = lastPhraseSearched;
	}
	
	if (key == 27) {
		//escape key pressed
		exitNumberMode(textbox);
		return false;
	}
	else if (text == "" && includeRetired == retired) {
		//searched on empty string (and didn't change retired status)
		return false;
	}
	else if (key == ENTERKEY) {
		if (debugBox) debugBox.innerHTML += '<br> Enter key pressed, search: ' + text;
		hideHighlight();
		// if the user hit the enter key then check for sequence of numbers
		if (text.match(/^\s*\d+\s*(,\d+\s*)*$/))
		{
			if (debugBox) debugBox.innerHTML += '<br> text matched set of numbers';
			var textWords = text.split(/\s*,\s*/);
			var objectsReturned = new Array();
			for (i=0; i<textWords.length; i++)
			{
				if (textWords[i] > 0 && textWords[i] <= objectsFound.length)
				{
					objectsReturned.push(objectsFound[textWords[i]-1]);
				}
				else
				{
					//if only one number entered, assumed searching on object id or just a number and its not an error
					if (textWords.length != 1) {
						alert("Invalid choice: '" + textWords[i] + "'");
						return false;
					}
				}
			}
			if (objectsReturned.length > 0) {
				onSelect(objectsReturned);
				if (textbox != null)
					textbox.value = lastPhraseSearched;
				return false;
			}
		}
		textbox.focus();
		//textbox.select();
		textbox.value = "";
		if (debugBox) debugBox.innerHTML += '<br> textbox.value cleared';
		if (text != lastPhraseSearched || includeRetired != retired || (typeof allowNewSearch != 'undefined' && allowNewSearch() == true)) {
			//this was a new search with the enter key pressed
			if (debugBox) debugBox.innerHTML += '<br> This was a new search';
			if (text == "")
				text = lastPhraseSearched;
			keyCode = key; 	//save keyCode for later testing in fillTable()
			preFindObjects(text);
			if (debugBox) debugBox.innerHTML += '<br> preFindObjects called for ENTERKEY';
		}
		else if (objectsFound.length == 1 && (typeof allowAutoJump != 'undefined' && allowAutoJump() == true)) {
			// this was a new redundant 'search' with enter key pressed and only one object
			selectObject(1);
		}
		else {
			// this was a new redundant 'search' with enter key pressed
			showHighlight();
			if (debugBox) debugBox.innerHTML += '<br> This was a redundant search';
		}
	}

	else if (((key >= 48 && key <= 90) || (key >= 96 && key <= 111) ) ||
		key == 8 || key == 32 || key == 46 || key == 1) {
			//	 (if alphanumeric key entered or 
			//   backspace key pressed or
			//   spacebar pressed or 
			//   delete key pressed or
			//   mouse event)"
			if (!text.match(/\d/) || allowAutoListWithNumber) {
				// If there isn't a number in the search (force usage of enter key) and
				hideHighlight();
				if (text.length > 1) {
					clearPagingBars();
					if (debugBox) debugBox.innerHTML += '<br> setting preFindObjects timeout for other key: ' + key;
					keyCode = key;	//save keyCode for later testing in fillTable
					searchTimeout = setTimeout("preFindObjects(text)", delay);
					if (debugBox) debugBox.innerHTML += '<br> preFindObjects timeout called for other key: ' + key;
				}
			}
			if (event.type == "submit") {
				//infopath taskpane kludge to allow for no keyup and only onsubmit
				searchBoxChange(bodyElementId, obj, null, retired, 0);
			}
	}
	
	//value applied here because we use 'includeRetired' as 'lastRetired' as well
	// (this will still be called before the preFindObjects() timeout is called.)
	includeRetired = retired;
	if (!retired){ includeRetired = false; }
	
	return false;
}

function getSearchPhrase() {
	return lastPhraseSearched;
}

function preFindObjects(phrase) {
	if (debugBox) debugBox.innerHTML += '<br> preFindObjects initialized with search on: ' + phrase;
	clearTimeout(searchTimeout);			//stop any timeout that may have just occurred...fixes 'duplicate data' error
	objectsFound = new Array();				//zero-out numbered object list
	searchIndex = 0;						//our numbering is one-based, but the searchIndex is incremented prior to printing
	firstItemDisplayed = 1;					//zero-out our paging index (but we have a one-based list, see line above)
	lastPhraseSearched = text;
	
	if (debugBox) debugBox.innerHTML += '<br> findObjects being called';
	
	var b = findObjects(phrase);
	
	if (debugBox) debugBox.innerHTML += '<br> findObjects called';
	
	return b;
}

function selectObject(index) {
	var objectsReturned = new Array();
	if (objectsFound.length > 0) {
		objectsReturned.push(objectsFound[index-1]);
		//textbox.value = lastPhraseSearched;
		onSelect(objectsReturned);
	}
}

function showHighlight(box) {
	if (objectsFound.length > 0) {
		var elements = document.getElementsByTagName('TD')
		for(i=0; i <elements.length;i++)
		{
			if(elements[i].className == 'searchIndex')
				elements[i].className = 'searchIndexHighlight';
		}
		if (box != null)
			obj = box;
		else
			obj = textbox;
			
		if (obj != null) {
			obj.className = "searchHighlight";
			obj.focus();
		}
	}
}

function hideHighlight(box) {
	var elements = document.getElementsByTagName('TD')
	for(i=0; i <elements.length;i++)
	{
		if(elements[i].className == 'searchIndexHighlight')
			elements[i].className = 'searchIndex';
	}
	if (box != null)
		obj = box;
	else
		obj = textbox;
		
	if (obj != null) {
		obj.className = "";
	}
}

var noCell = function() {
	var td = document.createElement("td");
	td.style.display = "none";
	return td;
}

var getNumber = function(searchHit) {
		if (typeof searchHit == 'string') {
    		return "";
    	}
    	var td = document.createElement("td");
		td.className = "searchIndex";
		if (searchIndex >= objectsFound.length)
    		objectsFound.push(searchHit);
		searchIndex = searchIndex + 1;
		td.innerHTML = searchIndex + ". ";
		td.id = searchIndex;
		return td;
	};
var getString  = function(s) { return s; };
var getDateString = function(d) {
	var str = '';
	if (d != null) {
		var date = d.getDate();
		if (date < 10)
			str += "0";
		str += date;
		str += '-';
		var month = d.getMonth() + 1;
		if (month < 10)
			str += "0";
		str += month;
		str += '-';
		str += (d.getYear() + 1900);
	}
	return str;
}

var rowMouseOver = function() {
	if (this.className.indexOf("searchHighlight") == -1)
		this.className = "searchHighlight " + this.className;
}

var rowMouseOut = function() {
	var c = this.className;
	this.className = c.substring(c.indexOf(" ") + 1, c.length);
}

var rowCreator = function(row, i) {
	previousHit = objectsFound[searchIndex-1];
	
	var tr = document.createElement("tr");
	
	if (i % 2 == 0)
		tr.className = "evenRow";
	else
		tr.className = "oddRow";

	if (row != null && (row.voided == true || row.retired == true))
		tr.className += " voided";
	
	if (typeof row != "string") {
		tr.onclick= function() { selectObject(this.firstChild.id); };
		tr.onmouseover= rowMouseOver;
		tr.onmouseout = rowMouseOut;
	}
	
	return tr;
}

function fillTable(objects, cells) {
	// If we get only one result and the enter key was pressed jump to that object
	if (objects.length == 1 && 
		(keyCode == ENTERKEY || keyCode == null)) {
			if (typeof objects[0] == 'string') {
			// if only one string item returned, its a message
				hideHighlight();
			}
			else if (typeof allowAutoJump == 'undefined' || allowAutoJump() == true){
				objectsFound.push(objects[0]);
				selectObject(1);
				return;
			}
	}
	
    allObjectsFound = objects;
	
	updatePagingNumbers();
	
	// signal to the using script that we've cleared the rows
	if (typeof onRemoveAllRows != "undefined")
		onRemoveAllRows(objectHitsTableBody);
    DWRUtil.removeAllRows(objectHitsTableBody);	//clear out the current rows
	
    var objs = objects.slice(firstItemDisplayed - 1, (firstItemDisplayed - 1) + numItemsDisplayed);
    
    var funcs = new Array();
    if (cells != null)
    	funcs = cells;
    else if (typeof customCellFunctions == "undefined")
    	funcs = [ getNumber, getCellContent ];
    else
    	funcs = customCellFunctions;
    	
    if (typeof customRowOptions == "undefined")
    	customRowOptions = {'rowCreator':rowCreator};
    
    DWRUtil.addRows(objectHitsTableBody, objs, funcs, customRowOptions);
    
   	setTimeout("updatePagingBars()", 0);
    
    if (keyCode == ENTERKEY) {
    	// showHighlighting must be called here to assure it occurs after 
    	// objects are returned. Must be called with Timeout because 
    	// DWRUtil.addRows uses setTimeout
    	if (debugBox) debugBox.innerHTML += "<br>showing highlight at end of fillTable() due to enterkey";
    	setTimeout("showHighlight()", 0);
    }
    if (debugBox) debugBox.innerHTML += "<br>ending fillTable().  Keycode was: " + keyCode;
    
    if (typeof postFillTable == 'function')
    	postFillTable(objectsFound);
}

function showPrevious() {
	firstItemDisplayed = (firstItemDisplayed - numItemsDisplayed);
	if (firstItemDisplayed < 1) {
		firstItemDisplayed = 1;
		return false;
	}
	searchIndex = firstItemDisplayed - 1;
	fillTable(allObjectsFound);
	//if we're in 'number mode'
	if (textbox.value == "") 
		showHighlight();
	return false;
}

function showNext() {
	firstItemDisplayed = firstItemDisplayed + numItemsDisplayed;
	if (firstItemDisplayed > allObjectsFound.length) {
		firstItemDisplayed = allObjectsFound.length;
		return false;
	}
	searchIndex = firstItemDisplayed - 1;
	fillTable(allObjectsFound);
	//if we're in 'number mode'
	if (textbox.value == "") 
		showHighlight();
	return false;
}

function updatePagingNumbers() {

	//create/find information bars
	var infoBar = document.getElementById("searchInfoBar");
	var pagingBar = document.getElementById("searchPagingBar");
	if (infoBar == null) {
		infoBar = document.createElement('div');
		infoBar.id = "searchInfoBar";
		infoBar.innerHTML = "&nbsp;";
		var table = objectHitsTableBody.parentNode;
		table.parentNode.insertBefore(infoBar, table);
	}
	if (pagingBar == null) {
		pagingBar = document.createElement('div');
		pagingBar.id = "searchPagingBar";
		pagingBar.innerHTML = "&nbsp;";
		var table = objectHitsTableBody.parentNode;
		if (table.nextSibling == null)
			table.parentNode.appendChild(pagingBar);
		else
			table.parentNode.insertBefore(pagingBar, table.nextSibling);
	}

	// get top position of body element
	var tbody = objectHitsTableBody;
	var top = getElementTop(tbody);
	
	var height = getRowHeight(); //approx. row height
	
	// get approx room below tablebody
	var remainder = getWindowHeight() - parseInt(top);
	
	if (debugBox != null) debugBox.innerHTML += "<br>rowHeight(): " + height;
	if (debugBox != null) debugBox.innerHTML += "<br>top: " + top;
	
	//numItemsDisplayed=Math.floor(remainder/(height + 6))-2;
	//make this work in full page and popup mode
	//numItemsDisplayed=Math.floor(remainder/(height + 6))-6;
	//reasonable compromise for this to work in mini div popups
	numItemsDisplayed=Math.floor(remainder/(height + 6))-4;
	//must always show at least 1 item
	if (numItemsDisplayed <= 0) 
		numItemsDisplayed = 1;
	//also round (down) to the nearest 5
	if (numItemsDisplayed > 5) {
		var idealNumItemsDisplayed = numItemsDisplayed;
		numItemsDisplayed = Math.round(numItemsDisplayed / 5) * 5;
		if (numItemsDisplayed > idealNumItemsDisplayed) numItemsDisplayed += -5;
	}
	
	// if last object would be the only item on the 'next page', add it back in
	if (numItemsDisplayed + 1 == allObjectsFound.length) {
		numItemsDisplayed = numItemsDisplayed + 1;
	}
}

function updatePagingBars() {
	
	//TODO optional: create another dwr method to just get total # of hits
	//     so that we don't need to return all 200 hits and only show #31-#45
	
	var infoBar = document.getElementById("searchInfoBar");
	var pagingBar = document.getElementById("searchPagingBar");
	
	var total = allObjectsFound.length;
	
	// if the last object is a string (eg a link to Add New Patient), correct list size
	if (typeof(allObjectsFound[total-1]) == "string")
		total = total - 1;
	
	var lastItemDisplayed = (firstItemDisplayed + numItemsDisplayed) - 1;
	
	// if its a shortened page
	if (lastItemDisplayed > total) {
		lastItemDisplayed = total;
	}
	
	// there may be strings mixed in the list, correct the list size here
	if (lastItemDisplayed != objectsFound.length) {
		total = total - (lastItemDisplayed - objectsFound.length);
		lastItemDisplayed = objectsFound.length;
	}
	
	if (lastPhraseSearched != null)
		infoBar.innerHTML = ' &nbsp; Results for "' + lastPhraseSearched + '". &nbsp;';
	else
		infoBar.innerHTML = '';
	
	if (objectsFound.length > 0)
		infoBar.innerHTML += " Viewing <b>" + firstItemDisplayed + "-" + lastItemDisplayed + "</b> of <b>" + total + "</b> &nbsp; ";
	
	pagingBar.innerHTML = "&nbsp;";

	if (lastItemDisplayed != total || firstItemDisplayed > 1) {
		// if need to show previous or next links	
		var prev;
		if (firstItemDisplayed > 1) {
			//create previous link
			prev = document.createElement("a");
			prev.href = "#prev";
			prev.className = "prevItems";
			prev.innerHTML = "Previous " + (firstItemDisplayed-numItemsDisplayed < 1 ? firstItemDisplayed - 1: numItemsDisplayed) + " Result(s)";
			prev.onclick = showPrevious;
		}
		else {
			// create previous text node
			prev = document.createTextNode("Previous Results");
		}
		
		pagingBar.appendChild(prev);
		var s = document.createElement("span");
		s.innerHTML = " | ";
			
		pagingBar.appendChild(s);
	
		var next;
		if (lastItemDisplayed < total) {
			//create next link
			next = document.createElement("a");
			next.href = "#next";
			next.className = "nextItems";
			next.innerHTML = "Next " + (lastItemDisplayed+numItemsDisplayed > total ? total - lastItemDisplayed: numItemsDisplayed ) + " Results";
			next.onclick = showNext;
		}
		else {
			next = document.createTextNode("Next Results");
		}
		
		pagingBar.appendChild(next);
	}
}

function clearPagingBars() {
	var infoBar = document.getElementById("searchInfoBar");
	var pagingBar = document.getElementById("searchPagingBar");
	if (infoBar != null) {
		infoBar.innerHTML = "&nbsp;";
	}
	if (pagingBar != null) {
		pagingBar.innerHTML = "&nbsp;";
	}
}

function getStyle(obj,styleProp) {
	if (obj != null) {
		if (obj.currentStyle) {
			var y = obj.currentStyle[styleProp];
		}
		else if (window.getComputedStyle) {
			var y = document.defaultView.getComputedStyle(obj,null).getPropertyValue(styleProp);
		}
		return y;
	}
	return '';
}

function hotkey(event) {
	if (!event) var event = window.event;
	var k = event.keyCode;
	if (event.ctrlKey == true) { //if CONTROL-*
		if (k == 33) { //PAGE UP
			showPrevious();
		}
		else if (k == 34) { // PAGE DOWN
			showNext();
		}
		else if (k == 37) { //LEFT key
			showPrevious();
		}
		else if (k == 39) { //RIGHT key
			showNext();
		}
	}
}

function getWindowHeight() {
  var myWidth = 0, myHeight = 0;
  if( typeof( window.innerWidth ) == 'number' ) {
    //Non-IE
    myWidth = window.innerWidth;
    myHeight = window.innerHeight;
    //alert("myHeight1: " + myHeight);
  } else if( document.documentElement &&
      ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
    //IE 6+ in 'standards compliant mode'
    myWidth = document.documentElement.clientWidth;
    myHeight = document.documentElement.clientHeight;
	//alert("myHeight2: " + myHeight);
  } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
    //IE 4 compatible
    myWidth = document.body.clientWidth;
    myHeight = document.body.clientHeight;
    //alert("myHeight3 : " + myHeight);
  }

  return parseInt(myHeight);
}

function getRowHeight() {
	var h = 0;
	h = getStyle(textbox, 'height');
	if (h == '')
		h = 13;
	else if (h == 'auto') {
		// this silly code is brought to you courtesy of the ever-standards-compliant IE web browser designers
		h = getStyle(textbox, 'lineHeight');
		if (h == 'largest')
			h = 17;
		else if (h == 'smallest')
			h = 10;
		else
			h = 13; //normal
	}
	else {
		h = parseInt(h.slice(0, h.length - 2)) - 4; //remove 'px' from height
	}

	if (typeof customGetRowHeight != 'undefined') h = customGetRowHeight(h);
	
	return h;
	
}

function exitNumberMode(txtbox) {
	hideHighlight(txtbox);
	if (txtbox.value == "")
		txtbox.value = lastPhraseSearched;
}

function setPosition(btn, form, formWidth, formHeight) {
	var left = parseInt(getElementLeft(btn) + btn.offsetWidth + 20);
	var top  = parseInt(getElementTop(btn)-50);
	
	if (formWidth == null)
		formWidth = getDimension(form.style.width);
	if (formHeight == null)
		formHeight = getDimension(form.style.height);
	
	var windowWidth = parseInt(window.innerWidth + getScrollOffsetX());
	var windowHeight = parseInt(window.innerHeight + getScrollOffsetY());
	
	// if the box is popping off the right/bottom, move it back 
	//  onto the screen
	if (left + formWidth > windowWidth)
		left = windowWidth - formWidth - 10;
	if (top + formHeight > windowHeight)
		top = windowHeight - formHeight - 10;
		
	// keep the box on the screen and off the edge
	if (left < 2) left = 2;
	if (top < 2) top = 2;
	
	form.style.left = left + "px";
	form.style.top = top + "px";
}

function getDimension(style) {
	var s = "0";
	if (style.indexOf("px") == -1)
		s = style;
	else
		s = style.substring(0, style.indexOf("px"));
		
	return parseInt(s);
}

function getElementLeft(elm) {
	var x = 0;
	while (elm != null) {
		x+= elm.offsetLeft;
		elm = elm.offsetParent;
	}
	return parseInt(x);
}

function getElementTop(elm) {
	var y = 0;
	while (elm != null) {
		y+= elm.offsetTop;
		elm = elm.offsetParent;
	}
	return parseInt(y);
}

function getScrollOffsetY() {
	if (window.innerHeight) {
		return parseInt(window.pageYOffset);
	}
	else {
		return parseInt(document.documentElement.scrollTop);
	}
}

function getScrollOffsetX() {
	if (window.innerWidth) {
		return window.pageXOffset;
	}
	else {
		return document.documentElement.scrollLeft;
	}
}

resetForm();