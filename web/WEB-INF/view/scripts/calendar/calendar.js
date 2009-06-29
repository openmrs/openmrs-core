
function showCalendar(obj) {
	makeCalendar(obj);
	if(self.gfPop)
		gfPop.fPopCalendar(obj);
	return false;
}

function makeCalendar(obj) {

	// turn off auto complete on inputs using this calendar
	if (document.getElementsByTagName) {
		var inputs = document.getElementsByTagName("input");
		for (var i=0;i<inputs.length; i++) {
			if (inputs[i].onclick &&
				inputs[i].onclick.toString().indexOf("showCalendar") != -1) {
					inputs[i].setAttribute("autocomplete", "off");
			}
		}
	}

	// make the iframe to contain the calendar
	var id = "gToday:normal.jsp";
	if (document.getElementById(id) == null) {
		var iframe = document.createElement("iframe");
		iframe.width=174;
		iframe.height=189;
		iframe.name=id;	// also defined in ipopeng.jsp as an IE hack.
		iframe.id = id;
		iframe.src= openmrsContextPath + '/scripts/calendar/ipopeng.html';
		iframe.scrolling='no';
		iframe.frameBorder='0';
		iframe.style.visibility = 'visible';
		iframe.style.zIndex='999';
		iframe.style.position='absolute';
		iframe.style.top='-500px';
		iframe.style.left= '-500px';
		var bodies = document.getElementsByTagName("body");
		var body = bodies[0];
		iframe.name = id;
		body.appendChild(iframe);
	}
}

if (addEvent) {
	addEvent(window, "load", makeCalendar);
}
else {
	makeCalendar();
}
