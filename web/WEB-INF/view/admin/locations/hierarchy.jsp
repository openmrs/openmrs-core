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

<spring:message code="Location.hierarchy"/>
<div id="hierarchyTree"></div>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.hierarchy.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>
