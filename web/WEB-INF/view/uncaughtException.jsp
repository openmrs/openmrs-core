<%@ include file="/WEB-INF/template/includes.jsp" %>

<%@ include file="/WEB-INF/template/header.jsp" %>

&nbsp;<br />

<h2>Internal error</h2>

<script>
	function showOrHide() {
		var link = document.getElementById("toggleLink");
		var trace = document.getElementById("staceTrace");
		if (link.innerHTML == "Show") {
			link.innerHTML = "Hide";
			trace.style.display = "block";
		}
		else {
			link.innerHTML = "Show";
			trace.style.display = "none";
		}
</script>	

<% 
try {
	// The Servlet spec guarantees this attribute will be available
	Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception"); 

	out.println(exception.getMessage()); %>
	
	<br />
	<div class="box">
		<div class="boxHeader">
			<a href="javascript:showOrHide()" id="toggleLink" >Show</a>
			Stack Trace
		</div>
		<div id="stackTrace">
	<%		
	if (exception != null) {
		if (exception instanceof ServletException) {
			// It's a ServletException: we should extract the root cause
			ServletException sEx = (ServletException) exception;
			Throwable rootCause = sEx.getRootCause();
			if (rootCause == null)
				rootCause = sEx;
			out.println("** Root cause is: "+ rootCause.getMessage());
			rootCause.printStackTrace(new java.io.PrintWriter(out)); 
		}
		else {
			// It's not a ServletException, so we'll just show it
			exception.printStackTrace(new java.io.PrintWriter(out)); 
		}
	} 
	else  {
    	out.println("No error information available");
	} 

	// Display cookies
	out.println("\nCookies:\n");
	Cookie[] cookies = request.getCookies();
	if (cookies != null) {
    	for (int i = 0; i < cookies.length; i++) {
      		out.println(cookies[i].getName() + "=[" + cookies[i].getValue() + "]");
		}
	}
	    
} catch (Exception ex) { 
	ex.printStackTrace(new java.io.PrintWriter(out));
}
%>
		</div> <!-- close stack trace box -->
	</div> <!-- close box -->

<%@ include file="/WEB-INF/template/footer.jsp" %>