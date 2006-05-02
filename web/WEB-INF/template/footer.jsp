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
			pageContext.setAttribute("locales", org.openmrs.util.OpenmrsConstants.OPENMRS_LOCALES());
			pageContext.setAttribute("openmrsVersion", org.openmrs.util.OpenmrsConstants.OPENMRS_VERSION);
			pageContext.setAttribute("databaseVersion", org.openmrs.util.OpenmrsConstants.DATABASE_VERSION);
			pageContext.setAttribute("databaseVersionExpected", org.openmrs.util.OpenmrsConstants.DATABASE_VERSION_EXPECTED);
			pageContext.setAttribute("context", session.getAttribute(org.openmrs.web.WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR));
		%>

		<c:forEach items="${locales}" var="loc" varStatus="status">		
			<c:if test="${status.index != 0}">| </c:if>
			<c:if test="${fn:toLowerCase(context.locale) == fn:toLowerCase(loc)}">${loc.displayName}</c:if>
			<c:if test="${fn:toLowerCase(context.locale) != fn:toLowerCase(loc)}"><a href="?${qs}&lang=${loc}">${loc.displayName}</a></c:if> 
		</c:forEach>
		&nbsp; &nbsp; &nbsp;

		<span id="build">Last Build: @TIMESTAMP@</span> &nbsp;
		
		<span id="codeVersion">Version: ${openmrsVersion}</span> &nbsp;

		<span id="databaseVersion">Database Version: ${databaseVersion}</span> &nbsp;
		
		<c:if test="${databaseVersionExpected != databaseVersion}">
			<span id="databaseVersionError"><img src="${pageContext.request.contextPath}/images/problem.gif" align="top"> Expected: ${databaseVersionExpected}</span>
		</c:if>
		
	</div>

</body>
</html>