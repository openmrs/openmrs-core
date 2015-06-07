<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Scheduler" otherwise="/login.htm" redirect="/admin/scheduler/scheduler.form" />

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
		
		addNewProperty();
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

	function removeProperty(btn) {
		var row = btn.parentNode;
		while (row.tagName.toLowerCase() != "tr")
			row = row.parentNode;
		
		var parent = row.parentNode;
		parent.removeChild(row);
		//updateRowColors();
	}
	
	function addNewProperty(startup) {
		var tbody = document.getElementById("propertiesTable");
		var blankProp = document.getElementById("newProperty");
		var newProp = blankProp.cloneNode(true);
		newProp.style.display = '';
		newProp.id = '';
		
		tbody.appendChild(newProp);
	}

</script>

<style>
	#newProperty {
		display: none;
	}
	#optionsTOC {
		white-space: nowrap;
	}
</style>

<h2><openmrs:message code="Scheduler.header"/></h2>


<spring:hasBindErrors name="concept">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<spring:hasBindErrors name="task">
	<div class="error"><openmrs:message code="fix.error"/></div>
	<br />
</spring:hasBindErrors>

<form method="post" id="schedulerForm">

  <fieldset>
  <legend><openmrs:message code="Scheduler.taskForm.legend" /></legend>
	<table cellpadding="5">
		<tr>
			<td><openmrs:message code="general.id"/></td>
			<td>${task.id}</td>
		</tr>
		<tr>
			<td><openmrs:message code="general.name"/><span class="required">*</span></td>
			<td>
				<spring:bind path="task.name">
					<input type="text" name="name" value="${status.value}" size="35" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td><openmrs:message code="Scheduler.taskForm.class"/><span class="required">*</span></td>
			<td>
				<spring:bind path="task.taskClass">
					<input type="text" name="taskClass" value="${status.value}" size="60" />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.description"/></td>
			<td valign="top">
				<spring:bind path="task.description">
					<textarea name="description" rows="3" cols="60">${status.value}</textarea>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.createdBy"/></td>
			<td valign="top">
				<spring:bind path="task.creator">
					<openmrs:format user="${status.value}"/>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.dateCreated"/></td>
			<td valign="top">
				<spring:bind path="task.dateCreated">
					<openmrs:formatDate date="${status.editor.value}" type="long"/>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.changedBy"/></td>
			<td valign="top">
				<spring:bind path="task.changedBy">
					<openmrs:format user="${status.value}"/>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="general.dateChanged"/></td>
			<td valign="top">
				<spring:bind path="task.dateChanged">
					<openmrs:formatDate date="${status.editor.value}" type="long"/>
				</spring:bind>
			</td>
		</tr>
	</table>
</fieldset>

<fieldset>
	<legend><openmrs:message code="Scheduler.scheduleForm.legend" /></legend> 
	<table cellpadding="5">
		<tr>
		
			<td valign="top" colspan="2">
				<openmrs:message code="Scheduler.scheduleForm.instructions"/>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Scheduler.scheduleForm.started"/>:</td>
			<td>
				<spring:bind path="task.started">
					${status.value} 
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Scheduler.scheduleForm.startOnStartup"/>:</td>
			<td>
				<spring:bind path="task.startOnStartup">
					<input type="hidden" name="_${status.expression}"/>
					<input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value}">checked="checked"</c:if> />
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Scheduler.scheduleForm.startTimePattern"/>:</td>
			<td>
				<spring:bind path="task.startTimePattern">
					<input type="text" id="startTimePattern" name="startTimePattern" size="25" value="${status.value}" disabled/>
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Scheduler.scheduleForm.startTime"/>:</td>
			<td>
				<spring:bind path="task.startTime">
					<input type="text" id="startTime" name="startTime" size="25" value="${status.value}"/> 
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Scheduler.scheduleForm.repeatInterval"/>:</td>
			<td>
				<spring:bind path="task.repeatInterval">
					<input type="text" id="repeatInterval" name="repeatInterval" size="10" value="${repeatInterval}" /> 
					<select name="repeatIntervalUnits">
						<option value="seconds" <c:if test="${units=='seconds'}">selected</c:if>><openmrs:message code="Scheduler.scheduleForm.repeatInterval.units.seconds" /></option>
						<option value="minutes" <c:if test="${units=='minutes'}">selected</c:if>><openmrs:message code="Scheduler.scheduleForm.repeatInterval.units.minutes" /></option>
						<option value="hours" <c:if test="${units=='hours'}">selected</c:if>><openmrs:message code="Scheduler.scheduleForm.repeatInterval.units.hours" /></option>
						<option value="days" <c:if test="${units=='days'}">selected</c:if>><openmrs:message code="Scheduler.scheduleForm.repeatInterval.units.days" /></option>
					</select>
					
					<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
				</spring:bind>
			</td>
		</tr>
		<tr>
			<td valign="top"><openmrs:message code="Scheduler.scheduleForm.lastExecutionTime"/>:</td>
			<td>
				<openmrs:formatDate date="${task.lastExecutionTime}" type="long" />
			</td>
		</tr>		
	</table>
</fieldset>

<fieldset>
	<legend><openmrs:message code="Scheduler.propertyForm.legend" /></legend> 
	<table>
		<tbody id="propertiesTable">
			<tr>
				<td><openmrs:message code="general.name" /></td>
				<td><openmrs:message code="general.value" /></td>
			</tr>
			<c:forEach var="property" items="${task.properties}">			
			<tr>
				<td><input type="text" name="propertyName" size="20" value="<spring:message text="${property.key}" htmlEscape="true"/>" /></td>
				<td><input type="text" name="propertyValue" size="30" value="<spring:message text="${ property.value }" htmlEscape="true"/>" /></td>
				<td><input type="button" class="closeButton" onclick="removeProperty(this)" value="<openmrs:message code="Scheduler.propertyForm.remove"/>"></td>
			</tr>
			</c:forEach>
			<tr id="newProperty">
				<td>
					<input type="text" name="propertyName" size="20"/> 
				</td>
				<td>
					<input type="text" name="propertyValue" size="30"/> 
				</td>
				<td>
					<input type="button" class="closeButton" onclick="removeProperty(this)" value="<openmrs:message code="Scheduler.propertyForm.remove"/>">
				</td>
			</tr>
		</tbody>
	</table>
	<br/>
	<input type="button" class="smallButton" onclick="addNewProperty()" value="<openmrs:message code="Scheduler.propertyForm.add"/>">
	<br />
	<br />
	
</fieldset>

<input type="submit" value="<openmrs:message code="Scheduler.taskForm.save"/>">

<br />

</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
