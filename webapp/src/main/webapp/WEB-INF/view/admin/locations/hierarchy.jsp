<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Location Tags" otherwise="/login.htm" redirect="/admin/locations/locationTag.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<openmrs:htmlInclude file="/scripts/jquery/jsTree/jquery.tree.min.js" />
<openmrs:htmlInclude file="/scripts/jquery/jsTree/themes/classic/style.css" />

<script type="text/javascript">
	$j(document).ready(function() {
		$j('#hierarchyTree').tree({
				data: {
					type: "json",
					opts: {
						static: ${json}
					}
				},
				types: {
					"default" : {
						clickable	: false,
						renameable	: false,
						deletable	: false,
						creatable	: false,
						draggable	: false,
						max_children	: -1,
						max_depth	: -1,
						valid_children	: "all"
					}
				},
				ui: {
					theme_name: "classic"
				}
			});
		$j.tree.reference('#hierarchyTree').open_all();
	});
</script>

<fieldset style="clear: both">
	<legend><openmrs:message code="Location.hierarchy.chooseWidgetHeader"/>:</legend>
	<form method="post" action="changeLocationWidgetType.form">
		<openmrs:message code="Location.hierarchy.chooseWidgetStyle"/>:
		<select name="locationWidgetType" onchange="submit()">
			<option value="default" <c:if test="${locationWidgetType == 'default'}">selected="true"</c:if>>
				<openmrs:message code="Location.hierarchy.widget.default"/>
			</option>
			<option value="tree" <c:if test="${locationWidgetType == 'tree'}">selected="true"</c:if>>
				<openmrs:message code="Location.hierarchy.widget.tree"/>
			</option>
		</select>
	</form>

	<div style="margin: 0.5em 0;">
		(<openmrs:message code="Location.hierarchy.example"/>)
		<openmrs:message code="Location.hierarchy.exampleLabel"/>:
		<openmrs:fieldGen type="org.openmrs.Location" formFieldName="locationId" val="${selectedLocation}"/>
	</div>
</fieldset>

<br/>

<fieldset style="clear: both">
	<legend><openmrs:message code="Location.hierarchy"/></legend>
	<div id="hierarchyTree"></div>
</fieldset>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.hierarchy.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
