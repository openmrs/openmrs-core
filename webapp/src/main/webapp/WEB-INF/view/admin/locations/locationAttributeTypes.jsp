<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Location Attribute Types" otherwise="/login.htm" redirect="/admin/locations/locationAttributeTypes.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="LocationAttributeType.manage.title"/></h2>	

<a href="locationAttributeType.form"><openmrs:message code="LocationAttributeType.add"/></a> 

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationAttributeTypes.afterAdd" type="html" />

<br />
<br />

<b class="boxHeader"><openmrs:message code="LocationAttributeType.list.title"/></b>
<div class="box">
	<c:choose>
		<c:when test="${ not empty attributeTypes }">
			<table>
				<tr>
					<th> <openmrs:message code="general.name" /> </th>
					<th> <openmrs:message code="general.description" /> </th>
				</tr>
				<c:forEach var="attrType" items="${ attributeTypes }">
					<tr>
						<td valign="top">
							<a href="locationAttributeType.form?id=${ attrType.id }">
								<c:choose>
									<c:when test="${ attrType.retired }">
										<del><c:out value="${ attrType.name }"/></del>
									</c:when>
									<c:otherwise>
										<c:out value="${ attrType.name }"/>
									</c:otherwise>
								</c:choose>
							</a>
						</td>
						<td valign="top"><c:out value="${ attrType.description }"/></td>
					</tr>
				</c:forEach>
			</table>
		</c:when>
		<c:otherwise>
			<openmrs:message code="general.none" />
		</c:otherwise>
	</c:choose>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.locationAttributeTypes.footer" type="html" />

<%@ include file="/WEB-INF/template/footer.jsp" %>