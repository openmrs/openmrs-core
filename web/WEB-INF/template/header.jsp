<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page import="org.openmrs.web.WebConstants" %>
<%
	pageContext.setAttribute("msg", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR)); 
	pageContext.setAttribute("err", session.getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
	session.removeAttribute(WebConstants.OPENMRS_MSG_ATTR);
	session.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR); 
	if (request.getRequestURI().contains("taskpane") || 
	   (session.getAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR)!=null && 
	   ((String)session.getAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR)).contains("taskpane"))){
	   pageContext.setAttribute("taskpane", new Boolean(true));
	}
%>

<html>
	<head>
		<link href="<%= request.getContextPath() %>/openmrs.css" type="text/css" rel="stylesheet" />
		<link href="<%= request.getContextPath() %>/style.css" type="text/css" rel="stylesheet" />
		
		<c:choose>
			<c:when test="${taskpane}">
				<link href="<%= request.getContextPath() %>/formentry/taskpane/taskpane.css" type="text/css" rel="stylesheet" />
				<script src="<%= request.getContextPath() %>/formentry/taskpane/taskpane.js"></script>
				<meta http-equiv="msthemecompatible" content="yes" />
				<meta http-equiv="pragma" content="no-cache" />
				<meta http-equiv="expires" content="-1" />
			</c:when>
			<c:otherwise>
				<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
				<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRAlertService.js'></script>
				<script src="<%= request.getContextPath() %>/openmrs.js"></script>
			</c:otherwise>
		</c:choose>
	</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
	<div id="pageBody">
		<div id="userBar">
			<openmrs:authentication>
				<c:if test="${authenticatedUser != null}">
					<spring:message code="header.logged.in"/> ${authenticatedUser.firstName} ${authenticatedUser.lastName} | 
					<a href='<%= request.getContextPath() %>/logout'>
						<spring:message code="header.logout" />
					</a>
				</c:if>
				<c:if test="${authenticatedUser == null}">
					<spring:message code="header.logged.out"/> | 
					<a href='<%= request.getContextPath() %>/login.htm'>
						<spring:message code="header.login"/>
					</a>
				</c:if>
			</openmrs:authentication>
			| <a href='<%= request.getContextPath() %>/help.htm'><spring:message code="header.help"/></a>
		</div>

		<div id="banner">
			<%@ include file="/WEB-INF/template/banner.jsp" %>
		</div>
		
		<openmrs:hasPrivilege privilege="View Navigation Menu">
			<div id="gutter">
				<%@ include file="/WEB-INF/template/gutter.jsp" %>
			</div>
		</openmrs:hasPrivilege>
		
		<div id="content">

			<c:if test="${!taskpane}">
				<openmrs:forEachAlert>
					<c:if test="${varStatus.first}"><div id="alertOuterBox"><div id="alertInnerBox"></c:if>
						<div class="alert">
							<a href="#markRead" onClick="return markAlertRead(this, '${alert.alertId}')" HIDEFOCUS class="markAlertRead">
								<img src="<%= request.getContextPath() %>/images/markRead.gif" alt='<spring:message code="Alert.mark"/>' title='<spring:message code="Alert.mark"/>'/>
							</a>
							${alert.text} ${alert.dateToExpire} <c:if test="${alert.satisfiedByAny}"><i class="smallMessage">(<spring:message code="Alert.mark.satisfiedByAny"/>)</i></c:if>
						</div>
					<c:if test="${varStatus.last}">
						</div>
						<div id="alertBar">
							<img src="<%= request.getContextPath() %>/images/alert.gif" align="center"/>
							<c:if test="${varStatus.count == 1}"><spring:message code="Alert.unreadAlert"/></c:if>
							<c:if test="${varStatus.count != 1}"><spring:message code="Alert.unreadAlerts" arguments="${varStatus.count}" /></c:if>
						</div>
						</div>
					</c:if>
				</openmrs:forEachAlert>
			</c:if>

			<c:if test="${msg != null}">
				<div id="openmrs_msg"><spring:message code="${msg}" text="${msg}"/></div>
			</c:if>
			<c:if test="${err != null}">
				<div id="openmrs_error"><spring:message code="${err}" text="${err}"/></div>
			</c:if>