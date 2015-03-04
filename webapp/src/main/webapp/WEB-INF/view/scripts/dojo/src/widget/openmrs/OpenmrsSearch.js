/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.OpenmrsSearch");

dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.style");
dojo.require("dojo.event.*");

importJavascriptFile(openmrsContextPath + "/dwr/engine.js");
importJavascriptFile(openmrsContextPath + "/dwr/util.js");

// Add parse handler.  
dojo.widget.tags.addParseTreeHandler("dojo:OpenmrsSearch");

dojo.widget.openmrs.OpenmrsSearch = function() {
	//dojo.widget.HtmlWidget.call(this);

	this.eventNames = {};

}

dojo.widget.defineWidget(
	"dojo.widget.openmrs.OpenmrsSearch",
	dojo.widget.HtmlWidget,
	{
	widgetType: "OpenmrsSearch",
	searchTimeout: null,
	searchDelay: 400,
	searchIndex: 0,
	objectsFound: new Array(),
	allObjectsFound: new Array(),
	objHitsTableBody: null,
	text: null,
	lastPhraseSearched: "",
	numItemsDisplayed: 0,
	firstItemDisplayed: 1,
	previousHit: null,
	event: null,
	pagingBar: null,
	infoBar: null,
	minSearchCharacters: omsgs.minSearchCharactersGP, /* gp retrieved from openmrsmessages.js */
	tableHeight: 0,
	headerRow: null,
	showHeaderRow: false,
	allowAutoList: true,
	useOnKeyDown: false,
	isInNumberMode: false, /* quick way to tell if the user has hit enter and yellow is showing telling them to choose a numeral */
	
	// check box options
	showIncludeRetired: false,
	includeRetired: null,
	includeRetiredLabel: omsgs.includeRetired, /* var retrieved from openmrsmessages.js */
	showIncludeVoided: false,
	includeVoided: null,
	includeVoidedLabel: omsgs.includeVoided, /* var retrieved from openmrsmessages.js */
	showVerboseListing: false,
	verboseListing: null,
	verboseListingLabel: omsgs.includeVerbose, /* var retrieved from openmrsmessages.js */
	
	eventNamesDefault: {
		select : "select",
		findObjects: "findObjects",
		objectsFound: "objectsFound",
		fillTable: "fillTable",
		destroy : "destroy",
		inputChange : "inputChange"
	},
	
	inputNode: null,
	inputWidth: "25em",
	inputId: "",
	inputName: "",
	
	searchLabel: "",
	searchLabelNode: null,
	
	
	initialize: function() {
	
		dojo.debug("initializing openmrssearch");
		
		for(name in this.eventNamesDefault) {
			if (dojo.lang.isUndefined(this.eventNames[name])) {
				this.eventNames[name] = this.widgetId+"/"+this.eventNamesDefault[name];
			}
		}
		
		// all search pages should use the loading message
		useLoadingMessage();
		
		// add search label
		if (this.searchLabel)
			this.searchLabelNode.innerHTML = this.searchLabel;
	},
		
	fillInTemplate: function(args, frag){
			var source = this.getFragNodeRef(frag);
			
			// set input's id
			if(!this.inputId){ this.inputId = this.widgetId; }
			this.inputNode.id = this.inputId;
			
			if(this.inputName){ this.inputNode.name = this.inputName; }
			this.inputNode.style.width = this.inputWidth;
			
			if (this.showIncludeRetired) {
				var lbl = document.createElement("label");
				lbl.innerHTML = this.includeRetiredLabel;				
				this.includeRetired.style.display = "";
				this.includeRetired.id = lbl.htmlFor = this.widgetId + "retired";
				this.includeRetired.parentNode.insertBefore(lbl, this.includeRetired);
			}
			if (this.showIncludeVoided) {
				var lbl = document.createElement("label");
				lbl.innerHTML = this.includeVoidedLabel;				
				this.includeVoided.style.display = "";
				this.includeVoided.id = lbl.htmlFor = this.widgetId + "voided";
				this.includeVoided.parentNode.insertBefore(lbl, this.includeVoided);
			}
			if (this.showVerboseListing) {
				var lbl = document.createElement("label");
				lbl.innerHTML = this.verboseListingLabel;				
				this.verboseListing.style.display = "";
				this.verboseListing.id = lbl.htmlFor = this.widgetId + "verbose";
				this.verboseListing.parentNode.insertBefore(lbl, this.verboseListing);
			}
			
			// create header row from defined column names
			this.setHeaderCellContent(this.getHeaderCellContent());
			this.hideHeaderRow();
			
			
			if (this.useOnKeyDown)
				dojo.event.connect(this.inputNode, "onkeydown", this, "onInputChange");
			else
				dojo.event.connect("before", this.inputNode, "onkeyup", this, "onInputChange");
			
			dojo.event.connect("before", this.inputNode, "onkeypress", function(evt) {
				if (evt.keyCode == dojo.event.browser.keys.KEY_ENTER)
					dojo.event.browser.stopEvent(evt);
			});
			
			dojo.event.connect(this.includeRetired, "onclick", this, "onCheckboxClick");
			dojo.event.connect(this.includeVoided, "onclick", this, "onCheckboxClick");
			dojo.event.connect(this.verboseListing, "onclick", this, "onCheckboxClick");
	},

	templateString: '<span><span style="white-space: nowrap"><span dojoAttachPoint="searchLabelNode"></span> <input type="text" value="" dojoAttachPoint="inputNode" autocomplete="off" /> <input type="checkbox" style="display: none" dojoAttachPoint="includeRetired"/> <input type="checkbox" style="display: none" dojoAttachPoint="includeVoided"/> <input type="checkbox" style="display: none" dojoAttachPoint="verboseListing"/></span><span class="openmrsSearchDiv"><table class="openmrsSearchTable" cellpadding="2" cellspacing="0" style="width: 100%"><thead><tr dojoAttachPoint="headerRow"></tr></thead><tbody dojoAttachPoint="objHitsTableBody" style="vertical-align: top"><tr><td class="searchIndex"></td><td></td></tr></tbody></table></span></span>',
	templateCssPath: "",

	setHeaderCellContent: function(arr) {
		if (this.showHeaderRow && arr) {
			while (this.headerRow.hasChildNodes())
				this.headerRow.removeChild(this.headerRow.firstChild);
			for( var i=0; i < arr.length; i++) {
				var td = document.createElement("th");
				td.innerHTML = arr[i];
				this.headerRow.appendChild(td);
			}
		}
	},

	search: function(evt, setupOnly) {
		this.text = this.inputNode.value.toString();
		if (this.text) {
			this.text = this.text.replace(/^\s+/, '');
			this.text = this.text.replace(/\s+$/, '');
		}
		
		this.event = dojo.event.browser.fixEvent(evt); //save event for later testing in fillTable
		
		if (setupOnly != true)
			this._enterKeyPressed();
	},


	onInputChange: function(evt) {
		this.search(evt, true);
		
		this.key = 0;
		
		// don't fire for things like alt-tab, ctrl-c -- but DO fire for cntrl-v
		if (!this.event.altKey && (!this.event.ctrlKey || this.key == 'v')) {
			this.key = this.event.keyCode;
			dojo.debug('event.type : ' + this.event.type);
			if ((this.key==0 || this.key==null) && (this.event.type == "click" || this.event.type == "change" || this.event.type == "submit"))
				//if non-key event like clicking checkbox or changing dropdown list
				this.key = 1;
		}
		
		// infopath hack since it doesn't let us use onkeyup or onkeypress	
		if (this.useOnKeyDown == true) {
			// only add if the key is a letter and no modifier key was pressed
			if ((this.isAlphaNumeric(this.key) || this.isDash(this.key)) && !this.event.altKey && !this.event.ctrlKey) {
				var newKey = String.fromCharCode(this.key).toLowerCase();
				if (this.isDash(this.key)) 
					newKey = "-"; // if key is a dash, make char a dash
				else if (this.text.length > 0) {
					// IE interprets all char codes as upper case.  
					// Only leave in uppercase if the previous char is uppercase (hack #2)
					var lastKey = this.text.substring(this.text.length - 1, this.text.length);
					if (lastKey >= 'A' && lastKey <= 'Z')
						newKey = newKey.toUpperCase();
				}
				
				this.text = this.text + newKey;
				//this.lastPhraseSearched = this.text;
			}
			if (this.key == 8 && this.text.length > 1) { //backspace
				this.text = this.text.substring(0, this.text.length - 1);
			}
		}
		
		if (this.key == dojo.event.browser.keys["KEY_ESCAPE"]) {
			this.exitNumberMode();
			return false;
		}
		//else if (text == "" && includeRetired == retired) {
			//searched on empty string (and didn't change retired status)
			//return false;
		//}
		else if (this.key == dojo.event.browser.keys.KEY_ENTER) {
			this._enterKeyPressed();
		}
		
		else if (this.allowAutoList) {
		
			if (this.isAlphaNumeric(this.key) ||
				this.key == dojo.event.browser.keys.KEY_BACKSPACE || this.key == dojo.event.browser.keys.KEY_SPACE || 
				this.isDash(this.key) ||
				this.key == dojo.event.browser.keys.KEY_DELETE || this.key == 1) {
					
					clearTimeout(this.searchTimeout);
				
					//	 (if alphanumeric key entered or 
					//   backspace key pressed or
					//   spacebar pressed or 
					//   delete key pressed or
					//   hyphen key pressed or
					//   mouse event)"
					if (!this.text.match(/\d/) || this.allowAutoListWithNumber()) {
						// force enter key for strings like 1,2,5,11
						if (this.text.match(/^\s*\d+\s*(,\d+\s*)+$/)) {
							return false;
						}
						
						// If there isn't a number in the search, reset the colors
						if (!this.text.match(/\d/)) {
							this.hideHighlight();
						}
						
						if (this.text != null && this.text.length == 0 && this.key == dojo.event.browser.keys.KEY_BACKSPACE) {
							// allows for "resetting" default list when user hits backspace on empty field
							this.resetSearch();
							this.searchCleared();
						}
						else if (this.text.length >= this.minSearchCharacters && this.isInNumberMode == false ) {
							this.clearPagingBars();
							dojo.debug('setting preFindObjects timeout for other key: ' + this.key);
							var callback = function(ts, text) { return function() {ts.findObjects(text)}};
							this.searchTimeout = setTimeout(callback(this, this.text), this.searchDelay);
						}
					}
					if (this.event.type == "submit") {
						//infopath taskpane kludge to allow for no keyup and only onsubmit
						this.onInputChange(null);
						return false;
					}
			}
		}
		
		return false;
	},
	
	
	_enterKeyPressed: function(mouseClicked) {
		clearTimeout(this.searchTimeout);
		
		dojo.debug('Enter key pressed1');
		// user hit enter on empty box
		
		try {
			// if the mouse wasn't clicked and our search box is empty, then
			// they just hit enter on an empty field, return
			if (!mouseclicked && this.inputNode.value == "" &&
				this.event && this.event.type == "keyup")
					return false;
		} catch (Exception) {
			// catching error when calling this.event.type on mouse click
		}
		
		dojo.debug('Enter key pressed, search: ' + this.text);
		dojo.debug('lastPhraseSearched: ' + this.lastPhraseSearched);
		this.hideHighlight();
		
		// get out of here if the text is empty. nothing to do
		if (this.text == null)
			return false;
			
		// if the user hit the enter key then check for sequence of numbers
		if (this.text.match(/^\s*\d+\s*(,\d+\s*)*$/)) {
			dojo.debug('text matched set of numbers');
			var textWords = this.text.split(/\s*,\s*/);
			var objectsReturned = new Array();
			for (i=0; i<textWords.length; i++) {
				if (textWords[i] > 0 && textWords[i] <= this.objectsFound.length)
					objectsReturned.push(this.objectsFound[textWords[i]-1]);
				else if (textWords.length != 1) {
					//if only one number had been entered, assumed searching on object id or 
					//  just a number and its not an error
					alert("Invalid choice: '" + textWords[i] + "'");
					return false;
				}
			}
			if (objectsReturned.length > 0) {
				this.select({objs: objectsReturned});
				if (this.lastPhraseSearched == null)
					this.inputNode.value = "";
				else
					this.inputNode.value = this.lastPhraseSearched; //save the search (for the back button)
				return false;
			}
		}
		this.inputNode.focus();
		if (!mouseClicked)
			this.inputNode.value = "";
		dojo.debug('this.inputNode.value cleared');
		
		//alert("text: " + this.text + " lastPhrase: " + this.lastPhraseSearched);
		if (this.allowNewSearch() && (this.text != this.lastPhraseSearched || mouseClicked)) {
			
			//this was a new search with the enter key pressed, call findObjects function 
			dojo.debug('This was a new search');
			if (this.text == null || this.text == "")
				this.text = this.lastPhraseSearched;
			this.findObjects(this.text);
			this.showHighlight();
			dojo.debug('findObjects called for enter key');
		}
		else if (this.objectsFound.length == 1 && this.allowAutoJump()) {
			// this was a new redundant 'search' with enter key pressed and only one object
			dojo.debug('This was a redundant search and auto jumping to single object returned');
			this.selectObject(1);
		}
		else {
			// this was a new redundant 'search' with enter key pressed
			dojo.debug('This was a redundant search');
			this.showHighlight();
		}
		
	},
	
	
	onCheckboxClick:function(event) {
		if (this.text == null && this.lastPhraseSearched == null) return;
		
		//reset textbox for mouse events
		if (this.text == "" && this.lastPhraseSearched != null)
			this.text = this.lastPhraseSearched;
		
		dojo.debug("'pressing' entry key");
		
		this._enterKeyPressed(/* mouse clicked = */ true);
	},

	
	getPhraseSearched: function() {
		return this.lastPhraseSearched;
	},


	findObjects: function(phrase) {	
		dojo.debug('findObjects initialized with search on: ' + phrase);		
		//must have at least x characters entered or that character be a number
		if (phrase.length >= this.minSearchCharacters || (parseInt(phrase) >= 0 && parseInt(phrase) <= 99)) {
			this.resetSearch(phrase);
			dojo.event.topic.publish(this.eventNames.findObjects, phrase);
			dojo.debug("Calling doFindObjects with " + phrase);
			this.doFindObjects(phrase);
		}
		else {
			this.objectsFound = new Array();
			this.objectsFound.push(omsgs.minCharactersPhrase1 + this.minSearchCharacters + omsgs.minCharactersPhrase2);
			this.doObjectsFound(this.objectsFound);
		}
		
		return false;
	},
		
	doFindObjects: function(phrase) {
		
		// override this method to make the necessary DWR calls 
		
		// e.g.:
		// DWREncounterService.findEncounters(text, this.includeVoided.checked, this.simpleClosure(this, "doObjectsFound"));
			
	},
	
	/* the most recently returned call count */
	lastCallCount: 0,
	
	/* The most recently sent call count */
	callCount: 1,
	
	doObjectsFound: function(objs, unusedData, extraData, currentCallCount) {
		/* optionally passed in from the doFindObjects method */
		if (currentCallCount) {
			if (this.lastCallCount > currentCallCount) {
				/* There has been a callback to this method after the call for these
				   results was made.  Ignore this request. */
				/* alert("Ignoring: " + objs.length + " results"); */
				return;
			}
			this.lastCallCount = currentCallCount;
		}
	
		// convert objs from single obj into array (if needed)
		if (objs.length == null)
			objs = [objs]
		
		dojo.event.topic.publish(this.eventNames.objectsFound, {"objs": objs});
		
		if (this.showHeaderRow == false || 
			objs.length == 0 || 
			(objs.length == 1 && (typeof objs[0] == "string"))) {
				this.hideHeaderRow();
		}
		else {
			this.displayHeaderRow();
		}
		
	    dojo.debug("in doObjectsFound");
		this.fillTable(objs);
	},
	
	
	selectObject: function(index) {
		if (this.objectsFound.length >= index - 1) {
			//textbox.value = lastPhraseSearched;
			this.select({obj: this.objectsFound[index-1]});
		}
	},


	showHighlight: function() {
		if (this.objectsFound.length > 0) {
			var elements = this.objHitsTableBody.getElementsByTagName('TD')
			for(i=0; i <elements.length;i++) {
				if(elements[i].className == 'searchIndex')
					elements[i].className = 'searchIndexHighlight';
			}
			
			this.inputNode.className = "searchHighlight";
			this.inputNode.focus();
		}
		this.isInNumberMode = true;
	},


	hideHighlight: function() {
		var elements = this.objHitsTableBody.getElementsByTagName('TD')
		for(i=0; i <elements.length;i++)
		{
			if(elements[i].className == 'searchIndexHighlight')
				elements[i].className = 'searchIndex';
		}

		this.inputNode.className = "";
		this.isInNumberMode = false;
	},


	noCell: function() {
		var td = document.createElement("td");
		td.style.display = "none";
		return td;
	},
		
	getNumber: function(searchHit) {
		if (typeof searchHit == 'string') return "";
    	
    	var td = document.createElement("td");
		td.className = "searchIndex";
		if (this.searchIndex >= this.objectsFound.length)
    		this.objectsFound.push(searchHit);
		this.searchIndex = this.searchIndex + 1;
		td.innerHTML = this.searchIndex + ". ";
		td.id = this.searchIndex;
		return td;
	},
	
	getString:		function(s)	{ return s;  },
	
	getCellContent:	function()	{ return ''; },
	
	getDateString: function(d) {
		var str = '';
		if (d != null) {
			
			// get the month, day, year values
			var month = "";
			var day = "";
			var year = "";
			var date = d.getDate();
			
			if (date < 10)
				day += "0";
			day += date;
			var m = d.getMonth() + 1;
			if (m < 10)
				month += "0";
			month += m;
			if (d.getYear() < 1900)
				year = (d.getYear() + 1900);
			else
				year = d.getYear();
		
			/* omsgs.datePattern from openmrsmessages.js */
			var sep = omsgs.datePattern.substr(2,1);
			var datePatternStart = omsgs.datePattern.substr(0,1).toLowerCase();
			
			if (datePatternStart == 'm') /* M-D-Y */
				str = month + sep + day + sep + year
			else if (datePatternStart == 'y') /* Y-M-D */
				str = year + sep + month + sep + day
			else /* (datePatternStart == 'd') D-M-Y */
				str = day + sep + month + sep + year
			
		}
		return str;
	},
	
	rowMouseOver: function() {
		var tr = this;
		if (tr.className.indexOf("searchHighlight") == -1)
			tr.className = "searchHighlight " + tr.className;
	},
	
	
	rowMouseOut: function() {
		var tr = this;
		var c = tr.className;
		tr.className = c.substring(c.indexOf(" ") + 1, c.length);
	},
	
	
	rowCreator: function(options) {
		var previousHit = this.objectsFound[this.searchIndex-1];
		
		var tr = document.createElement("tr");
		
		var row = options.rowData;
		var i = options.rowNum;
		
		if (i % 2)
			tr.className = "oddRow";
		else
			tr.className = "evenRow";
	
		if (row != null && (row.voided == true || row.retired == true))
			tr.className += " voided";
		
		if (typeof row != "string") {
			var callback = function(ts) { return function(obj) {ts.selectObject(this.firstChild.id);}}; //a javascript closure
			tr.onclick= callback(this);
			tr.onmouseover= this.rowMouseOver;
			tr.onmouseout = this.rowMouseOut;
		}
		
		return tr;
	},
	
	cellCreator: function(options) {
		if (dwr.util._isHTMLElement(options.data, "td") == true)
			return options.data;
		
		return document.createElement("td");
	},


	fillTable: function(objects, cells) {
	    if (objects.length > 1 || typeof objects[0] != 'string')
			dojo.event.topic.publish(this.eventNames.fillTable, {"objects": objects} );
		
	    // If we get only one result and the enter key was pressed jump to that object
		if (objects.length == 1 && ((this.event && this.key == dojo.event.browser.keys.KEY_ENTER) || 
			(this.event == null && !(this.key)))) { 
				//alert("type: " (typeof objects[0]));
				if (typeof objects[0] == 'string') {
				// if only one string item returned, it's a message
					this.hideHighlight();
				}
				else if (this.allowAutoJump()){
					this.objectsFound.push(objects[0]);
					this.selectObject(1);
					return;
				}
		}
		
	    this.allObjectsFound = objects;
		
		this.updatePagingNumbers();
		
        // signal to the using script that we've cleared the rows
		this.onRemoveAllRows(this.objHitsTableBody);
	    dwr.util.removeAllRows(this.objHitsTableBody);	//clear out the current rows
		
	    var objs = objects.slice(this.firstItemDisplayed - 1, (this.firstItemDisplayed - 1) + this.numItemsDisplayed);
	    
        dwr.util.addRows(this.objHitsTableBody, objs, this.getCellFunctions(), this.getRowOptions());
		
		setTimeout(this.simpleClosure(this, "updatePagingBars"), 0);
	    
        if (this.event && this.key == dojo.event.browser.keys.KEY_ENTER) {
	    	// showHighlighting must be called here to assure it occurs after 
	    	// objects are returned. Must be called with Timeout because 
	    	// DWRUtil.addRows uses setTimeout
	    	dojo.debug("showing highlight at end of fillTable() due to enterkey");
	    	setTimeout(this.simpleClosure(this, "showHighlight"), 0);
	    }
	    if (this.event)
		    dojo.debug("ending fillTable(). Keycode was: " + this.key);
	    
        this.postFillTable();
	},
	
	
	getRowOptions: function() {
		var arr = { 'rowCreator': this.simpleClosure(this, "rowCreator"),
					'cellCreator': this.simpleClosure(this, "cellCreator"),
					'escapeHtml': false
					};
		return arr;
	},
	
	
	getCellFunctions: function() {
		return [ this.simpleClosure(this, "getNumber"), this.simpleClosure(this, "getCellContent") ];
	},
	
	
	displayHeaderRow: function() {
		this.headerRow.style.display="";
	},
	
	
	hideHeaderRow: function() {
		this.headerRow.style.display="none";
	},
	
	
	getHeaderCellContent: function() {
		return null;
		// return ['Number', 'Cell Content'];
	},
	
	
	clearSearch: function() {
		this.resetSearch();
		this.searchCleared();
		this.clearPagingBars();
		this.hideHeaderRow();
		// signal to the using script that we've cleared the rows
		this.onRemoveAllRows(this.objHitsTableBody);
	    dwr.util.removeAllRows(this.objHitsTableBody);	//clear out the current rows
		this.inputNode.value = this.text = this.lastPhraseSearch = "";
	},

	onRemoveAllRows: function() { },

	postFillTable: function() {	},
	
	showPrevious: function() {
		this.firstItemDisplayed = (this.firstItemDisplayed - this.numItemsDisplayed);
		if (this.firstItemDisplayed < 1) {
			this.firstItemDisplayed = 1;
			return false;
		}
		this.searchIndex = this.firstItemDisplayed - 1;
		this.fillTable(this.allObjectsFound);
		//if we're in 'number mode'
		if (this.inputNode.value == "") 
			this.showHighlight();
			
		return false;
	},
	
	
	showNext: function() {
		this.firstItemDisplayed = this.firstItemDisplayed + this.numItemsDisplayed;
		if (this.firstItemDisplayed > this.allObjectsFound.length) {
			this.firstItemDisplayed = this.allObjectsFound.length;
			return false;
		}
		this.searchIndex = this.firstItemDisplayed - 1;
		this.fillTable(this.allObjectsFound);
		//if we're in 'number mode'
		if (this.inputNode.value == "") 
			this.showHighlight();
		
		return false;
	},
	

	updatePagingNumbers: function() {
	
		//create information bars if they don't exist
		if (this.infoBar == null) {
			this.infoBar = document.createElement('div');
			this.infoBar.id = "searchInfoBar";
			this.infoBar.innerHTML = "&nbsp;";
			var table = this.objHitsTableBody.parentNode;
			table.parentNode.insertBefore(this.infoBar, table);
		}
		if (this.pagingBar == null) {
			this.pagingBar = document.createElement('div');
			this.pagingBar.id = "searchPagingBar";
			this.pagingBar.innerHTML = "&nbsp;";
			var table = this.objHitsTableBody.parentNode;
			if (table.nextSibling == null)
				table.parentNode.appendChild(this.pagingBar);
			else
				table.parentNode.insertBefore(this.pagingBar, table.nextSibling);
		}
		
		this.numItemsDisplayed = 20;
		
		//If the number of objects is less than the number being displayed, set the number to be displayed to the number of objects
		if (this.numItemsDisplayed > this.allObjectsFound.length){
			this.numItemsDisplayed = this.allObjectsFound.length;
		}
		
		// if last object would be the only item on the 'next page', add it back in
		if (this.numItemsDisplayed + 1 == this.allObjectsFound.length) {
			this.numItemsDisplayed = this.numItemsDisplayed + 1;
		}
		
		dojo.debug("end update paging");
	},
	
	updatePagingBars: function() {
	
		//TODO optional: create another dwr method to just get total # of hits
		//     so that we don't need to return all 200 hits and only show #31-#45
		
		var total = this.allObjectsFound.length;
		
		// if the last object is a string (eg a link to Add New Patient), correct list size
		if (typeof(this.allObjectsFound[total-1]) == "string")
			total = total - 1;
		
		var lastItemDisplayed = (this.firstItemDisplayed + this.numItemsDisplayed) - 1;
		
		// if its a shortened page
		if (lastItemDisplayed > total)
			lastItemDisplayed = total;
			
		// there may be strings mixed in the list, correct the list size here
		if (lastItemDisplayed > this.objectsFound.length) {
			total = total - (lastItemDisplayed - this.objectsFound.length);
			lastItemDisplayed = this.objectsFound.length;
		}
		
		this.infoBar.innerHTML = '';
		if (this.lastPhraseSearched != null)
			this.infoBar.innerHTML = ' &nbsp; ' + omsgs.resultsFor + ' "' + this.lastPhraseSearched + '". &nbsp;';
		
		if (this.objectsFound.length > 0)
			this.infoBar.innerHTML += omsgs.searchResultsViewing + " <b>" + this.firstItemDisplayed + "-" + lastItemDisplayed + "</b> " + omsgs.searchResultsOf + " <b>" + total + "</b> &nbsp; ";
		
		this.pagingBar.innerHTML = "&nbsp;";
	
		if (lastItemDisplayed != total || this.firstItemDisplayed > 1) {
			// if need to show previous or next links	
			var prev = document.createTextNode("Previous Results"); // default previous text node
			if (this.firstItemDisplayed > 1) {
				//create previous link
				prev = document.createElement("a");
				prev.href = "#prev";
				prev.className = "prevItems";
				prev.innerHTML = "Previous " + (this.firstItemDisplayed-this.numItemsDisplayed < 1 ? this.firstItemDisplayed - 1: this.numItemsDisplayed) + " Result(s)";
				prev.onclick = this.simpleClosure(this, "showPrevious");
			}
			
			this.pagingBar.appendChild(prev);
			var s = document.createElement("span");
			s.innerHTML = " | ";
				
			this.pagingBar.appendChild(s);
		
			var next = document.createTextNode("Next Results"); // default next text node
			if (lastItemDisplayed < total) {
				//create next link
				next = document.createElement("a");
				next.href = "#next";
				next.className = "nextItems";
				next.innerHTML = "Next " + (lastItemDisplayed+this.numItemsDisplayed > total ? total - lastItemDisplayed: this.numItemsDisplayed ) + " Results";
				next.onclick = this.simpleClosure(this, "showNext");
			}
			
			this.pagingBar.appendChild(next);
		}
	},


	clearPagingBars: function() {
		if (this.infoBar != null)
			this.infoBar.innerHTML = "&nbsp;";
		if (this.pagingBar != null)
			this.pagingBar.innerHTML = "&nbsp;";
	},
	

	exitNumberMode: function() {
		this.hideHighlight();
		if (this.inputNode.value == "" && this.lastPhraseSearched)
			this.inputNode.value = this.lastPhraseSearched;
	},
	
	
	hotkey: function(event) {
		var k = event.keyCode;
		if (event.ctrlKey == true) { //if CONTROL-*
			if (k == dojo.event.browser.keys.KEY_PAGE_UP)
				this.showPrevious();
			else if (k == dojo.event.browser.keys.KEY_PAGE_DOWN)
				this.showNext();
			else if (k == dojo.event.browser.keys.KEY_LEFT)
				this.showPrevious();
			else if (k == dojo.event.browser.keys.KEY_RIGHT)
				this.showNext();
		}
	},
	
	destroy: function() {
		dojo.event.topic.publish(this.eventNames.destroy, { source: this } );

		return dojo.widget.HtmlWidget.prototype.destroy.apply(this, arguments);
	},


	// called when a user selects one of the list items
	// Selection is done by either clicking on an item or by 
	// entering numbers and pushing enter
	
	select: function(message) {
		if (!message.obj && !message.objs) {
			var newObj = {};
			newObj.obj = message;
			message = newObj;
		}
		// default the objs array to empty
		if (message.objs == null) {
			message.objs = new Array();
			message.objs.push(message.obj);
		}
		
		this.doSelect(message);

		// this timeout is used for the publish function so that in IE
		// I can guarantee that it is executed after the addOnLoad 
		// function on a page
		var f = function(ths, msg) {
			return function() {dojo.event.topic.publish(ths.eventNames.select, msg)};
		}
		setTimeout(new f(this, message), 10);
	},


	doSelect: function(message){

		// default implementation. Override this method.
		
	},
	
	/* This call keeps track of the order in which calls are sent and received 
	   from the server.  This information is passed on to the "method" given
	   in the params.  (Ideally the method would look at this id and ignore
	   any calls that were sent before, and yet received after, the latest call)  
	 */
	simpleClosure: function(thisObj, method, extra) { 
		var storedCallCount = this.callCount++;
		return function(arg1, arg2) {
				  return thisObj[method](arg1, arg2, extra, storedCallCount);
				}; 
	},
	
	
	// whether or not a number entered triggers the search
	allowAutoListWithNumber: function() { return true; },
	
	allowNewSearch: function() { return true; },
	
	allowAutoJump: function() { return true; },
	
	isAlphaNumeric: function(key) {
		 return key >= 48 && this.key <= 105;
	},
	
	isDash : function(key) {
		return key == 189 || key == 109;
	},
	
	searchCleared : function() {
		return;
	},
	
	resetSearch : function(phrase) {
		clearTimeout(this.searchTimeout);	//stop any timeout that may have just occured...fixes 'duplicate data' error
		this.objectsFound = new Array();	//zero-out numbered object list
		this.allObjectsFound = new Array();
		this.searchIndex = 0;				//our numbering is one-based, but the searchIndex is incremented prior to printing
		this.firstItemDisplayed = 1;		//zero-out our paging index (but we have a one-based list, see line above)
		this.lastPhraseSearched = phrase;
	}
	
	},
	"html"
);



