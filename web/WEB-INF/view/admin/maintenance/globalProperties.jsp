<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Global Properties" otherwise="/login.htm" redirect="/admin/maintenance/globalProperties.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>

<%@ include file="localHeader.jsp" %>

<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-1.3.2.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-ui-1.7.2/jquery-ui-1.7.2.core.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-ui-1.7.2/jquery-ui-1.7.2.tabs.min.js"/>"></script>
<script type="text/javascript" src="<c:url value="/scripts/jquery/jquery-ui-1.7.2/jquery-ui-1.7.2.dialog.min.js"/>"></script>

<style type="text/css">
#dialog {
	padding: 1em 2ex;
	width: 404px;
}
.record {
	margin: 1em 2ex;
}
.record label {
	float: left;
	width: 25ex;
}
</style>

<script type="text/javascript">
$(function() {
	$("#properties-container").tabs();

	$("#system-properties-link").click(
		function () {
			$(".module-tabs-item").slideUp();
			$(".namespace-item").slideDown();
		}
	);

	$("#all-properties-link").click(
		function () {
			$(".namespace-item").slideUp();
			$(".module-tabs-item").slideUp();
		}
	);

	$("#modules-properties-link").click(
		function () {
			$(".namespace-item").slideUp();
			$(".module-tabs-item").slideDown();
		}
	);
	
	$("#dialog").dialog({
		bgiframe: true,
		autoOpen: false,
		modal: true,
		width: 404,
		buttons: {
			'Create property': function() {
				$(this).dialog('close');
			},
			'Cancel': function() {
				$(this).dialog('close');
			}
		},
		close: function() {
		}
	});
	
	$('#add-global-property').click(function() {
		$('#dialog').dialog('open');
	});
	
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

#properties-container {
	font-family: Verdana, sans-serif;
}

#tabs-pane {
	float: left;
	padding-left: 0;
	margin: 0 10px 0 0;
    border-bottom: 1px solid #AAAAAA;
}

#tabs-pane a {
    display: block;
    padding: 0 1ex;
}

#tabs-pane li {
	padding: 0.3em 0;
}

.module-tabs-item {
	list-style-position: inside;
	padding: 0.1em 0;
	font-size: 8pt;
}

.module-tabs-item a {
	margin-left: 12px;
}

.namespace-item {
	font-size: 8pt;
}

.namespace-item a {
	margin-left: 12px;
}

.properties-pane table td {
	padding: 5px; 
}

.properties-pane table thead, #properties-container div.caption {
	background-color: #8FABC7;
	color: #fff;
}
.caption {
	font-weight: bold;
	padding: 1px 5px;
}
.odd {
	background-color: #F5F5F5;
}
.property-value {
	font-size: 8pt;
	border: none;
	height: 1.25em;
	font-family: Courier New, monospace;
	width: 100%;
}
.property-description {
	color: #888;
	font-size: 0.7em;
	width: 30%;
}
img {
	border: none;
}
.all-properties-item {
	font-size: 10pt;
}
.system-properties-item {
	font-size: 10pt;
}
.modules-properties-item {
	font-size: 10pt;
}
</style>

<div id="dialog" title="Add new global property">
	<form>
		<div class="record">
			<label for="name">Namespace</label>
			<input type="text" name="namespace" id="namespace" />
		</div>
		<div class="record">
			<label for="email">Property name</label>
			<input type="text" name="propertyName" id="propertyName" />
		</div>
		<div class="record">
			<label for="password">Property value</label>
			<input type="password" name="propertyValue" id="propertyValue" />
		</div>
		<div class="record">
			<label for="password">Property type</label>
			<input type="password" name="propertyType" id="propertyType" />
		</div>
	</form>
</div>

<div id="properties-container">

<button id="add-global-property">Add property</button>

<ul id="tabs-pane">
	<li class="all-properties-item"><a href="#all-properties-pane" id="all-properties-link"><spring:message code="GlobalProperty.all"/></a></li>
	<li class="system-properties-item"><a href="#system-properties-pane" id="system-properties-link"><spring:message code="GlobalProperty.system"/></a></li>
<c:forEach var="namespace" items="${globalProperties.systemNamespaces}" varStatus="status">
	<li class="namespace-item"><a href="#${namespace.name}-properties-pane">${namespace.name}</a></li>
</c:forEach>
	<li class="modules-properties-item"><a href="#system-properties-pane" id="modules-properties-link"><spring:message code="GlobalProperty.modules"/></a></li>
<c:forEach var="module" items="${globalProperties.modules}" varStatus="status">
	<li class="module-tabs-item"><a href="#${module.moduleId}-module-properties-pane">${module.name}</a></li>
</c:forEach>
</ul>

<div id="all-properties-pane" class="properties-pane">
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
				<td style="font-size: 0.8em">${globalProperty.propertyType}</td>
				<td class="property-description">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
		</tbody>
	</table>
</form>
</div> <!-- end all-properties -->

<div id="system-properties-pane" class="properties-pane">
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
				<td style="font-size: 0.8em">${globalProperty.propertyType}</td>
				<td class="property-description">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
		</tbody>
	</table>
</form>
</div> <!-- end system-properties -->

<c:forEach var="namespace" items="${globalProperties.systemNamespaces}">
<div id="${namespace.name}-properties-pane" class="namespace-properties-pane properties-pane">
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
			<c:forEach var="globalProperty" items="${namespace.globalProperties}" varStatus="status">
			<tr class="<c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">
				<td style="font-size: 0.8em">${globalProperty.property}</td>
				<td><textarea readonly="readonly" class="property-value <c:choose><c:when test="${status.index % 2 == 0}">even</c:when><c:otherwise>odd</c:otherwise></c:choose>">${globalProperty.propertyValue}</textarea></td>
				<td style="font-size: 0.8em">${globalProperty.propertyType}</td>
				<td class="property-description">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
		</tbody>
	</table>
</form>
</div> <!-- end ${namespace.name}-properties -->
</c:forEach>

<c:forEach var="module" items="${globalProperties.modules}">
<div id="${module.moduleId}-module-properties-pane" class="module-properties-pane properties-pane">
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
				<td style="font-size: 0.8em">${globalProperty.propertyType}</td>
				<td class="property-description">${globalProperty.description}</td>
				<td><a href="#"><img src="<c:url value="/images/edit.gif"/>"/></a></td>
			</tr>
			</c:forEach>
		</tbody>
	</table>
</form>
</div> <!-- end ${module.moduleId}-properties -->
</c:forEach>

</div> <!-- end properties-container -->

<%@ include file="/WEB-INF/template/footer.jsp" %>