<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><openmrs:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Orders,Add Orders,Edit Orders,Delete Orders,View Orders">
		<li <c:if test='<%= request.getRequestURI().contains("orders/orderList.jsp") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/order.list">
				<openmrs:message code="Order.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Orders,Add Orders,Edit Orders,Delete Orders,View Orders">
		<li <c:if test='<%= request.getRequestURI().contains("orderDrug") || request.getRequestURI().contains("orderListByPatient") %>'>class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/orderDrug.list">
				<openmrs:message code="Order.drug.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:extensionPoint pointId="org.openmrs.admin.orders.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="<openmrs_tag:url value="${link.key}"/>"><openmrs:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>