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
	DWREncounterService.findEncounters(text, opts.includeVoided, resultHandler);
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
		    	
	        	var text = input.val();
	    		if(text.length >= o.minLength) {
	    			self._doSearch(text);
	    		}
	    		else {
	    			self._table.fnClearTable();
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
		    	iDisplayLength: 10
		    });
		    
		    $('#openmrsSearchTable').hover(function() {
		    	$(self._table.fnGetNodes()[self.curRowSelection]).removeClass("row_highlight");
			}, function() {
				$(self._table.fnGetNodes()[self.curRowSelection]).addClass("row_highlight");
			});
		    
		    this._table.find("tbody tr").live('click', function() {
				var index = table.fnGetPosition(this);
				self._doSelected(index, self._results[index]);
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
				this.options.searchHandler(text, this._handleResults(storedCallCount, this.options.minLength), {includeVoided: tmpIncludeVoided});
			}
		},
		
		/** returns a closure for the returned results */
		_handleResults: function(curCallCount, minLength) {
			var self = this;
			return function(results) {
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
				if(currInput == '' || currInput.length < minLength)
					return;
				self._doHandleResults(results);
			};
		},
			
		_doHandleResults: function(results) {
			this._results = results;
			this.curRowSelection = null;
				
			if(this.options.resultsHandler) {
				this.options.resultsHandler(results);
			}
			else {
				this._buildDataTable(results);
				//reset to show first page always
				this._table.fnPageChange('first');
			}	
		},
		
		_buildDataTable: function(results) {
			this._fireEvent('beforeDataTable');
			
			this._table.fnClearTable();
			if((results != null) && (results.length > 0) && (typeof results[0] == 'string')) {
				//error				
				return;
			}
			
			var d = new Array();
			for(var r in results) {
				d[r] = this._buildRow(results[r], results[r].voided);
			}			

			this._table.fnAddData(d);
			this._div.find(".openmrsSearchDiv").show();
			
			this._fireEvent('afterDataTable');
		},
		
		_buildRow: function(rowData, isVoided) {
			var cols = this._columns;
			return $.map(cols, function(c) {
				var data = rowData[c.id];
				if(data == null) 
					data = " ";				
				else if(isVoided){
					//draw a strike through line for the voided encounters
					data = "<span class='voided'>"+data+"</span>";
				}
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
			
			if(this.curRowSelection >= this._results.length) {
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
				this.curRowSelection = this._results.length-1;
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
		},
		
		_doKeyLeft: function() {
			if(!this._div.find(".openmrsSearchDiv").is(":visible")) {
				return;
			}

			this._table.fnPageChange('previous');
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
	    
		destroy: function() {
			$j.Widget.prototype.destroy.apply(this, arguments); // default destroy
			// now do other stuff particular to this widget
		}
	});
})(jQuery);
