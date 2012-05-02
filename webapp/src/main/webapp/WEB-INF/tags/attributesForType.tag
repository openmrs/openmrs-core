<%@tag import="java.util.List"%>
<%@tag import="org.openmrs.attribute.Attribute"%>

<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldNamePrefix" required="true" type="java.lang.String" %>
<%@ attribute name="attributeType" required="true" type="org.openmrs.attribute.AttributeType" %>
<%@ attribute name="customizable" required="true" type="org.openmrs.customdatatype.Customizable" %>
<%
List<Attribute> existing = customizable.getActiveAttributes(attributeType);
int howManyToShow = attributeType.getMaxOccurs() == null ? 1 : attributeType.getMaxOccurs();
howManyToShow = Math.max(howManyToShow, existing.size());
%>
<tr>
    <th>${ attributeType.name }</th>
    <td>
<% for (int i = 0; i < howManyToShow; ++i) {
	Attribute val = null;
	String formFieldName = (String) jspContext.getAttribute("formFieldNamePrefix");
	if (existing.size() > i)
		val = existing.get(i);
	if (val != null && val.getId() != null)
		formFieldName += ".existing[" + val.getId() + "]";
	else
		formFieldName += ".new[" + i + "]";
%>
        <openmrs_tag:singleCustomValue
            customValueDescriptor="${ attributeType }"
            formFieldName="<%= formFieldName %>"
            value="<%= val %>"/>
        <br/>
<% } %>
    </td>
</tr>