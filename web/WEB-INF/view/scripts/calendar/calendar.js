
function showCalendar(obj) {
	//set appendText to something so it doesnt automagically pop into the page
	var dp = new DatePicker(jsDateFormat, obj, { appendText: " " });
	jQuery.datepicker.setDefaults(jQuery.datepicker.regional[jsLocale]);

	obj.onclick = null;
	dp.show();
	return false;
}
