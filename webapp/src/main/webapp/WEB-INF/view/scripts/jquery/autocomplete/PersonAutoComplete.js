/**
 * Used for JQuery Autocomplete
 */

/**
 * Helper function to call person DWR search methods and put results into an autocomplete list
 * 
 * Example use: 
 * 
 * new AutoComplete("person_name", new PersonSearchCallback().personCallback(), {
 *			select: function(event, ui) {
 *				$j('#person_name_id').val(ui.item.id);
 *			}
 *		});
 * 
 * Options:
 * onerror: a function to deal with errors</dd>
 * onsuccess: a function that gets called with the results if there are some and no error occurred
 * roles: a comma separated list of role names to restrict to
 * maxresults: limit the results to this. -1 means show all (defaults to 50)
 * 
 * @return PersonListItem array of matches
 */
function PersonSearchCallback(options) {
	if (options == null)
		options = {};
	
	var maxresults = options.maxresults;
	if (maxresults == null)
		maxresults = 50;
	
	// this will be the unique incrementing number assigned to the most recent query
	this.searchCounter = 0;
	
	/**
	 * Use this method if searching for general person objects
	 */
	this.personCallback = function() { var thisObject = this; return function(q, response) {
		if (q.trim().length == 0)
			return response(false);
		//alert("this.searchCount: " + thisObject.searchCounter);
		thisObject.searchCounter = thisObject.searchCounter + 1;
		
		DWRPersonService.findPeopleByRoles(q, false, options.roles, thisObject.makeRows(q, response, thisObject.searchCounter + 1));
	}}
	
	/**
	 * Use this method if searching for patients
	 */
	this.patientCallback = function() { var thisObject = this; return function(q, response) {
		if (q.trim().length == 0)
			return response(false);
		
		DWRPatientService.findPatients(q, false, thisObject.makeRows(q, response, thisObject.searchCounter + 1));
	}}
	
	/**
	 * Use this method if searching for users
	 */
	this.userCallback = function() { var thisObject = this; return function(q, response) {
		if (q.trim().length == 0)
			return response(false);
		
		var roles = [];
		if (options.roles.length > 0)
			roles = options.roles.split(",");
		
		DWRUserService.findUsers(q, false, roles, thisObject.makeRows(q, response, thisObject.searchCounter + 1));
	}}
	
	// a 'private' method
	// the method that dwr calls back with results
	this.makeRows = function(q, response, searchId) { var thisObject = this; return function(objs) {
		if (searchId < thisObject.searchCounter) {
			//alert("got a slow query with: '" + q + "' main counter at: " + autocompleteObject.searchCounter);
			return;
		}
		else
			thisObject.searchCounter = searchId;
		
		//convert objs from single obj into array (if needed)
		objs = $j.makeArray(objs);
		
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
		
		response($j.map(objs, thisObject.displayPerson(q)));
	}; }
	
	// a 'private' method
	// This is what maps each returned object to row values
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
		var words = origQuery.split(" ");
		for (var x=0; x<words.length; x++) {
			if (words[x].trim().length > 0) {
				var word = " " + words[x]; // only match the beginning of words
				// replace each occurrence case insensitively while replacing with original case
				textShown = textShown.replace(word, function(matchedTxt) { return "<span class='hit'>" + matchedTxt + "</span>"}, "gi");
			}
		}
				
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
}