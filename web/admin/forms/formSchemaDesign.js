//Starting with the given node, find the nearest containing element
//with the specified tag name and optionally class name.
function getAncestorWithClass(node, tagName, className)
{
  var lcTag = tagName.toLowerCase();
  while (node != null)
  {
	if (node.tagName != null && node.tagName.toLowerCase() == lcTag)
	{
		if (typeof(className) != "undefined")
		{
			if (YAHOO.util.Dom.hasClass(node, className))
			{
				return node;
			}
		}
		else
		{
			return node;
		}
	}
	node = node.parentNode;
  }

  return node;
}

/**
* Get the contents of an element, or a clone of them.
*/
function getElementContents(el, clone)
{
	el = YAHOO.util.Dom.get(el);
	var result = document.createDocumentFragment();
	if (clone)
	{
		for (var i = 0; i < el.childNodes.length; i++)
			result.appendChild(el.childNodes[i].cloneNode(true));
	}
	else
	{
		while (el.firstChild)
			result.appendChild(el.firstChild);
	}
	return result;
}

DDTreeView = function(id)
{
	this.init(id);
	this.insertHighlight = document.createElement("div");
	this.insertHighlight.style.display = "none";
	this.insertHighlight.style.position = "absolute";
	this.insertHighlight.style.backgroundColor = "activecaption";
	this.insertHighlight.style.height = "2px";
	this.insertHighlight.style.width = "45px";
	document.body.appendChild(this.insertHighlight);
	this.newNodeCount = 0;
	
	//	Create ContextMenu
	this.contextMenu = new YAHOO.widget.ContextMenu("contextMenu", {trigger:this.id});
    this.editMenuItem = new YAHOO.widget.ContextMenuItem("Edit This Field");
	this.editMenuItem.clickEvent.subscribe(this.editField, this, true);
	this.contextMenu.addItem(this.editMenuItem);
    this.deleteMenuItem = new YAHOO.widget.ContextMenuItem("Delete");
	this.deleteMenuItem.clickEvent.subscribe(this.deleteMenu, this, true);
	this.contextMenu.addItem(this.deleteMenuItem);

	//	Add a "move" event handler to the context menu 
    this.contextMenu.beforeShowEvent.subscribe(this.onMoveContextMenu, this, true);
	this.contextMenu.render(document.body);
	
	
	// Get div and input for editable formfields
	this.editDiv = document.getElementById("editFormField");
	
	this.fieldIdInput = document.getElementById("fieldId");
	this.fieldNameInput = document.getElementById("fieldName");
	this.descriptionInput = document.getElementById("description");
	this.fieldTypeInput = document.getElementById("fieldType");
	this.conceptIdInput = document.getElementById("conceptId");
	this.conceptNameTag = document.getElementById("conceptName");
	this.tableNameInput = document.getElementById("tableName");
	this.attributeNameInput = document.getElementById("attributeName");
	this.defaultValueInput = document.getElementById("defaultValue");
	this.selectMultipleCheckbox = document.getElementById("selectMultiple");
	this.numFormsTag = document.getElementById("numForms");
	
	this.formFieldIdInput = document.getElementById("formFieldId");
	this.fieldNumberInput = document.getElementById("fieldNumber");
	this.fieldPartInput = document.getElementById("fieldPart");
	this.pageNumberInput = document.getElementById("pageNumber");
	this.minOccursInput = document.getElementById("minOccurs");
	this.maxOccursInput = document.getElementById("maxOccurs");
	this.isRequiredCheckbox = document.getElementById("required");
	this.saveFieldButton = document.getElementById("saveFormField");
	this.cancelFieldButton = document.getElementById("cancelFormField");
	
};

DDTreeView.prototype = new YAHOO.widget.TreeView();
DDTreeView.prototype.superDraw = DDTreeView.prototype.draw;

DDTreeView.prototype.getData = function()
{
	var result = this.data || {};
	var r = this.getRoot();
	var c = [];
	result[result.length]= c;
	for (var i = 0; i < r.children.length; i++)
	{
		c.push({node:this.getNodeData(r.children[i])});
	}
	return {tree:result};
};

DDTreeView.prototype.getNodeData = function(node)
{
	var result = node.data || {};
	result.description = node.label;
	if (node.canHaveChildren)
	{
		var c = [];
		result[result.length] = c;
		for (var i = 0; i < node.children.length; i++)
		{
			c.push({node:this.getNodeData(node.children[i])});
		}
	}
	return result;
};

DDTreeView.prototype.submit = function(url)
{
	var result = objectToXml(this.getData());
	alert(result);
};

DDTreeView.prototype.draw = function()
{
//	Call superclass's draw()
	this.superDraw();

//	Make the tree droppable on. Extend this object to look like a DDTarget.
	this.ddTarget =  new YAHOO.util.DDTarget(this.id, this.id);
	var _this = this;
	this.ddTarget.onDragEnter = function()
	{
		this.onDragEnter.apply(_this, arguments);
	};
	this.ddTarget.onDragOver = function()
	{
		this.onDragOver.apply(_this, arguments);
	};
	this.ddTarget.onDragDrop = function()
	{
		return _this.onDragDrop.apply(_this, arguments);
	};
	this.ddTarget.onDragOut = function()
	{
		_this.onDragOut.apply(_this, arguments);
	};


};

//Given a target DOM element, work up to find its label,
//extract the node index, and look it up in the tree.
DDTreeView.prototype.getTreeNode = function(target)
{
	var result = getAncestorWithClass(target, "div", "ygtvitem");
	if (result && result.id)
	{
		result = YAHOO.widget.TreeView.getNode(this.id, parseInt(/ygtv(\d+)/.exec(result.id)[1]));
	}
	return result;
};

//Call when a label has been changed
DDTreeView.prototype.nodeRenamed = function(eventType, args)
{
	var changedElement = args[0];
	var target = this.getTreeNode(changedElement);
	target.setUpLabel(changedElement.innerHTML);
};

DDTreeView.prototype.onMoveContextMenu = function(e, o)
{
	var target = this.getTreeNode(this.contextMenu.contextEventTarget);
	if (target)
	{	
		//Disable editing and deletion if we clicked on the top level Menu.
		this.editMenuItem.cfg.setProperty("disabled", (target.parent == this.getRoot()));
		this.deleteMenuItem.cfg.setProperty("disabled", (target.parent == this.getRoot()));
	}
	else
	{
		this.contextMenu.cfg.applyConfig({visible:false });
    }
	YAHOO.util.Event.preventDefault(e);
};

//Called from the context menu in the scope of the MenuTree
DDTreeView.prototype.editField = function(e, o)
{
	var target = this.getTreeNode(this.contextMenu.contextEventTarget);
	
	if (target)
	{
		target.getLabelEl().parentNode.appendChild(this.editDiv);
		
		var labelRegion = YAHOO.util.Region.getRegion(target.getLabelEl());
		var treeRegion = YAHOO.util.Region.getRegion(document.getElementById(tree.id));

		YAHOO.util.Event.addListener(this.saveFieldButton, "click", this.saveField, this, true);
		YAHOO.util.Event.addListener(this.cancelFieldButton, "click", this.cancelField, this, true);
		
		var s = this.editDiv.style;
		s.top = labelRegion.top + "px";
		s.left = labelRegion.left + "px";
		s.width = (treeRegion.right - labelRegion.left) + "px";
		
		// add edit values fields here
		if (target.data) {
			var data = target.data;
			// field info
			this.fieldNameInput.value = data["fieldName"];
			this.descriptionInput.value = data["description"];
			for (var i=0; i<this.fieldTypeInput.options.length; i++) {
				if (this.fieldTypeInput.options[i].value == data["fieldType"])
					this.fieldTypeInput.options[i].selected = true;
			}
			chooseFieldType(this.fieldTypeInput.value);
			this.conceptIdInput.value = data["conceptId"];
			this.conceptNameTag.innerHTML = data["conceptName"];
			
			if (data["fieldId"]) {
				this.fieldIdInput.value = data["fieldId"];
				this.tableNameInput.value = data["tableName"];
				this.attributeNameInput.value = data["attributeName"];
				this.defaultValueInput.value = data["defaultValue"];
				this.selectMultipleCheckbox.checked = data["selectMultiple"];
				this.numFormsTag.innerHTML = data["numForms"];
			}
			else {
				this.fieldIdInput.value = this.tableNameInput.value = "";
				this.attributeNameInput.value = this.defaultValueInput.value = "";
				this.selectMultipleCheckbox.checked = this.numFormsTag.innerHTML = "";
			}
			
			// formField info
			this.formFieldIdInput.value = data["uuid"];
			this.fieldNumberInput.value = data["fieldNumber"];
			if (data["fieldPart"])
				this.fieldPartInput.value = data["fieldPart"];
			this.pageNumberInput.value = data["pageNumber"];
			this.minOccursInput.value = data["minOccurs"];
			this.maxOccursInput.value = data["maxOccurs"];
			this.isRequiredCheckbox.checked = data["isRequired"];
		}
		
		s.display = "";
		
		if (data["numForms"] && parseInt(data["numForms"]) > 1)
			disableField();
		else
			enableField();

		this.fieldNumberInput.focus();
	}
	this.contextMenu.hide();
};

DDTreeView.prototype.saveField = function()
{
	YAHOO.util.Event.removeListener(this.saveFieldButton, "click", this.saveField);
	var target = this.getTreeNode(this.contextMenu.contextEventTarget);
	
	if (target.data) {
		var data = target.data;
		var changed = false;
		changed = changed || (this.fieldNameInput.value != data["fieldName"]);
		changed = changed || (this.descriptionInput.value != data["description"]);
		changed = changed || (this.fieldTypeInput.value != data["fieldType"]);
		changed = changed || (this.conceptIdInput.value != data["conceptId"]);
		changed = changed || (this.tableNameInput.value != data["tableName"]);
		changed = changed || (this.attributeNameInput.value != data["attributeName"]);
		changed = changed || (this.defaultValueInput.value != data["defaultValue"]);
		changed = changed || (this.selectMultipleCheckbox.checked != data["selectMultiple"]);
		
		changed = changed || (this.fieldNumberInput.value != data["fieldNumber"]);
		changed = changed || (this.fieldPartInput.value != data["fieldPart"]);
		changed = changed || (this.pageNumberInput.value != data["pageNumber"]);
		changed = changed || (this.minOccursInput.value != data["minOccurs"]);
		changed = changed || (this.maxOccursInput.value != data["maxOccurs"]);
		changed = changed || (this.isRequiredCheckbox.checked != data["isRequired"]);
	}
	
	this.editDiv.style.display = "none";
	if (changed) {
		// save field
		save(target);
		var el = document.getElementById(target.labelElId);
		el.innerHTML = getFieldLabel(target.data);
	}
};

DDTreeView.prototype.cancelField = function()
{
	YAHOO.util.Event.removeListener(this.saveFieldButton, "click", this.saveField);
	YAHOO.util.Event.removeListener(this.cancelFieldButton, "click", this.cancelField);
	
	this.editDiv.style.display = "none";
	cancel();
};

//Called from the context menu in the scope of this DDTreeView
DDTreeView.prototype.deleteMenu = function(e, o)
{
	this.contextMenu.hide();
	var answer = confirm("Are you sure you want to delete this item?");
	if (answer == true) {
		var target = this.getTreeNode(this.contextMenu.contextEventTarget);
		if (target) {
			this.removeNode(target, true);
			if (target.data) {
				var id = target.data["uuid"];
				DWRFormService.deleteFormField('', id);
			}
		}
	}
};

//Returns the lowest target node at the event's coordinates
DDTreeView.prototype.getInsertionPoint = function(e, ddObject)
{
	this.insertHighlight.style.display = "none";
	var p = new YAHOO.util.Point(YAHOO.util.Event.getPageX(e),YAHOO.util.Event.getPageY(e));
	var c = this.getRoot().children;
	for (var i = 0; i < c.length; i++)
	{
		var result = this._getInsertionPoint(c[i], p, ddObject);
		if (result)
		{
		//	Handle no-op cases, like inserting before or after self.
			if (ddObject.originalNode)
			{
				var dragNode = ddObject.originalNode;
				if (((result.beforeNode == null) && (result.root == dragNode.parent) && !dragNode.nextSibling) ||
					(result.afterNode == dragNode) ||
					(result.beforeNode == dragNode) ||
					(result.afterNode && (result.afterNode== dragNode.previousSibling)) ||
					(result.beforeNode && (result.beforeNode == ddObject.nextSibling)))
				{
					this.insertHighlight.style.display = "none";
					return;
				}
			}
			return result;
		}
	}
};

/**			Returns an object representing the target node position at the event's coordinates.

The object is {root:nodeToInsertInto
				beforeNode|afterNode:reference node to insert before or after}
*/
DDTreeView.prototype._getInsertionPoint = function(node, p, ddObject)
{
//	If the dragged object was one created by the DDTreeView as a new
//	node, then hovering over itself does nothing.
	if (ddObject.originalNode)
	{
		if (ddObject.originalNode == node)
			return;
	}

//	The region occupied by the label element.
	var labelRegion = YAHOO.util.Region.getRegion(node.getLabelEl());

	if (node.canHaveChildren)
	{
		//The element containing child nodes.
		var childElement = node.getChildrenEl();

		//Get the region occupied by the child nodes.
		var childRegion = YAHOO.util.Region.getRegion(childElement);

		//Get the region occupied by the whole root line. Ensure it's as wide as the tree
		var rootRegion = YAHOO.util.Region.getRegion(childElement.previousSibling);
		var treeRegion = YAHOO.util.Region.getRegion(this.getRoot().getEl());
		rootRegion.left = treeRegion.left;
		rootRegion.right = treeRegion.right;

		//If we're over the label, we're inserting either at the top,
		//or, if it's a collapsed node and we're below its centre line,
		//below it.
		if (rootRegion.contains(p))
		{
			this.insertHighlight.style.top = (labelRegion.bottom + 5) + "px";
			this.insertHighlight.style.left = (labelRegion.left + 17) + "px";
			this.insertHighlight.style.display = "";

		//	If the node is expanded, we're inserting before its first child.
			if (node.expanded)
				return {root:node, beforeNode:node.children[0]};
			else
			{
		//		If the node is collapsed, then decide whether to insert
		//		AFTER it, or INTO it.
				if (!node.parent.isRoot())
				{
		//			Hovering within 4 pixels of the bottom - insert AFTER it
					if (p.top > (rootRegion.bottom - 5))
					{
						this.insertHighlight.style.left = labelRegion.left + "px";
						return {root:node.parent, afterNode:node};
					}

		//			Insert INTO it: Hide insertion point bar
					this.insertHighlight.style.display = "none";
				}

		//		Unless it's a top level, in which case INTO it.
				return {root:node, beforeNode:null};
			}
		}

		else if (childRegion.contains(p))
		{
		//	var result;
			for (var i = 0; i < node.children.length; i++)
			{
				var result = this._getInsertionPoint(node.children[i], p, ddObject);
				if (result)
					return result;
			}
		}
	}

//	We're hovering over a leaf node.
	else
	{
		var e = YAHOO.util.Region.getRegion(node.getEl());
		if (e.contains(p))
		{
			this.insertHighlight.style.left = labelRegion.left + "px";
			this.insertHighlight.style.display = "";
			if (p.isAbove(e))
			{
				prevNode = node.previousSibling;
				if (!prevNode)
					prevNode = node.parent;
				this.insertHighlight.style.top = (YAHOO.util.Region.getRegion(prevNode.getLabelEl()).bottom + 5) + "px";
				return {root:node.parent, beforeNode:node};
			}
			else
			{
				this.insertHighlight.style.top = (labelRegion.bottom + 5) + "px";
				return {root:node.parent, afterNode:node};
			}
		}
	}
	this.insertHighlight.style.display = "none";
};

DDTreeView.prototype.appendNode = function(label, root, ddObject, expanded)
{
	var node = new YAHOO.widget.TextNode({label:label, href:"#"}, root, expanded);
	node.data = ddObject.data ? ddObject.data : ddObject;
	if (ddObject.originalNode)
		node.canHaveChildren = ddObject.originalNode.canHaveChildren;
	this.activateTreeNode(node);
	return node;
};

DDTreeView.prototype.insertTextNodeBefore = function(label, root, refNode, ddObject, expanded)
{
	var node = new YAHOO.widget.TextNode({label:label, href:"#"}, root, expanded);
	if (refNode && (refNode != null))
	{
		this.removeNode(node);
		root.insertBefore(node, refNode);
	}
	node.data = ddObject.data;
	if (ddObject.originalNode)
		node.canHaveChildren = ddObject.originalNode.canHaveChildren;
	this.activateTreeNode(node);
	return node;
};

DDTreeView.prototype.insertTextNodeAfter = function(label, root, refNode, ddObject, expanded)
{
	var node = new YAHOO.widget.TextNode({label:label, href:"#"}, root, expanded);
	this.removeNode(node);
	root.insertAfter(node, refNode);
	node.data = ddObject.data;
	if (ddObject.originalNode)
		node.canHaveChildren = ddObject.originalNode.canHaveChildren;
	this.activateTreeNode(node);
	return node;
};

DDTreeView.prototype.addNode = function(label, formFieldId, parent, 
	fieldId, name, desc, type, conceptId, conceptName, table, attr, defaultValue, multiple, numForms,
	fieldNumber, fieldPart, pageNumber, minOccurs, maxOccurs, required)
{
	if (parent == '')
		parent = 0;
	
	if (typeof parent != "object")
		parent = tree.getNodeByProperty('uuid', parent);
	
	var data = {label: label, 
				href: '#',
				uuid: formFieldId,
				fieldId: fieldId,
				fieldName: name,
				description: desc,
				fieldType: type,
				conceptId: conceptId,
				conceptName: conceptName,
				tableName: table,
				attributeName: attr,
				defaultValue: defaultValue,
				selectMultiple: multiple,
				numForms: numForms,
				
				fieldNumber: fieldNumber,
				fieldPart: fieldPart,
				pageNumber: pageNumber,
				minOccurs: minOccurs,
				maxOccurs: maxOccurs,
				isRequired: required };
		
	var node = new YAHOO.widget.TextNode(data, parent, false);
	node.canHaveChildren = true;
	this.activateTreeNode(node);
	return node;
}

DDTreeView.prototype.activateTreeNode = function(newNode)
{
	newNode.renderHidden = true;

//	Retrieve the first group.
	var sGroup;
	for (var g in this.ddTarget.groups)
	{
		sGroup = g;
		break;
	}
		
//	Make this newly dropped node a Draggable
	//newNode.parent.refresh();
	dd = new Draggable(newNode.labelElId, sGroup, newNode);
	//newNode.parent.refresh();
	dd.removeInvalidHandleType("A");

//	Override the Draggable's onDragDrop to remove the node when it gets
//	dropped into its new location. Refresh the node that it was removed from
//	unless the call to superOnDragDrop (the enclosing function) just added
//	to the same one and refreshed it.
	dd.originalNode = newNode;
	dd.dragFromTree = this;
	dd.onDragDrop = function(e, id)
	{
		var dropNode = Draggable.prototype.onDragDrop.apply(this, arguments);

		//If DDTreeView.opDragDop vetoed the drop, don't remove the original
		if (!this.dropVetoed)
		{
			var originalsChildren = this.originalNode.children;

		//	Delete the original node
			this.originalNode.tree.removeNode(this.originalNode, true);

		//	Transfer over all the original node's child nodes.
			if (originalsChildren)
			{
				for (var i = 0; i < originalsChildren.length; i++)
				{
					dropNode.appendChild(originalsChildren[i]);
				}
				dropNode.childrenRendered = false;
				dropNode.refresh();
			}
		}
		return dropNode;
	};
	dd.data = newNode.data;
};

//Call this when a drag item enters the element
DDTreeView.prototype.onDragEnter = function(e, ddObject)
{
};

//Call this when dragging over it
DDTreeView.prototype.onDragOver = function(e, ddObject)
{
	var insertionPoint = this.getInsertionPoint(e, ddObject);
	if (insertionPoint)
	{
		var el = insertionPoint.root.getLabelEl();
		if (this.selectedTarget && (this.selectedTarget != el))
		{
			this.selectedTarget.style.backgroundColor = "";
		}
		this.selectedTarget = el;
		this.selectedTarget.style.backgroundColor = "#ccddcc";
	}
}

/**
* Receive a ddObject of some kind.
*/
DDTreeView.prototype.onDragDrop = function(e, ddObject)
{
	var newNode;
	var insertionPoint = this.getInsertionPoint(e, ddObject);
	if (insertionPoint)
	{
		ddObject.dropVetoed = false;
		if (insertionPoint.afterNode)
		{
			newNode = this.insertTextNodeAfter(ddObject.getDragEl().innerHTML,
				insertionPoint.root, insertionPoint.afterNode, ddObject);
		}
		else
		{
			newNode = this.insertTextNodeBefore(ddObject.getDragEl().innerHTML,
				insertionPoint.root, insertionPoint.beforeNode, ddObject);
		}

		//We've moved, not copied, so remove the original node, and clear it's DragDrop
		//The original might have been removed by the code above (an in-tree move),
		//in which case el will have no properties.
		if (!ddObject.copy)
		{
			var el = ddObject.getDragEl();
			if (el.parent)
			{
				el.parent.removeChild(el);
			}
			ddObject.unreg();
		}
	}
	else
	{
		ddObject.dropVetoed = true;
	}
	
	
	newNode.canHaveChildren = true;
	newNode.parent.expand();
	
	// if this was dragged from the field search box
	if (ddObject.editOnMove) {
		this.contextMenu.contextEventTarget = newNode;
		this.editField(e, ddObject, tree, newNode);
	}
	
	return newNode;
};

DDTreeView.prototype.onDragOut = function()
{
	document.body.style.cursor = "no-drop";
	if (this.selectedTarget)
		this.selectedTarget.style.backgroundColor = "";
	this.insertHighlight.style.display = "none";
};

/**
Extends DDProxy to allow copying of data.
*/
var Draggable = function(id, sGroup, textNode)
{
	this.copy = false;
	this.init(id, sGroup, textNode);
	this.initFrame(); 
	this.selectedTarget = null;
	this.isTarget = false;
};
Draggable.prototype = new YAHOO.util.DDProxy();
Draggable.prototype.superB4StartDrag = Draggable.prototype.b4StartDrag;

/**
Override init. Use the extra addListener function of TextNode, if the element we
are making draggable is a text node - IE has a TextNode argument passed.
*/
Draggable.prototype.init = function(id, sGroup, textNode)
{
    this.initTarget(id, sGroup);
    if (textNode && textNode.addListener)
    {
    	textNode.addListener("mousedown", this.handleMouseDown, this, true);
    }
    else
        YAHOO.util.Event.addListener(this.id, "mousedown", this.handleMouseDown, this, true);
},

Draggable.prototype.initFrame = function()
{
	if (!Draggable.frameDiv)
	{
		if (!document || !document.body)
		{
			_this = this;
			setTimeout(function()
			{
				Draggable.prototype.initFrame.call(_this)
			}, 50);
			return;
		}
		this.setDragElId("draggableProxy");
		Draggable.frameDiv = document.createElement("div");
		Draggable.frameDiv.id = "draggableProxy";
		var s = Draggable.frameDiv.style;
		s.position = "absolute";
		s.visibility = "hidden";
		s.border = "none";
		s.zIndex = 999;
		document.body.appendChild(Draggable.frameDiv);
	}
	this.useAbsMath = true;
};

Draggable.prototype.b4StartDrag = function(x, y)
{
	this.superB4StartDrag(x,y);
	if (this.data && this.data["uuid"] == 0)
		this.getDragEl().innerHTML = this.fieldNameInput.value;
	else
		this.getDragEl().innerHTML = getFieldLabel(this.data);
}

//The element may change because of tree re-rendering, so *always*
//look it up.
Draggable.prototype.getEl = function()
{
    this._domRef = this.DDM.getElement(this.id); 
    return this._domRef;
};

Draggable.prototype.getDragEl = function()
{
	return Draggable.frameDiv;
}

Draggable.prototype.showFrame = function(iPageX, iPageY)
{
	var el = this.getEl();
	var dragEl = Draggable.frameDiv;
	while (dragEl.firstChild)
		dragEl.removeChild(dragEl.firstChild);
	YAHOO.util.Dom.setStyle(dragEl, "opacity", 0.5);
	this.dragTag = el.tagName.toLowerCase();

//	If the child nodes of the element which is being dragged must be contained
//	in a special way, then set up an elCarrier within the dragEl div to hold it.
	this.elCarrier = null;
	if (this.dragTag == "tr")
	{
		var t = document.createElement("table");
		elCarrier = document.createElement("tr");
		t.appendChild(elCarrier);
		dragEl.appendChild(t);
	}
	else if (this.dragTag == "td")
	{
		var t = document.createElement("table");
		var r = document.createElement("tr");
		elCarrier = document.createElement("td");
		t.appendChild(r);
		r.appendChild(elCarrier)
		dragEl.appendChild(t);
	}
	else if (this.dragTag == "li")
	{
		var u = document.createElement("ul");
		elCarrier = document.createElement("li");
		u.appendChild(elCarrier);
		dragEl.appendChild(elCarrier);
	}
	else
	{
		elCarrier = dragEl;
	}

	elCarrier.className = el.className;
	elCarrier.appendChild(getElementContents(el, true));
	var s = elCarrier.style;
	s.width = parseInt(el.offsetWidth, 10) + "px";
	s.height = parseInt(el.offsetHeight, 10) + "px";
	this.setDragElPos(iPageX, iPageY);
	s.visibility = "";
	document.body.style.cursor = "no-drop";
};

//Inform any recipient that we are no longer drag-dropping.
Draggable.prototype.b4EndDrag = function()
{
	this.getDragEl().style.visibility = "hidden";
	if (this.target)
	{
		this.target.onDragOut();
		delete this.target;
	}
	document.body.style.cursor = "";
	
	
};

//When we enter a potential drop zone
Draggable.prototype.onDragEnter = function(e, id)
{
	document.body.style.cursor = "copy";
};

//This informs the recipient that it is being dragged over
Draggable.prototype.onDragOver = function(e, id)
{
	this.target = ((typeof id == "string") ? YAHOO.util.DragDropMgr.getDDById(id) : id[0]);
	this.target.onDragOver(e, this);
}

/**
This informs the recipient that it has been dropped upon.
@return The object created as a result of the drop
*/
Draggable.prototype.onDragDrop = function(e, id)
{
	this.target = ((typeof id == "string") ? YAHOO.util.DragDropMgr.getDDById(id) : id[0]);
	return this.target.onDragDrop(e, this);
};

//This informs the recipient that it is no longer being being dragged over
Draggable.prototype.onDragOut = function()
{
	document.body.style.cursor = "no-drop";
	this.target.onDragOut();
};

//Override: don't remove the original node.
Draggable.prototype.endDrag = function()
{
};
