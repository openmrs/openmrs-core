<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page import="org.openmrs.web.WebConstants" %>
<%
	pageContext.setAttribute("msg", session.getAttribute(WebConstants.OPENMRS_MSG_ATTR));
	pageContext.setAttribute("msgArgs", session.getAttribute(WebConstants.OPENMRS_MSG_ARGS));
	pageContext.setAttribute("err", session.getAttribute(WebConstants.OPENMRS_ERROR_ATTR));
	pageContext.setAttribute("errArgs", session.getAttribute(WebConstants.OPENMRS_ERROR_ARGS));
	session.removeAttribute(WebConstants.OPENMRS_MSG_ATTR);
	session.removeAttribute(WebConstants.OPENMRS_MSG_ARGS);
	session.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
	session.removeAttribute(WebConstants.OPENMRS_ERROR_ARGS);
%>

<html xmlns="http://www.w3.org/1999/xhtml" 
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:spring="http://www.springframework.org/tags"
	xmlns:openmrs="urn:jsptld:/WEB-INF/taglibs/openmrs.tld">
	<head>
		<openmrs:htmlInclude file="/openmrs.js" />
		<openmrs:htmlInclude file="/scripts/openmrsmessages.js" appendLocale="true" />
		<openmrs:htmlInclude file="/openmrs.css" />
		<link href="<openmrs:contextPath/><spring:theme code='stylesheet' />" type="text/css" rel="stylesheet" />
		<openmrs:htmlInclude file="/style.css" />
		<openmrs:htmlInclude file="/dwr/engine.js" />
        <openmrs:htmlInclude file="/scripts/html-sanitizer-min.js" />
		<openmrs:htmlInclude file="/dwr/interface/DWRAlertService.js" />
		<c:if test="${empty DO_NOT_INCLUDE_JQUERY}">
			<openmrs:htmlInclude file="/scripts/jquery/jquery.min.js" />
			<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
            <openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-timepicker-addon.js" />
			<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-datepicker-i18n.js" />
			<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-timepicker-i18n.js" />
			<link href="<openmrs:contextPath/>/scripts/jquery-ui/css/<spring:theme code='jqueryui.theme.name' />/jquery-ui.custom.css" type="text/css" rel="stylesheet" />
		</c:if>
		<link rel="shortcut icon" type="image/ico" href="<openmrs:contextPath/><spring:theme code='favicon' />">
		<link rel="icon" type="image/png" href="<openmrs:contextPath/><spring:theme code='favicon.png' />">

		<c:choose>
			<c:when test="${!empty pageTitle}">
				<title>${pageTitle}</title>
			</c:when>
			<c:otherwise>
				<title><openmrs:message code="openmrs.title"/></title>
			</c:otherwise>
		</c:choose>


		<script type="text/javascript">
			<c:if test="${empty DO_NOT_INCLUDE_JQUERY}">
				var $j = jQuery.noConflict();
			</c:if>
			/* variable used in js to know the context path */
			var openmrsContextPath = '${pageContext.request.contextPath}';
			var dwrLoadingMessage = '<openmrs:message code="general.loading" />';
			var jsDateFormat = '<openmrs:datePattern localize="false"/>';
			var jsTimeFormat = '<openmrs:timePattern format="jquery" localize="false"/>';
			var jsLocale = '<%= org.openmrs.api.context.Context.getLocale() %>';
			
			/* prevents users getting false dwr errors msgs when leaving pages */
			var pageIsExiting = false;
			if (typeof(jQuery) != "undefined")
			    jQuery(window).bind('beforeunload', function () { pageIsExiting = true; } );
			
			var handler = function(msg, ex) {
				if (!pageIsExiting) {
					var div = document.getElementById("openmrs_dwr_error");
					div.style.display = ""; // show the error div
					var msgDiv = document.getElementById("openmrs_dwr_error_msg");
					msgDiv.innerHTML = '<openmrs:message code="error.dwr"/>' + " <b>" + msg + "</b>";
				}
				
			};
			dwr.engine.setErrorHandler(handler);
			dwr.engine.setWarningHandler(handler);
		</script>

		<openmrs:extensionPoint pointId="org.openmrs.headerFullIncludeExt" type="html" requiredClass="org.openmrs.module.web.extension.HeaderIncludeExt">
			<c:forEach var="file" items="${extension.headerFiles}">
				<openmrs:htmlInclude file="${file}" />
			</c:forEach>
		</openmrs:extensionPoint>

	</head>

<body>
	<div id="pageBody">
        
		<div id="userBar">
			<openmrs:authentication>
				<c:if test="${authenticatedUser != null}">
					<span id="userLoggedInAs" class="firstChild">
						<openmrs:message code="header.logged.in"/> <c:out value="${authenticatedUser.personName}" />
					</span>
					<span id="userLogout">
						<a href='${pageContext.request.contextPath}/logout'><openmrs:message code="header.logout" /></a>
					</span>
					<span>
						<a href="${pageContext.request.contextPath}/options.form"><openmrs:message code="Navigation.options"/></a>
					</span>
				</c:if>
				<c:if test="${authenticatedUser == null}">
					<span id="userLoggedOut" class="firstChild">
						<openmrs:message code="header.logged.out"/>
					</span>
					<span id="userLogIn">
						<a href='${pageContext.request.contextPath}/login.htm'><openmrs:message code="header.login"/></a>
					</span>
				</c:if>
			</openmrs:authentication>

			<span id="userHelp">
				<a href='<%= request.getContextPath() %>/help.htm'><openmrs:message code="header.help"/></a>
			</span>
			<openmrs:extensionPoint pointId="org.openmrs.headerFull.userBar" type="html">
				<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
					<span>
						<a href="<c:url value="${extension.url}" />"><openmrs:message code="${extension.label}"/></a>
					</span>
					<c:if test="${extension.portletUrl != null}">
						<openmrs:portlet url="${extension.portletUrl}" moduleId="${extension.moduleId}" id="${extension.portletUrl}" />
					</c:if>
				</openmrs:hasPrivilege>
			</openmrs:extensionPoint>
		</div>

		<%@ include file="/WEB-INF/template/banner.jsp" %>

		<%-- This is where the My Patients popup used to be. I'm leaving this placeholder here
			as a reminder of where to put back an extension point when I've figured out what it should
			look like. -DJ
		<div id="popupTray">
		</div>
		--%>

		<div id="content">

			<openmrs:forEachAlert>
				<c:if test="${varStatus.first}"><div id="alertOuterBox"></c:if>
				<c:if test="${varStatus.last}">
					<div id="alertBar">
						<img src="${pageContext.request.contextPath}/images/alert.gif" align="center" alt='<openmrs:message htmlEscape="false" code="Alert.unreadAlert"/>' title='<openmrs:message htmlEscape="false" code="Alert.unreadAlert"/>'/>
						<c:if test="${varStatus.count == 1}"><openmrs:message htmlEscape="false" code="Alert.unreadAlert"/></c:if>
						<c:if test="${varStatus.count != 1}"><openmrs:message htmlEscape="false" code="Alert.unreadAlerts" arguments="${varStatus.count}" /></c:if>
						<c:if test="${alert.satisfiedByAny}"><i class="smallMessage">(<openmrs:message code="Alert.mark.satisfiedByAny"/>)</i></c:if>
						<a href="#markAllAsRead" onclick="return markAllAlertsRead(this)" HIDEFOCUS class="markAllAsRead" >
							<img src="${pageContext.request.contextPath}/images/markRead.gif" alt='<openmrs:message code="Alert.markAllAsRead"/>' title='<openmrs:message code="Alert.markAllAlertsAsRead"/>' /> <span class="markAllAsRead"><openmrs:message code="Alert.markAllAsRead"/></span>
						</a>
					</div>
				</c:if>
			</openmrs:forEachAlert>
			<openmrs:forEachAlert>
				<c:if test="${varStatus.first}"><div id="alertInnerBox"></c:if>
					<div class="alert">
						<a href="#markRead" onClick="return markAlertRead(this, '${alert.alertId}')" HIDEFOCUS class="markAlertRead">
							<img src="${pageContext.request.contextPath}/images/markRead.gif" alt='<openmrs:message code="Alert.mark"/>' title='<openmrs:message code="Alert.mark"/>'/> <span class="markAlertText"><openmrs:message code="Alert.markAsRead"/></span>
						</a>
						${alert.text} ${alert.dateToExpire} 
					</div>
				<c:if test="${varStatus.last}">
					</div>
					</div>
				</c:if>
			</openmrs:forEachAlert>

			<c:if test="${msg != null}">
				<div id="openmrs_msg"><openmrs:message code="${msg}" text="${msg}" arguments="${msgArgs}"  htmlEscape="false" /></div>
			</c:if>
			<c:if test="${err != null}">
				<div id="openmrs_error"><openmrs:message code="${err}" text="${err}" arguments="${errArgs}" htmlEscape="false"/></div>
			</c:if>
			<div id="openmrs_dwr_error" style="display:none" class="error">
				<div id="openmrs_dwr_error_msg"></div>
				<div id="openmrs_dwr_error_close" class="smallMessage">
					<i><openmrs:message code="error.dwr.stacktrace"/></i> 
					<a href="#" onclick="this.parentNode.parentNode.style.display='none'"><openmrs:message code="error.dwr.hide"/></a>
				</div>
			</div>
			
