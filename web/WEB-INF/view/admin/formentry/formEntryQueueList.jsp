<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View FormEntry Queue" otherwise="/login.htm" redirect="/admin/formentry/formEntryQueue.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2>
	<spring:message code="FormEntryQueue.title" />
</h2>

<script type="text/javascript">

	var lists = new Array();
	lists[""] = 0;
	lists["pending"] = ${queueSize};
	lists["archive"] = ${archiveSize};
	lists["error"] = ${errorSize};

	function changeSize(list) {
		var queueType = list.value;
		var start = document.getElementById("start");
		var end   = document.getElementById("end");
		var startOpts = start.options;
		var endOpts = end.options;
		while (startOpts.length) {
			startOpts[0] = null;
			endOpts[0] = null;
		}
		for (var i=1; i <= lists[queueType]; i++) {
			start.appendChild(new Option(i, i));
			end.appendChild(new Option(i, i));
		}
	}
</script>

<form method="post" action="${pageContext.request.contextPath}/formEntryQueueDownload">
	<b class="boxHeader"><spring:message code="FormEntryQueue.multiple" />:</b>
	<div class="box">
		<table>
			<tr>
				<td><spring:message code="FormEntryQueue.select"/></td>
				<td>
					<select name="queueType" onChange="changeSize(this)">
						<option value=""></option>
						<option value="pending"><spring:message code="FormEntryQueue.pending"/></option>
						<option value="archive"><spring:message code="FormEntryQueue.archive"/></option>
						<option value="error"><spring:message code="FormEntryQueue.error"/></option>
					</select>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.start" /></td>
				<td>
					<select name="startId" id="start"> </select>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.end" /></td>
				<td>
					<select name="endId" id="end"> </select>
				</td>
			</tr>
		</table>
		<input type="submit" value='<spring:message code="general.download" />' />
	</div>
</form>

<br/>

<%@ include file="/WEB-INF/template/footer.jsp"%>
