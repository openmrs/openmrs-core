function showCalendar(obj, yearsPrevious) {
	// if the object doesn't have an id, just set it to some random text so jq can use it
	if(!obj.id) {
		obj.id = "something_random" + (Math.random()*1000);
	}
	
	//set appendText to something so it doesn't automagically pop into the page
	var opts = { appendText: " " };
	if (yearsPrevious)
		opts["yearRange"] = "c-" + yearsPrevious + ":c10";
	
	var dp = new DatePicker(jsDateFormat, obj.id, opts);
	jQuery.datepicker.setDefaults(jQuery.datepicker.regional[jsLocale]);
	
	obj.onclick = null;
	dp.show();
	return false;
}
