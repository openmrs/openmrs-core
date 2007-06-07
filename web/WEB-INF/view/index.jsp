<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<br/>

<center>
	<img src="${pageContext.request.contextPath}/images/openmrs_logo_large.gif">
	
	<br/><br/><br/>
	
	<openmrs:portlet url="welcome" parameters="showName=true|showLogin=true" />
</center>

<br />

<%@ include file="/WEB-INF/template/footer.jsp" %> 