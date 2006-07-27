<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<br/>

<center>
	<img src="/openmrs/images/ribbon.jpg">
	
	<openmrs:portlet url="welcome" parameters="showName=true|showLogin=true" />
</center>

<br />

<%@ include file="/WEB-INF/template/footer.jsp" %> 