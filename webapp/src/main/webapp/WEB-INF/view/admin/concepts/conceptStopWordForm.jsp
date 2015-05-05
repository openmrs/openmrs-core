<%@page import="java.util.Locale" %>
<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Concept Stop Words" otherwise="/login.htm"
                 redirect="/admin/concepts/conceptStopWord.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="ConceptStopWord.title"/></h2>

<spring:hasBindErrors name="command">
    <div class="error"><openmrs:message code="fix.error"/></div><br/>
</spring:hasBindErrors>

<form method="post">
    <table>
        <tr>
            <td><openmrs:message code="general.name"/><span class="required">*</span></td>
            <td>
                <spring:bind path="command.value">
                    <input type="text" name="${status.expression}" value="<c:out value="${status.value}" />" size="35"/>
                    <c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
                </spring:bind>
            </td>
            <td>
                <spring:bind path="command.locale">
                    <select name="${status.expression}" id="${status.expression}">
                        <c:forEach items="${locales}" var="locale">
                            <option value="${locale}">${locale.displayName}</option>
                        </c:forEach>
                    </select>
                </spring:bind>

            </td>
        </tr>
    </table>
    <br/>
    <input type="submit" value="<openmrs:message code="ConceptStopWord.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
