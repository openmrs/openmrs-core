<%@ include file="/WEB-INF/template/include.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

<c:forEach var="widget" items="${model.spec}">
	<c:choose>
		<c:when test="${widget.widget}">
			<c:if test="${widget.divId != null}">
				<div id="${widget.divId}">
			</c:if>
			<jsp:include page="${widget.widgetName}.widget" flush="true">
				<jsp:param name="arguments" value="${widget.args}"/>
			</jsp:include>
			<c:if test="${widget.divId != null}">
				</div>
			</c:if>
		</c:when>
		<c:otherwise>
			${widget.html}
		</c:otherwise>
	</c:choose>
</c:forEach>

<%@ include file="/WEB-INF/template/footer.jsp" %> 