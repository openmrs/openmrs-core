<%@ taglib prefix="c" uri="/WEB-INF/taglibs/c-rt.tld" %>
<%@ taglib prefix="openmrs" uri="/WEB-INF/taglibs/openmrs.tld" %>
<%@ taglib prefix="page" uri="/WEB-INF/taglibs/page.tld" %>
<%@ taglib prefix="request" uri="/WEB-INF/taglibs/request.tld" %>
<%@ taglib prefix="response" uri="/WEB-INF/taglibs/response.tld" %>
<%@ taglib prefix="spring" uri="/WEB-INF/taglibs/spring.tld" %>

<%@page import="org.openmrs.notification.Message" %>
<%@page import="java.util.Date" %>
<html>

<head>

</head>

<%	
	
	String sender = request.getParameter("sender");
	sender = ( sender != null ) ? sender : "justin.miranda@yahoo.com";

	String recipients = request.getParameter("recipients");
	recipients = (recipients != null) ? recipients : "justin.miranda@gmail.com";
	
	String subject = request.getParameter("subject");
	subject = (subject != null) ? subject : "Testing " + request.getRequestURI();
	
	String content = request.getParameter("content");
	content = (content != null) ? content : "Testing messaging capabilities on " + ( new Date() ).toString();

	String errorMessage = "no error";
	
	if ( "POST".equalsIgnoreCase( request.getMethod() ) ) { 		
		try { 						
			Message message = new Message();
			message.setSender( sender );
			message.setRecipients( recipients );
			message.setSubject( subject );
			message.setContent( content );
			message.setSentDate( new Date() );
		
			ContextFactory.getContext().getMessageService().send( message );
				
			errorMessage = "Successfully sent message to " + recipients; 
		
		} 
		catch(Exception e) { 
			System.out.println("yo");
			e.printStackTrace();
			errorMessage = "Email was not sent due to " + e.getMessage();
		}
	}
%>

<body>
<p>
	This JSP tests the message sending capabilities of the application to make 
	sure that the message sender and preparator are working correctly.
</p>
<br/>

<b><%= errorMessage %></b>

<form method="post">
<table>
	<tr>
		<td>From:</td>
		<td><input name="sender" type="text" size="50" value="<%= sender %>"/></td>
	</tr>
	<tr>	
		<td>To: </td>
		<td><input name="recipients" type="text" size="50" value="<%= recipients %>"/></td>
	</tr>
	<tr>
		<td>Subject:  </td>
		<td><input name="subject" type="text" size="50" value="<%= subject %>"/></td>
	</tr>
	<tr>
		<td></td>
		<td><textarea name="content" rows="5" cols="40"><%= content %></textarea></td>
	</tr>
	<tr>
	<td colspan="2" align="center"><input name="submit" type="submit" value="Send Message"/></td>
	</tr>

</form>


</body>

</html>

