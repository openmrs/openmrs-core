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
XDocument.DOM.setProperty("SelectionNamespaces", 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:my="http://schemas.openmrs.org/2006/FormEntry" xmlns:xd="http://schemas.microsoft.com/office/infopath/2003"');
//</namespacesDefinition>


//=======
// The following function handler is created by Microsoft Office InfoPath.
// Do not modify the name of the function, or the name and number of arguments.
//=======
function COMPLETED::OnClick(eventObj)
{
 	submitAndClose(eventObj);
	if (eventObj.ReturnStatus)
		autoClose();
}
