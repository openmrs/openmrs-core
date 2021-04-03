/**
 * Used in the initial setup wizard.
 * @see org.openmrs.web.filter.initialization.InitializationFilter
 */


/**
 * Checks a radio box with the given name and given value
 * 
 * @param radioName the radio name to check
 * @param value the value to match to the radio box to check
 */
function clickRadio(radioName, value) {
    var radio = document.getElementsByName(radioName);
    var radioLength = radio.length;
    for(var i = 0; i < radioLength; i++) {
        radio[i].checked = false;
        if(radio[i].value == value.toString()) {
            radio[i].checked = true;
        }
    }
}

function submitenter(myfield,e) {
	var keycode;
    if (window.event) {
        keycode = window.event.keyCode;
    } else if (e) {
        keycode = e.which;
    } else {
        return true;
    }

    if (keycode == 13) {
        myfield.form.submit();
        return false;
    } else {
        return true;
    }
}