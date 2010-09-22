
function showCalendar(obj) {
	//set appendText to something so it doesnt automagically pop into the page
	var id = obj.id;
	if(!id) {
		obj.id = obj.name;
		if(!obj.id) {
			obj.id = "something_random" + (Math.random()*1000);
		}
		id = obj.id;
	}
	
	var dp = new DatePicker(jsDateFormat, id, { appendText: " " });
	$j.datepicker.setDefaults($j.datepicker.regional[jsLocale]);

	obj.onclick = null;
	dp.show();
	return false;
}
