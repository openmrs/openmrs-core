		<br/>
		</div>
	</div>

	<div id="footer">
		
		<%  //removes last instance of lang= from querystring
			String qs = request.getQueryString();
			if (qs == null)
				qs = "";
			int i = qs.lastIndexOf("&lang=");
			if (i == -1)
				i = qs.length();
			pageContext.setAttribute("qs", qs.substring(0, i));
			pageContext.setAttribute("locales", org.openmrs.web.WebConstants.OPENMRS_LOCALES());
			pageContext.setAttribute("context", session.getAttribute(org.openmrs.web.WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR));
		%>

		<c:forEach items="${locales}" var="loc" varStatus="status">		
			<c:if test="${status.index != 0}">| </c:if>
			<c:if test="${fn:toLowerCase(context.locale) == fn:toLowerCase(loc)}">${loc.displayName}</c:if>
			<c:if test="${fn:toLowerCase(context.locale) != fn:toLowerCase(loc)}"><a href="?${qs}&lang=${loc}">${loc.displayName}</a></c:if> 
		</c:forEach>
		&nbsp; &nbsp; &nbsp;

		Last Build: @TIMESTAMP@
	</div>

</body>
</html>