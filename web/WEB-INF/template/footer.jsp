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
		%>
		
		<a href="?${qs}&lang=en">English</a> |
		<a href="?${qs}&lang=fr">Français</a> |
		<a href="?${qs}&lang=de">Deutsch</a>
		
	</div>

</body>
</html>