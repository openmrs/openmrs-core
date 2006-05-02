
function markAlertRead(self, alertId) {
	DWRAlertService.markAlertRead(null, alertId);
	var parent = self.parentNode;
	parent.style.display = "none";
	var unreadAlertSizeBox = document.getElementById('unreadAlertSize');
	var unreadAlertSize = parseInt(unreadAlertSizeBox.innerHTML);
	if (unreadAlertSize == 1) {
		// hide the entire alert outer div because they read the last alert
		parent = parent.parentNode.parentNode;
		parent.style.display = "none";
	}
	else {
		var unreadAlertSize = unreadAlertSize - 1;
		unreadAlertSizeBox.innerHTML = unreadAlertSize;
	}
		
	return false;
}