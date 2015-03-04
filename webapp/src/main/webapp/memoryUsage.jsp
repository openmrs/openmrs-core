<%@ page import="java.lang.management.*" %>
<%@ page import="java.util.*" %>
<html>
<head>
  <title>JVM Memory Monitor</title>
</head>

<table border="0" width="100%">
	<tr><td colspan="2" align="center"><h3>Memory MXBean</h3></td></tr>
	<tr>
		<td width="200">Heap Memory Usage</td>
		<td><%= ManagementFactory.getMemoryMXBean().getHeapMemoryUsage() %></td>
		</tr>
	<tr>
		<td>Non-Heap Memory Usage</td>
		<td><%= ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage() %></td>
	</tr>
	<tr><td colspan="2">&nbsp;</td></tr>
	<tr><td colspan="2" align="center"><h3>Memory Pool MXBeans</h3></td></tr>
	<%
	        Iterator iter = ManagementFactory.getMemoryPoolMXBeans().iterator();
	        while (iter.hasNext()) {
	            MemoryPoolMXBean item = (MemoryPoolMXBean) iter.next();
	%>
		<tr>
			<td colspan="2">
				<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
					<tr><td colspan="2" align="center"><b><%= item.getName() %></b></td></tr>
					<tr><td width="200">Type</td><td><%= item.getType() %></td></tr>
					<tr><td>Usage</td><td><%= item.getUsage() %></td></tr>
					<tr><td>Peak Usage</td><td><%= item.getPeakUsage() %></td></tr>
					<tr><td>Collection Usage</td><td><%= item.getCollectionUsage() %></td></tr>
				</table>
			</td>
		</tr>
		<tr><td colspan="2">&nbsp;</td></tr>
	<%
	}
	%>
	<tr><td colspan="2" align="center"><h3>Thread MXBean</h3></td></tr>
	<tr>
		<td>Peak Thread Count</td>
		<td><%= ManagementFactory.getThreadMXBean().getPeakThreadCount() %></td>
	</tr>
	<tr>
		<td>Current Thread CPU Time</td>
		<td><%= ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() %></td>
	</tr>
	<tr>
		<td>Current Thread User Time</td>
		<td><%= ManagementFactory.getThreadMXBean().getCurrentThreadUserTime() %></td>
	</tr>
	<tr>
		<td>Daemon Thread Count</td>
		<td><%= ManagementFactory.getThreadMXBean().getDaemonThreadCount() %></td>
	</tr>
	<tr>
		<td>Thread Count</td>
		<td><%= ManagementFactory.getThreadMXBean().getThreadCount() %></td>
	</tr>
	<%
	        for(long id : ManagementFactory.getThreadMXBean().getAllThreadIds()) {
	%>
		<tr>
			<td colspan="2">
				<table border="0" width="100%" style="border: 1px #98AAB1 solid;">
					<tr><td colspan="2" align="center"><b><%= ManagementFactory.getThreadMXBean().getThreadInfo(id) %></b></td></tr>
					<tr><td width="200">Thread CPU Time</td><td><%= ManagementFactory.getThreadMXBean().getThreadCpuTime(id) %></td></tr>
					<tr><td>Thread User Time</td><td><%= ManagementFactory.getThreadMXBean().getThreadUserTime(id) %></td></tr>
					<tr><td>Thread Info (with stack trace)</td><td><%= ManagementFactory.getThreadMXBean().getThreadInfo(id, Integer.MAX_VALUE) %></td></tr>
				</table>
			</td>
		</tr>
		<tr><td colspan="2">&nbsp;</td></tr>
	<%
	}
	%>




</table>
