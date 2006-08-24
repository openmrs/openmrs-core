<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="Manage Orders,Add Orders,Edit Orders,Delete Orders,View Orders">
		<li <c:if test="<%= request.getRequestURI().contains("order.") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/order.list">
				<spring:message code="Order.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<openmrs:hasPrivilege privilege="Manage Orders,Add Orders,Edit Orders,Delete Orders,View Orders">
		<li <c:if test="<%= request.getRequestURI().contains("orderDrug.") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/orderDrug.list">
				<spring:message code="Order.drug.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	<%-- // MARKFORDELETION: THIS IS NO LONGER NEEDED
	<openmrs:hasPrivilege privilege="Add Orders,Edit Orders,Delete Orders,View Orders">
		<li <c:if test="<%= request.getRequestURI().contains("orderByUser.") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/orderByPatient.list">
				<spring:message code="Order.list.patient"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	--%>
	<openmrs:hasPrivilege privilege="Manage Order Types">
		<li <c:if test="<%= request.getRequestURI().contains("orderType") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/admin/orders/orderType.list">
				<spring:message code="OrderType.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
</ul>