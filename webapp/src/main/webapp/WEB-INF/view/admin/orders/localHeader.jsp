<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Orders,Add Orders,Edit Orders,Delete Orders,View Orders">
		<li <c:if test='<%= request.getRequestURI().contains("orders/orderList.jsp") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/order.list">
				<spring:message code="Order.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Orders,Add Orders,Edit Orders,Delete Orders,View Orders">
		<li <c:if test='<%= request.getRequestURI().contains("orderListByPatient") || request.getRequestURI().contains("choosePatient") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/orderDrug.list">
				<spring:message code="Order.drug.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Order Types">
		<li <c:if test='<%= request.getRequestURI().contains("orderType") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/orderType.list">
				<spring:message code="OrderType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.orders.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>