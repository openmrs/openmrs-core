<%@page import="org.apache.velocity.Template" %>
<%@page import="org.apache.velocity.VelocityContext" %>
<%@page import="org.apache.velocity.app.VelocityEngine" %>
<%@page import="java.io.StringWriter" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.HashMap" %>
<%@page import="org.openmrs.notification.mail.velocity.VelocityMessagePreparator" %>
<%@page import="org.openmrs.notification.MessagePreparator" %>
<%@page import="org.openmrs.Patient" %>


<%
	
	String templateText = "hello $givenName $familyName!";
	if ( request.getParameter("templateText") != null ) { 
		templateText = 	request.getParameter("templateText");
	}
	
	Patient patient = new Patient();
	patient.setFirstName("Justin");
	patient.setLastName("Miranda");
	patient.setWeight("185");
	
	Map contextMap = new HashMap();
	contextMap.put("patient", patient);
	
	MessagePreparator preparator = new VelocityMessagePreparator();
	String text = preparator.prepare(templateText, contextMap);
	

%>

<html>

<form>
	<textarea name="templateText" rows="5" cols="60"><%= templateText %></textarea><br/>
	<input type="submit" value="Submit"/>
</form>
</html>
	
<%= text %>