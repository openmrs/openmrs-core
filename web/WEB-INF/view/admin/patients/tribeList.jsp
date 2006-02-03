<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Tribes" otherwise="/login.htm" redirect="/admin/patients/tribe.list" />
	
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="Tribe.manage.title"/></h2>	

<a href="tribe.form"><spring:message code="Tribe.add"/></a>

<br /><br />

<b class="boxHeader"><spring:message code="Tribe.list.title"/></b>
<form method="post" class="box">
	<table>
		<tr>
			<th> </th>
			<th> <spring:message code="Tribe.name"/> </th>
		</tr>
		<c:forEach var="tribe" items="${tribeList}">
			<tr>
				<td><input type="checkbox" name="tribeId" value="${tribe.tribeId}"></td>
				<td><a href="tribe.form?tribeId=${tribe.tribeId}" 
					   class="<c:if test="${tribe.retired == true}">retired</c:if>">
					   ${tribe.name}
					</a>
				</td>
			</tr>
		</c:forEach>
	</table>
	<input type="submit" value="<spring:message code="Tribe.retire"/>" name="retire">
	<input type="submit" value="<spring:message code="Tribe.unretire"/>" name="unretire">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>