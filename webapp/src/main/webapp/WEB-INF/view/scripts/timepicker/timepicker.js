function showTimePicker(obj) {
     var id = obj.id;
    if (!id) {
        obj.id = obj.name;
        if (!obj.id) {
            obj.id = "something_random" + (Math.random() * 1000);
        }
        id = obj.id;
    }
    var opts = { appendText: " " };
    
    var dp = new TimePicker(jsTimeFormat, id, opts);
    
    jQuery.timepicker.setDefaults(jQuery.timepicker.regional[jsLocale]);
    
	obj.onclick = null;
	dp.show();
	return false;
}

function showDateTimePicker(obj) {
    var id = obj.id;
    if (!id) {
        obj.id = obj.name;
        if (!obj.id) {
            obj.id = "something_random" + (Math.random() * 1000);
        }
        id = obj.id;
    }
    var opts = { appendText: " " };

    if (gp.weekStart)
		opts["firstDay"] = gp.weekStart;
	
    var dp = new DateTimePicker(jsDateFormat, jsTimeFormat, id, opts);
	
    jQuery.datepicker.setDefaults(jQuery.datepicker.regional[jsLocale]);
    jQuery.timepicker.setDefaults(jQuery.timepicker.regional[jsLocale]);
    
	obj.onclick = null;
	dp.show();
	return false;
}