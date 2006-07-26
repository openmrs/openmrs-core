	dojo.require("dojo.widget.Tree");
	dojo.require("dojo.widget.TreeBasicController");
	dojo.require("dojo.widget.TreeSelector");
	dojo.require("dojo.widget.TreeNode");
	dojo.require("dojo.widget.TreeContextMenu");

	dojo.hostenv.writeIncludes();
	
	var mySearch = null;
	var mySearchStatus = null;
	var searchType = null;
	var fieldResults = null;
	var tree;
	var controller;
	var treeSelector;
	var searchTreeSelector;
	var searchTree;
	var selectedNode = null;
	var nodesToAdd = [];
	
	dojo.addOnLoad( function(){
		mySearch = new fx.Resize("searchForm", {duration: 100});
		mySearch.hide();
		mySearchStatus = null;
		fieldResults = $('fieldResults');
		
		DWRUtil.useLoadingMessage();
		
		dojo.event.topic.subscribe('treeContextMenuEdit/engage',
			function (menuItem) { editClicked( menuItem.getTreeNode()); }
		);
		
		dojo.event.topic.subscribe('treeContextMenuCreate/engage',
			function (menuItem) { createClicked( menuItem.getTreeNode()); }
		);

		dojo.event.topic.subscribe('treeContextMenuRemove/engage',
			function (menuItem) { removeClicked( menuItem.getTreeNode()); }
		);

		// initialize the tree
		controller = dojo.widget.manager.getWidgetById('treeController');
		tree = dojo.widget.manager.getWidgetById('tree');
		treeSelector = dojo.widget.manager.getWidgetById('treeSelector');
		searchTreeSelector = dojo.widget.manager.getWidgetById('searchTreeSelector');
		fieldResults = document.getElementById("fieldResults");
		
		DWRFormService.getJSTree(evalTreeJS, formId);
		
		dojo.event.topic.subscribe(tree.eventNames.moveTo, new nodeMoved(), "execute");
		dojo.event.topic.subscribe(tree.eventNames.removeNode, new nodeRemoved(), "execute");
		dojo.event.topic.subscribe(searchTreeSelector.eventNames.select, new nodeSelected(), "execute");
		dojo.event.topic.subscribe(treeSelector.eventNames.dblselect, function ( msg ){ editClicked(msg.node); } );
		
		// Get div and input for editable formfields
		tree.editDiv = document.getElementById("editFormField");
		
		tree.fieldIdInput = document.getElementById("fieldId");
		tree.fieldNameInput = document.getElementById("fieldName");
		tree.descriptionInput = document.getElementById("description");
		tree.fieldTypeInput = document.getElementById("fieldType");
		tree.conceptIdInput = document.getElementById("conceptId");
		tree.conceptNameTag = document.getElementById("conceptName");
		tree.tableNameInput = document.getElementById("tableName");
		tree.attributeNameInput = document.getElementById("attributeName");
		tree.defaultValueInput = document.getElementById("defaultValue");
		tree.selectMultipleCheckbox = document.getElementById("selectMultiple");
		tree.numFormsTag = document.getElementById("numForms");
		
		tree.formFieldIdInput = document.getElementById("formFieldId");
		tree.fieldNumberInput = document.getElementById("fieldNumber");
		tree.fieldPartInput = document.getElementById("fieldPart");
		tree.pageNumberInput = document.getElementById("pageNumber");
		tree.minOccursInput = document.getElementById("minOccurs");
		tree.maxOccursInput = document.getElementById("maxOccurs");
		tree.isRequiredCheckbox = document.getElementById("required");
		tree.saveFieldButton = document.getElementById("saveFormField");
		tree.cancelFieldButton = document.getElementById("cancelFormField");
		
		tree.containerNode.style.display = "";
		
		//remove ability to mark tree nodes.
		//dojo.widget.TreeNode.markSelected = function() { };
		
		dojo.event.topic.subscribe(tree.eventNames.createDOMNode, new domNodeCreated(), "execute");
		
	});
	
	var domNodeCreated = function(val) {
		dojo.debug("domNodeCreated: " + val);
		this.value = val;
		this.execute = function(msg) {
			var child = msg.source;
			if (child && child.labelNode && child.data) {
				child.labelNode.onmouseover = function() {
						var widg = dojo.widget.byNode(this);
						if (widg && widg.data) {
							var data = widg.data;
							var s = "";
							if (data.formFieldId)
								s += " FormField Id: " + data.formFieldId + " ";
							if (data.fieldId)
								s += " Field Id: " + data.fieldId + " ";
							if (data.conceptId)
								s += " Concept Id: " + data.conceptId + " ";
								
							window.status = s;
						}
				}
			}
			var t = 8;
		};
	}
	
	var nodeMoved = function() {
		this.value = null;
		this.execute = function(msg) {
			if (msg.oldTree != msg.newTree) {
				// add the item back in our search list
				var isFieldNode = false;
				if (msg.child.data.fieldId)
					isFieldNode = true;
				// add node back into search tree
				var node = addNode(msg.oldTree, msg.child.data, msg.child.title, 0, isFieldNode);
				
				if (isFieldNode)
					node.afterLabelNode.appendChild(document.createTextNode(" -"));
				else
					dojo.html.addClass(node.titleNode, "fieldConceptHit");
				
				msg.oldTree.containerNode.style.display = "";
				msg.newTree.containerNode.style.display = "";
				
				if (msg.skipEdit) {
					updateSortWeight(msg.child);
					save(msg.child, /* formNotUsed */ true);
				}
				else { // if we moved from another tree, open up the edit div
					selectedNode = msg.child;
					setTimeout("editClicked(selectedNode)", 0);
				}
				dojo.html.removeClass(msg.child.titleNode, "fieldConceptHit");
				dojo.dom.removeChildren(msg.child.afterLabelNode);
				msg.child.afterLabelNode.appendChild(getRemoveLink(msg.child.data));
				msg.child.unMarkSelected();
			}
			else if (msg.oldParent != msg.newParent) {
				updateSortWeight(msg.child);
				// save node's new parent 
				save(msg.child, true);
				if (msg.oldParent && msg.oldParent.updateExpandIcon)
					msg.oldParent.updateExpandIcon();
			}
			else {
				// changing the order of things
				updateSortWeight(msg.child);
				save(msg.child, true);
			}
			
		};
	}
	
	var nodeRemoved = function(val) {
		this.value = val;
		this.execute = function(msg) {
			DWREngine.setAsync(true);
			removeNode(msg.child);
			DWREngine.setAsync(false);
		};
	}
	
	function removeNode(node) {
		if (node.data && node.data["formFieldId"]) {
			for (child in node.children) {
				removeNode(node.children[child]);
			}
			DWRFormService.deleteFormField(node.data["formFieldId"]);
			
			if (node.parent && node.parent.updateExpandIcon)
				node.parent.updateExpandIcon();
		}
	}
	
	var nodeSelected = function(val) {
		this.value = val;
		this.execute = function(msg) {
			// mimic drag and drop "move" action
			if (msg.node) {
				var node = tree;
				if (treeSelector.selectedNode)
					node = treeSelector.selectedNode;
				tree.controller.move(msg.node, node, node.children.length);
			}
		};
	}
	
	// process create operation 
	function createClicked(selNode) {
		if (!selNode) {
			if (treeSelector.selectedNode)
				selNode = treeSelector.selectedNode;
			else
				selNode = tree;
		}
			
		if (selNode.actionIsDisabled(selNode.actions.ADDCHILD))
			return false;

		//this.controller = dojo.widget.manager.getWidgetById(controllerId);
		var newChild = controller.createChild(selNode, 0, { suggestedTitle: "New node" });
		//selectedNode = newChild;
		editClicked(newChild);
		tree.fieldNameInput.focus();
	}

	function removeClicked(selectedNode, skipConfirm) {
		if (selectedNode.actionIsDisabled(selectedNode.actions.REMOVE))
			return false;
		
		var answer = true;
		if (!skipConfirm)
			answer = confirm("Delete this node and all of its children?");
		
		if (answer)
			controller.removeNode(selectedNode);
	}
	
	function editClicked(node) {
		selectedNode = node;
		
		var domNode = node.domNode;
		
		tree.containerNode.style.display = "";
		tree.editDiv.style.display = "";
		
		var s = tree.editDiv.style;
		s.left = dojo.style.getAbsoluteX(node.domNode, true) + "px";
		s.top = dojo.style.getAbsoluteY(node.domNode, true) + "px";
		var w = dojo.style.getPixelValue(tree.domNode, "width");
		if (!w) w = dojo.style.getPixelValue(tree.domNode, "offsetWidth");
		if (!w) w = dojo.style.getBorderBoxWidth(tree.domNode);
		if (!w) w = 500;
		if (w != 0)
			s.width = w + "px";
		dojo.debug("s.width: " + s.width);
		enableField();
		
		// add edit values fields here
		if (node.data) {
			var data = node.data;
			// field info
			tree.fieldNameInput.value = data["fieldName"];
			tree.descriptionInput.value = data["description"];
			for (var i=0; i<tree.fieldTypeInput.options.length; i++) {
				if (tree.fieldTypeInput.options[i].value == data["fieldType"])
					tree.fieldTypeInput.options[i].selected = true;
			}
			chooseFieldType(tree.fieldTypeInput.value);
			tree.conceptIdInput.value = data["conceptId"];
			tree.conceptNameTag.innerHTML = data["conceptName"];
			
			if (data["fieldId"]) {
				tree.fieldIdInput.value = data["fieldId"];
				tree.tableNameInput.value = data["tableName"];
				tree.attributeNameInput.value = data["attributeName"];
				tree.defaultValueInput.value = data["defaultValue"];
				tree.selectMultipleCheckbox.checked = data["selectMultiple"];
				tree.numFormsTag.innerHTML = data["numForms"];
			}
			else {
				tree.fieldIdInput.value = tree.tableNameInput.value = "";
				tree.attributeNameInput.value = tree.defaultValueInput.value = "";
				tree.selectMultipleCheckbox.checked = tree.numFormsTag.innerHTML = "";
			}
			
			// formField info
			tree.formFieldIdInput.value = data["formFieldId"] || "";
			tree.fieldNumberInput.value = data["fieldNumber"] || "";
			tree.fieldPartInput.value = data["fieldPart"] || "";
			tree.pageNumberInput.value = data["pageNumber"] || "";
			tree.minOccursInput.value = data["minOccurs"] || "";
			tree.maxOccursInput.value = data["maxOccurs"] || "";
			tree.isRequiredCheckbox.checked = data["isRequired"];
			if (data["numForms"] && parseInt(data["numForms"]) > 1)
				disableField();
		}
		else
			node.data = [];
		
		s.display = "";
		
		// focus on save if save button is shown (not hidden due to unpublished form)
		if (tree.saveFieldButton)
			tree.saveFieldButton.focus();
		else
			tree.cancelFieldButton.focus();
		
		tree.fieldNumberInput.focus();
	}
	
	
	function evalTreeJS(js) {
		dojo.debug("evaluating js");
		if (js.indexOf("DWR") == -1) {
			eval(js);
		}
		dojo.debug("done evaluating js");
	}
	
	function chooseFieldType(fieldTypeId) {
		if (fieldTypeId == 1) { // == 'Concept'
			$('concept').style.display = "";
			$('database').style.display = "none";
		}
		else if (fieldTypeId == 2) { // -- db element
			$('database').style.display = "";
			$('concept').style.display = "none";
		}
		else {
			$('concept').style.display = "none";
			$('database').style.display = "none";
		}
	}
	
	function clearFormField() {
		$('fieldId').value = '';
		$('fieldName').value = '';
		$('numForms').innerHTML = '';
		$('description').value = '';
		var options = $('fieldType').options;
		for (var i = 0; i<options.length; i++) {
			options[i].selected = false;
		}
		$('concept').style.display = "";
		$('conceptName').innerHTML = "";
		$('conceptId').value = "";
		$('database').style.display = "none";
		$('tableName').value = "";
		$('attributeName').value = "";
		$('defaultValue').value = "";
		$('selectMultiple').checked = false;
		
		$('formFieldId').value = '';
		$('fieldNumber').value = '';
		$('fieldPart').value = '';
		$('pageNumber').value = '';
		$('minOccurs').value = '';
		$('maxOccurs').value = '';
		$('required').checked = false;	
	}

	function disableField() {
		$('fieldName').disabled = true;
		$('description').disabled = true;
		$('fieldType').disabled = true;
		$('conceptName').disabled = true;
		$('tableName').disabled = true;
		$('attributeName').disabled = true;
		$('defaultValue').disabled = true;
		$('selectMultiple').disabled = true;
		
		$('field').className = "disabled";
		$('fieldWarning').style.display = "";
		$('fieldWarningIframe').style.display = "";
	}
	
	function enableField() {
		$('fieldName').disabled = false;
		$('description').disabled = false;
		$('fieldType').disabled = false;
		$('conceptName').disabled = false;
		$('tableName').disabled = false;
		$('attributeName').disabled = false;
		$('defaultValue').disabled = false;
		$('selectMultiple').disabled = false;
		
		$('field').className = "";
		$('fieldWarning').style.display = "none";
		$('fieldWarningIframe').style.display = "none";
	}
	
	function editAllFields() {
		if (tree && tree.fieldIdInput) {
			var val = tree.fieldIdInput.value;
			if (val && val.length)
				window.open("field.form?fieldId=" + val);
			else
				alert("Field widget does not exist yet.  It will be created when you save this formField");
		}
		
		return false;
	}
	
	function editFieldForThisForm() {
		try {
			tree.fieldIdInput.value = "";
			enableField();
			$("fieldName").focus();
			tree.numFormsTag.innerHTML = "1";
		}
		catch (e) {
			alert("An error occured: " + e);
		}
		return false;
	}
	
	function getFieldLabel(data) {
		var fieldLabel = "";
		if (data) {
			if (data["fieldNumber"])
	    		fieldLabel += data["fieldNumber"] + ". ";
	    	if (data["fieldPart"] && data["fieldPart"] != "null")
	    		fieldLabel += data["fieldPart"] + ". ";
	    	if ((data["minOccurs"] && data["minOccurs"] > 0) || (data["maxOccurs"] && data["maxOccurs"] != 1)){
	    		fieldLabel += " (";
	    		if (!data["minOccurs"])
	    			fieldLabel += "0";
	    		else
	    			fieldLabel += data["minOccurs"];
	    		fieldLabel += "..";
	    		if (data["maxOccurs"] == -1)
	    			fieldLabel += "n";
	    		else {
	    			if (!data["maxOccurs"])
	    				fieldLabel += "0";
	    			else
	    				fieldLabel += data["maxOccurs"];
	    		}
	    		fieldLabel += ") ";
	    	}
			if (data["isRequired"])
				fieldLabel += "<span class=required> * </span>";
			
			if (data["conceptId"])
				fieldLabel += data["fieldName"] + " (" + data["conceptId"] + ")";
			else
				fieldLabel += data["fieldName"];
		
		}
		return fieldLabel;
	}
	
	function save(target, formNotUsed) {
		if (target && target.data) {
			var data = target.data;
			var changed = true;
			/*
			changed = changed || (tree.fieldNameInput.value != data["fieldName"]);
			changed = changed || (tree.descriptionInput.value != data["description"]);
			changed = changed || (tree.fieldTypeInput.value != data["fieldType"]);
			changed = changed || (tree.conceptIdInput.value != data["conceptId"]);
			changed = changed || (tree.tableNameInput.value != data["tableName"]);
			changed = changed || (tree.attributeNameInput.value != data["attributeName"]);
			changed = changed || (tree.defaultValueInput.value != data["defaultValue"]);
			changed = changed || (tree.selectMultipleCheckbox.checked != data["selectMultiple"]);
			
			changed = changed || (tree.fieldNumberInput.value != data["fieldNumber"]);
			changed = changed || (tree.fieldPartInput.value != data["fieldPart"]);
			changed = changed || (tree.pageNumberInput.value != data["pageNumber"]);
			changed = changed || (tree.minOccursInput.value != data["minOccurs"]);
			changed = changed || (tree.maxOccursInput.value != data["maxOccurs"]);
			changed = changed || (tree.isRequiredCheckbox.checked != data["isRequired"]);
			*/
			
			if (changed) {
			
				if (!formNotUsed) {
					if ($('concept').style.display != "none") {
						if (!tree.conceptIdInput.value) {
							// this check and fail must be done first so we can avoid 
							// having to reset data.*
							alert("You must select a concept");
							return false;
						}
						data["conceptId"] = data["conceptName"] = '';
						data["tableName"] = data["attributeName"] = '';
						data["conceptId"] = tree.conceptIdInput.value;
						data["conceptName"] = tree.conceptNameTag.innerHTML;					}
					else {
						data["tableName"] = tree.tableNameInput.value;
						data["attributeName"] = tree.attributeNameInput.value;
					}
					
					data["fieldId"] = null;
					if (tree.fieldIdInput.value != 'undefined' && tree.fieldIdInput.value != '')
						data["fieldId"] = tree.fieldIdInput.value;
					data["fieldName"] = tree.fieldNameInput.value;
					data["description"] = tree.descriptionInput.value;
					data["fieldType"] = tree.fieldTypeInput.value;
					data["defaultValue"] = tree.defaultValueInput.value;
					data["selectMultiple"] = tree.selectMultipleCheckbox.checked;
					data["numForms"] = tree.numFormsTag.innerHTML;
					
					data["formFieldId"] = (tree.formFieldIdInput.value) ? tree.formFieldIdInput.value : null;
					
					data["fieldNumber"] = tree.fieldNumberInput.value;
					if (data["fieldNumber"].length == 0)
						data["fieldNumber"] = null;
					data["fieldPart"] = tree.fieldPartInput.value;
					data["pageNumber"] = tree.pageNumberInput.value;
					if (data["pageNumber"].length == 0)
						data["pageNumber"] = null;
					data["minOccurs"] = tree.minOccursInput.value;
					if (data["minOccurs"].length == 0)
						data["minOccurs"] = null;
					data["maxOccurs"] = tree.maxOccursInput.value;
					if (data["maxOccurs"].length == 0)
						data["maxOccurs"] = null;
					data["isRequired"] = tree.isRequiredCheckbox.checked;
				} // end "if (!formNotUsed)"
				
				data["parent"] = null;
				if (target.parent && target.parent.data)
					data["parent"] = target.parent.data["formFieldId"];
				if (data["parent"] == 0)
					data["parent"] = null;				
				
				if (!data.sortWeight) {
					data["sortWeight"] = 0.0;
					updateSortWeight(target);
				}	
				
				selectedNode = target;
				DWRFormService.saveFormField(cancelClicked, data.fieldId, data.fieldName, data.description, data.fieldType,
					data.conceptId, data.tableName, data.attributeName, data.defaultValue, data.selectMultiple, 
					data.formFieldId, formId, data.parent, data.fieldNumber, data.fieldPart, data.pageNumber, data.minOccurs, data.maxOccurs, data.isRequired, data.sortWeight);
				
				target.titleNode.innerHTML = target.title = getFieldLabel(data);
				if (!formPublished && selectedNode) {
					dojo.dom.removeChildren(selectedNode.afterLabelNode);
					selectedNode.afterLabelNode.appendChild(getRemoveLink(data));
				}
				
				//target.unMarkSelected();
			}
			else
				tree.editDiv.style.display = "none";
		}
		return false;
	}
	
	
	function updateSortWeight(target) {
		if (target && target.data && tree) {
			var prev = target.getPreviousSibling();
			var next = target.getNextSibling();
			var sortWeight = 500.0;
			if (prev && prev.data && next && next.data) {
				// We're in the middle somewhere.
				var prevWeight = prev.data.sortWeight;
				var nextWeight = next.data.sortWeight;
				if (!prevWeight || !nextWeight)
					return sortWeightError();
				dojo.debug("1 nextweight: " + nextWeight + " prevweight: " + prevWeight);
				sortWeight = (nextWeight + prevWeight) / 2.0;
			}
			else if (prev && prev.data) {
				// We're at the end.  Make the next weight much larger than the current last one
				var prevWeight = prev.data.sortWeight || 0.0;
				sortWeight = prevWeight + 50.0;
			}
			else if (next && next.data) {
				// We're at the beginning.  Make the first weight low
				nextWeight = next.data.sortWeight || 1000.0;
				nextWeight = nextWeight - 50.0;
			}
			
			if (!sortWeight)
				sortWeight = 0.0
			
			// TODO if we error out, renumber all.
			
			target.data["sortWeight"] = sortWeight;
		}
	}
	
	function sortWeightError() {
		alert("Error assigning sort weight. \n\nUpdate Element Sort Order according to field Number and Part using the link on this page\n\The current visual order of elements may not be correct.");
		return null;
	}
	
	function addNode(addToTree, data, label, attemptCount, insertNode) {
		if (data) {
			var parent;
			if (data.parent && typeof data.parent != "object")
				parent = dojo.widget.byId(data.parent);
			else
				parent = addToTree;
			
			if (!parent) {
				// the nodes are being added in the wrong order
				// add this node to a list to finish later (after the parent is added, hopefully)
				if (attemptCount && attemptCount > 100)
					alert("Parent node of formField " + data.formFieldId + " has not been loaded yet (parent: " + data.parent + ")");
				else {
					nodesToAdd.push({data: data, label: label, attemptCount: attemptCount ? attemptCount : 0});
					setTimeout("addLeftoverNodes()", 10);
				}
				return;
			}
			
			var div = document.createElement("div");
			div.id=data.formFieldId;
			addToTree.domNode.appendChild(div);
	
			var ext = false;
			if (!label) {
				label = getFieldLabel(data);
				if (!formPublished)
					ext = getRemoveLink(data);
			}
			
			var props = [];
			props.title = label;
			props.id = data.formFieldId ? data.formFieldId : null;
			
			var node = dojo.widget.createWidget("TreeNode", props, div);
			node.data = data;
			
			if (ext) {
				dojo.dom.removeChildren(node.afterLabelNode);
				node.afterLabelNode.appendChild(ext);
			}
			
			if (formPublished)
				node.actionsDisabled = [node.actions.ADDCHILD, node.actions.REMOVE];
			
			if (insertNode)
				parent.addChild(node, 0);
			else
				parent.addChild(node);
			
			return node;
		}
	}
	
	
	function getRemoveLink(data) {
		var ext = document.createElement("a");
		ext.onclick=function(){
				var node = dojo.widget.manager.getWidgetByNode(this.parentNode.parentNode);
				removeClicked(node);
			};
		ext.className="delete";
		ext.widgetId=data.formFieldId;
		ext.innerHTML = " &nbsp; &nbsp; ";
		return ext;
	}
	
	
	function addLeftoverNodes() {
		while(nodesToAdd.length) {
			var msg = nodesToAdd[0];
			addNode(tree, msg.data, msg.label, msg.attemptCount+1);
			nodesToAdd = nodesToAdd.slice(1, nodesToAdd.length);
		}
	}
	
	var cancelClicked = function(savedNodeIds) {
		
		if (savedNodeIds && selectedNode.data) {
			if (savedNodeIds[0] == 0) {
				// remote call (dwr) returned an error
				alert("There was an error while processing your request. Consult the error logs");
			}
			else {
				// remote call went smoothly
				selectedNode.data["fieldId"] = savedNodeIds[0];
				selectedNode.data["formFieldId"] = savedNodeIds[1];
			}
		}
			
		// remove from main tree because they just dragged it over from the search
		else if (!selectedNode.data || !selectedNode.data["formFieldId"])
			removeClicked(selectedNode, true);
		
		clearFormField();
		closeBox();
		searchType = "";
		tree.editDiv.style.display = "none";
		
		selectedNode = null;
	}
	
	function showSearchForm(btn) {
		searchType = "concept";
		resetForm();
		DWRUtil.removeAllRows("searchBody");
		$("searchText").value = '';
		if (mySearchStatus == null) {
			mySearch.toggle();
			mySearchStatus = "1";
		}
		setPosition(btn, $('searchForm'), 400, 410);
		$('searchText').select();
	}
	
	var findConceptObjects = findObjects;
	
	var findObjects = function(txt) {
		if (searchType == "concept")
			return findConceptObjects(txt);
		else {
			fieldResults.style.display = "";
			DWRFormService.findFieldsAndConcepts(preFillFieldTable, txt);
		}
	}
	
	var preFillFieldTable = function(objs) {
		objs.push('<a href="#newField" onclick="createClicked(); return false;">Add New Field</a>');
		fillTable(objs);
	}
	
	function onSelect(objs) {
		if (searchType == "concept") {
			var obj = objs;
			if (objs.length)
				obj = objs[0];
		
			// selected a concept from the search popup
			$('conceptId').value = obj.conceptId;
			$('conceptName').innerHTML = obj.name;
			closeBox();
			DWRUtil.removeAllRows(fieldResults);
			$('searchField').value = "";
			fieldResults.style.display = "none";
			return false;
		}
		
		for (o in objs) {
			var obj = objs[o];
			var child = dojo.widget.byId(obj.widgetId);
			
			var oldParent = child.parent;
			var oldTree = child.tree;
			
			var newParent = tree;
			
			if (treeSelector.selectedNode)
				newParent = treeSelector.selectedNode;
			
			// create a new node for this item so that it mimicks the "move" functionality
			var node = addNode(newParent, child.data);
			
			// newParent.doAddChild(child, newParent.children.length);
			
			if (newParent.expand)
				newParent.expand();
			
			var newParent = node.parent;
			var newTree = node.tree;
	
			var message = {
					oldParent: oldParent, oldTree: oldTree,
					newParent: newParent, newTree: newTree,
					child: node,
					skipEdit: true
			};
			
			dojo.html.removeClass(node.titleNode, "fieldConceptHit");
			dojo.dom.removeChildren(node.afterLabelNode);
			node.afterLabelNode.appendChild(getRemoveLink(node.data));
			
			dojo.event.topic.publish(tree.eventNames.moveFrom, message);
			dojo.event.topic.publish(tree.eventNames.moveTo, message);
		}
		
		resetForm();
		onRemoveAllRows();
		DWRUtil.removeAllRows(fieldResults);
		$('searchField').value = "";
		fieldResults.style.display = "none";
		document.getElementById("searchField").focus();
		return false;
	}
	
	var getConceptCellContent = getCellContent;
	var searchTreeNodes = [];	

	var getCellContent = function(obj) {
		if (searchType == "concept")
			return getConceptCellContent(obj);
			
		if (typeof obj == 'string') return obj;
		
		var domNode = document.createElement("span");
		
		var data = getData(obj);
		domNode.title = data.title;
		
		// create a mini tree
		var properties = {id: "miniTree", 
						DNDMode: "between", 
						showRootGrid: false,
						DNDAcceptTypes: ["tree", "miniTree"],
						selector: "searchTreeSelector"};
						
		var parentNode = domNode;
		
		var miniTree = dojo.widget.createWidget("Tree", properties, parentNode, "last");
		searchTreeNodes.push(miniTree);
		
		var node = addNode(miniTree, data, data.label);
		
		obj.widgetId = node.widgetId;
		
		miniTree.actionIsDisabled = function(action) {
				if (!action || action.toUpperCase() == "MOVE")
					return false;
				return true;
			};
			
		if (obj.fieldId && obj.concept) {
			var d2 = getData(obj.concept);
			var n2 = addNode(miniTree, d2, d2["label"]);
			dojo.html.addClass(n2.titleNode, "fieldConceptHit");
			node.afterLabelNode.appendChild(document.createTextNode(" -"));
			domNode.className = "treeNodeRow";
		}
		
		return domNode;
	}

	// getting data from field object	
	function getData(obj) {
		var data = [];
		data["selectMultiple"] = false;
		data["fieldId"] = null;
		data["defaultValue"] = "";
		
		// object is a conceptListItem
		if (obj.conceptId != null) {
			data.id = data["conceptId"] = obj.conceptId;
			data["fieldName"] = data["conceptName"] = obj.name;
			data["description"] = obj.description;
			data["fieldType"] = 1;
			data["label"] = "CONCEPT." + obj.name;
			data.title = "Concept Id: " + obj.conceptId;
			//obj.widgetId = data["widgetId"] = "concept" + obj.conceptId;
		}
		// or object is a fieldListItem
		else if (obj.fieldId != null) {
			data.id = data["fieldId"] = obj.fieldId;
			data["numForms"] = obj.numForms;
			data["fieldId"] = obj.fieldId;
			data["fieldName"] = obj.name;
			data["description"] = obj.description;
			data["fieldType"] = obj.fieldTypeId;
			data["conceptId"] = obj.concept ? obj.concept.conceptId : null;
			data["conceptName"] = obj.concept ? obj.concept.name : null;
			data["tableName"] = obj.table;
			data["attributeName"] = obj.attribute;
			data["defaultValue"] = obj.defaultValue;
			data["selectMultiple"] = obj.selectMultiple;
			
			data["label"] = obj.name + " (" + obj.numForms + " forms)";
			data.title = "";
			//obj.widgetId = data["widgetId"] = "field" + obj.fieldId;
		}
		
		data["formFieldId"] = null;
		data["fieldNumber"] = null;
		data["fieldPart"] = '';
		data["pageNumber"] = null;
		data["minOccurs"] = null;
		data["maxOccurs"] = null;
		data["isRequired"] = false;
		
		return data;
	}
	
	var customCellFunctions = [getNumber, getCellContent];
	
	function allowAutoListWithNumber() {
		return true;
	}
	
	function closeBox() {
		mySearch.hide();
		mySearchStatus = null;
		return false;
	}
	
	// remove the nodes that were added in the search
	function onRemoveAllRows(tbody) {
		while(searchTreeNodes.length) {
			searchTreeNodes[0].destroy();
			searchTreeNodes.splice(0,1);
		}
	}
	
	function allowAutoJump() {
		return false;
	}
