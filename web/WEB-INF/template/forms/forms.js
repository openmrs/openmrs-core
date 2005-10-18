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
function selectObj(nodeName, obj) {
	// Fetch reference to the node
	var node = oDOM.selectSingleNode(nodeName);
	clearNil(node);

	// Set value of node to the obj
	node.text = obj.value + '^' + obj.innerText;

	closeTaskPane();
}

function insertObj(nodeName, obj) {
}

function appendObj(nodeName, obj) {
}



//	hide taskpane
function closeTaskPane() {
     window.location = "/WEB-INF/template/forms/blank";
}

function reloadPage() {
  document.location = document.location
}