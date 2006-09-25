/*
 * OpenMRS InfoPath Common Script Functions
 *
 * This document defines common JScript functions to be shared
 * across various InfoPath forms to simplify the function calls
 * needed in the individual InfoPath forms.
 *
 * author:  Burke Mamlin, MD
 * created: February 1, 2006
 * version: 1.2
 */

var SERVER_URL = "http://localhost:8080/amrs";
var TASKPANE_URL = SERVER_URL + "/formentry/taskpane";
var SUBMIT_URL = SERVER_URL + "/formUpload";
var PROBLEM_ADDED_ELEM = "problem_added";
var PROBLEM_RESOLVED_ELEM = "problem_resolved";

//==============================================================
// Delete row of a repeating table containing the event's source
// node. To call from click event use: deleteRow(eventObj)
//==============================================================
function deleteTableRow(eventObj) {
	// Delete row containing source node
	var parent = eventObj.Source.parentNode;
	parent.removeChild(eventObj.Source);
}

//===============================================================
// Navigate to an absolute URL within the taskpane.  If the 
// taskpane is not visible, it will be opened.
//===============================================================
function taskPaneNavigateToAbsoluteUrl(url) {
	var taskPane = XDocument.View.Window.TaskPanes.Item(0);
	taskPane.Visible = true;
	taskPane.HTMLDocument.parentWindow.location = url;
}

//===============================================================
// Navigate to a relative URL within the taskpane.  If the 
// taskpane is not visible, it will be opened.
//===============================================================
function taskPaneNavigateTo(url) {
	taskPaneNavigateToAbsoluteUrl(TASKPANE_URL + url);
}

//===============================================================
// Select a new diagnosis
//===============================================================
function selectNewDiagnosis() {
	taskPaneNavigateTo('/diagnosis.htm?mode=add');
}

//==============================================================
// Delete a new diagnosis from list
//==============================================================
function deleteNewProblem(eventObj) {
	// delete problem added
	var node = eventObj.Source;
	var newProbs = node.parentNode.selectNodes(PROBLEM_ADDED_ELEM);
	if (newProbs.length > 1) {
		deleteTableRow(eventObj);
	} else {
		node.selectSingleNode("value").text = "";
	}
}

//===============================================================
// Select a resolved diagnosis
//===============================================================
function selectResolvedDiagnosis() {
	taskPaneNavigateTo('/diagnosis.htm?mode=remove');
}

//==============================================================
// Delete a resolved diagnosis from list
//==============================================================
function deleteResolvedProblem(eventObj) {
	// delete problem resolved
	var node = eventObj.Source;
	var resolvedProbs = node.parentNode.selectNodes(PROBLEM_RESOLVED_ELEM);
	if (resolvedProbs.length > 1) {
		deleteTableRow(eventObj);
	} else {
		node.selectSingleNode("value").text = "";
	}
}

//===============================================================
// Select a provider
//===============================================================
function selectProvider() {
	taskPaneNavigateTo('/provider.htm');
}

//===============================================================
// Select a tribe
//===============================================================
function selectTribe() {
	taskPaneNavigateTo('/tribe.htm');
}


//===============================================================
// Submit form to server and close (if successful)
//===============================================================
// usage:
// 		submitAndClose(eventObj);
//		if (eventObj.ReturnStatus)
//			autoClose();
function submitAndClose(eventObj) {
	
	var err = XDocument.DOM.validate();
	if (err.errorCode != 0) {
		XDocument.UI.Alert("This form has errors. You cannot submit the form until you correct the errors.\nPlease check the form for errors and try again.\n\nERROR DETAILS:\n" + err.reason);
		eventObj.ReturnStatus = false;
		return;
	}

	// Create an XMLHTTP object for document transport.
	var objXmlHttp;
	try {
		objXmlHttp = new ActiveXObject("MSXML2.XMLHTTP");
	} catch (ex) {
		XDocument.UI.Alert("Could not create MSXML2.XMLHTTP object.\r\n" + ex.number + " - " + ex.description);

		// Exit with error
		eventObj.ReturnStatus = false;
		return;
	}

	// Post the XML document to strUrl.
	objXmlHttp.open("POST", SUBMIT_URL, false);
	try {
		objXmlHttp.send(XDocument.DOM.xml);
	} catch(ex) {
		XDocument.UI.Alert("Could not post (ASP) document to " + 
			SUBMIT_URL + "\r\n" + ex.number + " - " + ex.description);

		// Return with eventObj.ReturnStatus == false.
		return;
	}

	if (objXmlHttp.status == 200) {
		XDocument.UI.Alert("Form submitted successfully.");
		eventObj.ReturnStatus = true;
		return;
	} else {
		err = objXmlHttp.responseText;
		XDocument.UI.Alert(err);
		eventObj.ReturnStatus = false;
		return;
	}
}

function autoClose() {
    Application.ActiveWindow.Close(true);
}
