<%@ include file="/WEB-INF/template/include.jsp" %>

<%--<openmrs:require privilege="Task Scheduler" otherwise="/login.htm" redirect="/admin/scheduler/taskForm" />--%>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script type="text/javascript">

window.onload = init;

function init() {
	var sections = new Array();
	var form = document.getElementById("schedulerForm");
	children = form.childNodes;
	var seci = 0;
	for(i=0;i<children.length;i++) {
		if(children[i].nodeName.toLowerCase().indexOf('fieldset') != -1) {
			children[i].id = 'optsection-' + seci;
			children[i].className = 'optsection';
			legends = children[i].getElementsByTagName('legend');
			sections[seci] = new Object();
			if(legends[0] && legends[0].firstChild.nodeValue)
				sections[seci].text = legends[0].firstChild.nodeValue;
			else
				sections[seci].text = '# ' + seci;
			sections[seci].secid = children[i].id;
			sections[seci].error = containsError(children[i]);
			seci++;
			if(sections.length != 1)
				children[i].style.display = 'none';
			else
				var selectedid = children[i].id;
		}
	}
	
	var toc = document.createElement('ul');
	toc.id = 'optionsTOC';
	toc.selectedid = selectedid;
	for(i=0;i<sections.length;i++) {
		var li = document.createElement('li');
		if(i == 0) li.className = 'selected';
		var a =  document.createElement('a');
		a.href = '#' + sections[i].secid;
		a.onclick = uncoversection;
		a.appendChild(document.createTextNode(sections[i].text));
		a.secid = sections[i].secid;
		a.id = sections[i].secid + "_link";
		if (sections[i].error) {
			a.className = "error";
		}
		li.appendChild(a);
		toc.appendChild(li);
	}
	form.insertBefore(toc, children[0]);

	var hash = document.location.hash;
	if (hash.length > 1) {
		var autoSelect = hash.substring(1, hash.length);
		for(i=0;i<sections.length;i++) {
			if (sections[i].text == autoSelect)
				uncoversection(sections[i].secid + "_link");
		}
	}
}

function uncoversection(secid) {
	var obj = this;
	if (typeof secid == 'string') {
		obj = document.getElementById(secid);
		if (obj == null)
			return false;
	}

	var ul = document.getElementById('optionsTOC');
	var oldsecid = ul.selectedid;
	var newsec = document.getElementById(obj.secid);
	if(oldsecid != obj.secid) {
		document.getElementById(oldsecid).style.display = 'none';
		newsec.style.display = 'block';
		ul.selectedid = obj.secid;
		lis = ul.getElementsByTagName('li');
		for(i=0;i< lis.length;i++) {
			lis[i].className = '';
		}
		obj.parentNode.className = 'selected';
	}
	newsec.blur();
	return false;
}

function containsError(element) {
	if (element) {
		var child = element.firstChild;
		while (child != null) {
			if (child.className == 'error') {
				return true;
			}
			else if (containsError(child) == true) {
				return true;
			}
			child = child.nextSibling;
		}
	}
	return false;
}

</script>


<h2><spring:message code="Scheduler.header"/></h2>

<spring:hasBindErrors name="task">
	<spring:message code="fix.error"/>
	<br />
</spring:hasBindErrors>
<form method="post">

<div id="schedulerForm">
  <fieldset>
  <legend><spring:message code="Scheduler.taskForm.legend" /></legend>
	<table cellpadding="5">
		<tr>
			<td><spring:message code="general.id"/></td>
			<td>${task.id}</td>
		</tr>
		<tr>
			<td><spring:message code="general.name"/></td>
			<td>
				<spring:bind path="task.name">
					<input type="text" name="name" value="${status.value}" size="35" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><spring:message code="Scheduler.taskForm.class"/></td>
			<td>
				<spring:bind path="task.schedulableClass">
					<input type="text" name="schedulableClass" value="${status.value}" size="60" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.description"/></td>
			<td valign="top">
				<spring:bind path="task.description">
					<textarea name="description" rows="3" cols="60">${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.createdBy"/></td>
			<td valign="top">
				<spring:bind path="task.createdBy">
					${status.value}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.dateCreated"/></td>
			<td valign="top">
				<spring:bind path="task.dateCreated">
					${status.value}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.changedBy"/></td>
			<td valign="top">
				<spring:bind path="task.changedBy">
					${status.value}
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="general.dateChanged"/></td>
			<td valign="top">
				<spring:bind path="task.dateChanged">
					${status.value}
				</spring:bind>
			</td>
		</tr>
	</table>
</fieldset>

<fieldset>
	<legend><spring:message code="Scheduler.scheduleForm.legend" /></legend> 
	<table cellpadding="5">
		<tr>
			<td valign="top"><spring:message code="Scheduler.scheduleForm.started"/>:</td>
			<td>
				<spring:bind path="task.started">
					${status.value} 
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="Scheduler.scheduleForm.startOnStartup"/>:</td>
			<td>
				<spring:bind path="task.startOnStartup">
					<input type="hidden" name="_${status.expression}"/>
					<input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="Scheduler.scheduleForm.startTimePattern"/>:</td>
			<td>
				<spring:bind path="task.startTimePattern">
					<input type="text" id="startTimePattern" name="startTimePattern" size="25" value="${status.value}" disabled/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="Scheduler.scheduleForm.startTime"/>:</td>
			<td>
				<spring:bind path="task.startTime">
					<input type="text" id="startTime" name="startTime" size="25" value="${status.value}"/> 
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><spring:message code="Scheduler.scheduleForm.repeatInterval"/>:</td>
			<td>
				<spring:bind path="task.repeatInterval">
					<input type="text" id="repeatInterval" name="repeatInterval" size="10" value="${status.value}" /> 
					<spring:message code="Scheduler.scheduleForm.repeatInterval.units" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
	</table>
</fieldset>
<%--
<fieldset>
	<legend>Properties</legend> 
		<table>
			<tr>
				<td valign="top">

					<table>
						<input type="hidden" name="taskId" value="${task.id}">
						<tr>
							<td>Name</td>
							<td>Value</td>
						</tr>
						<c:forEach var="property" items="${task.properties}">			
						<tr>
							<td>
								<spring:bind path="property.name">
									<input type="text" id="name" name="name" size="10" value="${status.value}" /> (in seconds)
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
							<td>
								<spring:bind path="property.value">
									<input type="text" id="value" name="value" size="10" value="${status.value}" /> (in seconds)
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
							<td>
								<input type="button" class="smallButton" value="<spring:message code="Scheduler.property.remove"/>">
							</td>
						</tr>
						</c:forEach>
						<tr>
							<td>
								<input type="text" name="propertyName" size="30"/> 
							</td>
							<td>
								<input type="text" name="propertyValue" size="30"/> 
							</td>
							<td>
								<input type="button" class="smallButton" value="<spring:message code="Scheduler.property.save"/>">
							</td>
						</tr>
						<tr>
							<td colspan="2">
								
							</td>
						</tr>
					</table>

				</td>
			</tr>
		</table>
	<br />
	<br />
	</fieldset>
--%>

  <input type="submit" value="<spring:message code="Scheduler.taskForm.save"/>">

<br />

</div>
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>