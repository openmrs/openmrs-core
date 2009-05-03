<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/admin/maintenance/settings.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-1.3.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-ui-1.7.1/jquery-ui-1.7.1.core.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-ui-1.7.1/jquery-ui-1.7.1.tabs.min.js"/>"></script>
<script type="text/javascript">
$(document).ready(function(){
  $("#settings-container").tabs();
});
</script>

<style>
.ui-tabs .ui-tabs-hide {
     display: none;
}
#tabs-pane li.ui-tabs-selected a, #tabs-pane li.ui-state-active a {
	color: #8FABC7;
	text-decoration: none;
}
</style>

<style>
#settings-container {
	font-family: Verdana, sans-serif;
}

#tabs-pane {
	float: left; 
	width: 245px;
	padding-left: 0;
	clear: left;
	margin: 0;
	list-style: none;
}

#tabs-pane a {
	display: block;
	margin-left: 20px;
}

#tabs-pane li {
	padding: 0.3em 0;
}

.module-tabs-item {
	margin-left: 16px;
	list-style-image: url("<c:url value="/images/plug_arrow.png"/>");
	list-style-position: inside;
	padding: 0.1em 0;
}

.settings-pane {
	margin-left: 250px;
}

.settings-pane table {
}

.settings-pane table td {
	padding: 5px; 
}

.module-settings-pane {
}

.settings-pane table thead, #settings-container div.caption {
	background-color: #8FABC7;
	color: #fff;
}
.caption {
	width: 245px;
	float: left;
	font-weight: bold;
	padding: 1px 5px;
}
.odd {
	background-color: #F5F5F5;
}
.property-value {
	border: none;
	height: 1.25em;
	overflow: hidden;
	font-family: Courier New, monospace;
}
.property-description {
	color: #888;
	font-size: 0.7em;
}
img {
	border: none;
}
.all-settings-item {
	list-style-image: url("<c:url value="/images/wrench_screwdriver.png"/>");
	list-style-position: inside;
}
.system-settings-item {
	list-style-image: url("<c:url value="/images/hammer.png"/>");
	list-style-position: inside;
}
.modules-settings-item {
	list-style-image: url("<c:url value="/images/plug.png"/>");
	list-style-position: inside;
}
</style>

<div id="settings-container">
<div class="caption">Settings</div>
<ul id="tabs-pane">
	<li class="all-settings-item"><a href="#all-settings-pane">All Settings</a></li>
	<li class="system-settings-item"><a href="#system-settings-pane">System Settings</a></li>
	<li class="modules-settings-item">Modules Settings</li>
<c:forEach var="module" items="${settings.modules}" varStatus="status">
	<li class="module-tabs-item"><a href="#${module.moduleId}-settings-pane">${module.name}</a></li>
</c:forEach>
</ul>

<div id="all-settings-pane" class="settings-pane">
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
			<c:forEach var="globalProperty" items="${settings.allSettings}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">
				<td style="font-size: 0.8em">${globalProperty.property}</td>
				<td><textarea readonly="readonly" class="property-value <c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">${globalProperty.propertyValue}</textarea></td>
				<td style="font-size: 0.8em">Type&nbsp;name</td>
				<td class="property-description">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
			<tr>
				<td><input type="text" name="property"/></td>
				<td><input type="text" name="value"/></td>
				<td><input type="text" name="type"/></td>
				<td><textarea name="description"></textarea></td>
				<td><a href="#"><img src="<c:url value="/images/add.gif"/>"/></a></td>
			</tr>
		</tbody>
	</table>
</form>
</div> <!-- end all-settings -->

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
			<c:forEach var="globalProperty" items="${settings.systemSettings}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">
				<td style="font-size: 0.8em">${globalProperty.property}</td>
				<td><textarea readonly="readonly" class="property-value <c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">${globalProperty.propertyValue}</textarea></td>
				<td style="font-size: 0.8em">Type&nbsp;name</td>
				<td class="property-description">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
			<tr>
				<td><input type="text" name="property"/></td>
				<td><input type="text" name="value"/></td>
				<td><input type="text" name="type"/></td>
				<td><textarea name="description"></textarea></td>
				<td><a href="#"><img src="<c:url value="/images/add.gif"/>"/></a></td>
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
				<td style="font-size: 0.8em">${globalProperty.property}</td>
				<td><textarea readonly="readonly" class="property-value <c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">${globalProperty.propertyValue}</textarea></td>
				<td style="font-size: 0.8em">Type&nbsp;name</td>
				<td class="property-description">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
			<tr>
				<td><input type="text" name="property"/></td>
				<td><input type="text" name="value"/></td>
				<td><input type="text" name="type"/></td>
				<td><textarea name="description"></textarea></td>
				<td><a href="#"><img src="<c:url value="/images/add.gif"/>"/></a></td>
			</tr>
		</tbody>
	</table>
</form>
</div> <!-- end ${module.moduleId}-settings -->
</c:forEach>

</div> <!-- end settings-container -->

<%@ include file="/WEB-INF/template/footer.jsp" %>