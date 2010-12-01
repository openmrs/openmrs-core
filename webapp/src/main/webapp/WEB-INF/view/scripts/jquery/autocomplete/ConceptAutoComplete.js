/**
 * Used for JQuery Autocomplete
 */

/**
 * Helper function to call concept DWR search methods and put results into an autocomplete list
 * 
 * Example use: 
 * 
 * var allergyCallback = new ConceptSearchCallback({onerror: showAllergyAddError});
 * var autoAllergyConcept = new AutoComplete("allergy_concept", allergyCallback.callback, {
 *			select: function(event, ui) {
 *				$j('#allergy_concept_id').val(ui.item.id);
 *			}
 *		});
 * 
 * Options:
 * onerror: a function to deal with errors</dd>
 * onsuccess: a function that gets called with the results if there are some and no error occurred
 * includeClasses: an array of class names to restrict to
 * excludeClasses: an array of class names to leave out of the results
 * includeDatatypes: an array of datatype names to restrict to
 * excludeDatatypes: an array of datatype names to leave out of the results
 * showAnswersFor: (used by callbackForJustAnswers) a concept id. if non-null the search space is restricted to the answers to the given concept id
 * 
 * @return ConceptListItem array of matches
 */
function ConceptSearchCallback(options) {
	if (options == null)
		options = {};
	
	// this will be the unique incrementing number assigned to the most recent query
	this.searchCounter = 0;
	
	this.callback = function() { var thisObject = this; return function(q, response) {
		if (q.trim().length == 0)
			return response(false);
		
		// changes a single element into an array
		var includeClasses = $j.makeArray(options.includeClasses);
		var excludeClasses = $j.makeArray(options.excludeClasses);
		var includeDatatypes = $j.makeArray(options.includeDatatypes);
		var excludeDatatypes = $j.makeArray(options.excludeDatatypes);
		/*$j("#log").html($j("#log").html() + "<br/>" + thisObject.test + "--" + thisObject.testing);*/
		DWRConceptService.findConcepts(q, false, includeClasses, excludeClasses, includeDatatypes, excludeDatatypes, false, null, null, thisObject.makeRows(q, response, thisObject.searchCounter + 1));
	}}
	
	this.callbackForJustAnswers = function() { thisObject = this; return function(q, response) {
		// do NOT return false if no text given, instead shoudl return all answers
		DWRConceptService.findConceptAnswers(q, options.showAnswersFor, false, false, thisObject.makeRows(q, response, thisObject.searchCounter + 1));
	}}
	
	this.makeRows = function(q, response, searchId) { var thisObject = this; return function(objs) {
		
		if (searchId < thisObject.searchCounter) {
			//alert("got a slow query with: '" + q + "'");
			return;
		}
		else
			thisObject.searchCounter = searchId;
		
		//convert objs from single obj into array (if needed)
		objs = $j.makeArray(objs);

		//ualert(options.onerror);
		//check if we have an error
		if(options.onerror) {
			if(objs.length > 0) {
				if(typeof objs[0] == 'string') {
					// we have an error, tell the user
					options.onerror(objs[0]);
					// clear other results
					response(false);
					
					return;
				}
			}
		}
		
		// let the caller know about a success (if they sent us a function to call)
		if (options.onsuccess)
			options.onsuccess(objs);

		response($j.map(objs, thisObject.displayConcept(q)));
	}}
	
	this.displayConcept = function(origQuery) { return function(item) {
			// dwr sometimes puts strings into the results, just display those
			if (typeof item == 'string')
				return { label: item, value: "" };
			
			// item is a ConceptListItem object
			// add a space so the term highlighter below thinks the first word is a word
			var textShown = " " + item.name;
			
			// highlight each search term in the results
			var words = origQuery.split(" ");
			for (var x=0; x<words.length; x++) {
				if (words[x].trim().length > 0) {
					var word = " " + words[x]; // only match the beginning of words
					// replace each occurrence case insensitively while replacing with original case
					textShown = textShown.replace(word, function(matchedTxt) { return "<span class='hit'>" + matchedTxt + "</span>"}, "gi");
				}
			}
			
			var value = item.name;
			if (item.preferredName) {
				textShown += "<span class='preferredname'> &rArr; " + item.preferredName + "</span>";
				value = item.preferredName;
			}
			
			textShown = "<span class='autocompleteresult'>" + textShown + "</span>";
			
			return { label: textShown, value: value, id: item.conceptId, object: item};
		}
	}
}