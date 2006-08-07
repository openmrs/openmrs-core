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

		hiddenInputNode: null,
		hiddenInputName: "",
		
		changeButton: null,

		searchWidget: "",
		searchTitle: "",

		initializer: function(){
			dojo.debug("initializing OpenmrsPopup");
		},
		
		fillInTemplate: function(args, frag){
			dojo.event.connect(this.changeButton, "onmouseup", this, "onChangeButtonClick");
		},
		
		templateString: '<div><span style="white-space: nowrap"><span dojoAttachPoint="displayNode"></span> <input type="hidden" value="" dojoAttachPoint="hiddenInputNode" /> <input type="button" value="Change" dojoAttachPoint="changeButton" class="smallButton" /> </span> </div>',
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
				
				dojo.event.connect(this.searchWidget, "select", this, "closeSearch"); 
				
				this.searchWidget.inputNode.style.width="190px";
				
			}
			
			if (this.hiddenInputName)
				this.hiddenInputNode.name = this.hiddenInputName;	
		},
		
		onChangeButtonClick: function() {
			dojo.debug("Change button clicked");
			this.searchWidget.domNode.style.left = dojo.style.totalOffsetLeft(this.changeButton) + dojo.style.getBorderBoxWidth(this.changeButton) + 10;
			this.searchWidget.domNode.style.top = dojo.style.totalOffsetTop(this.changeButton);				
			this.searchWidget.clearSearch();
			this.searchWidget.toggleShowing();
			this.searchWidget.inputNode.select();
		},
		
		closeSearch: function() {
			this.searchWidget.hide();
		}

	},
	"html"
);