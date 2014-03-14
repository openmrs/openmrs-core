<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ attribute name="errors" required="true" type="org.springframework.validation.Errors" %>

<openmrs:message code="fix.error"/>
<div class="error">
    <c:forEach items="${errors.allErrors}" var="error">
        <openmrs:message code="${error.code}" text="${error.code}"/><br/>
    </c:forEach>
</div>
<br />
