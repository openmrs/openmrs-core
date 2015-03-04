<html>

<head>

<script src='<%= request.getContextPath() %>/dwr/interface/DWRMessageService.js'></script>
<script src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script>

	function sendMessage() {
		var sender = document.getElementById("sender").value;
    	var recipients = document.getElementById("recipients").value;
    	var subject = document.getElementById("subject").value;
    	var content = document.getElementById("content").value
		DWRMessageService.sendMessage(recipients, sender, subject, content);
		return false;
	}
	
</script>

</head>

<body>
<h2>DWR Message Test</h2>
<br/><br/>

<div id="sendMessage">
	<b class="boxHeader">Send Message</b>
	<div class="box">
		<form id="sendMessage" onSubmit="return sendMessage();">
			<table>
				<tr>
					<td>Sender:</td>
					<td><input type="text" id="sender" name="sender" size="40" value="info@openmrs.org"> </td>
				</tr>
				<tr>
					<td>Recipients <br>(comma-separated list):</td>
					<td><input type="text" id="recipients" name="recipients" size="40"> </td>
				</tr>
				<tr>
					<td>Subject:</td>
					<td><input type="text" id="subject" name="subject" size="40" value="OpenMRS Message"> </td>
				</tr>											
				<tr>
					<td>Message:</td>
					<td><textarea id="content" name="content" rows="4" cols="60"></textarea></td>
				</tr>
				<tr>
					<td colspan="2"><input type="submit" value="send"/></td>
				</tr>
			</table>
		</form>
	</div>
</div>

</body>

</html>
