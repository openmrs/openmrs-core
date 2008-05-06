<%@ page errorPage="/errorhandler.jsp" %>
<%@ page import="org.openmrs.web.WebConstants" %>
<%
	String useMinimalHeader = (String)session.getAttribute(WebConstants.OPENMRS_HEADER_USE_MINIMAL);
	if ("true".equals(useMinimalHeader)){
	   pageContext.setAttribute("useMinimalHeader", new Boolean(true));
	   session.removeAttribute(WebConstants.OPENMRS_HEADER_USE_MINIMAL);
	}
%>

<c:choose>
	<c:when test="${useMinimalHeader}">
		<%@ include file="headerMinimal.jsp" %>
	</c:when>
	<c:otherwise>
		<%@ include file="headerFull.jsp" %>
	</c:otherwise>
</c:choose>
