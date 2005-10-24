<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ page import="org.openmrs.context.Context" %>
<%@ page import="org.openmrs.api.AdministrationService" %>
<%@ page import="org.openmrs.api.OrderService" %>
<%@ page import="org.openmrs.api.APIException" %>
<%@ page import="org.openmrs.OrderType" %>
<%@ page import="org.openmrs.web.Constants" %>

<openmrs:require privilege="Manage Orders" otherwise="/login.jsp" />

<%
	Context context = (Context)session.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
	AdministrationService adminService = context.getAdministrationService();
	OrderService orderService = context.getOrderService();
	OrderType orderType = orderService.getOrderType(Integer.valueOf(request.getParameter("orderTypeId")));

	if (request.getParameter("name") != null) {
		orderType.setName(request.getParameter("name"));
		orderType.setDescription(request.getParameter("description"));
		try {
			adminService.updateOrderType(orderType);
			session.setAttribute(Constants.OPENMRS_MSG_ATTR, "Order Type updated");
			response.sendRedirect("orderTypes.jsp");
			return;
		}
		catch (APIException e) {
			session.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Unable to update order type " + e.getMessage());
		}
		
	}
	pageContext.setAttribute("orderType", orderType);
%>	

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader" %>

<br />
<h2>Editing Order Type</h2>

<form method="post">
<table>
	<tr>
		<td>Name</td>
		<td><input type="text" name="name" value="${orderType.name}" size="35" /></td>
	</tr>
	<tr>
		<td valign="top">Description</td>
		<td><textarea name="description" rows="3" cols="40">${orderType.description}</textarea></td>
	</tr>
	<tr>
		<td>Creator</td>
		<td>${orderType.creator}</td>
	</tr>
	<tr>
		<td>Date Created</td>
		<td>${orderType.dateCreated}</td>
	</tr>
</table>
<input type="hidden" name="orderTypeId" value="<c:out value="${orderType.orderTypeId}"/>">
<br />
<input type="submit" value="Save Order Type">
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>