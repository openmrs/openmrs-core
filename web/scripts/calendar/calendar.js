
function showCalendar(obj) {
	makeCalendar(obj);
	if(self.gfPop)
		gfPop.fPopCalendar(obj);
	return false;
}

function makeCalendar(obj) {
	var id = "gToday:normal.htm";
	if (document.getElementById(id) == null) {
		var iframe = document.createElement("iframe");
		iframe.width=174;
		iframe.height=189;
		iframe.name=id;
		iframe.id = id;
		iframe.src= '/@WEBAPP.NAME@/scripts/calendar/ipopeng.htm';
		iframe.scrolling='no';
		iframe.frameborder='0';
		iframe.style.visibility = 'visible';
		iframe.style.zIndex='999';
		iframe.style.position='absolute';
		iframe.style.top='-500px';
		iframe.style.left= '-500px';
		document.body.appendChild(iframe);
	}
}

window.onload=makeCalendar;