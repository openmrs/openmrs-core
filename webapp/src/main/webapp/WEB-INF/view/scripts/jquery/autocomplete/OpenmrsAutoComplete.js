/**
 * Used for JQuery Autocomplete dropdown input boxes
 */

/**
 * Helper function to call DWR search methods and put results into an autocomplete list
 * 
 * Example use: 
 * 
 * new AutoComplete("person_name", new CreateCallback().personCallback(), {
 *			select: function(event, ui) {
 *				$j('#person_name_id').val(ui.item.object.personId);
 *			},
 *          placeholder:'<openmrs:message code="Person.search.placeholder" javaScriptEscape="true"/>'
 *		});
 * 
 * General options:
 * onerror: a function to deal with errors</dd>
 * onsuccess: a function that gets called with the results if there are some and no error occurred
 * maxresults: limit the results to this. -1 means show all (defaults to 50)
 * placeholder: The text to appear as placeholder of the input text box
 * 
 * See each *callback method below for more options that can be used
 * 
 */
function CreateCallback(options) {
	if (options == null)
		options = {};
	
	var maxresults = options.maxresults;
	if (maxresults == null)
		maxresults = 50;
	
	// this will be the unique incrementing number assigned to the most recent query
	this.searchCounter = 0;
	
	/**
	 * Use this method if searching for general person objects
	 * 
	 * additional options:
	 * roles: a comma separated list of role names to restrict to
	 */
	this.personCallback = function() { var thisObject = this; return function(q, response) {
		if (jQuery.trim(q).length == 0)
			return response(false);
		
		thisObject.searchCounter += 1;
		DWRPersonService.findPeopleByRoles(q, false, options.roles, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayPerson));
	}}
	
	/**
	 * Use this method if searching for patients
	 */
	this.patientCallback = function() { var thisObject = this; return function(q, response) {
		if (jQuery.trim(q).length == 0)
			return response(false);
		
		thisObject.searchCounter += 1;
		DWRPatientService.findPatients(q, false, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayPerson));
	}}
	
	/**
	 * Use this method if searching for users
	 * 
	 * additional options:
	 * roles: a comma separated list of role names to restrict to
	 */
	this.userCallback = function() { var thisObject = this; return function(q, response) {
		if (jQuery.trim(q).length == 0)
			return response(false);
		
		var rolesArray = [];
		if (options.roles.length > 0)
			rolesArray = options.roles.split(",");
		
		thisObject.searchCounter += 1;
		DWRUserService.findUsers(q, rolesArray, false, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayPerson));
	}}
	
	/**
	 * Additional options:
	 * includeClasses: an array of class names to restrict to
	 * excludeClasses: an array of class names to leave out of the results
	 * includeDatatypes: an array of datatype names to restrict to
	 * excludeDatatypes: an array of datatype names to leave out of the results
	 */
	this.conceptCallback = function() { var thisObject = this; return function(q, response) {
		if (jQuery.trim(q).length == 0)
			return response(false);
		
		// changes a single element into an array
		var includeClasses = jQuery.makeArray(options.includeClasses);
		var excludeClasses = jQuery.makeArray(options.excludeClasses);
		var includeDatatypes = jQuery.makeArray(options.includeDatatypes);
		var excludeDatatypes = jQuery.makeArray(options.excludeDatatypes);
		/*$j("#log").html($j("#log").html() + "<br/>" + thisObject.test + "--" + thisObject.testing);*/
		
		thisObject.searchCounter += 1;
		DWRConceptService.findConcepts(q, false, includeClasses, excludeClasses, includeDatatypes, excludeDatatypes, false, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayNamedObject));
	}}
	
	/**
	 * Additional options:
	 * none (yet)
	 */
	this.drugCallback = function() { var thisObject = this; return function(q, response) {
		if (jQuery.trim(q).length == 0)
			return response(false);
		
		thisObject.searchCounter += 1;
		DWRConceptService.findDrugs(q, false, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayDrugObject));
	}}

	/**
	 * Additional options:
	 * showAnswersFor: a concept id. if non-null the search space is restricted to the answers to the given concept id
	 */
	this.conceptAnswersCallback = function() { var thisObject = this; return function(q, response) {
		// do NOT return false if no text given, instead should return all answers
		thisObject.searchCounter += 1;
		DWRConceptService.findConceptAnswers(q, options.showAnswersFor, false, false, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayNamedObject));
	}}
	
	/**
	 * Additional options:
	 * showAnswersFor: a concept id. if non-null the search space is restricted to the answers to the given concept id
	 */
	this.encounterCallback = function() { var thisObject = this; return function(q, response) {
		if (jQuery.trim(q).length == 0)
			return response(false);
		
		// do NOT return false if no text given, instead should return all answers
		thisObject.searchCounter += 1;
		DWREncounterService.findBatchOfEncountersByPatient(q, options.patientId, false, null, maxresults, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayEncounter));
	}}
	
	/**
	 * Use this method if searching for concept reference terms
	 * @param sourceElement (optional) the element whose value is the conceptSourceId for the source to search for terms
	 */
	this.conceptReferenceTermCallback = function(sourceElement) { var thisObject = this; return function(q, response) {
		if (jQuery.trim(q).length == 0)
			return response(false);
		var sourceId = null;
		if(sourceElement)
			sourceId = sourceElement.value;
		
		thisObject.searchCounter += 1;
		DWRConceptService.findBatchOfConceptReferenceTerms(q, sourceId, null, maxresults, false, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayConceptReferenceTerm));
	}}
	
	/**
	 * Use this method if searching for provider objects
	 */
	this.providerCallback = function() { var thisObject = this; return function(q, response) {
		if (jQuery.trim(q).length == 0)
			return response(false);
		
		thisObject.searchCounter += 1;
		DWRProviderService.findProvider(q, false, 0, 50, thisObject.makeRows(q, response, thisObject.searchCounter, thisObject.displayProvider));
	}}
	
	/*
	 * a 'private' method
	 * the method that dwr calls back with results 
	 */ 
	this.makeRows = function(q, response, searchId, displayFunction) { var thisObject = this; return function(objs) {
		if (searchId < thisObject.searchCounter) {
			//alert("got a slow query with: '" + q + "' main counter at: " + thisObject.searchCounter);
			return;
		}
		else {
			//alert("not a slow query, " + q + " searchId: " + searchId + " main counter: " + thisObject.searchCounter);
			thisObject.searchCounter = searchId;
		}
		
		//convert objs from single obj into array (if needed)
		objs = jQuery.makeArray(objs);
		
		//check if we have an error
		if(options.onerror) {
			if(objs.length >= 1) {
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
		
		if (maxresults > 0 && maxresults < objs.length) {
			var objectsSliced = objs.length - maxresults;
			objs = objs.slice(0, maxresults);
			objs.push("(" + objectsSliced + " " + omsgs.resultsNotDisplayed +")");
		}
		
		var responseVal = jQuery.map(objs, displayFunction(q)); 
		if (options.afterResults) {
			responseVal = responseVal.concat(options.afterResults);
		}
		
		response(responseVal);
	}; }
	
	// a 'private' method
	// This is what maps each PersonListItem returned object to a name in the dropdown
	this.displayPerson = function(origQuery) { return function(person) {
		// dwr methods sometimes put strings into the results, just display those
		if (typeof person == 'string')
			return { label: person, value: "" };
					
		// item is a PersonListItem object
		var imageText = "";
		if (person.gender == 'M')
			imageText = "<span class='male'>&#9794;</span>"; //<img style='height: 1em' src='" + openmrsContextPath + "/images/male.gif'/>";
		else if (person.gender == "F")
			imageText = "<span class='female'>&#9792;</span>"; //"<img style='height: 1em' src='" + openmrsContextPath + "/images/female.gif'/>"
		//else
		//	imageText = "?";
		
		// adding space here for both the regexp matching and the gap
		// between the image and the identifier
		var textShown = " ";
		
		if (person.identifier)
			textShown += person.identifier;
		
		textShown += " ";
		
		textShown += person.personName;

		// highlight each search term in the results
		textShown = highlightWords(textShown, origQuery);
				
		var ageText = "";
		if (person.age) {
			ageText = " (" + person.age + " " + omsgs.yearsOld + ")";
		}
		
		// append the gender image and age AFTER word highlighting so regex doesn't match it
		
		textShown = imageText + textShown + ageText; // space was inserted into beginning of 'textShown' var
		
		// wrap each result in a span tag (needed?)
		textShown = "<span class='autocompleteresult'>" + textShown + "</span>";
		
		return { label: textShown, value: person.personName, id: person.personId, object: person };
	}; }
	
	/**
	 * This is what maps each ProviderListItem returned object to a name in the drop down
	 */
	this.displayProvider = function(origQuery) { return function(provider) {
		
		// dwr methods sometimes put strings into the results, just display those
		if (typeof provider == 'string')
			return { label: provider, value: "" };
			
		var textShown = "";
		
		if (provider.identifier)
			textShown += provider.identifier + " ";
		
		textShown += provider.displayName;
		
		// wrap each result in a span tag (needed?)
		textShown = "<span class='autocompleteresult'>" + textShown + "</span>";
		
		return { label: textShown, value: provider.displayName, id: provider.providerId, object: provider };
	}; }
	
	// a 'private' method
	// This is what maps each ConceptListItem or LocationListItem returned object to a name in the dropdown
	this.displayNamedObject = function(origQuery) { return function(item) {
		// dwr sometimes puts strings into the results, just display those
		if (typeof item == 'string')
			return { label: item, value: "" };
		
		// item is a ConceptListItem or LocationListItem object
		// add a space so the term highlighter below thinks the first word is a word
		var textShown = " " + item.name;
		
		// highlight each search term in the results
		textShown = highlightWords(textShown, origQuery);
		
		var value = item.name;
		if (item.preferredName) {
			textShown += "<span class='preferredname'> &rArr; " + item.preferredName + "</span>";
			//value = item.preferredName;
		}
		
		textShown = "<span class='autocompleteresult'>" + textShown + "</span>";
		
		return { label: textShown, value: value, object: item};
	}; };

	// a 'private' method
	// This is what maps each ConceptDrugListItem returned object to a name in the dropdown
	this.displayDrugObject = function(origQuery) { return function(item) {
		// dwr sometimes puts strings into the results, just display those
		if (typeof item == 'string')
			return { label: item, value: "" };
			
		// add a space so the term highlighter below thinks the first word is a word
		var textShown = " " + item.fullName;
		
		// highlight each search term in the results
		textShown = highlightWords(textShown, origQuery);
		
		var value = item.fullName;
		textShown = "<span class='autocompleteresult'>" + textShown + "</span>";
		
		return { label: textShown, value: value, object: item};
	}; };

	// a 'private' method
	// This is what maps each EncounterListItem returned object to a name in the dropdown
	this.displayEncounter = function(origQuery) { return function(enc) {
		// dwr sometimes puts strings into the results, just display those
		if (typeof enc == 'string')
			return { label: enc, value: "" };
			
		// enc is an EncounterListenc
		// add a space so the term highlighter below thinks the first word is a word
		var textShown = " " + enc.encounterDateString;
		textShown += " " + enc.encounterType;
		textShown += " - " + enc.personName;
		textShown += " - " + enc.providerName;
		
		if (enc.location) {
			textShown += " - " + enc.location;
		}
		
		// highlight each search term in the results
		textShown = highlightWords(textShown, origQuery);
		
		textShown = "<span class='autocompleteresult'>" + textShown + "</span>";
		
		value = enc.location + " - " + enc.encounterDateString;
		
		return { label: textShown, value: value, object: enc};
	}; };
	
	/*
	 * Private method, used when display persons or concepts to show 
	 * which part of the word was a match.
	 * 
	 * Each separate word in "origQuery" will be highlighted with a 'hit' class in 
	 * the "textShown" string.   
	 */
	function highlightWords(textShown, origQuery) {
		var words = origQuery.split(" ");
		for (var x=0; x<words.length; x++) {
			if (jQuery.trim(words[x]).length > 0) {
				var word = " " + words[x]; // only match the beginning of words
				// replace each occurrence case insensitively while replacing with original case
				textShown = textShown.replace(word, function(matchedTxt) { return "{{{{" + matchedTxt + "}}}}"}, "gi");
			}
		}
		
		textShown = textShown.replace(/{{{{/g, "<span class='hit'>");
		textShown = textShown.replace(/}}}}/g, "</span>");
		
		return textShown;
	}
	
	// a 'private' method
	// This is what displays the ConceptReferenceTermListItem's code, name and concept source name in the dropdown
	this.displayConceptReferenceTerm = function(origQuery) { return function(item) {
		// dwr sometimes puts strings into the results, just display those
		if (typeof item == 'string')
			return { label: item, value: "" };
			
		var textShown = " " + item.code+((item.name != null && $j.trim(item.name) != '') ? " - "+item.name : "")+" ["+item.conceptSourceName+"]";
		
		// highlight each search term in the results
		textShown = highlightWords(textShown, origQuery);
		
		var value = item.code;
		textShown = "<span class='autocompleteresult'>" + textShown + "</span>";
		
		return { label: textShown, value: value, object: item};
	}; };
}