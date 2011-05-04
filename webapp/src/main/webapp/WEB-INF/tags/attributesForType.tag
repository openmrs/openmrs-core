<%@tag import="java.util.List"%>
<%@tag import="org.openmrs.attribute.Attribute"%>

<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldNamePrefix" required="true" type="java.lang.String" %>
<%@ attribute name="attributeType" required="true" type="org.openmrs.attribute.AttributeType" %>
<%@ attribute name="customizable" required="true" type="org.openmrs.attribute.Customizable" %>
<%
List<Attribute> existing = customizable.getActiveAttributes(attributeType);
int howManyToShow = attributeType.getMaxOccurs() == null ? 1 : attributeType.getMaxOccurs();
howManyToShow = Math.max(howManyToShow, existing.size());
%>
<tr>
    <td>${ attributeType.name }</td>
    <td>
<% for (int i = 0; i < howManyToShow; ++i) {
	Attribute val = null;
	if (existing.size() > i)
		val = existing.get(i);
%>
        <openmrs_tag:attribute
            attributeType="${ attributeType }"
            formFieldName="<%= jspContext.getAttribute("formFieldNamePrefix") + "[" + i + "]" %>"
            value="<%= val %>"/>
        <br/>
<% } %>
    </td>
</tr>