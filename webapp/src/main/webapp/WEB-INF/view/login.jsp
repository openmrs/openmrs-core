<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:message var="pageTitle" code="login.title" scope="page"/>
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
	
	function goToPreviousPage(){
		<c:choose>
			<c:when test="${not empty refererUrl}">
			window.location= "${refererUrl}";
			</c:when>
			<c:otherwise>
			//unfortunately this can bring us back to the login page, probably should go to index page
			history.go(-1);
			</c:otherwise>
		</c:choose>
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
		<span id="successMsg" style="display: none"><openmrs:message code="general.alertSent" /></span>
		<span id="failureMsg" class="error" style="display: none"><openmrs:message code="error.failedToSendRequest" /></span>
	</div></c:if>
	
	<br />
	<openmrs:message code="general.unableToViewPage" />
	<c:if test="${not empty reason}">
		<br />
		<span>${reason}</span>
	</c:if>
	
	<br /><br />
	<openmrs:message code="general.accountHasNoPrivilege" />
	<br /><br />
	<input type="button" value='<openmrs:message code="general.back" />' onclick="goToPreviousPage()" />
	
	<c:if test="${not empty alertMessage}">
	<div id="sendAlertDetails">
	<br /><br />
	<openmrs:message code="general.sendAlertToAdminMessage" />
		<br /><br />
		<input id="notificationButton" type="button" value="<openmrs:message code="general.alertSystemAdmin" />" onclick="createAlert()" />
	</div>
	</c:if>
	
	<br /><br />
	<div>
		<openmrs:message code="general.loginWithAnotherAccountMessage" />
		<br /><br />
		<a href="#" onclick="javascript:$j('#loginPortlet').show()">
			<openmrs:message code="general.loginWithAnotherAccount" />
		</a>
	</div>
</c:if>

<span id="loginPortlet"	<c:if test="${foundMissingPrivileges == true}">style='display:none'</c:if>>
	<openmrs:portlet url="login" />
</span>

<%@ include file="/WEB-INF/template/footer.jsp"%>
