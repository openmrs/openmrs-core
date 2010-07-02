jQuery.autocomplete = function(input, options) {
	// Create a link to self
	var		me = this;

	// Version
	//SYH added the .1 for the modifications that I made 18 June 2010
	var		version = "1.6.1";

	// Create jQuery object for input element
	var		$input = jQuery(input).attr('autocomplete', 'off');

	// Create jQuery object as a unique request ID
	var		$requestId = 1;
	
	// Maximum request ID (it's kind of silly to have this, but whatever.
	var		$MAX_REQUEST_ID = 65000;

	// Apply inputClass if necessary
	if (options.inputClass) {
		$input.addClass(options.inputClass);
	}

	/*
	 * Create results (DOM element)
	 */
	var		results = document.createElement('div');

	/*
	 * Create jQuery object for results
	 * 
	 * var	$results = $(results);
	 */
	var		$results = jQuery(results).hide().addClass(options.resultsClass).css('position', 'absolute');

	if (options.width > 0) {
		$results.css('width', options.width);
	}

	// Add to body element
	jQuery('body').append(results);

	input.autocompleter = me;

	var		timeout = null;
	var		prev = '';
	var		active = -1;
	var		cache = {};
	var		keyb = false;
	var		hasFocus = false;
	var		lastKeyPressCode = null;
	var		mouseDownOnSelect = false;
	var		hidingResults = false;

	// flush cache
	function flushCache(){
		cache = {};
		cache.data = {};
		cache.length = 0;
	};

	// flush cache
	flushCache();

	// if there is a data array supplied
	if (options.data != null) {
		var		sFirstChar = '', stMatchSets = {}, row = [];

		// no url was specified, we need to adjust the cache length to make sure it fits the local data store
		if( typeof options.url != 'string' ) {
			options.cacheLength = 1;
		}

		// loop through the array and create a lookup structure
		for (var i=0; i < options.data.length; i++) {
			// if row is a string, make an array otherwise just reference the array
			row = ((typeof options.data[i] == 'string') ? [options.data[i]] : options.data[i]);

			// if the length is zero, don't add to list
			if( row[0].length > 0 ){
				// get the first character
				sFirstChar = row[0].substring(0, 1).toLowerCase();
				// if no lookup array for this character exists, look it up now
				if( !stMatchSets[sFirstChar] ) stMatchSets[sFirstChar] = [];
				// if the match is a string
				stMatchSets[sFirstChar].push(row);
			}
		}

		// add the data items to the cache
		for( var k in stMatchSets ) {
			// increase the cache size
			options.cacheLength++;
			// add to the cache
			addToCache(k, stMatchSets[k]);
		}
	}

	$input.keydown(function(e) {
		// track last key pressed
		lastKeyPressCode = e.keyCode;
		switch(e.keyCode) {
			case 38: // up
				e.preventDefault();
				moveSelect(-1);
				break;
			case 40: // down
				e.preventDefault();
				moveSelect(1);
				break;
			case 9:  // tab
			case 13: // return
				if( selectCurrent() ){
					// make sure to blur off the current field
					$input.get(0).blur();
					e.preventDefault();
				}
				break;
			case 16:
			case 17:
			case 18:
				// shift, control, and alt keys... ignore!
				break;
			default:
				active = -1;
				if (timeout) clearTimeout(timeout);
				timeout = setTimeout(function(){onChange();}, options.delay);
				break;
		}
	})
	.focus(function(){
		// track whether the field has focus, we shouldn't process any results if the field no longer has focus
		hasFocus = true;
	})
	.blur(function() {
		// track whether the field has focus
		hasFocus = false;
		if (!mouseDownOnSelect) {
			hideResults();
		}
	});

	hideResultsNow();
	
	if (options.focus) {
		$input.focus();
	}

	function onChange() {
		// ignore if the following keys are pressed: [del] [shift] [capslock]
		if ((lastKeyPressCode == 46) || ((lastKeyPressCode > 8) && (lastKeyPressCode < 32))) {
			doResultsHide();
		}

		var		v = $input.val();

		if (v == prev) return;

		prev = v;

		if (v.length >= options.minChars) {
			$input.addClass(options.loadingClass);
			requestData(v);
		}
		else {
			$input.removeClass(options.loadingClass);

			doResultsHide();
		}
	};

 	function moveSelect(step) {

		var		lis = $('li', results);
		if (!lis) return;

		active += step;

		if (active < 0) {
			active = 0;
		}
		else if (active >= lis.size()) {
			active = lis.size() - 1;
		}

		lis.removeClass('ac_over');

		$(lis[active]).addClass('ac_over');

		// Weird behaviour in IE
		// if (lis[active] && lis[active].scrollIntoView) {
		// 	lis[active].scrollIntoView(false);
		// }

	};

	function selectCurrent() {
		var		li = $('li.ac_over', results)[0];
		
		if (!li) {
			var		$li = $('li', results);
			
			if (options.selectOnly) {
				if ($li.length == 1) li = $li[0];
			}
			else if (options.selectFirst) {
				li = $li[0];
			}
		}
		
		if (li) {
			selectItem(li);
			return true;
		}
		else {
			return false;
		}
	};

	function selectItem(li) {
		if (!li) {
			li = document.createElement('li');
			li.extra = [];
			li.selectValue = '';
		}

		var		v = li.selectValue ? li.selectValue : li.innerHTML;//jQuery.trim();

		input.lastSelected = v;
		prev = v;
		$results.html('');
		$input.val(li.innerHTML);
		
		hideResultsNow();

		if (options.onItemSelect) {
			setTimeout(function() {
				options.onItemSelect(li)
			}, 1);
		}
	};

	// selects a portion of the input string
	function createSelection(start, end){
		/*
		 * get a reference to the input element
		 */
		var		field = $input.get(0);

		if (field.createTextRange) {
			var		selRange = field.createTextRange();
			selRange.collapse(true);
			selRange.moveStart('character', start);
			selRange.moveEnd('character', end);
			selRange.select();
		}
		else if (field.setSelectionRange) {
			field.setSelectionRange(start, end);
		}
		else {
			if (field.selectionStart) {
				field.selectionStart = start;
				field.selectionEnd = end;
			}
		}

		field.focus();
	};

	// fills in the input box w/the first match (assumed to be the best match)
	function autoFill(sValue) {
		/*
		 * if the last user key pressed was backspace, don't autofill
		 */
		if (lastKeyPressCode != 8) {
			/*
			 * fill in the value (keep the case the user has typed)
			 */
			$input.val($input.val() + sValue.substring(prev.length));
			/*
			 * select the portion of the value not typed by the user (so the next character will erase)
			 */
			createSelection(prev.length, sValue.length);
		}
	};

	function showResults() {
		/*
		 * get the position of the input field right now (in case the DOM is shifted)
		 */
		var		pos = findPos(options.popupParent || $input);

		/*
		 * either use the specified width, or autocalculate based on form element
		 */
		var		iWidth;

		if (options.width > 0) {
			iWidth = options.width;
		}
		else {
			if (options.popupParent) {
				iWidth = options.popupParent.innerWidth();
			}
			else {
				iWidth = $input.innerWidth();
			}
		}

		var		height;

		if (options.popupParent) {
			/*
			 * for an arbitrary parent, we use inner height... it seems to work better (tighter to parent)
			 */
			height = options.popupParent.innerHeight();
		}
		else {
			/*
			 * Otherwise for input fields, use outer height.
			 */
			height = $input.outerHeight();
		}

		/*
		 * re-position
		 */
		$results.css({
						width: parseInt(iWidth) + 'px',
						top: (pos.y + height) + 'px',
						left: pos.x + 'px',
						'z-index': pos.zIndex
					  });

		doResultsShow();
	};

	function hideResults() {
		if (timeout) clearTimeout(timeout);
		timeout = setTimeout(hideResultsNow, 200);
	};

	function hideResultsNow() {
		if (hidingResults) {
			return;
		}
		hidingResults = true;
	
		if (timeout) {
			clearTimeout(timeout);
		}
		
		var		v = $input.removeClass(options.loadingClass).val();

		if ($results.is(':visible')) {
			doResultsHide();
		}

		if (options.mustMatch) {
			if (!input.lastSelected || input.lastSelected != v) {
				selectItem(null);
			}
		}

		hidingResults = false;
	};

	function doResultsShow() {
		if (options.resultsId) {
			$results.attr('id', options.resultsId);
		}

		$results.show();

		if (typeof options.onResultsShow === 'function') {
			options.onResultsShow($results[0]);
		}
	};

	function doResultsHide() {
		$results.hide();

		if (typeof options.onResultsHide === 'function') {
			options.onResultsHide($results[0]);
		}

		if (options.resultsId) {
			$results.removeAttr('id');
		}
	};

	function receiveData(q, data) {
		if (data) {
			$input.removeClass(options.loadingClass);
			results.innerHTML = '';

			/*
			 * if the field no longer has focus or if there are no matches, do not display the drop down
			 */
			if (!hasFocus || (data.length == 0)) {
				return hideResultsNow();
			}

			if (jQuery.browser.msie) {
				/* 
				 * we put a styled iframe behind the calendar so HTML SELECT elements don't show through
				 */
				$results.append(document.createElement('iframe'));
			}
			results.appendChild(dataToDom(data));

			if (options.autoFill && ($input.val().toLowerCase() == q.toLowerCase()) ) {
				/*
				 * autofill in the complete box w/the first match
				 * as long as the user hasn't entered in more data
				 */
				autoFill(data[0][0]);
			}
			else if (options.selectFirst) {
				/*
				 * If the 'selectFirst' option was specified, then hilite the first element in the list.
				 */
				jQuery('li:first', results).addClass('ac_over');
			}
	
			showResults();
		}
		else {
			hideResultsNow();
		}
	};

	function parseData(data) {
		if (!data) return null;
		var		parsed = [];
		var		rows = data.split(options.lineSeparator);
		for (var i=0; i < rows.length; i++) {
			var		row = jQuery.trim(rows[i]);
			if (row) {
				parsed[parsed.length] = row.split(options.cellSeparator);
			}
		}
		return parsed;
	};

	/**
	 * Create a new pop-up list element.
	 * 
	 * @param data		the data to use when constructing the element.
	 * 		should be a json object consisting of
	 * 				name - the string to represent the data
	 * 				value - stored in the LI.selectValue property
	 * 
	 * @return			a 'ul' dom element.
	 */
	function dataToDom(data) {
		var		ul = document.createElement('ul');
		var		num = data.length;

		// limited results to a max number
		if( (options.maxItemsToShow > 0) && (options.maxItemsToShow < num) ) num = options.maxItemsToShow;

		for (var i=0; i < num; i++) {
			var		row = data[i];
			if (!row) continue;
			
			var		li = document.createElement('li');
			
			if (options.formatItem) {
				li.innerHTML = options.formatItem(row, i, num);
				li.selectValue = row.value;
			}
			else {
				li.innerHTML = row.name;
				li.selectValue = row.value;
			}

			var		extra = null;

			if (row.length > 1) {
				extra = [];
				for (var j=1; j < row.length; j++) {
					extra[extra.length] = row[j];
				}
			}
			li.extra = extra;
			ul.appendChild(li);
			
			jQuery(li).hover(
				function() { jQuery('li', ul).removeClass('ac_over'); jQuery(this).addClass('ac_over'); active = jQuery('li', ul).indexOf(jQuery(this).get(0)); },
				function() { jQuery(this).removeClass('ac_over'); }
			).click(function(e) { 
				e.preventDefault();
				e.stopPropagation();
				selectItem(this)
			});
			
		}
		jQuery(ul).mousedown(function() {
			mouseDownOnSelect = true;
		}).mouseup(function() {
			mouseDownOnSelect = false;
		});
		return ul;
	};

	/*
	 * Perform an ajax request for the specified query string
	 */
	function requestData(q) {
		if (!options.matchCase) {
			q = q.toLowerCase();
		}

		var		data = options.cacheLength ? loadFromCache(q) : null;

		if (data) {
			/*
			 * receive the cached data
			 */
			receiveData(q, data);
		}
		else if ((typeof options.url == 'string') && (options.url.length > 0)) {
			/*
			 * if an AJAX url has been supplied, try loading the data now
			 * 
			 * Increment the unique request ID, to ensure that we're only using the most recent request
			 */
			$requestId += 1;
			if ($requestId >= $MAX_REQUEST_ID) {
				$requestId = 0;	// roll over
			}
			// Perform the request.
			var		thisRequestId = $requestId;
			jQuery.get(makeUrl(q), function(data) {
									handleReceivedData(q, data, thisRequestId);
								});
		}
		else {
			$requestId += 1;
			if ($requestId >= $MAX_REQUEST_ID) {
				$requestId = 0;	// roll over
			}
			var		thisRequestId = $requestId;
			
			/**
			 * SYH: modified 18 June 2010
			 * This specifies the use of a function callback
			 */
			options.url(q, function(data) {
				handleReceivedDataSy(q, data, thisRequestId);
			});
		}
	};

	/**
	 * SYH: modified 18 June 2010
	 * Changed so we dont go to parse data because the {data} we receive is json
	 */
	function handleReceivedDataSy(q, data, thisRequestId) {
		addToCache(q, data);
		
		//if this is the most recent request, then put it in the list
		if(thisRequestId == $requestId) {
			receiveData(q, data);
		}
	}

	function handleReceivedData(q, data, thisRequestId) {
		data = parseData(data);
		addToCache(q, data);
		
		if (thisRequestId == $requestId) {
			/*
			 * If this is the most recent request, then put it into the list.
			 */
			receiveData(q, data);
		}
	};

	function makeUrl(q) {
		var		sep = options.url.indexOf('?') == -1 ? '?' : '&'; 
		var		url = options.url + sep + options.queryParam + '=' + encodeURI(q);

		for (var i in options.extraParams) {
			url += '&' + i + '=' + encodeURI(options.extraParams[i]);
		}

		return url;
	};

	function loadFromCache(q) {
		if (!q) {
			return null;
		}

		if (cache.data[q]) {
			return cache.data[q];
		}

		if (options.matchSubset) {
			for (var i = q.length - 1; i >= options.minChars; i--) {
				var		qs = q.substr(0, i);
				var		c = cache.data[qs];
				if (c) {
					var		csub = [];
					for (var j = 0; j < c.length; j++) {
						var		x = c[j];
						var		x0 = x[0];
						if (matchSubset(x0, q)) {
							csub[csub.length] = x;
						}
					}
					return csub;
				}
			}
		}
		return null;
	};

	function matchSubset(s, sub) {
		if (!options.matchCase) {
			s = s.toLowerCase();
		}

		var		i = s.indexOf(sub);

		if (i == -1) {
			return false;
		}

		return i == 0 || options.matchContains;
	};

	this.flushCache = function() {
		flushCache();
	};

	this.setExtraParams = function(p) {
		options.extraParams = p;
	};

	this.findValue = function(){
		var		q = $input.val();

		if (!options.matchCase) {
			q = q.toLowerCase();
		}

		var		data = options.cacheLength ? loadFromCache(q) : null;

		if (data) {
			findValueCallback(q, data);
		}
		else if ((typeof options.url == 'string') && (options.url.length > 0)) {
			jQuery.get(makeUrl(q), function(data) {
				data = parseData(data)
				addToCache(q, data);
				findValueCallback(q, data);
			});
		}
		else {
			// no matches
			findValueCallback(q, null);
		}
	}

	function findValueCallback(q, data){
		if (data) {
			$input.removeClass(options.loadingClass);
		}

		var		num = (data) ? data.length : 0;
		var		li = null;

		for (var i=0; i < num; i++) {
			var		row = data[i];

			if (row[0].toLowerCase() == q.toLowerCase()) {
				li = document.createElement('li');

				if (options.formatItem) {
					li.innerHTML = options.formatItem(row, i, num);
					li.selectValue = row[0];
				}
				else {
					li.innerHTML = row[0];
					li.selectValue = row[0];
				}

				var		extra = null;

				if (row.length > 1) {
					extra = [];

					for (var j=1; j < row.length; j++) {
						extra[extra.length] = row[j];
					}
				}
				li.extra = extra;
			}
		}

		if (options.onFindValue) {
			setTimeout(function() { options.onFindValue(li) }, 1);
		}
	}

	function addToCache(q, data) {
		if (!data || !q || !options.cacheLength) {
			return;
		}
		
		if (!cache.length || cache.length > options.cacheLength) {
			flushCache();
			cache.length++;
		}
		else if (!cache[q]) {
			cache.length++;
		}
		cache.data[q] = data;
	};

	function findPos(popupParent) {
		var		offset = popupParent.offset();
		var		maxZIndex = 0;
		var		zIndexParent = popupParent;

		while (zIndexParent.length && !zIndexParent.is('body')) {
			var		thisZIndex = zIndexParent.css('z-index');

			thisZIndex = parseInt(thisZIndex);

			if (thisZIndex || !isNaN(thisZIndex)) {
				maxZIndex = Math.max(thisZIndex, maxZIndex);
			}

			zIndexParent = zIndexParent.offsetParent();
		}

		return {
					x: Math.ceil(offset.left),
					y: Math.ceil(offset.top),
					zIndex: maxZIndex + 10
			   };
	}
}

jQuery.fn.autocomplete = function(url, options, data) {
	// Make sure options exists
	options = options || {};
	// Set url as option
	options.url = url;
	// set some bulk local data
	options.data = ((typeof data == 'object') && (data.constructor == Array)) ? data : null;

	// Set default values for required options
	options = jQuery.extend({
		autoFill: false,
		cacheLength: 1,
		cellSeparator: '|',
		delay: 400,
		extraParams: {},
		focus: false,
		inputClass: 'ac_input',
		lineSeparator: '\n',
		loadingClass: 'ac_loading',
		matchCase: 0,
		matchSubset: 1,
		matchContains: 0,
		maxItemsToShow: -1,
		minChars: 1,
		mustMatch: 0,
		onResultsShow: null,
		onResultsHide: null,
		popupParent: null,
		queryParam: 'q',
		resultsClass: 'ac_results',
		resultsId: null,
		selectFirst: false,
		selectOnly: false,
		width: 0
	}, options);
	options.width = parseInt(options.width, 10);

	if (options.popupParent) {
		options.popupParent = jQuery(options.popupParent);

		if (options.popupParent.length == 0) {
			options.popupParent = null;
		}
	}

	this.each(function() {
		var		input = this;
		new jQuery.autocomplete(input, options);
	});

	// Don't break the chain
	return this;
}

jQuery.fn.autocompleteArray = function(data, options) {
	return this.autocomplete(null, options, data);
}

jQuery.fn.indexOf = function(e){
	for (var i = 0; i < this.length; i++) {
		if (this[i] == e) {
			return i;
		}
	}

	return -1;
};
