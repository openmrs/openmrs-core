/**
 * Used for JQuery Autocomplete
 */

/**
 * Helper function to call DWR search methods and put results into an autocomplete list
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
 * includedClasses: an array of class names to restrict to
 * 
 * @return ConceptListItem array of matches
 */
function ConceptSearchCallback(options) {
		this.callback = function(q, response) {
			
			var includedClasses = $j.makeArray(options.includedClasses);
			DWRConceptService.findConcepts(q, false, includedClasses, [], [], [], false, function(objs) {
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

				response($j.map(objs, function(item) {
					// item is a ConceptListItem object
					var textShown = item.name;
					var value = item.name;
					if (item.preferredName) {
						textShown += "<span class='preferredname'> &rArr; " + item.preferredName + "</span>";
						value = item.preferredName;
					}

					textShown = "<span class='autocompleteresult'>" + textShown + "</span>";
					
					return { label: textShown, value: value, id: item.conceptId };
				}));
			});
		}
	}