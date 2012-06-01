<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="login.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<openmrs:htmlInclude file="/dwr/interface/DWRAlertService.js"/>


<script type="text/javascript">
	function createAlert(){
		DWRAlertService.createAlert("${alertMessage}", function(success){
			if(success)
				$j('#successMsg').show();
			else
				$j('#failureMsg').show();
				
			$j('#sendAlertDetails').hide();
			$j('#responseDiv').show();
		});
	}
</script>

<style>
	#successMsg{
		border: 1px dashed lightgrey;
   		margin-bottom: 5px;
    	margin-top: 3px;
    	padding: 2px 2px 2px 18px;
    	background: url("images/info.gif") no-repeat scroll left center lightyellow;
	}
</style>

<c:if test="${foundMissingPrivileges == true}">
	<c:if test="${not empty alertMessage}">
	<div id="responseDiv" style="display: none">
		<br />
		<span id="successMsg" style="display: none"><spring:message code="general.requestSent" /></span>
		<span id="failureMsg" class="error" style="display: none"><spring:message code="error.failedToSendRequest" /></span>
	</div>
	<div id="sendAlertDetails">
		<br />
		<input id="notificationButton" type="button" value="${buttonLabel}" onclick="createAlert()" />
	</div>
	</c:if>
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