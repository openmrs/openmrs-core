	<%@ page import="java.security.MessageDigest" %>
<html>
<body>

<%!

private String hexString(byte[] b) {
	if (b == null || b.length < 1)
		return "";
	StringBuffer s = new StringBuffer();
	for (int i=0; i<b.length; i++) {
		s.append(Integer.toHexString(b[i] & 0xFF));
	}
	return new String(s);
}

%>

<%

try {
	String password = "test";
	MessageDigest md = MessageDigest.getInstance("MD5");
	byte[] input = password.getBytes();
	out.println(password + " -> " + hexString(md.digest(input)));
} catch (Exception e) {
	out.println(e.getMessage());
}
%>

</html>