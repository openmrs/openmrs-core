function toggleLayer(whichLayer) {
	if (document.getElementById) {
        var style = document.getElementById(whichLayer).style;
     	if (style.display == "none") {
            style.display = "";
        } else {
            style.display = "none";
        }
    } else {
        window.alert("Your browser doesn't support document.getElementById");
    }
}

function showLayer(whichLayer) {
    var style = document.getElementById(whichLayer).style;
    style.display = "";
}

function hideLayer(whichLayer) {
    var style = document.getElementById(whichLayer).style;
    style.display = "none";
}

function GetXmlHttpObject() { 
	var objXMLHttp = null;
	if (window.XMLHttpRequest) {
		objXMLHttp=new XMLHttpRequest()
	} else if (window.ActiveXObject) {
		objXMLHttp=new ActiveXObject("Microsoft.XMLHTTP")
	}
	return objXMLHttp
}

function loadInto(loadingText, url, layer) {
	document.getElementById(layer).innerHtml = loadingText;
	var xmlHttp=GetXmlHttpObject()
	xmlHttp.onreadystatechange=function() { loadStateChanged(xmlHttp, layer); }
	xmlHttp.open("GET",url,true)
	xmlHttp.send(null)
}

function loadStateChanged(xmlHttpObj, layer) {
	if (xmlHttpObj.readyState==4 || xmlHttpObj.readyState=="complete") { 
		document.getElementById(layer).innerHTML = xmlHttpObj.responseText;
	} 
}
