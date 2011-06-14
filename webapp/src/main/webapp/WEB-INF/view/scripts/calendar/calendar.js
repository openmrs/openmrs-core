function showCalendar(obj, yearsPrevious) {
	//set appendText to something so it doesnt automagically pop into the page
	var id = obj.id;
	if(!id) {
		obj.id = obj.name;
		if(!obj.id) {
			obj.id = "something_random" + (Math.random()*1000);
		}
		id = obj.id;
	}
	
	var opts = { appendText: " " };
	if (yearsPrevious)
		opts["yearRange"] = "c-" + yearsPrevious + ":c10";
	
	var dp = new DatePicker(jsDateFormat, id, opts);
	jQuery.datepicker.setDefaults(jQuery.datepicker.regional[jsLocale]);
	
	obj.onclick = null;
	dp.show();
	return false;
}
