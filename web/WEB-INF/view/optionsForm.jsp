<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:require privilege="" otherwise="/login.htm"
	redirect="/options.form" />

<script type="text/javascript">

window.onload = init;

function init() {
	var sections = new Array();
	var optform = document.getElementById("optionsForm");
	optform.id = "optionsForm";
	children = optform.childNodes;
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
			if(sections.length != 1) children[i].style.display = 'none';
			else var selectedid = children[i].id;
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
		if (sections[i].error) {
			a.className = "error";
		}
		li.appendChild(a);
		toc.appendChild(li);
	}
	optform.insertBefore(toc, children[0]);
}

function uncoversection() {
	oldsecid = this.parentNode.parentNode.selectedid;
	newsec = document.getElementById(this.secid);
	if(oldsecid != this.secid) {
		ul = document.getElementById('optionsTOC');
		document.getElementById(oldsecid).style.display = 'none';
		newsec.style.display = 'block';
		ul.selectedid = this.secid;
		lis = ul.getElementsByTagName('li');
		for(i=0;i< lis.length;i++) {
			lis[i].className = '';
		}
		this.parentNode.className = 'selected';
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

<h2><spring:message code="options.title" /></h2>

<spring:hasBindErrors name="opts">
	<spring:message code="fix.error" />
	<div class="error"><c:forEach items="${errors.allErrors}" var="error">
		<spring:message code="${error.code}" text="${error.code}" />
		<br />
		<!-- ${error} -->
	</c:forEach></div>
	<br />
</spring:hasBindErrors>

<form method="post" id="optionsForm">

<fieldset><legend><spring:message code="options.default.legend" /></legend>
<table>
	<tr>
		<td><spring:message code="options.default.location" /></td>
		<td>
			<spring:bind path="opts.defaultLocation">
				<select name="${status.expression}">
					<c:forEach items="${locations}" var="loc">
						<option value="${loc.locationId}" <c:if test="${loc.locationId == status.value}">selected</c:if>>${loc.name}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="options.default.language" /></td>
		<td>
			<spring:bind path="opts.defaultLanguage">
				<select name="${status.expression}">
					<option value="en">English</option>
					<option value="fr">Français</option>
					<option value="de">Deutsch</option>
				</select>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="options.showRetiredMessage" /></td>
		<td>
			<spring:bind path="opts.showRetiredMessage">
				<input type="hidden" name="_${status.expression}" value="true" />
				<input type="checkbox" name="${status.expression}" value="true" <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<br />
<br />
</fieldset>

<fieldset><legend><spring:message code="options.password.legend" /></legend>
<table>
	<tr>
		<td><spring:message code="options.password.old" /></td>
		<td>
			<spring:bind path="opts.oldPassword">
				<input type="password" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="options.password.new" /></td>
		<td>
			<spring:bind path="opts.newPassword">
				<input type="password" name="${status.expression}"
			value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="options.password.confirm" /></td>
		<td>
			<spring:bind path="opts.confirmPassword">
				<input type="password" name="${status.expression}"
			value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<br />
<br />
</fieldset>

<fieldset><legend><spring:message code="options.secretQuestion.legend" /></legend>
<table>
	<tr>
		<td><spring:message code="options.password.old" /></td>
		<td>
			<spring:bind path="opts.secretQuestionPassword">
				<input type="password" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="options.secretQuestionNew" /></td>
		<td>
			<spring:bind path="opts.secretQuestionNew">
				<input type="text" name="${status.expression}"
			value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="options.secretAnswerNew" /></td>
		<td>
			<spring:bind path="opts.secretAnswerNew">
				<input type="password" name="${status.expression}"
			value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><spring:message code="options.secretAnswerConfirm" /></td>
		<td>
			<spring:bind path="opts.secretAnswerConfirm">
				<input type="password" name="${status.expression}"
					value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<br />
<br />
</fieldset>
<div><br />
<input type="submit" value="<spring:message code="options.save"/>"></div>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
