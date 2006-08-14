<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="startValue" required="true" type="java.util.Date" %>
<%@ attribute name="datePattern" required="true" %>

<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
<script type="text/javascript">
	dojo.require("dojo.widget.html.DatePicker");
	dojo.require("dojo.widget.DropdownDatePicker");
</script>

<div dojoType="dropdowndatepicker" dateFormat="%Y-%m-%d"></div>