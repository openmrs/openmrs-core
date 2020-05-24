/**
 * Helper function to get started quickly, below are the required arguments:
 * <ul>
 * <li>div - Container element for the search widget</li>
 * <li>showIncludeVoided - If the includeVoided/includeRetired checkbox should be displayed</li>
 * <li>searchHandler - The function to be triggered to fetch results from server,
 * 		should return the projected count and results</li>
 * <li>selectionHandler - The function to trigger when the user selects a row in the results table</li>
 * <li>fieldHeaders - Array of fieldName and column header maps)</li>
 * <li>opts - A map that can contain any option of the openmrsSearch widget, see below for the options</li>
 * </ul>
 * The parameters 'showIncludeVoided' and 'selectionHandler' are options to the widget but
 * given here as simple params.
 *
 * These approaches below are the same:<br/><br/>
 * <b>Approach 1:</b>
 * <pre>
 *  $j(document).ready(function() {
 *		$j("#elementId").openmrsSearch({
 *			searchLabel:'<openmrs:message code="General.search"/>',
 *			showIncludeVoided: true,
 *			displayLength: 5,
 *			minLength: 3,
 *			columnWidths: ["15%","15%","15%","15%","15%", "25%"],
 *			columnRenderers: [null, null, null, null, null, null], 
 *			columnVisibility: [true, true, true, true, true, true],
 *			searchHandler: doSearchHandler,
 *			selectionHandler: doSelectionHandler,
 *			fieldsAndHeaders: [
 *				{fieldName:"field1", header:"Header1"},
 *				{fieldName:"fiels2", header:"Header2"},
 *				{fieldName:"field3", header:"Header3"},
 *				{fieldName:"field4", header:"Header4"},
 *				{fieldName:"fiels5", header:"Header5"},
 *				{fieldName:"field6", header:"Header6"}
 *			]
 *		});
 *	});
 *</pre>
 *<br/><br/>
 *<b>Approach 2:</b>
 *<pre>
 *	new OpenmrsSearch("elementId", true, doSearchHandler, doSelectionHandler,
 *			[	{fieldName:"field1", header:"Header1"},
 *				{fieldName:"fiels2", header:"Header2"},
 *				{fieldName:"field3", header:"Header3"},
 *				{fieldName:"field4", header:"Header4"},
 *				{fieldName:"fiels5", header:"Header5"},
 *				{fieldName:"field6", header:"Header6"}
 *			],
 *			{searchLabel: '<openmrs:message code="General.search"/>',
 *              searchPlaceholder:'<openmrs:message code="general.search" javaScriptEscape="true"/>',
 *              displayLength: 5,
 *				minLength: 3, 
 *				columnWidths: ["15%","15%","15%","15%","15%", "25%"],
 *				columnRenderers: [null, null, null, null, null, null], 
 *				columnVisibility: [true, true, true, true, true, true]}
 *		);
 *	});
 *	</pre>
 **/
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
 *   minLength:int (default: 1) The minimum number of characters required to trigger a search, this is ignored if 'doSearchWhenEmpty' is set to true
 *   searchLabel:string (default: omsgs.searchLabel) The text to be used as the label for the search textbox
 *   includeVoidedLabel:string (default: omsgs.includeVoided) The text to be used as the label for the 'includeVoided' checkbox
 *   showIncludeVoided:bool (default: false) - Specifies whether the 'includeVoided' checkbox and label should be displayed
 *   includeVerboseLabel:string (default: omsgs.includeVerbose) The text to be used as the label for the 'includeVerbose' checkbox
 *   showIncludeVerbose:bool (default: false) Specifies whether the 'includeVerbose' checkbox and label should be displayed
 *   searchHandler:function(text, resultHandler, options) (default:null) The function to be called to fetch search results from the server
 *   resultsHandler:function(results) (default:null) The function to be called
 *   selectionHandler:function(index, rowData)
 *   fieldsAndHeaders: Array of fieldNames and column header maps
 *   displayLength: int (default: 10)
 *   columnWidths: an array of column widths, the length of the array should be equal to the number of columns
 *   columnRenderers: array of fnRender for each column
 *   columnVisibility: array of bVisible values for each column
 *   initialData:The initial data to be displayed e.g if it is an encounter search, it should be an encounter list
 *   searchPhrase: The phrase to be set in the search box so that a search is triggered on page load to display initial items
 *   doSearchWhenEmpty: If it is set to true, it lists all items initially and filters them with the given search phrase. (default:false)
 *   verboseHandler: function to be called to return the text to display as verbose output
 *   attributes: Array of names for attributes types to display in the list of results
 *   showSearchButton: Boolean, indicating whether to use search button for immediate search
 *   lastSearchParams: Object with properties lastSearchText, includeVoided and includeVerbose, to preserve data with browser back button
 *
 * The styling on this table works like this:
 * <pre>
 *#openmrsSearchTable tbody tr:hover {
 *	background-color: #F0E68C;
 *}
 *</pre>
 */
(function($j) {
    var openmrsSearch_div = 
    '<span>'+
        '<span>'+
            '<table cellspacing="0" width="100%">'+
                '<tr>'+
                    '<td align="left">'+
                        '<span id="searchLabelNode"></span>'+
                        '<input type="text" value="" id="inputNode" autocomplete="off" placeholder=" " />'+
                        '<img id="spinner" src="" /><input type="checkbox" style="display: none" id="includeVoided" />&nbsp;&nbsp;'+
                        '<input type="checkbox" style="display: none" id="includeVerbose" />'+
                        '<span id="loadingMsg"></span>'+
                        '<span id="minCharError" class="error"></span>'+
                    '</td>'+
                    '<td align="right"><span id="pageInfo"></span></td>'+
                '</tr>'+
                '<tr>'+
                    '<td colspan="2" align="left"><span id="searchWidgetNotification"></span></td>'+
                '</tr>'+
            '</table>'+
        '</span>'+
        '<span class="openmrsSearchDiv">'+
            '<table id="openmrsSearchTable" cellpadding="2" cellspacing="0" style="width: 100%">'+
                '<thead id="searchTableHeader">'+
                    '<tr></tr>'+
                '</thead>'+
                '<tbody></tbody>'+
            '</table>'+
        '</span>'+
    '</span>';

    var BATCH_SIZE = gp.maxSearchResults;
    var SEARCH_DELAY = gp.searchDelay;//time interval in ms between keyup and triggering the search off
    if(!Number(BATCH_SIZE))
        BATCH_SIZE = 200;
    var ajaxTimer = null;
    var buffer = null;
    var inSerialMode = Boolean(gp.searchRunInSerialMode);
    var MAXIMUM_NUMBER_OF_RESULTS = gp.maximumResults;
    if(!Number(MAXIMUM_NUMBER_OF_RESULTS))
        MAXIMUM_NUMBER_OF_RESULTS = 2000;
    $j.widget("ui.openmrsSearch", {
        plugins: {},
        options: {
            minLength: omsgs.minSearchCharactersGP,
            searchLabel: ' ',
            includeVoidedLabel: omsgs.includeVoided,
            includeVerboseLabel: omsgs.showVerbose,
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
        _searchDelayTimer: null,

        _create: function() {
            var self = this,
                o = self.options,
                el = self.element,
                div = el.append(openmrsSearch_div),
                lbl = div.find("#searchLabelNode"),
                input = div.find("#inputNode"),
                table = div.find("#openmrsSearchTable");
            checkBox = div.find("#includeVoided");
            verboseCheckBox = div.find("#includeVerbose");
            spinnerObj = div.find("#spinner");
            spinnerObj.css("visibility", "hidden");
            spinnerObj.prop("src", openmrsContextPath+"/images/loading.gif");
            minCharErrorObj = div.find("#minCharError");
            minCharErrorObj.html(omsgs.minCharRequired.replace("_REQUIRED_NUMBER_", o.minLength));
            notification = div.find("#searchWidgetNotification");
            loadingMsgObj = div.find("#loadingMsg");
            showSearchButton = o.showSearchButton ? true : false;
            lastSearchParams = (o.lastSearchParams != null) ? o.lastSearchParams : null;

            this._div = div;

            lbl.text(o.searchLabel);

            //3 should be the minimum number of results to display per page
            if(o.displayLength < 3)
                o.displayLength = 3;

            // If need search button
            if (showSearchButton) {
                input.after("<input type='button' id='searchButton' name='searchButton' value='" + omsgs.searchLabel + "' />");
                $j('#searchButton').click(function() {
                    if ($j.trim(input.val()) != '' || self.options.doSearchWhenEmpty) {
                        //if there is any delay in progress, cancel it
                        if(self._searchDelayTimer != null) {
                            window.clearTimeout(self._searchDelayTimer);
                        }
                        self._doSearch($j.trim(input.val()));
                        input.focus();
                    }
                });
            }

            if(o.showIncludeVoided) {
                var tmp = div.find("#includeVoided");
                tmp.after("<label for='includeVoided'>" + o.includeVoidedLabel + "</label>");
                tmp.show();

                //when the user checks/unchecks the includeVoided checkbox, trigger a search
                checkBox.click(function() {
                    if($j.trim(input.val()) != '' || self.options.doSearchWhenEmpty)
                        self._doSearch(input.val());
                    else{
                        if(spinnerObj.css("visibility") == 'visible')
                            spinnerObj.css("visibility", "hidden");
                        //if the user is viewing initial data, ignore
                        if($j.trim(input.val()) != ''){
                            $j("#minCharError").css("visibility", "visible");
                            $j(".openmrsSearchDiv").hide();
                        }
                        if($j('#pageInfo').css("visibility") == 'visible')
                            $j('#pageInfo').css("visibility", "hidden");
                    }
                    //to maintain keyDown and keyUp events since they are only fired when the input box has focus
                    input.focus();
                });

                if(userProperties.showRetired)
                    tmp.prop('checked', true);
            }

            if(o.showIncludeVerbose) {
                var tmp = div.find("#includeVerbose");
                tmp.after("<label for='includeVerbose'>" + o.includeVerboseLabel + "</label>");
                tmp.show();

                //when the user checks/unchecks the includeVerbose checkbox, show/hide the verbose rows
                verboseCheckBox.click(function() {
                    $j('.verbose').toggle();
                    input.focus();
                });

                if(userProperties.showVerbose)
                    tmp.prop('checked', true);
            }

            //this._trigger('initialized');
            input.keyup(function(event) {
                //catch control keys
                //LEFT(37), UP(38), RIGHT(39), DOWN(40), ENTER(13), HOME(36), END(35), PAGE UP(33), PAGE DOWN(34)
                var kc = event.keyCode;
                if(((kc >= 33) && (kc <= 40)) || (kc == 13)) {
                    if(!(self._div.find(".openmrsSearchDiv").css("display") != 'none') && ($j.trim(input.val()) == '')) {
                        return true;
                    }
                    if(kc == 13) {
                        //if there is any delay in progress, cancel it
                        if(self._searchDelayTimer != null) {
                            window.clearTimeout(self._searchDelayTimer);
                        }
                        self._doKeyEnter();
                    }

                    //kill the event
                    event.stopPropagation();

                    return false;
                }
                //ignore the following keys SHIFT(16), ESC(27), CAPSLOCK(20), CTRL(17), ALT(18), SPACE(32), ALT_TAB(9)
                else if((kc >= 16 && kc <= 18) || kc == 20 || kc == 27 || kc == 32 || kc == 9)
                    return false;

                $j(notification).html(" ");
                if(self.onCharTyped) {
                    self.onCharTyped(self, event.keyCode);
                }

                var text = $j.trim(input.val());
                if(self._textInputTimer != null){
                    window.clearTimeout(self._textInputTimer);
                }

                if(text == '' && !self.options.doSearchWhenEmpty){
                    $j('#pageInfo').css("visibility", "hidden");
                    $j("#spinner").css("visibility", "hidden");
                    $j("#minCharError").css("visibility", "hidden");
                    $j(".openmrsSearchDiv").hide();
                    loadingMsgObj.html("");

                    return false;
                }

                //This discontinues any further ajax SUB calls from the last triggered search
                if(!inSerialMode && ajaxTimer)
                    window.clearInterval(ajaxTimer);

                var searchDelay = SEARCH_DELAY;
                if(text.length < o.minLength && !self.options.doSearchWhenEmpty) {
                    searchDelay = searchDelay * 2;
                }
                //if there is any delay in progress, cancel it
                if(self._searchDelayTimer != null)
                    window.clearTimeout(self._searchDelayTimer);

                //wait for a couple of milliseconds, if the user isn't typing anymore chars before triggering search
                //this minimizes the number of un-necessary calls made to the server for first typists
                self._searchDelayTimer = window.setTimeout(function(){
                    if($j('#pageInfo').css("visibility") == 'visible')
                        $j('#pageInfo').css("visibility", "hidden");

                    if($j("#minCharError").css("visibility") == 'visible')
                        $j("#minCharError").css("visibility", "hidden");

                    //Once the very first search is triggered, we need to clear the initial data
                    //if any was added because it is no longer relevant until the page is reloaded
                    if(self.options.initialData)
                        self.options.initialData = null;

                    self._doSearch(text);
                }, searchDelay);

                return true;
            });

            //catch control keys to stop the cursor in the input box from moving.
            input.keydown(function(event) {
                //UP(38), DOWN(40), PAGE UP(33), PAGE DOWN(34)
                var kc = event.keyCode;
                if(kc == 33 || kc == 34 || kc == 38 || kc == 40) {
                    if(!(self._div.find(".openmrsSearchDiv").css("display") != 'none')) {
                        return true;
                    }

                    switch(event.keyCode) {
                        case 33:
                            self._doPageUp();
                            break;
                        case 34:
                            self._doPageDown();
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

            /*=============== Begin Processing of some initialization stuff  =======================*/

            //on widget load the focus should be on the search box if there are no
            //other enabled and visible text boxes on the page
            var inputs = document.getElementsByTagName("input");
            var numberOfTextInputs = 0;
            for(var x in inputs){
                var inputField = inputs[x];
                if(inputField && inputField.type == 'text' && $j(inputField).prop("disabled") == false &&
                    $j(inputField).is(":visible") && $j(inputField).css("visibility") != "hidden")
                    numberOfTextInputs++;
            }

            if(numberOfTextInputs == 1)
                input.focus();

            if(self.options.initialData)
                self._results = self.options.initialData;
            else
                div.find(".openmrsSearchDiv").hide();

            //Add the placeholder text to the Search field
            if(self.options.searchPlaceholder){
                //The value should not contain line feeds or carriage returns.
                var textShown=self.options.searchPlaceholder.toString().replace(/(\r\n|\n|\r)/gm,"");
                $j('#inputNode').prop('placeHolder', textShown);
            }

            //Create an array of arrays from the array of objects if we have any initial data
            if(self.options.initialData){
                self.options.initialRows = new Array();//array to hold the arrays of initial row data
                var cols = self.options.fieldsAndHeaders;
                for(var i in self.options.initialData){
                    var obj = self.options.initialData[i];
                    //create an array to hold each initial row's column values
                    var iRowData = new Array();
                    $j.map(cols, function(c) {
                        iRowData.push(obj[c.fieldName]);
                    });

                    self.options.initialRows.push(iRowData);
                }
            }

            /*=============== End Processing of some initialization stuff  =======================*/

            //TODO columns need to be built: id='searchTableHeader'
            this._table = table.dataTable({
                bFilter: false,
                bLengthChange: true,
                bSort: false,
                sPaginationType: "full_numbers",
                aaData: self.options.initialRows,
                aoColumns: this._makeColumns(),
                iDisplayLength: self.options.displayLength,
                numberOfPages: 0,
                bAutoWidth: false,
                bJQueryUI: true,
                sDom: 't<"fg-button ui-helper-clearfix"ip><"ui-helper-clearfix"l>',
                oLanguage: {
                    "sInfo": omsgs.sInfoLabel,
                    "oPaginate": {"sFirst": omsgs.first, "sPrevious": omsgs.previous, "sNext": omsgs.next, "sLast": omsgs.last},
                    "sZeroRecords": omsgs.noMatchesFound,
                    "sInfoEmpty": " ",
                    "sLengthMenu": omsgs.showNumberofEntries
                },

                /* Called to toggle the verbose output */
                fnDrawCallback : function(oSettings){
                    //we have nothing to hide
                    if(!self.options.showIncludeVerbose || !self._table || self._table.fnGetNodes().length == 0)
                        return;
                    pageRowCount = oSettings._iDisplayStart+oSettings._iDisplayLength;
                    for(var i = oSettings._iDisplayStart; i < pageRowCount; i++){
                        if(self.options.showIncludeVerbose && self.options.verboseHandler){
                            rowData = self._results[i];
                            verboseText = self.options.verboseHandler(i, rowData);
                            nRow = self._table.fnGetNodes()[i];
                            if(!nRow)
                                break;

                            verboseRow = self._table.fnOpen( nRow, verboseText, 'verbose' );
                            $j(verboseRow).css('background-color', $j(nRow).css('background-color'));
                            $j(verboseRow).hover(
                                function(){
                                    $j(nRow).css("cursor", "pointer");
                                    if(self.curRowSelection != null){
                                        currNode = self._table.fnGetNodes()[self.curRowSelection];
                                        self._unHighlightRow(currNode);
                                        self._unHighlightVerboseRow(currNode.nextSibling);
                                    }
                                    self.hoverRowSelection = i;
                                    $j(this.previousSibling).addClass('row_highlight');
                                }, function(){
                                    if(self.curRowSelection != null){
                                        currNode = self._table.fnGetNodes()[self.curRowSelection];
                                        $j(currNode).addClass("row_highlight");
                                        $j(currNode.nextSibling).addClass("row_highlight");
                                    }
                                    self.hoverRowSelection = null;
                                    dataRow = this.previousSibling;
                                    //If this is the current highlighted row with up/down arrows and at the sametime
                                    //was hovered over, keep it highlighted
                                    if(self.curRowSelection != null && self._table.fnGetPosition(dataRow) == self.curRowSelection)
                                        return;
                                    $j(dataRow).removeClass('row_highlight');
                                }
                            );
                            //draw a strike through for all voided/retired objects that have been loaded
                            if(rowData && (rowData.voided || rowData.retired)){
                                $j(verboseRow).children().each(function(){
                                    $j(this).addClass('voided');
                                });
                            }
                            if(self.options.selectionHandler) {
                                $j(verboseRow).unbind('click').bind('click', function() {
                                    rowIndex = self._table.fnGetPosition(this.previousSibling);
                                    //Onclick handlers should work on the verbose row too
                                    self._doSelected(rowIndex, self._results[rowIndex]);
                                });
                            }
                        }}

                    if(!$j(verboseCheckBox).prop('checked')){
                        $j('.verbose').hide();
                    }
                },

                fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
                    //register hover event handlers to unhighlight the current row highlighted with up/down keys
                    $j(nRow).hover(
                        function(){
                            if(self.curRowSelection != null){
                                currentNode = self._table.fnGetNodes()[self.curRowSelection];
                                self._unHighlightRow(currentNode);
                            }
                            self.hoverRowSelection = iDisplayIndexFull;
                            if(self.options.showIncludeVerbose && $j(verboseCheckBox).prop('checked'))
                                $j(this.nextSibling).addClass('row_highlight');
                        }, function(){
                            if(self.curRowSelection != null){
                                currentNode = self._table.fnGetNodes()[self.curRowSelection];
                                $j(currentNode).addClass("row_highlight");
                                if(self.options.showIncludeVerbose)
                                    $j(currentNode.nextSibling).addClass('row_highlight');
                            }
                            self.hoverRowSelection = null;
                            if(self.curRowSelection != null && self._table.fnGetPosition(this) == self.curRowSelection)
                                return;
                            if(self.options.showIncludeVerbose && $j(verboseCheckBox).prop('checked'))
                                $j(this.nextSibling).removeClass('row_highlight');
                        }
                    );

                    var currItem = self._results[iDisplayIndexFull];
                    //draw a strike through for all voided/retired objects that have been loaded
                    if(currItem && (currItem.voided || currItem.retired)){
                        $j(nRow).children().each(function(){
                            $j(this).addClass('voided');
                        });
                    }

                    if(self.options.selectionHandler) {
                        $j(nRow).unbind('click').bind('click', function() {
                            //Register onclick handlers to each row
                            self._doSelected(iDisplayIndexFull, currItem);
                        });
                    }

                    return nRow;
                }
            });

            // Browser back button support, for preserve data
            if (lastSearchParams !== null) {
                $j('#inputNode').val(lastSearchParams.lastSearchText);
                if (lastSearchParams.includeVoided == 1) {
                    $j('#includeVoided').attr('checked','checked');
                }
                if (lastSearchParams.includeVerbose == 1) {
                    $j('#includeVerbose').attr('checked','checked');
                }
                var keyEvent = jQuery.Event("keyup");

                var codex = 13;

                // Patient search doesn't support for Enter Key. So invoke keyup
                // event with last character of searched text.
                if (el.attr('id') === "findPatients") {
                    var text = $j('#inputNode').val();
                    codex = $j('#inputNode').val().charCodeAt($j('#inputNode').val().length - 1);
                }

                keyEvent.keyCode = codex;
                $j("#inputNode").trigger(keyEvent);
            }

            //register an onchange event handler for the length dropdown so that we don't lose
            //the row highlight when the user makes changes to the length
            var selectElement = document.getElementById('openmrsSearchTable_length').getElementsByTagName('select')[0];
            if(selectElement){
                $j(selectElement).change(function(){
                    input.focus();
                });
            }

            //if we have initial data, set the current page and number of pages for the row highlight not to break
            if(self.options.initialData){
                var initialRowCount = self.options.initialData.length;
                if(initialRowCount % self._table.fnSettings()._iDisplayLength == 0)
                    self._table.numberOfPages = initialRowCount/self._table.fnSettings()._iDisplayLength;
                else
                    self._table.numberOfPages = Math.floor(initialRowCount/self._table.fnSettings()._iDisplayLength)+1;

            } else if(self.options.searchPhrase || self.options.doSearchWhenEmpty) {
                if (self.options.searchPhrase == null) {
                    self.options.searchPhrase = "";
                }
                $j(input).val(self.options.searchPhrase).keyup();
            }
        },

        _makeColumns: function() {
            var self = this;
            var fieldsAndHeaders = self.options.fieldsAndHeaders;
            var columnIndex = 0
            columData = $j.map(fieldsAndHeaders, function(c) {
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

            //add attribute column headers if any
            if(self.options.attributes){
                $j.each(self.options.attributes, function(index, a) {
                    attribColWidth = null;
                    attribColFnRenderer = null;
                    attribColVisibility = (a.columnVisible != false);

                    if(a.columnWidth)
                        attribColWidth = a.columnWidth;
                    if(a.columnRenderer)
                        attribColFnRenderer = a.columnRenderer;

                    columData.push({ sTitle: a.header, sWidth: attribColWidth, fnRender: attribColFnRenderer, bVisible: attribColVisibility });
                });
            }

            return columData;
        },

        _doSearch: function(text) {
            if(this.options.searchHandler) {
                var tmpIncludeVoided = (this.options.showIncludeVoided && checkBox.prop('checked'));
                //associate the ajax call to be made to a call count number to track it , so on
                //its return we can identify it and determine if there are some later calls made
                //so that we can make sure older ajax calls donot overwrite later ones
                var storedCallCount = this._callCount++;
                spinnerObj.css("visibility", "visible");
                this._lastCallCount = storedCallCount;
                numberOfResults = this._table.fnSettings()._iDisplayLength;
                if(MAXIMUM_NUMBER_OF_RESULTS && MAXIMUM_NUMBER_OF_RESULTS > 0 &&
                    MAXIMUM_NUMBER_OF_RESULTS < numberOfResults){
                    numberOfResults = MAXIMUM_NUMBER_OF_RESULTS;
                }
                //First get data to appear on the first page
                this.options.searchHandler(text, this._handleResults(text, storedCallCount), true,
                    {includeVoided: tmpIncludeVoided, start: 0, length: numberOfResults});
            }
        },

        /** returns a closure for the returned results */
        _handleResults: function(searchText, curCallCount) {
            var self = this;
            return function(results) {
                //Don't display results from delayed ajax calls when the input box is blank or has less
                //than the minimum characters, this can arise when user presses backspace relatively fast
                //yet there were some intermediate calls that might have returned results
                var currInput = $j.trim($j("#inputNode").val());
                if(currInput == '' && !self.options.doSearchWhenEmpty){
                    if($j('#pageInfo').css("visibility") == 'visible')
                        $j('#pageInfo').css("visibility", "hidden");
                    if($j('#spinner').css("visibility") == 'visible')
                        $j("#spinner").css("visibility", "hidden");
                    $j(".openmrsSearchDiv").hide();
                    return;
                }

                if(curCallCount && self._lastCallCount > curCallCount) {
                    //stop old ajax calls from over writing later ones
                    return;
                }

                if(results["notification"])
                    $j(notification).html(results["notification"]);

                //this lets the specific widgets to signal that a new
                //search should be triggered for the specified text
                if(results["searchAgain"]){
                    newSearch = $j.trim(results["searchAgain"]);
                    if(newSearch != '' && newSearch != searchText)
                        self._doSearch(newSearch);
                    return;
                }

                var matchCount = results["count"];
                //if we have any hits, enforce the max results limit
                if(matchCount > 0 && MAXIMUM_NUMBER_OF_RESULTS > 0 && matchCount > MAXIMUM_NUMBER_OF_RESULTS)
                    matchCount = MAXIMUM_NUMBER_OF_RESULTS;

                self._results = results["objectList"];
                if(matchCount <= self._table.fnSettings()._iDisplayLength){
                    spinnerObj.css("visibility", "hidden");
                    loadingMsgObj.html("");
                }

                self._doHandleResults(matchCount, searchText);

                //FETCH THE REST OF THE RESULTS IF result COUNT is greater than the number of rows to display per page
                if(matchCount > self._table.fnSettings()._iDisplayLength){
                    //if the user wishes to fetch all results in one call without polling
                    //i.e their MyDWRService.findCountAndMyObjects() method always returns all hits
                    if(matchCount == self._results.length){
                        spinnerObj.css("visibility", "hidden");
                        loadingMsgObj.html("");
                        if(self._results.length % self._table.fnSettings()._iDisplayLength == 0)
                            self._table.numberOfPages = self._results.length/self._table.fnSettings()._iDisplayLength;
                        else
                            self._table.numberOfPages = Math.floor(self._results.length/self._table.fnSettings()._iDisplayLength)+1;

                        $j('#pageInfo').append(" - "+omsgs.pagesWithPlaceHolder.replace("_NUMBER_OF_PAGES_", self._table.numberOfPages));
                        return;
                    }

                    spinnerObj.css("visibility", "visible");
                    var startIndex = self._table.fnSettings()._iDisplayLength;
                    if(!inSerialMode){
                        //empty the arrays for the next set of subcalls
                        buffer = new Array;
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
            actualBatchSize = BATCH_SIZE;
            if((startIndex+BATCH_SIZE) > matchCount){
                //startIndex always matches the actual row count
                actualBatchSize = matchCount-startIndex;
            }

            this.options.searchHandler(searchText, this._addMoreRows(curCallCount, searchText, matchCount, startIndex, curSubCallCount),
                false, {includeVoided: this.options.showIncludeVoided && checkBox.prop('checked'),
                    start: startIndex, length: actualBatchSize});

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
            rRowData = $j.map(cols, function(c) {
                var data = rowData[c.fieldName];
                if(data == null)
                    data = " ";

                return data;
            });

            //include the attributes
            if(this.options.attributes){
                $j.each(this.options.attributes, function(index, a) {
                    attributeValue = rowData.attributes[a.name];
                    if(attributeValue == null)
                        attributeValue = '';

                    rRowData.push(attributeValue);
                });
            }

            return rRowData;
        },

        _fireEvent: function(eventType, data) {
            //TODO also pass 'this'
        },

        _doKeyDown: function() {
            //the user is using the mouse and they also want to use up/down keys?, dont support this
            if(this.hoverRowSelection != null)
                return;

            var prevRow = this.curRowSelection;
            //if we are on the last page, and the last row is highlighted, do nothing
            if(this._getCurrVisiblePage() == this._table.numberOfPages && prevRow >= (this._results.length-1) && this._results.length > 1)
                return;

            //only move the highlight to next row if it is currently on the visible page otherwise should be on first row
            if(this._isHighlightedRowOnVisiblePage()){
                this.curRowSelection++;

                //If the selected row is the first one on the next page, flip over to its page
                if(this.curRowSelection != 0 && (this.curRowSelection % this._table.fnSettings()._iDisplayLength) == 0) {
                    this._table.fnPageChange('next');
                }
            }

            if(prevRow != null && this._results.length > 1) {
                this._unHighlightRow(this._table.fnGetNodes()[prevRow]);
            }

            this._highlightRow();
        },

        _doKeyUp: function() {
            if(this.hoverRowSelection != null)
                return;

            //we are on the first page and the first row is already highlighted, do nothing
            if(this._table.fnSettings()._iDisplayStart == 0 && this.curRowSelection == 0)
                return;

            var prevRow = this.curRowSelection;
            //only move the highlight to prev row if it is currently on the visible page otherwise shoule be last row
            if(this._isHighlightedRowOnVisiblePage()){
                this.curRowSelection--;
                if(prevRow != null) {
                    if(prevRow % this._table.fnSettings()._iDisplayLength == 0)
                        this._table.fnPageChange('previous');
                }
            }else{
                //user just flipped pages, highlight the last row on the currently visible page
                if(this._getCurrVisiblePage() < this._table.numberOfPages){
                    this.curRowSelection = this._table.fnSettings()._iDisplayStart + this._table.fnSettings()._iDisplayLength - 1;
                }else{
                    //this is the last page, highlight the last item in the table
                    this.curRowSelection = this._results.length-1;
                }
            }

            if(prevRow != null){
                this._unHighlightRow(this._table.fnGetNodes()[prevRow]);
            }

            this._highlightRow();
        },

        _doPageUp: function() {
            this._table.fnPageChange('previous');
            if(this._isHighlightedRowOnVisiblePage())
                return;
            //update the highlight to go to last row on previous page
            this._highlightRowOnPageFlip();
        },

        _doPageDown: function() {
            this._table.fnPageChange('next');
            //if this is the last page and we already have a selected row, do nothing
            if( (this._getCurrVisiblePage() == this._table.numberOfPages) && this.curRowSelection > this._table.fnSettings()._iDisplayStart)
                return;

            this._highlightRowOnPageFlip();
        },

        _doKeyEnter: function() {

            var selectedRowIndex = null;
            if(this.hoverRowSelection != null) {
                selectedRowIndex = this.hoverRowSelection;
            }else if(this.curRowSelection != null){
                selectedRowIndex = this.curRowSelection;
            }

            if(selectedRowIndex != null) {
                this._doSelected(selectedRowIndex, this._results[selectedRowIndex]);
            } else if (showSearchButton) {
                if (($j.trim($j('#inputNode').val()) != '') || self.options.doSearchWhenEmpty) {
                    this._doSearch($j.trim($j('#inputNode').val()));
                }
            }

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

        _highlightRowOnPageFlip: function(){
            //deselect the current selected row if any
            if(this.curRowSelection != null){
                currentNode = this._table.fnGetNodes()[this.curRowSelection];
                $j(currentNode).removeClass("row_highlight");
                if(this.options.showIncludeVerbose)
                    $j(currentNode.nextSibling).removeClass('row_highlight');
            }

            this.curRowSelection = null;
            this._highlightRow();
        },

        /*
         * Highlights the row at the index that matches the value of 'curRowSelection' otherwise the
         * first on the current visible page
         */
        _highlightRow: function(){
            //the row to hightlight has to be on the visible page, this helps not to lose the highlight
            //when the user uses the pagination buttons(datatables provides no callback function)
            if(!this._isHighlightedRowOnVisiblePage()){
                //highlight the first row on the currently visible page
                this.curRowSelection = this._table.fnSettings()._iDisplayStart;
            }
            currentNode = this._table.fnGetNodes()[this.curRowSelection];
            $j(currentNode).addClass("row_highlight");
            if(this.options.showIncludeVerbose)
                $j($j(currentNode).next()).addClass('row_highlight');
        },

        /*
         * Unhighlights the specified row
         */
        _unHighlightRow: function(row){
            $j(row).removeClass("row_highlight");
            if(this.options.showIncludeVerbose)
                this._unHighlightVerboseRow(row.nextSibling);
        },

        /**
         * Unhighlights the specified verbose row
         * @param vRow the verbose row to be unhighlighted
         */
        _unHighlightVerboseRow: function(vRow){
            if(vRow){
                //the verbose row inherits its bg color from the actual data row
                //so we need to do the same if the class is not present
                if($j(vRow).hasClass('row_highlight'))
                    $j(vRow).removeClass('row_highlight');
                else
                    $j(vRow).css('background-color', $j(vRow.previousSibling).css('background-color'));
            }
        },

        /* Returnss true if the row highlight is on the current visible page */
        _isHighlightedRowOnVisiblePage:function(){
            return this.curRowSelection != null && (this.curRowSelection >= this._table.fnSettings()._iDisplayStart)
                && (this.curRowSelection < (this._table.fnSettings()._iDisplayStart + this._table.fnSettings()._iDisplayLength));
        },

        /* Gets the number of columns that will be visible */
        _getVisibleColumnCount: function(){
            if(!this.options.columnVisibility)
                return this.options.fieldsAndHeaders.length;

            var self = this;
            var count = 0;
            var columnIndex = 0;
            $j.map(self.options.fieldsAndHeaders, function(c) {
                if(self.options.columnVisibility[columnIndex] == true )
                    count++;

                columnIndex++;
            });

            return count;
        },

        /* Gets the current page the user is viewing on the screen */
        _getCurrVisiblePage:function(){
            return Math.ceil(this._table.fnSettings()._iDisplayStart / this._table.fnSettings()._iDisplayLength) + 1;
        },
        _updatePageInfo: function(searchText) {
            textToDisplay = omsgs.viewingResultsFor.replace("_SEARCH_TEXT_", "'<b>"+searchText+"</b>'");
            if($j.trim(searchText) == '')
                textToDisplay = omsgs.viewingAll;

            $j('#pageInfo').html(sanitizeHtml(textToDisplay));

            if($j('#pageInfo').css("visibility") != 'visible')
                $j('#pageInfo').css("visibility", "visible");
        },

        //This function adds the data returned by the second ajax call that fetches the remaining rows
        _addMoreRows: function(curCallCount2, searchText, matchCount, startIndex, curSubCallCount){
            var self = this;
            return function(results) {

                //Don't display results from delayed ajax calls when the input box is blank or has less
                //than the minimum characters
                var currInput = $j.trim($j("#inputNode").val());
                if(currInput == '' && !self.options.doSearchWhenEmpty){
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
                        //peep to the next sub call and search through the subcall counters of buffered rows for a match
                        while(true){
                            //if the this is true, it means the next subcall was found in the buffer and we need to
                            //we need to loop over it again to see if even the next in ine after it is also in the buffer
                            wasNextSubCallInBuffer = false;
                            foundAtIndex = null;// in case the next subcal was in the buffer, store its index here for removal
                            for(var i in self._bufferedAjaxCallCounters){
                                subCallCounter = self._bufferedAjaxCallCounters[i];
                                //Skip past the ones that come after those that are not yet returned by DWR calls e.g if we have ajax
                                //calls 3 and 5 in the buffer, when 2 returns, then add only 3 and ignore 5 since it has to wait on 4
                                bufferedData = buffer[subCallCounter];
                                if(subCallCounter && (subCallCounter == nextSubCallCount) && bufferedData){
                                    rowsToInsert = new Array();
                                    for(var j in bufferedData) {
                                        bufferedRowData = bufferedData[j];
                                        rowsToInsert.push(self._buildRow(bufferedRowData));
                                        self._results.push(bufferedRowData);
                                    }
                                    self._table.fnAddData(rowsToInsert);
                                    buffer[subCallCounter] = null;//drop rows from buffer
                                    nextSubCallCount++;
                                    wasNextSubCallInBuffer = true;
                                    foundAtIndex = i;
                                }
                            }

                            if(!wasNextSubCallInBuffer)
                                break;

                            //remove the sub call counter
                            self._bufferedAjaxCallCounters.splice(foundAtIndex, 1);
                        }
                    }

                    self._lastSubCallCount = nextSubCallCount;
                }
                else if(!inSerialMode && curSubCallCount > self._lastSubCallCount){
                    //this ajax request returned before others that were made before it, add its results to the buffer
                    self._bufferedAjaxCallCounters.push(curSubCallCount);
                    buffer[curSubCallCount] = data;

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

                //if there are still more hits to fetch and we are in serial mode, get them
                if(inSerialMode && actualResultCount < matchCount){
                    self._fetchMoreResults(searchText, curCallCount2, (startIndex+BATCH_SIZE), matchCount);
                }
            };
        }
    });
})(jQuery);