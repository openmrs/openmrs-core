<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="View Current Users" otherwise="/login.htm" redirect="/admin/maintenance/currentUsers.list" />
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ViewCurrentUsers.title"/></h2>

<openmrs:message code="ViewCurrentUsers.help" />
<br />
<br />

<table class="box">
    <tr>
        <th><openmrs:message code="ViewCurrentUsers.users" /></th>
    </tr>
    <c:if test="${not empty currentUsers}">
        <c:forEach var="username" varStatus="status" items="${currentUsers}">
            <tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
                <td>${username}</td>
            </tr>
        </c:forEach>
    </c:if>
</table>

<%@ include file="/WEB-INF/template/footer.jsp" %> 
