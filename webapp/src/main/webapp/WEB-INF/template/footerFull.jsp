		<br/>
		</div>
	</div>

	<div id="footer">
		<div id="footerInner">
		
			<span id="localeOptions">
				<%  //removes last instance of lang= from querystring and encodes the url to avoid xml problems
					String qs = org.apache.commons.lang.StringEscapeUtils.escapeXml(request.getQueryString());
					if (qs == null)
						qs = "";
					int i = qs.lastIndexOf("&amp;lang=");
					if (i == -1)
						i = qs.length();
					pageContext.setAttribute("qs", qs.substring(0, i));
					pageContext.setAttribute("locales", org.openmrs.api.context.Context.getAdministrationService().getPresentationLocales());
					pageContext.setAttribute("openmrsVersion", org.openmrs.util.OpenmrsConstants.OPENMRS_VERSION);
					pageContext.setAttribute("locale", org.openmrs.api.context.Context.getLocale());
				%>
		
				<c:forEach items="${locales}" var="loc" varStatus="status">
					<%
						java.util.Locale locTmp = (java.util.Locale) pageContext.getAttribute("loc");
						pageContext.setAttribute("locDisplayName", locTmp.getDisplayName(locTmp));
					%>
					<c:if test="${status.index != 0}">| </c:if>
					<c:if test="${fn:toLowerCase(locale) == fn:toLowerCase(loc)}">${locDisplayName}</c:if>
					<c:if test="${fn:toLowerCase(locale) != fn:toLowerCase(loc)}"><a href="?${qs}&amp;lang=${loc}">${locDisplayName}</a></c:if> 
				</c:forEach>
			</span>	
	
			<span id="buildDate"><spring:message code="footer.lastBuild"/>: <%= org.openmrs.web.WebConstants.BUILD_TIMESTAMP %></span>
			
			<span id="codeVersion"><spring:message code="footer.version"/>: ${openmrsVersion}</span>
			
			<span id="poweredBy"><a href="http://openmrs.org"><spring:message code="footer.poweredBy"/> <img border="0" align="top" src="<%= request.getContextPath() %>/images/openmrs_logo_tiny.png"/></a></span>
		</div>
	</div>

</body>
</html>