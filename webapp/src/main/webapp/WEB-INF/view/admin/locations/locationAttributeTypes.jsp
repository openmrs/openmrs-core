<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Location Attribute Types" otherwise="/login.htm" redirect="/admin/locations/locationAttributeTypes.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="LocationAttributeType.manage.title"/></h2>	

<a href="locationAttributeType.form"><spring:message code="LocationAttributeType.add"/></a> 

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationAttributeTypes.afterAdd" type="html" />

<br />
<br />

<b class="boxHeader"><spring:message code="LocationAttributeType.list.title"/></b>
<div class="box">
	<c:choose>
		<c:when test="${ not empty attributeTypes }">
			<table>
				<tr>
					<th> <spring:message code="general.name" /> </th>
					<th> <spring:message code="general.description" /> </th>
				</tr>
				<c:forEach var="attrType" items="${ attributeTypes }">
					<tr>
						<td valign="top">
							<a href="locationAttributeType.form?id=${ attrType.id }">
								<c:choose>
									<c:when test="${ attrType.retired }">
										<del><c:out value="${ attrType.name }" escapeXml="true" /></del>
									</c:when>
									<c:otherwise>
										<c:out value="${ attrType.name }" escapeXml="true" />
									</c:otherwise>
								</c:choose>
							</a>
						</td>
						<td valign="top"><c:out value="${ attrType.description }" escapeXml="true" /></td>
					</tr>
				</c:forEach>
			</table>
		</c:when>
		<c:otherwise>
			<spring:message code="general.none" />
		</c:otherwise>
	</c:choose>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationAttributeTypes.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>