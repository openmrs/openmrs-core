<%@ include file="/WEB-INF/template/include.jsp" %>

<html>
<body bgcolor="#DDECFE" topmargin="0" leftmargin="0">
	
	<script type="text/javascript">
			
		function getSessionId() {
		  // Reference to InfoPath document
		  var oXDocument = window.external.Window.XDocument;
		  
		  // Reference to XML DOM in InfoPath's active window
		  var oDOM = oXDocument.DOM;
		  
		  // return session identifier
		  return oDOM.selectSingleNode('/form/misc/session').text;
		}
		
	</script>
			
	<c:if test="<%= request.getParameter("session") == null %>">
		 
		<!-- This is the first loading of the page, get the session id and post it back -->
		
		<form id="bootstrap_form" method="POST">
			<input id="session" name="session" type="text" value="">
		</form>
		
		<script type="text/javascript">
			document.getElementById("session").value = getSessionId();
			document.getElementById("bootstrap_form").submit();
		</script>
	
	</c:if>
	
	<c:if test="<%= request.getParameter("session") != null %>">

		<!-- The session id has been posted back, save it to the session cookie -->
		
		<response:addCookie name="JSESSIONID">
			<response:value><%= request.getParameter("session") %></response:value>
		</response:addCookie>
		<!-- <%= request.getParameter("session") %> -->

	</c:if>

</body>
</html>