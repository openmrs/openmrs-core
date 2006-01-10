<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page import="org.openmrs.web.WebConstants" %>
<%
	pageContext.setAttribute("msg", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR)); 
	pageContext.setAttribute("err", session.getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
	session.removeAttribute(WebConstants.OPENMRS_MSG_ATTR);
	session.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR); 
%>

<html>
	<head>
		<link href="<%= request.getContextPath() %>/openmrs.css" type="text/css" rel="stylesheet" />
		<link href="<%= request.getContextPath() %>/style.css" type="text/css" rel="stylesheet" />
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
		<div id="gutter">
			<%@ include file="/WEB-INF/template/gutter.jsp" %>
		</div>
		<div id="content">
	
				<c:if test="${msg != null}">
					<div id="openmrs_msg"><spring:message code="${msg}" text="${msg}"/></div>
				</c:if>
				<c:if test="${err != null}">
					<div id="openmrs_error"><spring:message code="${err}" text="${err}"/></div>
				</c:if>
	

					