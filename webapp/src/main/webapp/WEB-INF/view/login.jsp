<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="login.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<c:if test="${foundMissingPrivileges == true}">
	<br />
	<div>
		<a href="#" onclick="javascript:$j('#loginPortlet').show()">
			<spring:message code="general.authentication.loginWithAnotherAccount" />
		</a>
	</div>
</c:if>

<span id="loginPortlet"	<c:if test="${foundMissingPrivileges == true}">style='display:none'</c:if>>
	<openmrs:portlet url="login" />
</span>

<%@ include file="/WEB-INF/template/footer.jsp"%>