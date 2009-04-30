<%@ include file="/WEB-INF/template/include.jsp" %>

<h2>Settings</h2>	

<div id="settings-container" style="overflow: hidden">

<div id="settings-tree" style="float: left; width: 24%">
	<ul>
		<li id="system-settings"><a href="#">System Wide Settings</a></li>
		<li id="modules-settings">
			<div>Modules Settings</div>
			<ul>
				<c:forEach var="module" items="${settings.modules}" varStatus="status">
					<li class="module" id="${module.name}"><a href="#">${module.name}</a></li>
				</c:forEach>
			</ul>
		</li>
	</ul>
</div>

<div id="system-settings-content" style="float: right; width: 75.5%">
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
</div> <!-- end system-settings-content -->

</div> <!-- end settings-container -->
