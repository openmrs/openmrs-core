
function showCalendar(obj) {
	//set appendText to something so it doesnt automagically pop into the page
	var dp = new DatePicker(jsDateFormat, obj.id, { appendText: " " });
	$j.datepicker.setDefaults($j.datepicker.regional[jsLocale]);

	obj.onclick = null;
	dp.show();
	return false;
}
