/*
 * This file contains functions for data validation and form-level events.
 * Because the functions are referenced in the form definition (.xsf) file, 
 * it is recommended that you do not modify the name of the function,
 * or the name and number of arguments.
 *
*/

// The following line is created by Microsoft Office InfoPath to define the prefixes
// for all the known namespaces in the main XML data file.
// Any modification to the form files made outside of InfoPath
// will not be automatically updated.
//<namespacesDefinition>
XDocument.DOM.setProperty("SelectionNamespaces", 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:openmrs="http://schema.yoursite.org/FormEntry/1" xmlns:my="http://schemas.microsoft.com/office/infopath/2003/myXSD/2006-07-25T11:22:21" xmlns:xd="http://schemas.microsoft.com/office/infopath/2003"');
//</namespacesDefinition>


//=======
// The following function handler is created by Microsoft Office InfoPath.
// Do not modify the name of the function, or the name and number of arguments.
//=======
function SubmitButton::OnClick(eventObj)
{
 	submitAndClose(eventObj);
	if (eventObj.ReturnStatus)
		autoClose();
}

//=======
// The following function handler is created by Microsoft Office InfoPath.
// Do not modify the name of the function, or the name and number of arguments.
//=======
function DeleteNewProblem::OnClick(eventObj)
{
	deleteNewProblem(eventObj);
}

//=======
// The following function handler is created by Microsoft Office InfoPath.
// Do not modify the name of the function, or the name and number of arguments.
//=======
function DeleteResolvedProblem::OnClick(eventObj)
{
	deleteResolvedProblem(eventObj);
}

//=======
// The following function handler is created by Microsoft Office InfoPath.
// Do not modify the name of the function, or the name and number of arguments.
//=======
function AddNewProblem::OnClick(eventObj)
{
	selectNewDiagnosis();
}

//=======
// The following function handler is created by Microsoft Office InfoPath.
// Do not modify the name of the function, or the name and number of arguments.
//=======
function AddResolvedProblem::OnClick(eventObj)
{
	selectResolvedDiagnosis();
}

//=======
// The following function handler is created by Microsoft Office InfoPath.
// Do not modify the name of the function, or the name and number of arguments.
//=======
function SelectProvider::OnClick(eventObj)
{
	selectProvider();
}

