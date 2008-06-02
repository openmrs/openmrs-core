<%@ page import="org.openmrs.api.context.Context" %>
<%@ page import="org.openmrs.User" %>
<%@ page import="org.openmrs.Role" %>
<%@ page import="org.openmrs.Privilege" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.openmrs.api.UserService" %>
<%@ page import="java.util.List" %>
<%@ page import="org.openmrs.util.Security" %>

<html>
<body>

<%


try {
	out.write("provider size: " + Context.getDataSetService().getProviders().size());
	for (Object o : Context.getDataSetService().getProviders())
		out.write("provider size: " + o.getClass());
	

	out.write("Locale.displayName: " + request.getLocale().getDisplayName() + "<br>");
	out.write("Locale.getLanguage: " + request.getLocale().getLanguage() + "<br>");
	out.write("Locale.toString: " + request.getLocale().toString() + "<br>");
	
	out.write("request.getRequestURL: " + request.getRequestURL() + "<br>");
	out.write("request.getRequestURI: " + request.getRequestURI() + "<br>");
	out.write("request.getQueryString: " + request.getQueryString() + "<br>");
	out.write("request.getPathTransalated: " + request.getPathTranslated() + "<br>");
	out.write("request.getPathInfo: " + request.getPathInfo() + "<br>");
	out.write("request.getServletPath: " + request.getServletPath() + "<br>");
	
	Context.authenticate("admin", "test");			
	out.write("<br>Authenticated<br>");
	User user = Context.getAuthenticatedUser();
	out.write("As " + user.getFirstName() + " " + user.getLastName());

	UserService us = Context.getUserService();		out.write("--Getting userService--<br>");
	String uname = "USER-1";
	User u2 = us.getUserByUsername(uname);	out.write("--Getting by username--<br>");
	
	if (u2 == null)
		out.write("Error: " + uname + " not found<br>");
	else {
		out.write("Got " + u2.getFirstName() + " " + u2.getLastName() + "<br>");
		User creator = u2.getCreator();
		out.write("Created by " + creator.getUsername() + "<br>");
		out.write("Creator privileges: ");
		for (Iterator i = creator.getRoles().iterator(); i.hasNext();) {
			Role role = (Role)i.next();
			out.write("Role: " + role.getRole());
			for (Iterator i2 = role.getPrivileges().iterator(); i2.hasNext();) {
				Privilege p = (Privilege)i2.next();
				out.write("Priv: " + p.getPrivilege());
			}
		}
	}
	
	out.write("<br><br>Users:<br><table border=1>");
	out.write("<tr><th>username</th><th>first</th><th>last</th><th>Creator</th><th>Roles</th></tr>");
	List users = us.getUsersByRole((Role)u2.getRoles().toArray()[0]);
	for (Iterator i3 = users.iterator(); i3.hasNext();) {
		User tmpuser = (User)i3.next();
		out.write("<tr>");
		out.write("<td>" + tmpuser.getUsername() + "</td>");
		out.write("<td>" + tmpuser.getFirstName()+ "</td>");
		out.write("<td>" + tmpuser.getLastName() + "</td>");
		out.write("<td>" + tmpuser.getCreator().getUsername() + "</td>");
		out.write("<td>" + tmpuser.getRoles() + "</td>");
		out.write("</tr>");
	}
	out.write("</table>");
	
	
} catch (Exception e) {
	out.println("Uh oh, error: <b>" + e + "</b>");
}

String pw = request.getParameter("pw");
if (pw != null) {
	String salt = Security.getRandomToken();
	String hashedpw = Security.encodeString(pw + salt);
	out.write("<br/><br/>" + hashedpw.toString());
	out.write("<br/>" + salt.toString());
}

for (Cookie cookie : request.getCookies()) {
	out.write("<br/> Cookie (domain, name, value): (");
	out.write(cookie.getDomain() + ", ");
	out.write(cookie.getName() + ", ");
	out.write(cookie.getValue() + ")");
}

 java.util.Enumeration e;
 e = request.getAttributeNames();
 while (e.hasMoreElements()) { out.write(e.nextElement().toString()); out.write("<br>");}
 out.write("<br>");out.write("<br>");
 e = request.getHeaderNames();
 while (e.hasMoreElements()) { out.write(e.nextElement().toString()); out.write("<br>");} 
 out.write("<br>");out.write("<br>");
 e = request.getParameterNames();
 while (e.hasMoreElements()) { out.write(e.nextElement().toString()); out.write("<br>");}
%>
<br/><br/>Locale.UK
<% 
  java.util.Locale locale = java.util.Locale.UK;
  out.write("<br/>.getDisplayLanguage: " + locale.getDisplayLanguage());
  out.write("<br/>.getDisplayCountry: " + locale.getDisplayCountry());
  out.write("<br/>.getDisplayVariant: " + locale.getDisplayVariant());
  out.write("<br/>.getLanguage: " + locale.getLanguage());
  out.write("<br/>.getCountry: " + locale.getCountry());
  out.write("<br/>.getVariant: " + locale.getVariant());
  out.write("<br/>.getDisplayName: " + locale.getDisplayName());
  out.write("<br/>.toString: " + locale);
 
 %>

<br><br>Press a key: <input type="text" onKeyDown="document.getElementById('keycode').innerHTML = event.keyCode; return false;"/> KeyCode: <span id="keycode"></span>

<br><br>Random MRNs<br>
<input type="button" onclick="createMRNs()" value="Create" />
<div id="ids"></div>


<script type="text/javascript" src="/openmrs/scripts/validation.js" ></script>
<script type="text/javascript">
	var sites = new Array();
	sites[0] = 'BF';
	sites[1] = 'MO';
	sites[2] = 'MT'; 
	sites[3] = 'MP'; 
	sites[4] = 'TU';
	sites[5] = 'AM';
	sites[6] = 'CH';
	sites[7] = 'EG';
	sites[8] = 'KP';
	sites[9] = 'KT';
	sites[10] = 'NT';
	sites[11] = 'TS';
	sites[12] = 'WB';
	
	function createMRNs() {
		var ids = document.getElementById("ids");
		
		var i=0;
		var str = "";
		while (i <10000) {
			var mrn = i + sites[Math.floor(Math.random()*12)];
			mrn = mrn + "-" + getCheckDigit(mrn);
			str += "<br>" + mrn;
			i = i + 1;
		}
		ids.innerHTML = str;
	}
	
</script>



 <br><br> done 

</html>