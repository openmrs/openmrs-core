function GetXmlHttpObject() { 
	var objXMLHttp = null;
	if (window.XMLHttpRequest) {
		objXMLHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		objXMLHttp=new ActiveXObject("Microsoft.XMLHTTP");
	}
	return objXMLHttp;
}

function loadInto(loadingText, url, layer, delay) {
	if (delay > 0) {
		setTimeout("loadInto('" + loadingText + "', '" + url + "', ':" + layer + "')", delay);
	} else {
		loadInto(loadingText, url, layer);
	}
}

function loadInto(loadingText, url, layer) {
	document.getElementById(layer).innerHTML = loadingText;
	var xmlHttp=GetXmlHttpObject();
	xmlHttp.onreadystatechange=function() { loadStateChanged(xmlHttp, layer); }
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
}

function loadStateChanged(xmlHttpObj, layer) {
	if (xmlHttpObj.readyState==4 || xmlHttpObj.readyState=="complete") { 
		document.getElementById(layer).innerHTML = xmlHttpObj.responseText;
	} 
}