// Reference to InfoPath document
var oXDocument = window.external.Window.XDocument;

// Reference to XML DOM in InfoPath's active window
var oDOM = oXDocument.DOM;

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
	node.text = obj.value + '^' + obj.innerText;

	closeTaskPane();
}

function pickProblem(mode, nodeName, o) {
	var node = oDOM.selectSingleNode(nodeName);
	clearNil(node);
	if (mode == 'add')
		insertObj(node, "problem_added", o);
	else
		appendObj(node, "problem_resolved", o);

	closeTaskPane();
}

function insertObj(node, newNodeName, obj) {
	new_elem = oDOM.createNode(1, newNodeName, "");
	new_elem_value = oDOM.createNode(1, "value", "");
	new_elem.appendChild(new_elem_value);
	new_elem_value.text = obj.value + '^' + obj.innerText;
	node.insertBefore(new_elem, node.childNodes.item(0));
}

function appendObj(node, newNodeName, obj) {
	new_elem = oDOM.createNode(1, newNodeName, "");
	new_elem_value = oDOM.createNode(1, "value", "");
	new_elem.appendChild(new_elem_value);
	new_elem_value.text = obj.value + '^' + obj.innerText;
	node.appendChild(new_elem);
}



//	hide taskpane
function closeTaskPane() {
     window.location = "index.jsp";
}

function reloadPage() {
  document.location = document.location
}