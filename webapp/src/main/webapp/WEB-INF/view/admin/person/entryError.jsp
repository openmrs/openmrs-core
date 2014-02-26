<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Add People" otherwise="/login.htm" redirect="/admin/person/addPerson.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>

		<h2><openmrs:message code="${errorTitle}"/></h2>
		<b id="detail"><openmrs:message code="${errorMessage}"/></b>
		
		<br/><br/>
		
		<br/>
		<input type="hidden" name="personId" id="personId" />
		<input type="hidden" name="personType" value="${param.personType}" />
		<input type="hidden" name="viewType" value="${param.viewType}" />
		
		<input type="button" value='<openmrs:message code="general.back"/>' onClick="history.go(-1)" />
		
		<br/><br/>
		
<%@ include file="/WEB-INF/template/footer.jsp" %>
