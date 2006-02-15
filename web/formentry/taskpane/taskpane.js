
if (window.external != null) {
	// Reference to InfoPath document
	var oXDocument = window.external.Window.XDocument;
}

if (typeof oXDocument != 'undefined') {
	// Reference to XML DOM in InfoPath's active window
	var oDOM = oXDocument.DOM;
}

// Clear xsi:nil entry (needed before adding children to nodes)
function clearNil(node) {
	// The xsi:nil needs to be removed before we set the value.
	if (node.getAttribute("xsi:nil"))
		node.removeAttribute("xsi:nil");
}

// Close the InfoPath TaskPane
function closeTaskPane() {
	window.external.Window.XDocument.View.Window.TaskPanes.Item(0).Visible = false;
}

// set nodeName's value to obj
function setObj(nodeName, obj) {
	// Fetch reference to the node
	var node = oDOM.selectSingleNode(nodeName);
	clearNil(node);

	// Set value of node to the obj
	node.text = obj.key + '^' + obj.value;

	closeTaskPane();
}

// returns HL7 version of problem for inserting into form
function getProblemValue(obj) {
	return obj.key + "^" + obj.value.toUpperCase() + "^99DCT";
}

// add problem (diagnosis) concept to a list (new problems or resolved problems)
function pickProblem(mode, nodeName, obj) {
	var node = oDOM.selectSingleNode(nodeName);
	clearNil(node);
	var newProblem;
	var nodeName;
	if (mode == 'add') {
		newProblem = true;
		nodeName = "problem_added";
	} else if (mode == 'remove') {
		newProblem = false;
		nodeName = "problem_resolved";
	} else {
		return;
	}
	
	var refNode = node.selectSingleNode(nodeName);
	var valueNode = refNode.selectSingleNode("value");
	if (valueNode.text == "") {
		clearNil(valueNode);
		valueNode.text = getProblemValue(obj);
	} else {
		// create new elem as clone with proper value
		var newElem = refNode.cloneNode(true);
		var newElemValue = newElem.selectSingleNode("value");
		clearNil(newElemValue);

		// insert *before* setting value to avoid a bug where value is corrupted during insert
		if (newProblem) {
			var firstResolved = node.selectSingleNode("problem_resolved");
			node.insertBefore(newElem, firstResolved);
		} else {
			node.appendChild(newElem);
		}

		// value must be set *after* inserting node; otherwise it gets munged
		newElemValue.text = getProblemValue(obj);
	}

	closeTaskPane();
}

//	hide taskpane
function closeTaskPane() {
     window.location = "index.jsp";
}

function reloadPage() {
  document.location = document.location
}