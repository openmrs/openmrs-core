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
 *   columnWidths: an array of column widths, the length of the array should be equal to the number of columns
 *   columnRenderers: array of fnRender for each column
 *   columnVisibility: array of bVisible values for each column
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
			columnWidths: ["15%","15%","15%","15%","15%", "25%"],
			columnRenderers: [null, null, null, null, null, null], 
			columnVisibility: [true, true, true, true, true, true],
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
			{searchLabel: '<spring:message code="General.search"/>', displayLength: 5, 
				minLength: 3, columnWidths: ["15%","15%","15%","15%","15%", "25%"],
				columnRenderers: [null, null, null, null, null, null], 
				columnVisibility: [true, true, true, true, true, true]}
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
 *   columnWidths: an array of column widths, the length of the array should be equal to the number of columns
 *   columnRenderers: array of fnRender for each column
 *   columnVisibility: array of bVisible values for each column
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
	var openmrsSearch_div = '<span><span style="white-space: nowrap"><span><span id="searchLabelNode"></span><input type="text" value="" id="inputNode" autocomplete="off"/><input type="checkbox" style="display: none" id="includeRetired"/><img id="spinner" src=""/><input type="checkbox" style="display: none" id="includeVoided"/><input type="checkbox" style="display: none" id="verboseListing"/><span id="loadingMsg"></span><span id="minCharError" class="error"></span><span id="pageInfo"></span><br /><span id="searchWidgetNotification"></span></span></span><span class="openmrsSearchDiv"><table id="openmrsSearchTable" cellpadding="2" cellspacing="0" style="width: 100%"><thead id="searchTableHeader"><tr></tr></thead><tbody></tbody></table></span></span>';
	var BATCH_SIZE = omsgs.maxSearchResults;
	if(!Number(BATCH_SIZE))
		BATCH_SIZE = 200;
	var ajaxTimer = null;
	var buffer = null;
	var inSerialMode = Boolean(omsgs.searchRunInSerialMode);
	$j.widget("ui.openmrsSearch", {
		plugins: {},
		options: {
			minLength: 3,
			searchLabel: ' ',
			includeVoidedLabel: omsgs.includeVoided,
			showIncludeVoided: false,
			displayLength: 10,
			columnWidths: null,
			columnRenderers: null,
			columnVisibility: null
		},
		_lastCallCount: 0,
		_callCount: 1,
		_results: null,
		_div: null,
		_table: null,
		_textInputTimer: null,
		_lastSubCallCount: 0,
		_bufferedAjaxCallCounters: null,
		
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
		    	notification = div.find("#searchWidgetNotification");
		    	loadingMsgObj = div.find("#loadingMsg");
		    
		    this._div = div;

		    lbl.text(o.searchLabel);
		    
		    //3 should be the minimum number of results to display per page
		    if(o.displayLength < 3)
		    	o.displayLength = 3;
		    
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
		    		if(!(self._div.find(".openmrsSearchDiv").css("display") != 'none')) {
						return true;
					}
		    		if(kc == 13)
		    			self._doKeyEnter();
			    	//kill the event
			    	event.stopPropagation();
			    				    	
			    	return false;
		    	}
		    	//ignore the following keys SHIFT(16), ESC(27), CAPSLOCK(20), CTRL(17), ALT(18)
		    	else if((kc >= 16 && kc <= 18) || kc == 20 || kc == 27)
		    		return false;
		    	
		    	$j(notification).html(" ");
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
	    			//This discontinues any further ajax SUB calls from the last triggered search
	    			if(!inSerialMode && ajaxTimer)
	    				window.clearInterval(ajaxTimer);
	    			
	    			self._doSearch(text);
	    		}
	    		else {
	    			self._table.fnClearTable();
	    			if(spinnerObj.css("visibility") == 'visible'){
	    				spinnerObj.css("visibility", "hidden");
	    			}
	    			if($j('#pageInfo').css("visibility") == 'visible')
						$j('#pageInfo').css("visibility", "hidden");
	    			loadingMsgObj.html(" ");
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
		    input.keydown(function(event) {
		    	//UP(38), DOWN(40), HOME(36), END(35), PAGE UP(33), PAGE DOWN(34)
		    	var kc = event.keyCode;
		    	if(((kc >= 33) && (kc <= 36)) || (kc == 38) || (kc == 40)) {
		    		if(!(self._div.find(".openmrsSearchDiv").css("display") != 'none')) {
						return true;
					}
					
					switch(event.keyCode) {
			    		case 33:
			    			self._doPageDown();
			    			break;
			    		case 34:
			    			self._doPageUp();
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
		    	sDom: 'rt<"fg-button ui-helper-clearfix"flip>',
		    	oLanguage: {
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
			var self = this;
			var fieldsAndHeaders = self.options.fieldsAndHeaders;
			var columnIndex = 0
			return $j.map(fieldsAndHeaders, function(c) {
				var width = null;
				var fnRenderer = null;
				var visible = true;
				
				if(self.options.columnWidths && self.options.columnWidths[columnIndex])
					width = self.options.columnWidths[columnIndex];
				if(self.options.columnRenderers && self.options.columnRenderers[columnIndex])
					fnRenderer = self.options.columnRenderers[columnIndex];
				if(self.options.columnVisibility && self.options.columnVisibility[columnIndex] == false )
					visible = false;
				
				var column = { sTitle: c.header, sWidth: width, fnRender: fnRenderer, bVisible: visible };
				
				columnIndex++;
				return column;
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
				this._lastCallCount = storedCallCount;
				//First get data to appear on the first page
				this.options.searchHandler(text, this._handleResults(text, storedCallCount), true, 
						{includeVoided: tmpIncludeVoided, start: 0, length: this._table.fnSettings()._iDisplayLength});
			}
		},
		
		/** returns a closure for the returned results */
		_handleResults: function(searchText, curCallCount) {
			var self = this;
			return function(results) {
				if(results["notification"])					
					$j(notification).html(results["notification"]);
				
				var matchCount = results["count"];
				self._results = results["objectList"];
				if(matchCount <= self._table.fnSettings()._iDisplayLength){
					spinnerObj.css("visibility", "hidden");
					loadingMsgObj.html("");
				}

				if(curCallCount && self._lastCallCount > curCallCount) {
					//stop old ajax calls from over writing later ones
					return;
				}
				
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
					var startIndex = self._table.fnSettings()._iDisplayLength;
					if(!inSerialMode){
						buffer = new Array;//empty the buffer
						self._bufferedAjaxCallCounters = new Array;
					}
					
					loadingMsgObj.html(omsgs.loadingWithArgument.replace("_NUMBER_OF_PAGES_", matchCount));
					self._lastSubCallCount = 1;
					self._fetchMoreResults(searchText, curCallCount, startIndex, matchCount, 1);										
				}else if(matchCount > 0) 
					$j('#pageInfo').append(" - "+omsgs.onePage);
			};
		},
		
		_fetchMoreResults: function(searchText, curCallCount, startIndex, matchCount, curSubCallCount){
			//if a new ajax call has been triggered off
			if(curCallCount && this._lastCallCount > curCallCount) {
				return;
			}
			
			this.options.searchHandler(searchText, this._addMoreRows(curCallCount, searchText, matchCount, startIndex, curSubCallCount),
				false, {includeVoided: this.options.showIncludeVoided && checkBox.attr('checked'),
				start: startIndex, length: BATCH_SIZE});
					
			if(inSerialMode)
				return;
				
			var self = this;
			ajaxTimer = window.setTimeout(function(){
				nextStart = startIndex+BATCH_SIZE;
				if(nextStart < matchCount){
					self._fetchMoreResults(searchText, curCallCount, nextStart, matchCount, ++curSubCallCount);
				}
				else{
					if(ajaxTimer)
						window.clearTimeout(ajaxTimer);
						return;
					}
			}, 10);//fetch more results every 10ms till we have all
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
				//error on server
				$j(notification).html(this._results[0]);
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
			
			this._table.numberOfPages = 1;
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
			$j('#pageInfo').html(omsgs.viewingResultsFor.replace("_SEARCH_TEXT_", "'<b>"+searchText+"</b>'"));

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
		_addMoreRows: function(curCallCount2, searchText, matchCount, startIndex, curSubCallCount){
			var self = this;
			return function(results) {
				
				//Don't display results from delayed ajax calls when the input box is blank or has less 
				//than the minimum characters
				var currInput = $j.trim($j("#inputNode").val());
				if(currInput == '' || currInput.length < self.options.minLength){
					$j(notification).html(" ");
					if($j('#pageInfo').css("visibility") == 'visible')
						$j('#pageInfo').css("visibility", "hidden");
					$j(".openmrsSearchDiv").hide();
					if(currInput.length > 0)
						$j("#minCharError").css("visibility", "visible");
					spinnerObj.css("visibility", "hidden");
					return;
				}
				
				//Since this method is called on the second ajax call to return the remaining results,
				//therefore (self._lastCallCount == curCallCount) so it will pass if no later ajax call were made				
				if(curCallCount2 && self._lastCallCount > curCallCount2) {
                    //stop old ajax calls from over writing later ones
					return;
				}
				
				var data = results["objectList"];
				//if error occured on server
				if(data && data.length > 0 && typeof data[0] == 'string') {
					if(!inSerialMode && ajaxTimer)
						window.clearTimeout(ajaxTimer);
					
					$j(notification).html(data[0]);
					spinnerObj.css("visibility", "hidden");
					loadingMsgObj.html("");
					return;
				}
				
				if(results["notification"])
					$j(notification).html(results["notification"]);
				
				//or if we are in serial mode
				if(inSerialMode || (curSubCallCount == self._lastSubCallCount)){
					var newRows = new Array();
					for(var x in data) {
						currentData = data[x];
						newRows.push(self._buildRow(currentData));
						//add the data to the results list
						self._results.push(currentData);
					}
					
					self._table.fnAddData(newRows);
					nextSubCallCount = curSubCallCount + 1;
					
					if(!inSerialMode && self._bufferedAjaxCallCounters.length > 0){
						
						for(var i = 0; i < self._bufferedAjaxCallCounters.length; i++){
							subCallCounter = self._bufferedAjaxCallCounters[i];
							//Skip past the ones that come after those that are not yet returned by DWR calls e.g if we have ajax
							//calls 3 and 5 in the buffer, when 2 returns, then add only 3 and ingore 5 since it has to wait on 4							
							bufferedRows = buffer[subCallCounter];							
							if(subCallCounter && (subCallCounter == nextSubCallCount) && bufferedRows){
								self._table.fnAddData(bufferedRows);
								buffer[subCallCounter] = null;//drop rows from buffer	
								self._bufferedAjaxCallCounters[i] = null;//drop counter from buffer								
								nextSubCallCount++;
							}
						}
					}
					
					self._lastSubCallCount = nextSubCallCount;
				}
				else if(!inSerialMode && curSubCallCount > self._lastSubCallCount){
					//this ajax request returned before others that were made before it, add its results to the buffer
					var bufferedRows = new Array();
					for(var x in data) {
						bufferedData = data[x];
						bufferedRows.push(self._buildRow(bufferedData));
						//add the data to the results list
						self._results.push(bufferedData);
					}
					self._bufferedAjaxCallCounters.push(curSubCallCount);	
					buffer[curSubCallCount] = bufferedRows;

					return;
				}
				
				//update the page statistics to match the actual hit count, this is important for searches
				//where the actual result count is less or more than the predicted matchCount
				var actualResultCount = self._table.fnGetNodes().length;
				if(actualResultCount % self._table.fnSettings()._iDisplayLength == 0)
					self._table.numberOfPages = actualResultCount/self._table.fnSettings()._iDisplayLength;
				else
					self._table.numberOfPages = Math.floor(actualResultCount/self._table.fnSettings()._iDisplayLength)+1;
				
				self._updatePageInfo(searchText);
				
				//all the hits have been fetched
				if(actualResultCount >= matchCount){
					spinnerObj.css("visibility", "hidden");
					loadingMsgObj.html("");
					$j('#pageInfo').html(omsgs.viewingResultsFor.replace("_SEARCH_TEXT_", "'<b>"+searchText+"</b>'"));
					pageStr = omsgs.pagesWithPlaceHolder.replace("_NUMBER_OF_PAGES_", self._table.numberOfPages);
					$j('#pageInfo').append(" - "+pageStr);
				}
				
				//TODO Fix this very hacky way to capture the visible buttons before the user clicks any of them,
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
				
				//if there are still more hits to fetch and we are in serial mode, get them
				if(inSerialMode && actualResultCount < matchCount){
					self._fetchMoreResults(searchText, curCallCount2, (startIndex+BATCH_SIZE), matchCount);
				}
			};
		}
	});
})(jQuery);
