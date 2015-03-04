dojo.require("dojo.widget.openmrs.UserSearch");
dojo.require("dojo.widget.openmrs.OpenmrsPopup");

function gotoUrl(url, userId) {
	if (url === null || url === '') {
		return false;
	} else {
		window.location = url + "?userId=" + userId;
	}
	return false;
}