/*
	Copyright (c) 2006, The OpenMRS Cooperative
	All Rights Reserved.
*/

dojo.provide("dojo.widget.openmrs.OpenmrsPopup");

dojo.require("dojo.widget.*");

dojo.widget.tags.addParseTreeHandler("dojo:OpenmrsPopup");

dojo.widget.defineWidget(
	"dojo.widget.openmrs.OpenmrsPopup",
	dojo.widget.HtmlWidget,
	{
		isContainer: true,

		displayNode: null,
		descriptionDisplayNode: null,

		hiddenInputNode: null,
		hiddenInputName: "",
		hiddenInputId: "",
		
		showChangeButton: true,
		changeButton: null,
		changeButtonValue: "",

		searchWidget: "",
		searchTitle: "",
		
		allowSearch: true,
		
		form: null,
		
		eventNames: {},
		eventNamesDefault: {
			select : "select"
		},
	
		initializer: function(){
			dojo.debug("initializing OpenmrsPopup");
			
			for(name in this.eventNamesDefault) {
				if (dojo.lang.isUndefined(this.eventNames[name])) {
					this.eventNames[name] = this.widgetId+"/"+this.eventNamesDefault[name];
				}
			}
		},
		
		fillInTemplate: function(args, frag){
			dojo.event.connect(this.changeButton, "onmouseup", this, "onChangeButtonClick");
			dojo.event.connect(this.changeButton, "onkeyup", this, "onChangeButtonKeyup");
			
			if (!this.allowSearch)
				this.changeButton.style.display = "none";
		},
		
		templateString: '<span id="${this.widgetId}"><span style="white-space: nowrap"><span dojoAttachPoint="displayNode"></span> <input type="hidden" value="" dojoAttachPoint="hiddenInputNode" /><input type="button" value="Change" dojoAttachPoint="changeButton" class="smallButton" /></span><div class="description" dojoAttachPoint="descriptionDisplayNode"></div> </span>',
		templateCssPath: "",
		
		postCreate: function(){
			var widg = dojo.widget.manager.getWidgetById(this.searchWidget);
			
			if (widg) {
				dojo.debug("Adding searchWidget: " + this.searchWidget);
				this.searchWidget = widg;
				this.addChild(this.searchWidget);
				this.searchWidget.domNode.className = "popupSearchForm";
				this.searchWidget.toggleShowing();
				
				if (!this.searchWidget.tableHeight)
					this.searchWidget.tableHeight = 332;
				
				if (this.searchTitle) {
					var title = document.createElement("h4");
					title.innerHTML = this.searchTitle;
					this.searchWidget.domNode.insertBefore(title, this.searchWidget.domNode.firstChild);
					this.searchWidget.tableHeight = 310;
				}
				
				var closeButton = document.createElement("input");
				closeButton.type = "button";
				closeButton.className="closeButton";
				closeButton.value="X";
				this.searchWidget.domNode.insertBefore(closeButton, this.searchWidget.domNode.firstChild);
				dojo.event.connect(closeButton, "onmouseup", this, "closeSearch");
				
				dojo.event.connect(this.searchWidget, "select", this, "doSelect"); 
				dojo.event.connect(this.searchWidget, "doObjectsFound", this, "doObjectsFound"); 
				
				this.searchWidget.inputNode.style.width="190px";
				
			}
			else {
				alert("searchWidget not found: '" + this.searchWidget + "'");
			}
			
			if (this.hiddenInputName)
				this.hiddenInputNode.name = this.hiddenInputName;	
			
			if (this.hiddenInputId)
				this.hiddenInputNode.id = this.hiddenInputId;	
			
			if (this.changeButtonValue)
				this.changeButton.value = this.changeButtonValue;
			
			if (!this.showChangeButton)
				this.changeButton.style.display = "none";
			
		},
		
		onChangeButtonClick: function() {
			dojo.debug("Change button clicked");
			
			this.searchWidget.clearSearch();
			this.searchWidget.toggleShowing();
			
			var left = dojo.style.sumAncestorProperties(this.changeButton, false) + dojo.style.getBorderBoxWidth(this.changeButton) + 10;
			if (left + dojo.style.getBorderBoxWidth(this.searchWidget.domNode) > dojo.html.getViewportWidth())
				left = dojo.html.getViewportWidth() - dojo.style.getBorderBoxWidth(this.searchWidget.domNode) - 10 + dojo.html.getScrollLeft();
			
			//var top = pos.y;
			//if (top + dojo.style.getBorderBoxHeight(this.searchWidget.domNode) > dojo.html.getViewportHeight())
			//	top = dojo.html.getViewportHeight() - dojo.style.getBorderBoxHeight(this.searchWidget.domNode) - 10 + dojo.html.getScrollTop();
			
			dojo.style.setPositivePixelValue(this.searchWidget.domNode, "left", left);
			//dojo.style.setPositivePixelValue(this.searchWidget.domNode, "top", top);
			
			this.searchWidget.inputNode.select();
			if (this.searchWidget.domNode.scrollIntoView)
				this.searchWidget.domNode.scrollIntoView();
			
		},
		
		onChangeButtonKeyup: function(evt) {
			if (evt.keyCode == dojo.event.browser.keys.KEY_SPACE || 
				evt.keyCode == dojo.event.browser.keys.KEY_ENTER)
					this.onChangeButtonClick();
		},
		
		showIfHiding: false,
		doObjectsFound: function(objs) {
			if (!this.searchWidget.isShowing() && this.showIfHiding)
				this.searchWidget.toggleShowing();
		},
		
		doSelect: function(objs) {
			this.closeSearch();
			dojo.event.topic.publish(this.eventNames.select, {"objs":objs});
		},
		
		closeSearch: function() {
			this.searchWidget.hide();
			this.changeButton.focus();
		}

	},
	"html"
);