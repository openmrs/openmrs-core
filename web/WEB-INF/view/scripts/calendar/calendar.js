
function showCalendar(obj, yearsPrevious) {
	//set appendText to something so it doesn't automagically pop into the page
	var opts = { appendText: " " };
	if (yearsPrevious)
		opts["yearRange"] = "c-" + yearsPrevious + ":c10";
	
	var dp = new DatePicker(jsDateFormat, obj, opts);
	jQuery.datepicker.setDefaults(jQuery.datepicker.regional[jsLocale]);

	obj.onclick = null;
	dp.show();
	return false;
}
