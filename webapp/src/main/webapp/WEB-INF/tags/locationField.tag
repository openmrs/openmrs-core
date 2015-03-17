<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="formFieldName" required="true" %>
<%@ attribute name="initialValue" required="false" type="java.lang.Object" %>
<%@ attribute name="optionHeader" required="false" %>
<%@ attribute name="onChange" required="false" %>
<%@ attribute name="selectableTags" required="false" type="java.lang.String"%>

<%@ tag import="org.openmrs.api.context.Context" %>

<% /* Convert initialValue to a Location. We need to support String/Integer for legacy reasons. */
if (jspContext.getAttribute("initialValue") != null) {
	Object initialValue = jspContext.getAttribute("initialValue");
	if (initialValue instanceof String && !"".equals(initialValue)) {
		jspContext.setAttribute("initialValue", Context.getLocationService().getLocation(Integer.valueOf((String) initialValue)));
	} else if (initialValue instanceof Integer) {
		jspContext.setAttribute("initialValue", Context.getLocationService().getLocation((Integer) initialValue));
	}
}
%>

<openmrs:globalProperty var="locationFieldStyle" key="location.field.style" defaultValue="default"/>

<c:choose>
	<c:when test="${locationFieldStyle == 'tree'}">
		<openmrs_tag:locationTree formFieldName="${formFieldName}" initialValue="${initialValue}" selectableTags="${selectableTags}"/>
	</c:when>
	<c:otherwise>
		<select name="${formFieldName}" id="${formFieldName}"<c:if test="${not empty onChange}"> onChange=${onChange}</c:if>>
			<c:if test="${optionHeader != ''}">
				<c:if test="${optionHeader == '[blank]'}">
					<option value=""></option>
				</c:if>
				<c:if test="${optionHeader != '[blank]'}">
					<option value="">${optionHeader}</option>
				</c:if>
			</c:if>
			<openmrs:forEachRecord name="locationHierarchy">
				<c:if test="${!record.location.retired }">
					<option value="${record.location.locationId}" <c:if test="${record.location == initialValue}">selected</c:if>>
						<c:forEach begin="1" end="${record.depth}">&nbsp;&nbsp;</c:forEach><c:out value="${record.location.name}"/>
					</option>
				</c:if>
			</openmrs:forEachRecord>
			<c:set var="anyRetired" value="false"/>
			<openmrs:forEachRecord name="locationHierarchy">
			<c:if test="${record.location.retired }">
				<c:if test= "${ !anyRetired }" >
				 <c:set var="anyRetired" value="true" />
					<optgroup label="<openmrs:message code="Location.retiredList"/>">
				</c:if>
				<option value="${record.location.locationId}" <c:if test="${record.location == initialValue}">selected</c:if>>
					<c:forEach begin="1" end="${record.depth}">&nbsp;&nbsp;</c:forEach><c:out value="${record.location.name}"/>
				</option>
			</c:if>
			</openmrs:forEachRecord>
			<c:if test= "${anyRetired}" >
				</optgroup>
			</c:if>
		</select>
	</c:otherwise>
</c:choose>