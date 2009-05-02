<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/admin/maintenance/settings.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="http://jqueryui.com/latest/jquery-1.3.2.js"></script>
<script type="text/javascript" src="http://jqueryui.com/latest/ui/ui.core.js"></script>
<script type="text/javascript" src="http://jqueryui.com/latest/ui/ui.tabs.js"></script>
<script type="text/javascript">
$(document).ready(function(){
  $("#settings-container").tabs();
});
</script>

<style>
.ui-tabs .ui-tabs-hide {
     display: none;
}
.ui-tabs-selected a, .ui-state-active a {
	color: #8FABC7;
}
</style>

<style>
#settings-container {
	font-family: Verdana, sans-serif;
	font-size: 10pt;
}

#tabs-pane {
	float: left; 
	width: 250px;
	padding-left: 0;
	clear: left;
	margin: 0;
	list-style: none;
}

.settings-pane {
	margin-left: 250px;
}

.settings-pane table {
	width: 100%;
}

.settings-pane table tr {
	width: 100%;
	padding: 0; 
	font-size: 10pt
}

.module-settings-pane {
}

.settings-pane table thead, #settings-container div.caption {
	background-color: #8FABC7;
	color: #fff;
}
.caption {
	width: 250px;
	float: left;
	font-weight: bold;
	font-size: 11pt;
}
.even {
	background-color: #F5F5F5;
}
img {
	border: none;
}
a {
	color: #666666;
}
</style>

<div id="settings-container">
<div class="caption">Settings</div>
<ul id="tabs-pane">
	<li><a href="#system-settings-pane">System Wide Settings</a></li>
<c:forEach var="module" items="${settings.modules}" varStatus="status">
	<li class="module"><a href="#${module.moduleId}-settings-pane">${module.name}</a></li>
</c:forEach>
</ul>

<div id="system-settings-pane" class="settings-pane">
<form method="post">
	<table cellpadding="1" cellspacing="0">
		<thead>
			<tr>
				<th>Property Name</th>
				<th>Property Value</th>
				<th>Property Type</th>
				<th>Description</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="globalProperty" items="${settings.globalProperties}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">
				<td>${globalProperty.property}</td>
				<td>${globalProperty.propertyValue}</td>
				<td>Type&nbsp;name</td>
				<td style="font-size: 8pt;">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
			<tr>
				<td><input type="text" name="property"/></td>
				<td><input type="text" name="value"/></td>
				<td><input type="text" name="type"/></td>
				<td><textarea name="description"></textarea></td>
				<td><a href="#">Add</a></td>
			</tr>
		</tbody>
	</table>
</form>
</div> <!-- end system-settings -->

<c:forEach var="module" items="${settings.modules}">
<div id="${module.moduleId}-settings-pane" class="module-settings-pane settings-pane">
<form method="post">
	<table cellpadding="1" cellspacing="0">
		<thead>
			<tr>
				<th>Property Name</th>
				<th>Property Value</th>
				<th>Property Type</th>
				<th>Description</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="globalProperty" items="${module.globalProperties}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">
				<td>${globalProperty.property}</td>
				<td>${globalProperty.propertyValue}</td>
				<td>Type&nbsp;name</td>
				<td style="font-size: 8pt;">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
			<tr>
				<td><input type="text" name="property"/></td>
				<td><input type="text" name="value"/></td>
				<td><input type="text" name="type"/></td>
				<td><textarea name="description"></textarea></td>
				<td><a href="#">Add</a></td>
			</tr>
		</tbody>
	</table>
</form>
</div> <!-- end ${module.moduleId}-settings -->
</c:forEach>

</div> <!-- end settings-container -->

<%@ include file="/WEB-INF/template/footer.jsp" %>