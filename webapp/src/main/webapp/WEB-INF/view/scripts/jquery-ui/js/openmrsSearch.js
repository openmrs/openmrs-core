/**
 * Helper function to get started quickly
 * 
 * opts is a map that can be any option of the openmrsSearch widget:
 *   minLength - The minimum required number of characters a user has to enter for a search to be triggered
 *   searchLabel - The text label to appear on the left of the input text box
 *   includeVoidedLabel - The label for the includeVoided/includeRetired checkbox
 *   showIncludeVoided - If the includeVoided/includeRetired checkbox should be displayed
 *   searchHandler (Required)
 *   resultsHandler
 *   selectionHandler
 *   fieldHeaders (Required) - Array of fieldName and column header maps)
 *   displayLength - The number of results to display per page
 *   
 * The parameters 'showIncludeVoided' and 'selectionHandler' are options to the widget but
 * given here as simple params.
 * 
 * These are the same:
 * <pre>
   $j(document).ready(function() {
		$j("#elementId").openmrsSearch({
			searchLabel:'<spring:message code="General.search"/>',
			showIncludeVoided: true,
			displayLength: 5,
			minLength: 3,
			searchHandler: doSearchHandler,
			selectionHandler: doSelectionHandler,
			fieldsAndHeaders: [
				{fieldName:"field1", header:"Header1"},
				{fieldName:"fiels2", header:"Header2"},
				{fieldName:"field3", header:"Header3"},
				{fieldName:"field4", header:"Header4"},
				{fieldName:"fiels5", header:"Header5"},
				{fieldName:"field6", header:"Header6"}
			]
		});

		new OpenmrsSearch("elementid", true, doSearchHandler, doSelectionHandler, 
			[	{fieldName:"field1", header:"Header1"},
				{fieldName:"fiels2", header:"Header2"},
				{fieldName:"field3", header:"Header3"},
				{fieldName:"field4", header:"Header4"},
				{fieldName:"fiels5", header:"Header5"},
				{fieldName:"field6", header:"Header6"}
			],
			{searchLabel: '<spring:message code="General.search"/>', displayLength: 5, minLength: 3}
		);
	});
	</pre>
 */
function OpenmrsSearch(div, showIncludeVoided, searchHandler, selectionHandler, fieldsAndHeaders, opts) {
	var el;
	if(typeof div == 'string') {
		el = jQuery("#" + div);
	}
	
	if(!opts) {
		opts = {};
	}
	
	if(!opts.showIncludeVoided) 
		opts.showIncludeVoided = showIncludeVoided;
	if(!opts.selectionHandler) 
		opts.selectionHandler = selectionHandler;
	if(!opts.searchHandler)
		opts.searchHandler = searchHandler;
	if(!opts.fieldsAndHeaders)
		opts.fieldsAndHeaders = fieldsAndHeaders;
	
	jQuery(el).openmrsSearch(opts);
}

/**
 * Expects to be put on a div.
 * Options:
 *   minLength:int (default: 3)
 *   searchLabel:string (default: omsgs.searchLabel)
 *   includeVoidedLabel:string (default: omsgs.includeVoided)
 *   showIncludeVoided:bool (default: false)
 *   searchHandler:function(text, resultHandler, options) (default:null)
 *   resultsHandler:function(results) (default:null)
 *   selectionHandler:function(index, rowData)
 *   fieldsAndHeaders: Array of fieldNames and column header maps
 *   displayLength: int (default: 10)
 *   
 * The styling on this table works like this:
 * <pre> 
#openmrsSearchTable tbody tr.even:hover {
	background-color: #ECFFB3;
}
#openmrsSearchTable tbody tr.odd:hover {
	background-color: #E6FF99;
}
#openmrsSearchTable tbody tr.even.row_highlight {
	background-color: #ECFFB3;
}
#openmrsSearchTable tbody tr.odd.row_highlight {
	background-color: #E6FF99;
}
	</pre>
 */
(function($j) {
	var openmrsSearch_div = '<span><span style="white-space: nowrap"><span><span id="searchLabelNode"></span><input type="text" value="" id="inputNode" autocomplete="off"/><input type="checkbox" style="display: none" id="includeRetired"/><img id="spinner" src=""/><input type="checkbox" style="display: none" id="includeVoided"/><input type="checkbox" style="display: none" id="verboseListing"/><span id="minCharError" class="error"></span><span id="pageInfo"></span></span></span><span class="openmrsSearchDiv"><table id="openmrsSearchTable" cellpadding="2" cellspacing="0" style="width: 100%"><thead id="searchTableHeader"><tr><th></th><th></th><th></th><th></th><th></th><th></th></tr></thead><tbody></tbody></table></span></span>';
	
	$j.widget("ui.openmrsSearch", {
		plugins: {},
		options: {
			minLength: 3,
			searchLabel: omsgs.searchLabel,
			includeVoidedLabel: omsgs.includeVoided,
			showIncludeVoided: false,
			displayLength: 10
		},
		_lastCallCount: 0,
		_callCount: 1,
		_results: null,
		_div: null,
		_table: null,
		_textInputTimer: null,
		
		_create: function() {
		    var self = this,
		        o = self.options,
		        el = self.element,
		        div = el.append(openmrsSearch_div),
		        lbl = div.find("#searchLabelNode"),
		        input = div.find("#inputNode"),
		        table = div.find("#openmrsSearchTable");
		    	checkBox = div.find("#includeVoided");
		    	spinnerObj = div.find("#spinner");
		    	spinnerObj.css("visibility", "hidden");
		    	spinnerObj.attr("src", openmrsContextPath+"/images/loading.gif");
		    	minCharErrorObj = div.find("#minCharError");
		    	minCharErrorObj.html(omsgs.minCharRequired.replace("_REQUIRED_NUMBER_", o.minLength));
		    
		    this._div = div;

		    lbl.text(o.searchLabel);
		    
		    if(o.showIncludeVoided) {
		    	var tmp = div.find("#includeVoided");
			    tmp.before("<label for='includeVoided'>" + o.includeVoidedLabel + "</label>");
		    	tmp.show();
		    }
		    
		    //when the user checks/unchecks the includeVoided checkbox, trigger a search
		    checkBox.click(function() {   	
		    	if($j.trim(input.val()) != '' && $j.trim(input.val()).length >= o.minLength)
		    		self._doSearch(input.val());
		    	else{
		    		if(spinnerObj.css("visibility") == 'visible')
	    				spinnerObj.css("visibility", "hidden");
		    		$j(".openmrsSearchDiv").hide();
		    		$j("#minCharError").css("visibility", "visible");
		    		if($j('#pageInfo').css("visibility") == 'visible')
						$j('#pageInfo').css("visibility", "hidden");
		    	}
		    	//to maintain keyDown and keyUp events since they are only fired when the input box has focus
		    	input.focus();
			});
		    
		    //this._trigger('initialized');
		    input.keyup(function(event) {
		    	//catch control keys
		    	//LEFT(37), UP(38), RIGHT(39), DOWN(40), ENTER(13), HOME(36), END(35), PAGE UP(33), PAGE DOWN(34)
		    	var kc = event.keyCode;
		    	if(((kc >= 33) && (kc <= 40)) || (kc == 13)) {
		    		if(!self._div.find(".openmrsSearchDiv").is(":visible")) {
						return true;
					}
		    		if(kc == 13)
		    			self._doKeyEnter();
			    	//kill the event
			    	event.stopPropagation();
			    				    	
			    	return false;
		    	}
		    	
		    	if(self.onCharTyped) {
		    		self.onCharTyped(self, event.keyCode);
		    	}
		    	
	        	var text = $j.trim(input.val());
	        	if(this._textInputTimer != null){
    				window.clearTimeout(this._textInputTimer);
    			}
	    		if(text.length >= o.minLength) {
	    			if($j('#pageInfo').css("visibility") == 'visible')
						$j('#pageInfo').css("visibility", "hidden");
						
	    			if($j("#minCharError").css("visibility") == 'visible')
	    				$j("#minCharError").css("visibility", "hidden");
	    			
	    			self._doSearch(text);
	    		}
	    		else {
	    			self._table.fnClearTable();
	    			if(spinnerObj.css("visibility") == 'visible'){
	    				spinnerObj.css("visibility", "hidden");
	    			}
	    			if($j('#pageInfo').css("visibility") == 'visible')
						$j('#pageInfo').css("visibility", "hidden");
						
	    			$j(".openmrsSearchDiv").hide();
	    			//wait for a 400ms, if the user isn't typing anymore chars, show the error msg
	    			this._textInputTimer = window.setTimeout(function(){
	    				if($j.trim(input.val()).length > 0 && $j.trim(input.val()).length < o.minLength)
	    					$j("#minCharError").css("visibility", "visible");
	    				else if($j.trim(input.val()).length == 0 && $j("#minCharError").css("visibility") == 'visible')
	    					$j("#minCharError").css("visibility", "hidden");
	    			}, 600);
	    			
	    		}
	    		return true;
		    });
		    
		    //catch control keys to stop the cursor in the input box from moving.
		    input.keypress(function(event) {
		    	//UP(38), DOWN(40), HOME(36), END(35), PAGE UP(33), PAGE DOWN(34)
		    	var kc = event.keyCode;
		    	if(((kc >= 33) && (kc <= 36)) || (kc == 38) || (kc == 40)) {
		    		if(!self._div.find(".openmrsSearchDiv").is(":visible")) {
						return true;
					}
		    		//if the pages are not yet all fully loaded, block usage of HOME(36), END(35), PAGE UP(33), PAGE DOWN(34)
		    		if((kc >= 33 && kc <= 36) && $j(spinnerObj).css("visibility") == "visible"){
		    			return false
		    		}
			    	switch(event.keyCode) {
			    		case 33:
			    			self._doPageUp();
			    			break;
			    		case 34:
			    			self._doPageDown();
			    			break;
				    	case 35:
				    		self._doKeyEnd();
				    		break;
				    	case 36:
				    		self._doKeyHome();
				    		break;
				    	case 38:
				    		self._doKeyUp();
				    		break;
				    	case 40:
				    		self._doKeyDown();
				    		break;
			    	}
			    	//kill the event
			    	event.stopPropagation();    	
			    	
			    	return false;
		    	}
		    	
	    		return true;
		    });
		    
		    //on widget load the focus should be on the search box if there are no 
		    //other enabled and visible text boxes on the page
		    var inputs = document.getElementsByTagName("input");
		    var numberOfTextInputs = 0;
		    for(var x in inputs){
		    	var inputField = inputs[x];
		    	if(inputField && inputField.type == 'text' && $j(inputField).attr("disabled") == false && 
		    			$j(inputField).is(":visible") && $j(inputField).css("visibility") != "hidden")
		    		numberOfTextInputs++;
		    }
		   
		    if(numberOfTextInputs == 1)
		    	input.focus();
		    
			//setup 'openmrsSearchTable'
			div.find(".openmrsSearchDiv").hide();

			//TODO columns need to be built: id='searchTableHeader'
		    this._table = table.dataTable({
		    	bFilter: false,
		    	bLengthChange: false,
		    	bSort: false,
		    	sPaginationType: "full_numbers",
		    	aoColumns: this._makeColumns(),
		    	iDisplayLength: self.options.displayLength,
		    	numberOfPages: 0,
		    	currPage: 0,
		    	bAutoWidth: false,
		    	bJQueryUI: true,
		    	"oLanguage": {
		    		"sInfo": omsgs.sInfoLabel,
		    		"oPaginate": {"sFirst": omsgs.first, "sPrevious": omsgs.previous, "sNext": omsgs.next, "sLast": omsgs.last},
		    		"sZeroRecords": omsgs.noMatchesFound,
		    		"sInfoEmpty": " "
		    	},
		    	fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {					
		    		//register mouseover/out events handlers to have row highlighting
		    		$j(nRow).bind('mouseover', function(){
		    			$j(this).addClass('tr_row_highlight_hover');
		    			$j(this).css("cursor", "pointer");
		    			if(self.curRowSelection != null)
		    				$j(self._table.fnGetNodes()[self.curRowSelection]).removeClass("row_highlight");
					});
		    		$j(nRow).bind('mouseout', function(){
		    			$j(this).removeClass('tr_row_highlight_hover');
		    			if(self.curRowSelection != null)
		    				$j(self._table.fnGetNodes()[self.curRowSelection]).addClass("row_highlight");
		    	    });
		    		
		    		var currItem = self._results[iDisplayIndexFull];
		    		//draw a strike through for all voided/retired objects that have been loaded
		    		if(currItem && (currItem.voided || currItem.retired)){		    			
		    			$j(nRow).children().each(function(){		    				
		    				$j(this).addClass('voided');
		    			}); 
		    		}
		    		
		    		if(self.options.selectionHandler) {
		    			$j(nRow).bind('click', function() {
		    				//Register onclick handlers to each row
		    				self._doSelected(iDisplayIndexFull, self._results[iDisplayIndexFull]);
		    			});
		    		}
		    		
		    		return nRow;
		    	}
		    });
		},
		
		_makeColumns: function() {
			var fieldsAndHeaders = this.options.fieldsAndHeaders;
			return $j.map(fieldsAndHeaders, function(c) {
				return { sTitle: c.header };
			});
		},
		
		_doSearch: function(text) {
			if(this.options.searchHandler) {
				var tmpIncludeVoided = (this.options.showIncludeVoided && checkBox.attr('checked'));
				//associate the ajax call to be made to a call count number to track it , so on
				//its return we can identify it and determine if there are some later calls made 
				//so that we can make sure older ajax calls donot overwrite later ones
				var storedCallCount = this._callCount++;				
				spinnerObj.css("visibility", "visible");
				
				//First get data to appear on the first page			
				this.options.searchHandler(text, this._handleResults(text, storedCallCount), 
						{includeVoided: tmpIncludeVoided, start: 0, length: this._table.fnSettings()._iDisplayLength});
			}
		},
		
		/** returns a closure for the returned results */
		_handleResults: function(searchText, curCallCount) {
			var self = this;
			return function(results) {
				var matchCount = results["count"];
				self._results = results["objectList"];
				if(matchCount <= self._table.fnSettings()._iDisplayLength)
					spinnerObj.css("visibility", "hidden");
				if(curCallCount && self._lastCallCount > curCallCount) {
					//stop old ajax calls from over writing later ones
					return;
				}
				
				self._lastCallCount = curCallCount;
				//Don't display results from delayed ajax calls when the input box is blank or has less 
				//than the minimum characters, this can arise when user presses backspace relatively fast
				//yet there were some intermediate calls that might have returned results
				var currInput = $j.trim($j("#inputNode").val());
				if(currInput == '' || currInput.length < self.options.minLength){
					if($j('#pageInfo').css("visibility") == 'visible')
						$j('#pageInfo').css("visibility", "hidden");
					$j(".openmrsSearchDiv").hide();
					if(currInput.length > 0)
						$j("#minCharError").css("visibility", "visible");
					return;
				}
				self._doHandleResults(matchCount, searchText);
							
				//FETCH THE REST OF THE RESULTS IF result COUNT is greater than the number of rows to display per page
				if(matchCount > self._table.fnSettings()._iDisplayLength){
					spinnerObj.css("visibility", "visible");
					$j('#openmrsSearchTable_info').html(omsgs.sInfoLabel.replace("_START_", 1).
							replace("_END_", self._table.fnSettings()._iDisplayLength).replace("_TOTAL_", matchCount));
					
					self.options.searchHandler(searchText, self._addMoreRows(curCallCount, searchText, matchCount),
						{includeVoided: self.options.showIncludeVoided && checkBox.attr('checked'),
						start: self._table.fnSettings()._iDisplayLength, length: null});
				}
			};
		},
			
		_doHandleResults: function(matchCount, searchText) {
			this.curRowSelection = null;
				
			if(this.options.resultsHandler) {
				this.options.resultsHandler(this._results);
			}
			else {
				this._buildDataTable(matchCount, searchText);
				//reset to show first page always
				this._table.fnPageChange('first');
			}	
		},
		
		_buildDataTable: function(matchCount, searchText) {
			this._fireEvent('beforeDataTable');
			
			this._table.fnClearTable();
			if((this._results != null) && (this._results.length > 0) && (typeof this._results[0] == 'string')) {
				//error
				//hide pagination buttons
	    	    if($j('#openmrsSearchTable_paginate')){
	    	    	$j('#openmrsSearchTable_paginate').hide();
				}
	    	    if($j('#pageInfo').css("visibility") == 'visible')
					$j('#pageInfo').css("visibility", "hidden");
	    	    if($j('#openmrsSearchTable_info').is(":visible"))
					$j('#openmrsSearchTable_info').hide();
				return;
			}
			
			var d = new Array();
			for(var r in this._results) {
				d[r] = this._buildRow(this._results[r]);
			}
			
			this._table.fnAddData(d);
			
			if(matchCount % this._table.fnSettings()._iDisplayLength == 0)
				this._table.numberOfPages = matchCount/this._table.fnSettings()._iDisplayLength;
			else
				this._table.numberOfPages = Math.floor(matchCount/this._table.fnSettings()._iDisplayLength)+1;
			
			this._table.currPage = 1;
			
    	    if(matchCount <= this._table.fnSettings()._iDisplayLength){
    	    	$j('#openmrsSearchTable_paginate').hide();
			}else if(!$j('#openmrsSearchTable_paginate').is(":visible")){
				//if the buttons were previously hidden, show them
				$j('#openmrsSearchTable_paginate').show();
			}
    	    
    	    this._updatePageInfo(searchText);
    	    if(matchCount == 0){
    	    	if($j('#openmrsSearchTable_info').is(":visible"))
					$j('#openmrsSearchTable_info').hide();
    	    }else if(!$j('#openmrsSearchTable_info').is(":visible"))
				$j('#openmrsSearchTable_info').show();
    	    
			this._div.find(".openmrsSearchDiv").show();
			
			this._fireEvent('afterDataTable');
		},
		
		_buildRow: function(rowData) {
			var cols = this.options.fieldsAndHeaders;
			return $j.map(cols, function(c) {
				var data = rowData[c.fieldName];
				if(data == null) 
					data = " ";
				
				return data;
			});
		},
		
		_fireEvent: function(eventType, data) {
			//TODO also pass 'this'
		},
		
		_doKeyDown: function() {
			var prevRow = this.curRowSelection;
			if(this.curRowSelection == null) {
				this.curRowSelection = 0;
			}
			else {
				this.curRowSelection++;
			}
			//if the row has yet been populated, don't highlight it
			if(!this._results[this.curRowSelection]){
				this.curRowSelection--;//reverse
				return;
			}
			if(this.curRowSelection >= this._table.fnGetData().length) {
				this.curRowSelection--;//redact it
				//cant go any further so return
				return;
			}
			
			if(prevRow != null) {
				$j(this._table.fnGetNodes()[prevRow]).removeClass("row_highlight");
			}
			
			//If the selected row is the first one on the next page, flip over to its page
			if(this.curRowSelection != 0 && (this.curRowSelection % this._table.fnSettings()._iDisplayLength) == 0) {
				this._table.fnPageChange('next');
			}
			
			//hide the hover
			$j('.tr_row_highlight_hover').removeClass("tr_row_highlight_hover");
			$j(this._table.fnGetNodes()[this.curRowSelection]).addClass("row_highlight");
		},
		
		_doKeyUp: function() {
			var prevRow = this.curRowSelection;
			if(this.curRowSelection == null) {
				if($j(spinnerObj).css("visibility") == "visible" || 
						this._table.fnGetData().length < this._table.fnSettings()._iDisplayLength)
					return;
				this.curRowSelection = this._table.fnGetData().length-1;
				this._table.currPage = this._table.numberOfPages;
				this._table.fnPageChange('last');
			}
			else {
				this.curRowSelection--;
			}
			
			if(this.curRowSelection < 0) {
				this.curRowSelection++;//redact it
				//cant go any further so return
				return;
			}
			
			if(prevRow != null) {
				$j(this._table.fnGetNodes()[prevRow]).removeClass("row_highlight");

				if(prevRow % this._table.fnSettings()._iDisplayLength == 0) {
					this._table.fnPageChange('previous');
				}
			}
			
			//hide the hover
			$j('.tr_row_highlight_hover').removeClass("tr_row_highlight_hover");
			$j(this._table.fnGetNodes()[this.curRowSelection]).addClass("row_highlight");
		},
		
		_doPageUp: function() {
			this._table.fnPageChange('next');
			if(++this._table.currPage > this._table.numberOfPages)
				this._table.currPage = this._table.numberOfPages;
			
			//move the highlight to the first row on the next page so that we dont lose it if the highlight isn't on the page			
			if(this._table.currPage < this._table.numberOfPages || (this._table.currPage == this._table.numberOfPages && this.curRowSelection < 
					((this._table.numberOfPages - 1)*this._table.fnSettings()._iDisplayLength)))
				this._updateRowHighlight(((this._table.currPage - 1)*this._table.fnSettings()._iDisplayLength));
		},
		
		_doPageDown: function() {
			var rowToHighlight = null;
			if(--this._table.currPage < 1){
				this._table.currPage = 1;
				this._table.fnPageChange('first');
				if(this.curRowSelection == null || this.curRowSelection < this._table.fnSettings()._iDisplayLength)
					return;
				rowToHighlight = 0;
			}
			else{
				rowToHighlight = (this._table.currPage - 1)*this._table.fnSettings()._iDisplayLength;
				this._table.fnPageChange('previous');
			}
			
			this._updateRowHighlight(rowToHighlight);
		},
		
		_doKeyEnter: function() {
			if(this.curRowSelection != null) {
				this._doSelected(this.curRowSelection, this._results[this.curRowSelection]);
			}
		},
		
		_doKeyHome: function() {
			this._table.fnPageChange('first');
			this._table.currPage = 1;
			if(this.curRowSelection == null || this.curRowSelection < this._table.fnSettings()._iDisplayLength)
				return;
			this._updateRowHighlight(0);
		},
		
		_doKeyEnd: function() {
			this._table.fnPageChange('last');
			this._table.currPage = this._table.numberOfPages;
			//if the highlight is already on the last page, don't switch it
			if( this.curRowSelection != null && this.curRowSelection > (this._table.numberOfPages - 1)*this._table.fnSettings()._iDisplayLength )
				return;
				
			this._updateRowHighlight(((this._table.numberOfPages - 1)*this._table.fnSettings()._iDisplayLength));
		},
		
		_doSelected: function(position, rowData) {
			if(this.options.selectionHandler) {
				this.options.selectionHandler(position, rowData);
			}
		},
		
		getResults: function() {
			return this._results;
		},

	    log: function(obj, title) {
	        var s = "";
	        if(title) {
	        	s = title + "\n";
	        }
	        for(var p in obj) {
	        	s += p + (((typeof obj[p]) == 'function') ? ": function" : "="+obj[p]) + "\n";
	        }
	        alert("LOG=[" + s + "]");
	    },
		
		_updatePageInfo: function(searchText) {
			if(this._results.length > 0){
				var pageString = (this._table.numberOfPages == 1) ? omsgs.page : omsgs.pages;
				$j('#pageInfo').html(omsgs.viewingResultsFor.replace("_SEARCH_TEXT_", "'<b>"+searchText+"</b>'").
						concat(" ( "+this._table.numberOfPages+" "+pageString+" )"));
			}else {
				$j('#pageInfo').html(omsgs.viewingResultsFor.replace("_SEARCH_TEXT_", "'<b>"+searchText+"</b>'"));
			}
			
			if($j('#pageInfo').css("visibility") != 'visible')
				$j('#pageInfo').css("visibility", "visible");
		},
		
		_updateRowHighlight: function(rowNumber){
			//highlight the row if the highlight is visible
			$j(this._table.fnGetNodes()[this.curRowSelection]).removeClass("row_highlight");
			$j(this._table.fnGetNodes()[rowNumber]).addClass("row_highlight");
			this.curRowSelection = rowNumber;
		},
		
		//This function adds the data returned by the second ajax call that fetches the remaining rows
		_addMoreRows: function(curCallCount2, searchText, matchCount){
			var self = this;
			return function(results) {
				//Don't display results from delayed ajax calls when the input box is blank or has less 
				//than the minimum characters
				var currInput = $j.trim($j("#inputNode").val());
				if(currInput == '' || currInput.length < self.options.minLength){
					if($j('#pageInfo').css("visibility") == 'visible')
						$j('#pageInfo').css("visibility", "hidden");
					$j(".openmrsSearchDiv").hide();
					if(currInput.length > 0)
						$j("#minCharError").css("visibility", "visible");
					spinnerObj.css("visibility", "hidden");
					return;
				}
				
				var data = results["objectList"];								
				
				//Since this method is called on the second ajax call to return the remaining results,
				//therefore (self._lastCallCount == curCallCount) so it will pass if no later ajax call were made				
				if(curCallCount2 && self._lastCallCount > curCallCount2) {
					//stop old ajax calls from over writing later ones
					return;
				}
									
				var newData = new Array();
				for(var x in data) {
					currentData = data[x];
					newData.push(self._buildRow(currentData));
					//add the rest of this data to the results list
					self._results.push(currentData);
				}
				
				self._updatePageInfo(searchText);
				self._table.fnAddData(newData);
				spinnerObj.css("visibility", "hidden");
				//This is hacky way to capture the visible buttons before the user clicks any of them,
				//so that we can add/remove onclick events to/from each.
				$j("#openmrsSearchTable_paginate").mouseenter(function(){
					var buttonElement = document.getElementById('openmrsSearchTable_paginate');
					if(buttonElement){
					    var spans = buttonElement.getElementsByTagName("span");
					    if(self._results && self._results.length > 0){
					    	for(var i in spans){
					    		var span = spans[i];
					    		var elementClass = span.className;	
					    		if(span.getElementsByTagName){
					    			var children = span.getElementsByTagName("span");
					    			//ignore disbaled buttons and the span tag that has nested spans for the numbering 1,2,3,4,5,........
					    			if(children == null || children.length == 0){					    			
					    				//ignore the greyed out buttons
					    				if(span.className && span.className.indexOf("ui-state-disabled") < 0 ){
					    					span.onclick = function(){
					    						if(this.innerHTML){
					    							var buttonText = $j.trim(this.innerHTML);
					    							//if the clicked button bears a number
					    							if(Number(buttonText)){
					    								self._table.currPage = buttonText;
					    								self._updateRowHighlight((self._table.currPage - 1)*self._table.fnSettings()._iDisplayLength);
					    							}else{
					    								//move the highlight to the first row on the displayed page
					    								if(buttonText == omsgs.next){
					    									self._table.currPage++;								
					    									self._updateRowHighlight((self._table.currPage - 1)*self._table.fnSettings()._iDisplayLength);
					    								}else if(buttonText == omsgs.previous){
					    									self._table.currPage--;
					    									self._updateRowHighlight((self._table.currPage - 1)*self._table.fnSettings()._iDisplayLength);
					    								}else if(buttonText == omsgs.first){
					    									self._table.currPage = 1;
					    									self._updateRowHighlight(0);
					    								}else if(buttonText == omsgs.last){
					    									self._table.currPage = self._table.numberOfPages;
					    									self._updateRowHighlight((self._table.numberOfPages - 1)*self._table.fnSettings()._iDisplayLength);
					    								}
					    							}
					    						}
					    					};
					    				}else{
					    					//drop the event handler if the button was previously active
					    					span.onclick = "";
					    				}
					    			}
					    		}
					    	}
					    }
					}
				});
			};
		}
	});
})(jQuery);
