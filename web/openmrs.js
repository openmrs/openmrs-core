
function setDisplay(className, display) {
	if (document.getElementById) {
		contentNode = document.getElementById("content");
		setDisplayRecursion(contentNode, className, display);
	} 
}

function setDisplayRecursion(node, className, display) {
	for (i=0; i < node.childNodes.length; i++) {
		childNode = node.childNodes[i];
		if (childNode.className == className) {
			node.style.display = display;
		}
		setDisplayRecursion(childNode, className, display);
	}
}	