
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

function pickProblem(mode, nodeName, o) {
	var node = oDOM.selectSingleNode(nodeName);
	clearNil(node);
	
	o.value = o.value.toUpperCase();
	
	if (mode == 'add')
		insertObj(node, "problem_added", o);
	else
		appendObj(node, "problem_resolved", o);

	closeTaskPane();
}

function insertObj(node, newNodeName, obj) {
	var firstChild = node.childNodes.item(0);
	var new_elem = firstChild.cloneNode(true);
	
	var new_elem_value = new_elem.selectSingleNode("value");
	clearNil(new_elem_value);
	new_elem_value.text = obj.key + '^' + obj.value;
	var firstResolved = node.selectSingleNode("problem_resolved");
	if (firstChild.selectSingleNode("value").text == "")
		node.removeChild(firstChild);
	node.insertBefore(new_elem, firstResolved);
}

function appendObj(node, newNodeName, obj) {
	var firstResolved = node.selectSingleNode("problem_resolved");
	var new_elem = firstResolved.cloneNode(true);
	var new_elem_value = new_elem.selectSingleNode("value");
	clearNil(new_elem_value);
	new_elem_value.text = obj.key + '^' + obj.value;
	if (firstResolved.selectSingleNode("value").text == "")
		node.removeChild(firstResolved);
		
	node.appendChild(new_elem);
}

function old_appendObj(node, newNodeName, obj) {
	
	var lastChild = node.lastChild;
	var new_elem = oDOM.createNode(1, newNodeName, "");
	var new_elem_value = oDOM.createNode(1, "value", "");
	new_elem.appendChild(new_elem_value);
	new_elem_value.text = obj.key + '^' + obj.value;
	node.appendChild(new_elem);
	if (lastChild.text == '')
		node.removeChild(lastChild);
}

//	hide taskpane
function closeTaskPane() {
     window.location = "index.jsp";
}

function reloadPage() {
  document.location = document.location
}