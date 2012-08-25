dojo.require("dojo.widget.openmrs.ConceptSearch");
dojo.require("dojo.widget.openmrs.OpenmrsPopup");

var nameListBox= null;
var idListBox  = null;
var addButton  = null;
var answerSearch;
var setSearch;

dojo.addOnLoad( function() {
		
	answerSearch = dojo.widget.manager.getWidgetById("aSearch");
	setSearch = dojo.widget.manager.getWidgetById("sSearch");
	
	dojo.event.topic.subscribe("aSearch/select", 
		function(msg) {
			selectConcept('answerNames', 'answerIds', msg.objs, answerSearch);
		}
	);
	
	dojo.event.topic.subscribe("sSearch/select", 
		function(msg) {
			selectConcept('conceptSetsNames', 'conceptSets', msg.objs, setSearch);
		}
	);
	
	changeDatatype(document.getElementById("datatype"));
	changeSetStatus(document.getElementById('conceptSet'));
});

function selectConcept(nameList, idList, conceptList, widget) {
	var nameListBox = document.getElementById(nameList);
	var idListBox = document.getElementById(idList);
	
	var options = nameListBox.options;
	for (i=0; i<conceptList.length; i++)
		addOption(conceptList[i], options);
		
	copyIds(nameListBox.id, idListBox.id, ' ');
}

function removeItem(nameList, idList, delim)
{
	var sel   = document.getElementById(nameList);
	var input = document.getElementById(idList);
	var optList   = sel.options;
	var lastIndex = -1;
	var i = 0;
	while (i<optList.length) {
		// loop over and erase all selected items
		if (optList[i].selected) {
			optList[i] = null;
			lastIndex = i;
		}
		else {
			i++;
		}
	}
	copyIds(nameList, idList, delim);
	while (lastIndex >= optList.length)
		lastIndex = lastIndex - 1;
	if (lastIndex >= 0) {
		optList[lastIndex].selected = true;
		return optList[lastIndex];
	}
	return null;
}

function moveUp(nameList, idList)
{
	var input = document.getElementById(idList);
	var sel = document.getElementById(nameList);
	var optList = sel.options;
	for (var i=1; i<optList.length; i++) {
		// loop over and move up all selected items
		if (optList[i].selected && !optList[i-1].selected) {
			var id   = optList[i].value;
			var name = optList[i].text;
			optList[i].value = optList[i-1].value;
			optList[i].text  = optList[i-1].text;
			optList[i].selected = false;
			optList[i-1].value = id;
			optList[i-1].text  = name;
			optList[i-1].selected = true;
		}
	}
	copyIds(nameList, idList, ' ');
}

function moveDown(nameList, idList)
{
	var input = document.getElementById(idList);
	var sel = document.getElementById(nameList);
	var optList = sel.options;
	for (var i=optList.length-2; i>=0; i--) {
		if (optList[i].selected && !optList[i+1].selected) {
			var id   = optList[i].value;
			var name = optList[i].text;
			optList[i].value = optList[i+1].value;
			optList[i].text  = optList[i+1].text;
			optList[i].selected = false;
			optList[i+1].value = id;
			optList[i+1].text  = name;
			optList[i+1].selected = true;
		}
	}
	copyIds(nameList, idList, ' ');
}

function copyIds(from, to, delimiter)
{
	var sel = document.getElementById(from);
	var input = document.getElementById(to);
	var optList = sel.options;
	var remaining = new Array();
	var i=0;
	while (i < optList.length)
	{
		remaining.push(optList[i].value);
		i++;
	}
	input.value = remaining.join(delimiter);
}

function addOption(obj, options) {
	var objId = obj.conceptId;
	var objName =  (obj.drugId != null ? obj.fullName : obj.name) + ' ('+objId+')';
	
	if (obj.drugId != null) //if obj is actually a drug object
		objId = objId + "^" + obj.drugId;
	
	if (isAddable(objId, options)==true) {
		var opt = new Option(objName, objId);
		opt.selected = true;
		options[options.length] = opt;
	}
}

function isAddable(value, options) {
	for (x=0; x<options.length; x++)
		if (options[x].value == value)
			return false;

	return true;
}

function removeHiddenRows() {
	var rows = document.getElementsByTagName("TR");
	var i = 0;
	while (i < rows.length) {
		if (rows[i].style.display == "none")
			rows[i].parentNode.removeChild(rows[i]);
		else
			i = i + 1;
	}
}

function changeSetStatus(obj) {
	var row = document.getElementById("conceptSetRow");
	if (obj.checked)
		row.style.display = "";
	else
		row.style.display = "none";
}

var customDatatypes = new Array();
customDatatypes.push("numeric");
customDatatypes.push("coded");
customDatatypes.push("complex");

function changeDatatype(obj) {
	for (var i=0; i < customDatatypes.length; i++) {
		var row = document.getElementById(customDatatypes[i] + "DatatypeRow");
		if (obj[obj.selectedIndex].text.toLowerCase() == customDatatypes[i])
			row.style.display = "";
		else
			row.style.display = "none";
	}
}

function listKeyPress(from, to, delim, event) {
	if(!event)
		event = window.event;
	var keyCode = event.keyCode;
	if (keyCode == 8 || keyCode == 46) {
		removeItem(from, to, delim);
		window.Event.keyCode = 0;	//attempt to prevent backspace key (#8) from going back in browser
	}
}

function hotkeys(event) {
	if(!event)
		event = window.event;
	var k = event.keyCode;
	if (event.cntrlKey == true) {
		if (k == 86) { // v
			var element = document.getElementById('viewConcept');
			if (element) {
				document.location = element.href;
			}
		}
	}
	if (k == 37) { // left key
		var element = document.getElementById('previousConcept');
		if (element) {
			document.location = element.href;
		}
	}
	else if (k == 39) { //right key
		var element = document.getElementById('nextConcept');
		if (element) {
			document.location = element.href;
		}
	}
}

var numberOfClonedElements = {};

/**
 * Clone the synonym text field element given by the id and put the newly cloned
 * element right before said id. Also it checks whether do need to show 
 * preferred label and radio button
 * 
 * 
 * @param id 
 *                      the string id of the element to clone
 * @param initialSizeOfClonedSiblings 
 *                      integer number of other objects
 * @param inputNamePrefix 
 *                      string to prepend to all input names in the cloned element
 * @param preferredContainerId 
 *                      the string with id of preferred elements group container
 */
function cloneSynonymElement(id, initialSizeOfClonedSiblings, inputNamePrefix, preferredContainerId) {
	// simply cloning of synonym field element
	cloneElement(id, initialSizeOfClonedSiblings, inputNamePrefix);
	// try to make preferred radio button and label visible
	// in case of adding new text field
	if (preferredContainerId != null) {
		// makes group of preferred elements visible in case if it was hidden
		var preferredContainer = document.getElementById(preferredContainerId);
		for (var i = 0; i < preferredContainer.children.length; i++) {
			if (preferredContainer.children[i].style.visibility == "hidden")
				preferredContainer.children[i].style.visibility = "visible";
		}		
	}	
}

/**
 * Clone the element given by the id and put the newly cloned
 * element right before said id.
 * 
 * This method replaces all "[]" strings in input names with the next 
 * iteration.  This allows spring to save the elements in order.
 * 
 * The iteration will start at (int)initialSizeOfClonedSiblings.
 * 
 * @param id 
 *                      the string id of the element to clone
 * @param initialSizeOfClonedSiblings 
 *                      integer number of other objects
 * @param inputNamePrefix 
 *                      string to prepend to all input names in the cloned element
 */
function cloneElement(id, initialSizeOfClonedSiblings, inputNamePrefix) {
	if (numberOfClonedElements[id] != null) {
		numberOfClonedElements[id] = numberOfClonedElements[id] + 1;
	}
	else {
		numberOfClonedElements[id] = initialSizeOfClonedSiblings;
	}
	
	var elementToClone = document.getElementById(id);
	var clone = elementToClone.cloneNode(true);
	var inputs = clone.childNodes;
	for (var x = 0; x < inputs.length; x++) {
		var input = inputs[x];
		//ingore the name of the radio button so as to maintain the button group name per locale
		if (input.name && input.type != 'radio') {
			input.name = inputNamePrefix + input.name.replace('[x]', '[' + numberOfClonedElements[id] + ']')
		}
	}
	clone.id = "";
	elementToClone.parentNode.insertBefore(clone, elementToClone);
	clone.style.display = "";

	return clone;
}

/**
 * Handles synonym field deleting and update visibility of 
 * preferred concept name radio button
 *
 * @param btn 
 *           the source object of event, that use it handler
 * @param key
 *           the key object for receiving amount of existing synonyms
 *           for specified concept name
 * @param preferredContainerId 
 *           the string with id of preferred label container element
 * @param amount 
 *           number of existing synonym's names in case of removing existing one
 *           or null, if removing new synonym name
 * @param checkBoxId 
 *           the id of the checkbox to mark as checked
*/
function removeSynonymElement(btn, key, preferredContainerId, amount, checkBoxId) {
	// we need to set number of existing elements if it's null
	if (numberOfClonedElements[key] == null)
		numberOfClonedElements[key] = amount - 1;

	if (checkBoxId != null) {
		// we need to process already existing 
		// synonym name in special way
		voidName(btn, checkBoxId);
	} else {
		// as we are removing new synonym
		// we should remove synonym field element
		removeParentElement(btn);
	}
		
	// try to make preferred radio button unvisible
	// in case of deleting all its synonyms
	if (preferredContainerId != null) {
		if (numberOfClonedElements[key] == 0) {
			// makes group of preferred elements invisible in case if it was visible
			var preferrContainer = document.getElementById(preferredContainerId);
			for (var i = 0; i < preferrContainer.children.length; i++) {
				if (preferrContainer.children[i].style.visibility == "visible")
					preferrContainer.children[i].style.visibility = "hidden";
			}
		}
		numberOfClonedElements[key]--;
	}
}

/**
 * Handles removing of child element from parent one. It can be used when deleting new synonym field or
 * concept mapping field or concept index term field. 
 *
 * @param btn 
 *           the source object of event, that use it handler
*/
function removeParentElement(btn) {
	btn.parentNode.parentNode.removeChild(btn.parentNode);
}

/**
 * Calls the server via ajax to convert the concept datatype from boolean to coded
 * @param confirmationMessage the confirmation message to display to the user
 * @param conceptId the concept id of the concept to be converted
 */
function addAnswerToBooleanConcept(confirmationMessage, conceptId){
	 
	if (confirm(confirmationMessage)) {
		DWRConceptService.convertBooleanConceptToCoded(conceptId, function(reply) {
		    if(reply != null){
		    	if(reply == "refresh"){
		    		//refresh page to display the changes
		    		location.reload();
		    	}
		    	else{
		    		$j("#addAnswerError").html(reply);
		    		$j("#addAnswerError").show();
		    	}
		    }
		});	
    }
}

/**
 * This method is called when the remove button of an existing synonymm is called, it sets the hidden checkbox to check
 * to mark the name as voided
 * 
 * @param buttonElement the button object that was clicked
 * @param checkBoxId the id of the checkbox to mark as checked
 * 
 */
function voidName(buttonElement, checkBoxId) {	
	//set the value of the hidden input field to true to mark that it is voided
	document.getElementById(checkBoxId).value = "true";	
	$j(buttonElement.parentNode).hide();
}

/**
 * Method is called when the user edits an existing concept name to update the radio button value to be submitted to the server
 * @param textElement the text element whose text was edited
 * @param the id of radio button whose value attribute to change
 */
function setRadioValue(textElement, radioButtonId){
	if(textElement.value)
		document.getElementById(radioButtonId).value = textElement.value;
}

/**
 * Method is called when the user types in a name for a new synonym to update the radio button value to be submitted to the server
 * @param textElement the text element whose value was edited
 */
function setCloneRadioValue(textElement){
	 var inputs = textElement.parentNode.childNodes;
	//find the radio button and set its value attribute
	for (var x = 0; x < inputs.length; x++) {						
		if (inputs[x].type == 'radio')
			inputs[x].value = textElement.value;
	}
}

/**
 * Method is called to to add a new concept mapping and assigns the attribute values to the cloned element
 * @param id id of the element to clone
 * @param initialSizeOfClonedSiblings the number of mappings on page load
 */
function addConceptMapping(initialSizeOfClonedSiblings){
		
	var inputNamePrefix = 'conceptMappings';
	var newMappingRow = cloneElement('newConceptMapping', initialSizeOfClonedSiblings, inputNamePrefix);
	
	if(newMappingRow){
		newMappingRow.id = "";
		var index = numberOfClonedElements['newConceptMapping'];
		var inputs = newMappingRow.getElementsByTagName("input");
		var selects = newMappingRow.getElementsByTagName("select");
		
		//find the term(termId), name and code text boxes and set their names and ids
		for (var x = 0; x < inputs.length; x++) {
			var input = inputs[x];
			if (input && input.name == 'termId' && input.type == 'hidden') {
				input.name = inputNamePrefix+'[' + index + '].conceptReferenceTerm';
				input.id = inputNamePrefix+'[' + index + '].conceptReferenceTerm';
			}else if (input && input.name == 'term.code' && input.type == 'text') {
				input.id = 'term[' + index + '].code';
			}else if (input && input.name == 'term.name' && input.type == 'text') {
				input.id = 'term[' + index + '].name';
			}
		}
		
		//find the select element and set the name attribute for the conceptSource and map type
		for (var i in selects) {
			var select = selects[i];
			if (select && select.name == "type.name") {
				select.id = 'term[' + index + '].conceptMapType';
				select.name = inputNamePrefix+'[' + index + '].conceptMapType';
			}else if (select && select.name == "term.source") {
				select.id = 'term[' + index + '].source';
			}
		}
		
		if(!$j("#headerRow").is(":visible"))
			$j(".hideableEle").show();
		
		//set up the auto complete
		addAutoComplete('term[' + index + '].code', 'term[' + index + '].source', inputNamePrefix+'[' + index + '].conceptReferenceTerm', 'term[' + index + '].name')
	}
}

function addAutoComplete(displayInputId, sourceSelectElementId, hiddenElementId, nameInputId){
	var selectOption = document.getElementById(sourceSelectElementId);
	// set up the autocomplete on the conceptReferenceTerm input box for the new mapping
	new AutoComplete(displayInputId, new CreateCallback().conceptReferenceTermCallback(selectOption), {
		select: function(event, ui) {
			jquerySelectEscaped(hiddenElementId).val(ui.item.object.conceptReferenceTermId);
			//set the concept source in the source dropdown to match that of the selected term
			selectOption.value = ui.item.object.conceptSourceId;
			//display the name of the term
			if(document.getElementById(nameInputId))
				document.getElementById(nameInputId).value = ui.item.object.name;
		},
		placeholder:omsgs.referencTermSearchPlaceholder
	});
}

document.onkeypress = hotkeys;