/**
 * Helper function to get started quickly
 * 
 * opts is a map that can be any option of the encounterSearch widget:
 *   minLength
 *   searchLabel
 *   includeVoidedLabel
 *   showIncludeVoided
 *   searchHandler
 *   resultsHandler
 *   selectionHandler
 *   
 * The parameters 'showIncludeVoided' and 'selectionHandler' are options to the widget but
 * given here as simple params.
 * 
 * These are the same:
 * <pre>
   $j(document).ready(function() {
		$j("#findEncounter1").encounterSearch({
			searchLabel:'<spring:message code="Encounter.search"/>',
			showIncludeVoided: true,
			searchHandler: doEncounterSearch,
			selectionHandler: doSelectionHandler
		});

		new EncounterSearch("findEncounter1", true, doSelectionHandler, {
			searchLabel: '<spring:message code="Encounter.search"/>',
			searchHandler: doEncounterSearch
		});
	});
	</pre>
 */
function EncounterSearch(div, showIncludeVoided, selectionHandler, opts) {
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
		opts.searchHandler = doEncounterSearch;
	
	jQuery(el).encounterSearch(opts);
}

/**
 * This is the default searchHandler to be called by the widget, if you wish to over ride it and add some 
 * extra custom code, pass in your searchHandler as one of the options for the opts argument to the 
 * constructor of the widget.
 */
function doEncounterSearch(text, resultHandler, opts) {
	DWREncounterService.findCountAndEncounters(text, opts.includeVoided, opts.start, opts.length, resultHandler);
}

/**
 * Expects to be put on a div.
 * Options:
 *   minLength:int (default: 3)
 *   searchLabel:string (default: "Search Encounters")
 *   includeVoidedLabel:string (default: "Include Voided")
 *   showIncludeVoided:bool (default: false)
 *   searchHandler:function(text, resultHandler, options) (default:null)
 *   resultsHandler:function(results) (default:null)
 *   selectionHandler:function(index, rowData)
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
(function($) {
	var encounterSearch_div = '<span><span style="white-space: nowrap"><span id="searchLabelNode"></span><input type="text" value="" id="inputNode" autocomplete="off"/><input type="checkbox" style="display: none" id="includeRetired"/><img id="spinner" src=""/><input type="checkbox" style="display: none" id="includeVoided"/><input type="checkbox" style="display: none" id="verboseListing"/></span><span class="openmrsSearchDiv"><table id="openmrsSearchTable" cellpadding="2" cellspacing="0" style="width: 100%"><thead id="searchTableHeader"><tr><th></th><th></th><th></th><th></th><th></th><th></th></tr></thead><tbody></tbody></table></span></span>';
	
	$.widget("ui.encounterSearch", {
		plugins: {},
		options: {
			minLength: 3,
			searchLabel: "Search Encounters",
			includeVoidedLabel: "Include Voided",
			showIncludeVoided: false
		},
		_lastCallCount: 0,
		_callCount: 1,
		_results: null,
		_div: null,
		_table: null,
		_columns: [
			{id:"personName", name:"Patient Name"},
			{id:"encounterType", name:"Encounter Type"},
			{id:"formName", name:"Form"},
			{id:"providerName", name:"Provider"},
			{id:"location", name:"Location"},
			{id:"encounterDateString", name:"Encounter Date"}
		],
		
		_create: function() {
		    var self = this,
		        o = self.options,
		        el = self.element,
		        div = el.append(encounterSearch_div),
		        lbl = div.find("#searchLabelNode"),
		        input = div.find("#inputNode"),
		        table = div.find("#openmrsSearchTable");
		    	checkBox = div.find("#includeVoided");
		    	spinnerObj = div.find("#spinner");
		    	spinnerObj.css("visibility", "hidden");
		    	spinnerObj.attr("src", openmrsContextPath+"/images/loading.gif");
		    
		    this._div = div;

		    lbl.text(o.searchLabel);
		    
		    if(o.showIncludeVoided) {
		    	var tmp = div.find("#includeVoided");
			    tmp.before("<label for='includeVoided'>" + o.includeVoidedLabel + "</label>");
		    	tmp.show();
		    }
		    
		    //when the user checks/unchecks the includeVoided checkbox, trigger a search
		    checkBox.click(function() {   	
		    	if($j.trim(input.val()) != '')
		    		self._doSearch(input.val());
		    	//to maintain keyDown and keyUp events since they are only fired when the input box has focus
		    	input.focus();		    	
			});
		    
		    //this._trigger('initialized');
		    input.keyup(function(event) {
		    	//catch control keys
		    	//LEFT(37), UP(38), RIGHT(39), DOWN(40), ENTER(13), HOME(36), END(35)
		    	var kc = event.keyCode;
		    	if(((kc >= 35) && (kc <= 40)) || (kc == 13)) {
			    	switch(event.keyCode) {
				    	case 35:
				    		self._doKeyEnd();
				    		break;
				    	case 36:
				    		self._doKeyHome();
				    		break;
				    	case 37:
				    		self._doKeyLeft();
				    		break;
				    	case 38:
				    		self._doKeyUp();
				    		break;
				    	case 39:
				    		self._doKeyRight();
				    		break;
				    	case 40:
				    		self._doKeyDown();
				    		break;
				    	case 13:
				    		self._doKeyEnter();
				    		break;
			    	}
			    	//kill the event
			    	event.stopPropagation();
			    	return;
		    	}		    	
		    	
		    	if(self.onCharTyped) {
		    		self.onCharTyped(self, event.keyCode);
		    	}
		    	
	        	var text = $j.trim(input.val());
	    		if(text.length >= o.minLength) {
	    			self._doSearch(text);
	    		}
	    		else {
	    			self._table.fnClearTable();
	    			if($('#openmrsSearchTable_paginate').is(":visible")){
		    	    	$('#openmrsSearchTable_paginate').hide();
		    		}
	    		}
	    		return true;
		    });
		    
			//setup 'openmrsSearchTable'
			div.find(".openmrsSearchDiv").hide();
			
			//TODO columns need to be built: id='searchTableHeader'
		    this._table = table.dataTable({
		    	bFilter: false,
		    	bLengthChange: false,
		    	bSort: false,
		    	sPaginationType: "full_numbers",
		    	aoColumns: this._makeColumns(),
		    	iDisplayLength: 10,
		    	numberOfPages: 0,
		    	currPage: 0,
		    	fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {					
		    		//register mouseover/out events handlers to have row highlighting
		    		$(nRow).bind('mouseover', function(){
		    			//for only loaded rows
		    			if(self._results[iDisplayIndexFull])
		    				$(this).addClass('tr_row_highlight');						
					});
		    		$(nRow).bind('mouseout', function(){
		    			if(self._results[iDisplayIndexFull])
		    				$(this).removeClass('tr_row_highlight');	
		    	    });
		    				    		
		    		//draw a strike through for all voided encounters that have been loaded
		    		if(self._results[iDisplayIndexFull] && self._results[iDisplayIndexFull].voided){		    			
		    			$(nRow).children().each(function(){		    				
		    				$(this).addClass('voided');
		    			}); 
		    		}
		    		
		    		if(self.options.selectionHandler) {
		    			$(nRow).bind('click', function() {
		    				//Register onclick handlers to each row that is loaded
		    				if(self._results[iDisplayIndexFull])
		    					self._doSelected(iDisplayIndexFull, self._results[iDisplayIndexFull]);
		    			});
		    		}
		    		
		    		return nRow;
		    	}				
		    });		    
		},
		
		_makeColumns: function() {
			return $.map(this._columns, function(c) {
				return { sTitle: c.name };
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
				//than the minimun characters, this can arise when user presses backspace relatively fast
				//yet there were some intermediate calls that might have returned results
				var currInput = $j.trim($j("#inputNode").val());
				if(currInput == '' || currInput.length < self.options.minLength){
					if($('#openmrsSearchTable_paginate').is(":visible")){
			    	    	$('#openmrsSearchTable_paginate').hide();
					}
					return;
				}
				self._doHandleResults(matchCount);
				
				//FETCH THE REST OF THE RESULTS IF encounter COUNT is greater than the number of rows to display per page
				if(matchCount > self._table.fnSettings()._iDisplayLength){
					//This forces the browser to spawn a new thread to avoid "Unresponsive script errors"
					//in case server responses delay
					setTimeout(function(){
						self.options.searchHandler(searchText, self._updateBlankRows(curCallCount),
							{includeVoided: self.options.showIncludeVoided && checkBox.attr('checked'),
							start: self._table.fnSettings()._iDisplayLength, length: null})
					}, 50);
				}
			};
		},
			
		_doHandleResults: function(matchCount) {
			this.curRowSelection = null;
				
			if(this.options.resultsHandler) {
				this.options.resultsHandler(this._results);
			}
			else {
				this._buildDataTable(matchCount);
				//reset to show first page always
				this._table.fnPageChange('first');
				
				if((this._results != null) && (this._results.length > 0) && (typeof this._results[0] != 'string'))
					this.updatePageInfo();
			}	
		},
		
		_buildDataTable: function(matchCount) {
			this._fireEvent('beforeDataTable');
			
			this._table.fnClearTable();
			if((this._results != null) && (this._results.length > 0) && (typeof this._results[0] == 'string')) {
				//error
				//hide pagination buttons
	    	    if($('#openmrsSearchTable_paginate')){
	    	    	$('#openmrsSearchTable_paginate').hide();
				}
				return;
			}
			
			var d = new Array();
			for(var r in this._results) {
				d[r] = this._buildRow(this._results[r]);
			}
			//add some blank rows to the datatable for the remaining rows to be fetched
			var blankRows = matchCount - this._table.fnSettings()._iDisplayLength;			
			var pos = this._table.fnSettings()._iDisplayLength;			
			var emptyData = {"personName":"Loading data....", "":"", "":"", "":"", "":"", "":""};
			for(var x = 0; x < blankRows; x++){
				d[pos] = this._buildRow(emptyData);				
				pos++;
			}
			
			this._table.fnAddData(d);
			if(matchCount % this._table.fnSettings()._iDisplayLength == 0)
				this._table.numberOfPages = matchCount/this._table.fnSettings()._iDisplayLength;
			else
				this._table.numberOfPages = Math.floor(matchCount/this._table.fnSettings()._iDisplayLength)+1;
			
			this._table.currPage = 1;
			
    	    if(matchCount <= this._table.fnSettings()._iDisplayLength){
    	    	$('#openmrsSearchTable_paginate').hide();
			}else if(!$('#openmrsSearchTable_paginate').is(":visible")){
				//if the buttons were previously hidden, show them
				$('#openmrsSearchTable_paginate').show(); 
			}
			this._div.find(".openmrsSearchDiv").show();
			
			this._fireEvent('afterDataTable');
		},
		
		_buildRow: function(rowData) {
			var cols = this._columns;
			return $.map(cols, function(c) {
				var data = rowData[c.id];
				if(data == null) 
					data = " ";
				
				return data;
			});
		},
		
		_fireEvent: function(eventType, data) {
			//TODO also pass 'this'
		},
		
		_doKeyDown: function() {
			if(!this._div.find(".openmrsSearchDiv").is(":visible")) {
				return;
			}
			
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
				//TODO might want to recycle and go to the beginning again??
				return;
			}
			
			if(prevRow != null) {
				$(this._table.fnGetNodes()[prevRow]).removeClass("row_highlight");
			}
			
			//If the selected row is the first one on the next page, flip over to its page
			if(this.curRowSelection != 0 && (this.curRowSelection % this._table.fnSettings()._iDisplayLength) == 0) {
				this._table.fnPageChange('next');
			}
			
			$(this._table.fnGetNodes()[this.curRowSelection]).addClass("row_highlight");
		},
		
		_doKeyUp: function() {
			if(!this._div.find(".openmrsSearchDiv").is(":visible")) {
				return;
			}
			
			var prevRow = this.curRowSelection;
			if(this.curRowSelection == null) {
				this.curRowSelection = this._table.fnGetData().length-1;
				//if the row has yet been populated, don't highlight it, stay on the first page
				if(!this._results[this.curRowSelection]){
					this.curRowSelection = null;
					return;
				}
				this._table.fnPageChange('last');
			}
			else {
				this.curRowSelection--;
			}
			
			if(this.curRowSelection < 0) {
				this.curRowSelection++;//redact it
				//cant go any further so return
				//TODO might want to recycle and go to the beginning again??
				return;
			}
			
			if(prevRow != null) {
				$(this._table.fnGetNodes()[prevRow]).removeClass("row_highlight");

				if(prevRow % this._table.fnSettings()._iDisplayLength == 0) {
					this._table.fnPageChange('previous');
				}
			}
			
			$(this._table.fnGetNodes()[this.curRowSelection]).addClass("row_highlight");
		},
		
		_doKeyRight: function() {
			if(!this._div.find(".openmrsSearchDiv").is(":visible")) {
				return;
			}

			this._table.fnPageChange('next');
			if(++this._table.currPage > this._table.numberOfPages)
				this._table.currPage = this._table.numberOfPages;
			this.updatePageInfo();
		},
		
		_doKeyLeft: function() {
			if(!this._div.find(".openmrsSearchDiv").is(":visible")) {
				return;
			}

			this._table.fnPageChange('previous');
			if(--this._table.currPage < 1)
				this._table.currPage = 1;
			this.updatePageInfo();
		},
		
		_doKeyEnter: function() {
			if(!this._div.find(".openmrsSearchDiv").is(":visible")) {
				return;
			}
			
			if(this.curRowSelection != null) {
				this._doSelected(this.curRowSelection, this._results[this.curRowSelection]);
			}
		},
		
		_doKeyHome: function() {
			if(!this._div.find(".openmrsSearchDiv").is(":visible")) {
				return;
			}

			this._table.fnPageChange('first');
		},
		
		_doKeyEnd: function() {
			if(!this._div.find(".openmrsSearchDiv").is(":visible")) {
				return;
			}

			this._table.fnPageChange('last');
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
		
	    updatePageInfo: function() {
	    	var pageString = (this._table.numberOfPages == 1) ? "Page" : "Pages";
	    	$('#openmrsSearchTable_info').append(" ( "+this._table.numberOfPages+" "+pageString+" )");
		},
		
		//This function replaces the blank rows in the datatable with actual data returned by the 
		//second ajax call that retrieves the remaining rows
		_updateBlankRows: function(curCallCount2){
			var self = this;
			return function(results) {
				spinnerObj.css("visibility", "hidden");
				//Don't display results from delayed ajax calls when the input box is blank or has less 
				//than the minimun characters
				var currInput = $j.trim($j("#inputNode").val());
				if(currInput == '' || currInput.length < self.options.minLength){
					return;
				}
				
				var data = results["objectList"];								
				
				//Since this method is called on the second ajax call to return the remaining results,
				//therefore (self._lastCallCount == curCallCount) so it will pass if no later ajax call were made				
				if(curCallCount2 && self._lastCallCount > curCallCount2) {
					//stop old ajax calls from over writing later ones
					return;
				}
				
				var pos = self._table.fnSettings()._iDisplayLength;
				for(var x in data) {
					currentData = data[x];
					newRowData = self._buildRow(currentData);
					//add the rest of this data to the results list
					self._results[pos] = currentData;
					self._table.fnUpdate(newRowData, pos);		
					pos++;
				}
			};
		},
		
		destroy: function() {
			$j.Widget.prototype.destroy.apply(this, arguments); // default destroy
			// now do other stuff particular to this widget
		}
	});
})(jQuery);
