function popWindow(url, winName, width, height, top, left) {
	var openmrsWindow = window.open(url, winName, "toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=0,resizable=0,width=" + width + ",height=" + height + ",top=" + top + ",left=" + left);
}

function copyValueToParentWindow( parentFieldName, val ) {
	var parentWindow = window.opener;
	var obj = parentWindow.document.getElementById(parentFieldName);
	if ( obj ) {
		obj.value = val;
	} else {
		// do nothing
	}
}

function focusParent() {
	if ( window.opener ) window.opener.focus();
}

function unfocusParentField() {
	var parentWindow = window.opener;
	var obj = parentWindow.document.getElementById(parentFieldName);
	if ( obj ) {
		if ( obj.unfocus ) {
			obj.unfocus();
		} else if ( obj.blur ) {
			obj.blur();
		} else {
			//alert('obj has no unfocus() or blur()');
		}
	} else {
		// do nothing
	}
}