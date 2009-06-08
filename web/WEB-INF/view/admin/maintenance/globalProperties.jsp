<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/admin/maintenance/globalProperties.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-1.3.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-ui-1.7.1/jquery-ui-1.7.1.core.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-ui-1.7.1/jquery-ui-1.7.1.tabs.min.js"/>"></script>
<script type="text/javascript">
$(document).ready(function(){
  $("#propeties-container").tabs();
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
#propeties-container {
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

.propeties-pane {
	margin-left: 250px;
}

.propeties-pane table {
}

.propeties-pane table td {
	padding: 5px; 
}

.module-propeties-pane {
}

.propeties-pane table thead, #propeties-container div.caption {
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
.all-propeties-item {
	list-style-image: url("<c:url value="/images/wrench_screwdriver.png"/>");
	list-style-position: inside;
}
.system-propeties-item {
	list-style-image: url("<c:url value="/images/hammer.png"/>");
	list-style-position: inside;
}
.modules-propeties-item {
	list-style-image: url("<c:url value="/images/plug.png"/>");
	list-style-position: inside;
}
</style>

<div id="propeties-container">
<div class="caption"><spring:message code="GlobalProperty.manage.title"/></div>
<ul id="tabs-pane">
	<li class="all-propeties-item"><a href="#all-propeties-pane"><spring:message code="GlobalProperty.all"/></a></li>
	<li class="system-propeties-item"><a href="#system-propeties-pane"><spring:message code="GlobalProperty.system"/></a></li>
	<li class="modules-propeties-item"><spring:message code="GlobalProperty.modules"/></li>
<c:forEach var="module" items="${globalProperties.modules}" varStatus="status">
	<li class="module-tabs-item"><a href="#${module.moduleId}-propeties-pane">${module.name}</a></li>
</c:forEach>
</ul>

<div id="all-propeties-pane" class="propeties-pane">
<form method="post">
	<table cellpadding="1" cellspacing="0">
		<thead>
			<tr>
				<th><spring:message code="GlobalProperty.name"/></th>
				<th><spring:message code="GlobalProperty.value"/></th>
				<th><spring:message code="GlobalProperty.type"/></th>
				<th><spring:message code="GlobalProperty.description"/></th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="globalProperty" items="${globalProperties.allProperties}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">
				<td style="font-size: 0.8em">${globalProperty.property}</td>
				<td><textarea readonly="readonly" class="property-value <c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">${globalProperty.propertyValue}</textarea></td>
				<td style="font-size: 0.8em">${globalProperty.propertyType.name}</td>
				<td class="property-description">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
			<tr>
				<td><input type="text" name="property" onchange=""/></td>
				<td><input type="text" name="value"/></td>
				<td><input type="text" name="type"/></td>
				<td><textarea name="description"></textarea></td>
				<td><a href="#"><img src="<c:url value="/images/add.gif"/>"/></a></td>
			</tr>
		</tbody>
	</table>
</form>
</div> <!-- end all-propeties -->

<div id="system-propeties-pane" class="propeties-pane">
<form method="post">
	<table cellpadding="1" cellspacing="0">
		<thead>
			<tr>
				<th><spring:message code="GlobalProperty.name"/></th>
				<th><spring:message code="GlobalProperty.value"/></th>
				<th><spring:message code="GlobalProperty.type"/></th>
				<th><spring:message code="GlobalProperty.description"/></th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="globalProperty" items="${globalProperties.systemProperties}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">
				<td style="font-size: 0.8em">${globalProperty.property}</td>
				<td><textarea readonly="readonly" class="property-value <c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">${globalProperty.propertyValue}</textarea></td>
				<td style="font-size: 0.8em">${globalProperty.propertyType.name}</td>
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
</div> <!-- end system-propeties -->

<c:forEach var="module" items="${globalProperties.modules}">
<div id="${module.moduleId}-propeties-pane" class="module-propeties-pane propeties-pane">
<form method="post">
	<table cellpadding="1" cellspacing="0">
		<thead>
			<tr>
				<th><spring:message code="GlobalProperty.name"/></th>
				<th><spring:message code="GlobalProperty.value"/></th>
				<th><spring:message code="GlobalProperty.type"/></th>
				<th><spring:message code="GlobalProperty.description"/></th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="globalProperty" items="${module.globalProperties}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">
				<td style="font-size: 0.8em">${globalProperty.property}</td>
				<td><textarea readonly="readonly" class="property-value <c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">${globalProperty.propertyValue}</textarea></td>
				<td style="font-size: 0.8em">${globalProperty.propertyType.name}</td>
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
</div> <!-- end ${module.moduleId}-propeties -->
</c:forEach>

</div> <!-- end propeties-container -->

<%@ include file="/WEB-INF/template/footer.jsp" %>