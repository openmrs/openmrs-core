<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Forms" otherwise="/login.htm" redirect="/admin/forms/form.form" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>

<style>
	.indent {
		padding-left: 2em;
	}
	.required {
		color: red;
	}
	#addForm {
		width: 400px;
		position: absolute;
		z-index: 10;
		margin: 5px;
	}
	#addForm #wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 235px;
		overflow: auto;
	}
	.delete {
		background-image: (/@WEBAPP.NAME@/images/delete.gif no-repeat center center);
	}
</style>

<script type="text/javascript">

	var myEffect = null;
	
	function init() {
		myEffect = new fx.Resize("addForm", {duration: 100});
		myEffect.hide();
	}
	
	function selectField(id, link) {
		div = document.getElementById("addForm");
		setPosition(link, div);
		myEffect.toggle();
		return false;
	}
	
	function deleteField(id, link) {
		
		return false;
	}
	
	function setPosition(btn, form) {
		var left  = getElementLeft(btn) + 10;
		var top   = getElementTop(btn)+13;
		var formWidth  = 520;
		var formHeight = 280;
		var windowWidth = window.innerWidth + getScrollOffsetX();
		var windowHeight = window.innerHeight + getScrollOffsetY();
		if (left + formWidth > windowWidth)
			left = windowWidth - formWidth - 10;
		if (top + formHeight > windowHeight)
			top = windowHeight - formHeight - 10;
		form.style.left = left + "px";
		form.style.top = top + "px";
	}
	
	function getElementLeft(elm) {
		var x = 0;
		while (elm != null) {
			x+= elm.offsetLeft;
			elm = elm.offsetParent;
		}
		return parseInt(x);
	}
	
	function getElementTop(elm) {
		var y = 0;
		while (elm != null) {
			y+= elm.offsetTop;
			elm = elm.offsetParent;
		}
		return parseInt(y);
	}
	
	function getScrollOffsetY() {
		if (window.innerHeight) {
			return window.pageYOffset;
		}
		else {
			return document.documentElement.scrollTop;
		}
	}
	
	function getScrollOffsetX() {
		if (window.innerWidth) {
			return window.pageXOffset;
		}
		else {
			return document.documentElement.scrollLeft;
		}
	}
	
	function closeBox() {
		myEffect.hide();
		return false;
	}

	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = init;
	} else {
		window.onload = function() {
			oldonload();
			init();
		}
	}

</script>

<h2><spring:message code="Form.manage" /></h2>	

<a href="formField.edit"><spring:message code="FormField.add" /></a> <br />

<br />

<c:set var="parent" value=""/>
<c:set var="last_ff" value=""/>

${tree}

<div id="addForm">
	<form id="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<%@ include file="formField.jsp" %>
		<input type="submit" value="<spring:message code="general.save"/>"/>
	</form>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>