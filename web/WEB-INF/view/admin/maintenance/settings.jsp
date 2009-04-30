<%@ include file="/WEB-INF/template/include.jsp" %>

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
</style>

<h2>Settings</h2>	

<div id="settings-container" style="overflow: hidden">

	<ul style="float: left; width: 24%">
		<li><a href="#system-settings">System Wide Settings</a></li>
	<c:forEach var="module" items="${settings.modules}" varStatus="status">
		<li class="module"><a href="#${module.moduleId}-settings">${module.name}</a></li>
	</c:forEach>
	</ul>

<div id="system-settings" style="float: right; width: 75.5%">
<form method="post">
	<table cellpadding="1" cellspacing="0" style="width: 100%;">
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
			<c:forEach var="globalProperty" items="${settings.globalProperties}">
				<tr>
					<td>${globalProperty.property}</td>
					<td>${globalProperty.propertyValue}</td>
					<td>Type&nbsp;name</td>
					<td>${globalProperty.description}</td>
					<td><a href="#">Edit</a></td>
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
<div id="${module.moduleId}-settings" style="float: right; width: 75.5%; diaplay: none">
<form method="post">
	<table cellpadding="1" cellspacing="0" style="width: 100%;">
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
			<c:forEach var="globalProperty" items="${module.globalProperties}">
				<tr>
					<td>${globalProperty.property}</td>
					<td>${globalProperty.propertyValue}</td>
					<td>Type&nbsp;name</td>
					<td>${globalProperty.description}</td>
					<td><a href="#">Edit</a></td>
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
