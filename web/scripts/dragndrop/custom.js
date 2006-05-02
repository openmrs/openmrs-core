
function init(name) {
	var cont = document.getElementById(name);
	var list = getChild(cont, 'currentItems_' + name);
	DragDrop.makeListContainer( list, name );
	list.onDragOver = function() { this.style['background'] = '#EEF'; };
	list.onDragOut = function() {this.style['background'] = 'none'; };
	list.onDragDrop = function() {onDrop(this); };
	list.containerName = name;
			
	var list = getChild(cont, 'allItems_' + name);
	DragDrop.makeListContainer( list, name );
	list.onDragOver = function() { this.style['background'] = '#EEF'; };
	list.onDragOut = function() {this.style['background'] = 'none'; };
	list.onDragDrop = function() {onDrop(this); };
	list.containerName = name;
}

function onDrop(ul) {
	var items = DragDrop.serData(ul.containerName, "currentItems_" + ul.containerName);
	var list = getChild(ul.containerName, "savedItems_" + ul.containerName);
	//remove all savedItems
	while (list.options.length > 0) {
		list.options[0] = null;
	}
	
	for (var i = 0; i<items.length; i++) {
		var li = items[i];
		if (li.id != null) {
			var opt = document.createElement("option");
			opt.value = li.id;
			opt.innerHTML = li.innerHTML;
			opt.selected = true;
			list.appendChild(opt);
		}
	}
	
	formChanged = true;
};

function getChild(parent, id) {
	if (typeof parent == 'string')
		parent = document.getElementById(parent);
	var child = parent.firstChild;
	while (child != null) {
		if (child.id == id) 
			return child;
		if (child.hasChildNodes) {
			var tmp = getChild(child, id);
			if (tmp != null)
				return tmp;
		}
		child = child.nextSibling;
	}
	return null;
}