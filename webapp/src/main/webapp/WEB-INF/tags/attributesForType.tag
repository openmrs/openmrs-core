<%@tag import="java.util.List"%>
<%@tag import="java.util.ArrayList"%>
<%@tag import="org.openmrs.attribute.Attribute"%>

<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldNamePrefix" required="true" type="java.lang.String" %>
<%@ attribute name="attributeType" required="true" type="org.openmrs.attribute.AttributeType" %>
<%@ attribute name="customizable" required="true" type="org.openmrs.customdatatype.Customizable" %>
<%
List<Attribute> existing = customizable.getActiveAttributes(attributeType);
List<Integer> excludedAttributesIndexes = new ArrayList<Integer>();
Boolean notEmptyRetired = false;
int howManyToShow = attributeType.getMaxOccurs() == null ? 1 : attributeType.getMaxOccurs();
howManyToShow = Math.max(howManyToShow, existing.size() );
if (attributeType.isRetired()) {
    Attribute val = null;
    for(int i = 0; i < existing.size(); i++) {
            val = existing.get(i);
            if (val != null && val.getId() != null && !val.getValueReference().trim().isEmpty()) {
                notEmptyRetired = true;
            }
            else {
                excludedAttributesIndexes.add(i);
            }
    }
    for (int i = 0; i < excludedAttributesIndexes.size(); i++) {
        existing.remove(excludedAttributesIndexes.get(i));
    }
    if (notEmptyRetired) {
        howManyToShow = existing.size();
    }
    else {
        howManyToShow = 0;
    }
}
%>
<tr>
     <c:choose>
        <c:when test="${ attributeType.retired }" >
            <c:if test= "<%= notEmptyRetired %>" >
                <th><del><c:out value="${ attributeType.name }"/></del></th>
            </c:if>
        </c:when>
        <c:otherwise>
            <th><c:out value="${ attributeType.name }"/></th>
        </c:otherwise>
     </c:choose>
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