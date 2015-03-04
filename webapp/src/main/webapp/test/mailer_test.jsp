<%@page import="javax.naming.Context" %>
<%@page import="javax.naming.InitialContext" %>
<%@page import="javax.mail.Session" %>
<%@page import="javax.mail.Message" %>
<%@page import="javax.mail.Transport" %>
<%@page import="javax.mail.internet.InternetAddress" %>
<%@page import="javax.mail.internet.MimeMessage" %>
<%@page import="java.util.Date" %>


<%

	String from = request.getParameter("from");
	from = ( from != null ) ? from : "justin.miranda@yahoo.com";

	String recipients = request.getParameter("to");
	recipients = (recipients != null) ? recipients : "justin.miranda@gmail.com";
	
	String subject = request.getParameter("subject");
	subject = (subject != null) ? subject : "Testing";
	
	String content = request.getParameter("content");
	content = (content != null) ? content : "Testing email capabilities. \n\n" + ( new Date() ).toString();

	String errorMessage = null;
	
	if ( "POST".equalsIgnoreCase( request.getMethod() ) ) { 
		Context ctx = new InitialContext();
		Object obj = ctx.lookup("java:comp/env/mail/OpenmrsMailSession");
		javax.mail.Session mailSession = (javax.mail.Session) obj;
		if ( mailSession != null ) { 	
			try { 
				Message mimeMessage = new MimeMessage(mailSession);
				mimeMessage.setFrom(new InternetAddress( from ));
				mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse( recipients, false ));
				mimeMessage.setSubject( subject );
				mimeMessage.setContent( content, "text/plain");
				Transport.send(mimeMessage);		
				errorMessage = "Successfully sent message to " + recipients; 
			} catch(Exception e) { 
				e.printStackTrace();
				errorMessage = e.getMessage();
			}
		} else { 
			
		}
	}
%>


<p>
This JSP tests the mail capabilities of the application to make sure that the mail session and 
mail server have been configured correctly.
</p>
<br/>
<%= ( errorMessage != null ) ? errorMessage : "" %><br/>

<form method="post">
<table>
	<tr>
		<td>From:</td>
		<td><input name="from" type="text" size="50" value="<%= from %>"/></td>
	</tr>
	<tr>	
		<td>To: </td>
		<td><input name="to" type="text" size="50" value="<%= recipients %>"/></td>
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
</table>
</form>




