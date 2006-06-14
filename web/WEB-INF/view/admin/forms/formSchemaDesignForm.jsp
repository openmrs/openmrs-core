<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formSchemaDesign.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">
	var djConfig = {debugAtAllCosts: true };
</script>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/dojo/dojo.js"></script>

<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRConceptService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/interface/DWRFormService.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/engine.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/dwr/util.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/conceptSearch.js"></script>

<style>
	.indent {
		padding-left: 2em;
	}
	.required {
		color: red;
	}
	a.delete {
		background: url(${pageContext.request.contextPath}/images/delete.gif) no-repeat center center;
		text-decoration: none;
		padding-left: 2px;
		cursor: pointer;
	}
	.disabled, .disabled * {
		color: gray;
		background-color: whitesmoke;
	}
		.disabled #formFieldTitle {
			background-color: whitesmoke;
		}
	
	#editFormField {
		position: absolute;
		left: -1000px;
		background-color: white;
		z-index: 9;
		width: 100px;
	}
		#editFormField.disabled {
			border-color: grey;
		}
	
	#formFieldTitle {
		background-color: lightgreen;
	}
	
	#editFormField, .editForm {
		border: 2px solid lightgreen;
		padding: 1px;
	}

	.searchForm {
		width: 400px;
		position: absolute;
		z-index: 10;
		margin: 5px;
		left: -1000px;
	}
	.searchForm .wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 370px;
	}
	.searchResults {
		height: 400px;
		overflow: auto;
		width: 390px;
	}
	#closeButton {
		border: 1px solid gray;
		background-color: lightpink;
		font-size: .6em;
		color: black;
		margin: 2px;
		padding: 1px;
		cursor: pointer;
	}
	#fieldWarning {
		position: absolute;
		margin-left: 5%;
		margin-top: 5%;
		width: 90%;
		color: firebrick;
		border: 2px solid firebrick;
		text-align: center;
		z-index: 999;
		background-color: white;
		padding: 3px;
	}
	span.fieldConceptHit {
		color: gray;
	}
	span.treeNodeRow div.dojoTree div.dojoTreeNode {
		display: inline;
	}
	
</style>

<script type="text/javascript">
	dojo.require("dojo.lang.*");
	dojo.require("dojo.fx.html");	// so can use toggle="fade"
	dojo.require("dojo.widget.Tree");
	dojo.require("dojo.widget.TreeBasicController");
	dojo.require("dojo.widget.TreeSelector");
	dojo.require("dojo.widget.TreeNode");
	dojo.require("dojo.widget.TreeContextMenu");

	dojo.hostenv.writeIncludes();
	
	var editForm = null;
	var formFieldTitle = null;
	var mySearch = null;
	var mySearchStatus = null;
	var searchType = null;
	var fieldResults = null;
	var tree;
	var controller;
	var selector;
	var searchTree;
	var selectedNode = null;
	var nodesToAdd = [];
	
	dojo.addOnLoad( function(){
		mySearch = new fx.Resize("searchForm", {duration: 100});
		mySearch.hide();
		mySearchStatus = null;
		editForm = $('editFormField');
		formFieldTitle = $('formFieldTitle');
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
		fieldResults = document.getElementById("fieldResults");
		
		DWRFormService.getJSTree(evalTreeJS, <request:parameter name="formId"/>);
		
		dojo.event.topic.subscribe(tree.eventNames.moveTo, new nodeMoved(), "execute")
		dojo.event.topic.subscribe(tree.eventNames.removeNode, new nodeRemoved(), "execute")
		dojo.event.topic.subscribe(treeSelector.eventNames.select, new nodeSelected(), "execute")
		
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
		
	});
	
	var nodeMoved = function() {
		this.value = null;
		this.execute = function(msg) {
			if (msg.oldTree != msg.newTree) {
				// add the item back in our search list
				var isFieldNode = false;
				if (msg.child.data.fieldId)
					isFieldNode = true;
				var node = addNode(msg.oldTree, msg.child.data, msg.child.title, 0, isFieldNode);
				
				if (isFieldNode)
					node.afterLabelNode.appendChild(document.createTextNode(" -"));
				else
					dojo.html.addClass(node.titleNode, "fieldConceptHit");
				
				msg.oldTree.containerNode.style.display = "";
				msg.newTree.containerNode.style.display = "";
				// if we moved from another tree, open up the edit div
				if (msg.skipEdit) {
					var formNotUsed = true;
					save(msg.child, formNotUsed);
				}
				else {
					selectedNode = msg.child;
					setTimeout("editClicked(selectedNode)", 0);
				}
				dojo.html.removeClass(msg.child.titleNode, "fieldConceptHit");
				dojo.dom.removeChildren(msg.child.afterLabelNode);
				msg.child.afterLabelNode.appendChild(getRemoveLink(msg.child.data));
			}
			else if (msg.oldParent != msg.newParent) {
				// save node's new parent 
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
		}
	}
	
	var nodeSelected = function(val) {
		this.value = val;
		this.execute = function(msg) {
			// mimic drag and drop "move" action
			if (msg.node) {
				tree.controller.move(msg.node, tree, tree.children.length);
				//msg.node.updateIconTree();
			}
		};
	}
	
	// process create operation 
	function createClicked(selectedNode) {
		if (selectedNode.actionIsDisabled(selectedNode.actions.ADDCHILD))
			return false;

		//this.controller = dojo.widget.manager.getWidgetById(controllerId);
		var newChild = controller.createChild(selectedNode, 0, { suggestedTitle: "New node" });
		//selectedNode = newChild;
		editClicked(newChild);
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
		s.width = dojo.style.getNumericStyle(tree.domNode, "width") + "px";
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
			tree.formFieldIdInput.value = data["formFieldId"];
			tree.fieldNumberInput.value = data["fieldNumber"];
			tree.fieldPartInput.value = "";
			if (data["fieldPart"])
				tree.fieldPartInput.value = data["fieldPart"];
			tree.pageNumberInput.value = data["pageNumber"];
			tree.minOccursInput.value = data["minOccurs"];
			tree.maxOccursInput.value = data["maxOccurs"];
			tree.isRequiredCheckbox.checked = data["isRequired"];
			if (data["numForms"] && parseInt(data["numForms"]) > 1)
				disableField();
		}
		
		s.display = "";
		
		tree.fieldNumberInput.focus();
	}
	
	
	function evalTreeJS(js) {
		if (js.indexOf("DWR") == -1) {
			eval(js);
		}
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
		$('description').innerHTML = '';
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
		$('numForms').innerHTML = "";
		
		$('formFieldId').innerHTML = '';
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
	}
	
	function editAllFields() {
		var target = tree.getTreeNode(tree.contextMenu.contextEventTarget);
		document.location = "field.form?fieldId=" + target.data["fieldId"];
	}
	
	function editFieldForThisForm() {
		var target = tree.getTreeNode(tree.contextMenu.contextEventTarget);
		
		tree.fieldIdInput.value = "";
		enableField();
		$("fieldName").focus();
		tree.numFormsTag.innerHTML = "1";
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
					data["fieldId"] = null;
					if (tree.fieldIdInput.value != 'undefined' && tree.fieldIdInput.value != '')
						data["fieldId"] = tree.fieldIdInput.value;
					data["fieldName"] = tree.fieldNameInput.value;
					data["description"] = tree.descriptionInput.value;
					data["fieldType"] = tree.fieldTypeInput.value;
					data["conceptId"] = data["conceptName"] = '';
					data["tableName"] = data["atttributeName"] = '';
					if ($('concept').style.display != "none") {
						data["conceptId"] = tree.conceptIdInput.value;
						data["conceptName"] = tree.conceptNameTag.innerHTML;
					}
					else {
						data["tableName"] = tree.tableNameInput.value;
						data["attributeName"] = tree.attributeNameInput.value;
					}
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
				}
				
				var formId = <request:parameter name="formId"/>;
				data["parent"] = null;
				if (target.parent && target.parent.data)
					data["parent"] = target.parent.data["formFieldId"];
				if (data["parent"] == 0)
					data["parent"] = null;
				
				selectedNode = target;
				DWRFormService.saveFormField(cancelClicked, data.fieldId, data.fieldName, data.description, data.fieldType,
					data.conceptId, data.tableName, data.attributeName, data.defaultValue, data.selectMultiple, 
					data.formFieldId, formId, data.parent, data.fieldNumber, data.fieldPart, data.pageNumber, data.minOccurs, data.maxOccurs, data.isRequired);
				
				target.titleNode.innerHTML = target.title = getFieldLabel(data);
			}
			else
				tree.editDiv.style.display = "none";
		}
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
				ext = getRemoveLink(data);
			}
			
			var props = [];
			props.title = label;
			props.id = data.formFieldId ? data.formFieldId : null;
			
			var node = dojo.widget.createWidget("TreeNode", props, div);
			node.data = data;
			
			if (ext) {
				node.afterLabelNode.appendChild(ext);
			}
			
			<c:if test="${form.published == true}">
				node.actionsDisabled = [node.actions.ADDCHILD, node.actions.REMOVE];
			</c:if>
			
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
	
	var cancelClicked = function(savedNodeId) {
		clearFormField();
		closeBox();
		searchType = "";
		tree.editDiv.style.display = "none";
		
		if (savedNodeId && selectedNode.data) {
			selectedNode.data["formFieldId"] = savedNodeId;
	        //delete dojo.widget.widgetIds[selectedNode.widgetId];
	        //dojo.widget.widgetIds[savedNodeId] = selectedNode;
	        
			//var cntlr = selectedNode.tree.controller.DNDController;
			//cntlr.unregisterDNDNode(selectedNode);
			
			//selectedNode.id = savedNodeId;
	        //selectedNode.widgetId = savedNodeId;
	        
	        //cntlr.registerDNDNode(selectedNode);
		}
			
		// remove from main tree because they just dragged it over from the search
		else if (!selectedNode.data || !selectedNode.data["formFieldId"])
			removeClicked(selectedNode, true);
		
		selectedNode = null;
	}
	
	function showSearchForm(btn) {
		searchType = "concept";
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
		//objs.push('<a href="#newField" onclick="javascript:return showProposeConceptForm();"><spring:message code="ConceptProposal.propose.new"/></a>');
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
			return false;
		}
		
		for (o in objs) {
			var obj = objs[o];
			var child = dojo.widget.byId(obj.widgetId);
			
			var oldParent = child.parent;
			var oldTree = child.tree;
	
			tree.doAddChild(child, tree.children.length);
	
			var newParent = child.parent;
			var newTree = child.tree;
	
			var message = {
					oldParent: oldParent, oldTree: oldTree,
					newParent: newParent, newTree: newTree,
					child: child,
					skipEdit: true
			};
			
			dojo.html.removeClass(child.titleNode, "fieldConceptHit");
			dojo.dom.removeChildren(child.afterLabelNode);
			child.afterLabelNode.appendChild(getRemoveLink(child.data));
			
			dojo.event.topic.publish(tree.eventNames.moveFrom, message);
			dojo.event.topic.publish(tree.eventNames.moveTo, message);
		}
		
		resetForm();
		DWRUtil.removeAllRows(fieldResults);
		$('searchField').value = "";
		fieldResults.style.display = "none";
		
		return false;
	}
	
	var getConceptCellContent = getCellContent;
	
	var getCellContent = function(obj) {
		if (searchType == "concept")
			return getConceptCellContent(obj);
			
		if (typeof obj == 'string') return obj;
		
		var span = document.createElement("span");
		
		var data = getData(obj);
		span.title = data.title;
		
		// create a mini tree
		var properties = {id: "miniTree", 
						DNDMode: "between", 
						showRootGrid: false,
						DNDAcceptTypes: ["tree", "miniTree"],
						selector: "treeSelector"};
						
		var parentNode = span;
		var miniTree = dojo.widget.fromScript("Tree", properties, parentNode, "last");
		
		var node = addNode(miniTree, data, data.label);
		
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
			span.className = "treeNodeRow";
		}
		
		return span;
	}
	
	function getData(obj) {
		var data = [];
		data["selectMultiple"] = false;
		data["fieldId"] = null;
		
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
		data["fieldNumber"] = '';
		data["fieldPart"] = '';
		data["pageNumber"] = '';
		data["minOccurs"] = '';
		data["maxOccurs"] = '';
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

</script>

<h2>
	<spring:message code="Form.design.title" /> - 
	${form.name}
</h2>

<br/>
<a href="formEdit.form?formId=${form.formId}"><spring:message code="Form.editProperties" /></a> |
<a href="${pageContext.request.contextPath}/formDownload?target=schema&formId=${form.formId}"><spring:message code="Form.downloadSchema" /></a> |
<a href="${pageContext.request.contextPath}/formDownload?target=template&formId=${form.formId}"><spring:message code="Form.downloadTemplate" /></a> |
<a href="${pageContext.request.contextPath}/formDownload?target=xsn&formId=${form.formId}"><spring:message code="Form.downloadXSN" /></a>

<br/><br/>

<c:if test="${form.published == true}">
	<div class="retiredMessage"><div><spring:message code="Form.design.disabled"/></div></div>
</c:if>

<div dojoType="TreeBasicController" widgetId="treeController" DNDController="create"></div>
<div dojoType="TreeSelector" widgetId="treeSelector"></div>
<div dojoType="TreeContextMenu" toggle="explode" contextMenuForWindow="false" widgetId="treeContextMenu">
	<div dojoType="TreeMenuItem" treeActions="edit" caption="Edit Field" widgetId="treeContextMenuEdit"></div>
	<div dojoType="TreeMenuItem" treeActions="addChild" caption="Create" widgetId="treeContextMenuCreate"></div>
	<div dojoType="TreeMenuItem" treeActions="remove" caption="Remove" widgetId="treeContextMenuRemove"></div>
</div>

<table width="99%">
	<tr>
		<td valign="top">
			<div dojoType="Tree" menu="treeContextMenu" strictFolders="false" DNDMode="between" toggler="fade" widgetId="tree" DNDAcceptTypes="<c:if test="${form.published != true}">tree;miniTree</c:if>" controller="treeController">
			</div>
		</td>
		<td valign="top" style="padding-left: 5px;" id="fieldSearch" width="40%">
			<spring:message code="Field.find" /> <br/>
			<input type="text" id="searchField" size="25" onFocus="searchType='field'" onKeyUp="searchBoxChange(fieldResults, this, event, false, 400)"/>
			<table cellspacing="0" cellpadding="2" width="100%">
				<tbody id="fieldResults">
				</tbody>
			</table>
		</td>
	</tr>
</table>

<div id="editFormField">
	<div id="formFieldTitle"><spring:message code="FormField.edit"/>:</div>
	
	<%@ include file="include/formFieldEdit.jsp" %>
	
	<c:if test="${form.published != true}">
		<input type="button" id="saveFormField" onclick="save(selectedNode)" value="<spring:message code="general.save"/>" />
	</c:if>
	<input type="button" id="cancelFormField" onclick="cancelClicked()" value="<spring:message code="general.cancel"/>" />
	
</div>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onclick="return closeBox();" class="closeButton" value="X" />
		<form method="get" onsubmit="return searchBoxChange('searchBody', searchText, null, false, 0); return false;" action="">
			<h3>
				<spring:message code="Concept.find" />
			</h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);">
			<input type="checkbox" id="verboseListing" value="true" <c:if test="${defaultVerbose == true}">checked</c:if> onclick="searchBoxChange('searchBody', searchText, event, false, 0); searchText.focus();"><label for="verboseListing"><spring:message code="dictionary.verboseListing"/></label>
		</form>
		<div id="searchResults" class="searchResults">
			<table cellspacing="0" cellpadding="2">
				<tbody id="searchBody">
				</tbody>
			</table>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>