/*
* jQuery UI Autocomplete Auto Select Extension
*
* Copyright 2010, Scott González (http://scottgonzalez.com)
* Dual licensed under the MIT or GPL Version 2 licenses.
*
* http://github.com/scottgonzalez/jquery-ui-extensions
* 
* Modified like 24 to have separate matcher calls
* Added line 30 to set the value of the text box like jq does for us
*/
(function( jQuery ) {

jQuery.ui.autocomplete.prototype.options.autoSelect = true;
jQuery( ".ui-autocomplete-input" ).live( "blur", function( event ) {
var autocomplete = jQuery( this ).data( "autocomplete" );
if ( !autocomplete.options.autoSelect || autocomplete.selectedItem ) { return; }

var matcher = new RegExp( "^" + jQuery.ui.autocomplete.escapeRegex( jQuery(this).val() ) + "$", "i" );
/*alert("value: " + jQuery(this).val() + " matcher: ");*/
autocomplete.widget().children( ".ui-menu-item" ).each(function() {
var item = jQuery( this ).data( "item.autocomplete" );
/*alert("matched? " + matcher.test(item.value) + " item.value: " + item.value);*/
if ( matcher.test( item.label ) || matcher.test( item.value ) || matcher.test( item ) ) {
autocomplete.selectedItem = item;
return false;
}
});
if ( autocomplete.selectedItem ) {
jQuery( this ).val(autocomplete.selectedItem.value);
/*alert("triggering select");*/
autocomplete._trigger( "select", event, { item: autocomplete.selectedItem } );
}
else {
	/* Nothing valid was selected */
	jQuery( this ).val("");
}
});

}( jQuery ));