<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="" otherwise="/login.htm"
	redirect="/options.form" />
	
<openmrs:message var="pageTitle" code="optionsForm.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp"%>

<script type="text/javascript">

window.onload = init;

function init() {
	var sections = new Array();
	var optform = document.getElementById("optionsForm");
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
	optform.insertBefore(toc, children[0]);

	var hash = document.location.hash;
	if (hash.length > 1) {
		var autoSelect = hash.substring(1, hash.length);
		for(i=0;i<sections.length;i++) {
			if (sections[i].text == autoSelect){
                uncoversection(sections[i].secid + "_link");
            }
		}
	}

    //If a section has errors, then it should be selected.
    for(i=0;i<sections.length;i++){
        if(sections[i].error){
           uncoversection(sections[i].secid + "_link");
           break;
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

<h2><openmrs:message code="options.title" /></h2>

<spring:hasBindErrors name="opts">
    <openmrs_tag:errorNotify errors="${errors}" />
</spring:hasBindErrors>

<form method="post">

<div id="optionsForm">
<fieldset><legend><openmrs:message code="options.default.legend" /></legend>
<table>
	<tr>
		<td><openmrs:message code="options.default.location" /></td>
		<td>
			<spring:bind path="opts.defaultLocation">
				<select name="${status.expression}">
					<option value=""></option>
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
		<td><openmrs:message code="options.default.locale" /></td>
		<td>
			<spring:bind path="opts.defaultLocale">
				<select name="${status.expression}">
					<option value=""></option>
					<c:forEach items="${languages}" var="locale">
						<option value="${locale}" <c:if test="${locale == status.value}">selected</c:if>>${locale.displayName}</option>
					</c:forEach>
				</select>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="options.proficient.locales" /></td>
		<td>
			<spring:bind path="opts.proficientLocales">
				<input type="text" name="${status.expression}" value="${status.value}" 
					size="35" />
				<span class="description">example: "en_US, en_GB, en, fr_RW"</span>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="options.showRetiredMessage" /></td>
		<td>
			<label for="${status.expression}"> <spring:bind path="opts.showRetiredMessage"> </label>
				<input type="hidden" name="_${status.expression}" value="true" />
				<input type="checkbox" name="${status.expression}" value="true" id="${status.expression}" <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="options.default.verbose" /></td>
		<td>
			<label for="${status.expression}"><spring:bind path="opts.verbose"></label>
				<input type="hidden" name="_${status.expression}" value="true" />
				<input type="checkbox" name="${status.expression}" value="true" id="${status.expression}" <c:if test="${status.value == true}">checked</c:if> />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<br /><br />
<br />
</fieldset>

<fieldset><legend><openmrs:message code="options.login.legend" /></legend>
<table>
	<tr>
		<td><openmrs:message code="options.login.username" /></td>
		<td>
			<spring:bind path="opts.username">
				<input type="text" name="${status.expression}" value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<spring:nestedPath path="opts.personName">
		<openmrs:portlet url="nameLayout" id="namePortlet" size="full" parameters="layoutMode=edit|layoutShowTable=false|layoutShowExtended=false" />
	</spring:nestedPath>
	<tr><td colspan="2"><br/></td></tr>
	<tr>
		<td><openmrs:message code="options.login.password.old" /></td>
		<td>
			<spring:bind path="opts.oldPassword">
				<input type="password" name="${status.expression}" value="${status.value}${resetPassword}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="options.login.password.new" /></td>
		<td>
			<spring:bind path="opts.newPassword">
				<input type="password" name="${status.expression}"
			value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
			<%-- Don't print empty brackets --%>
			<c:if test="${passwordHint != ''}">
				(${passwordHint})
			</c:if>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="options.login.password.confirm" /></td>
		<td>
			<spring:bind path="opts.confirmPassword">
				<input type="password" name="${status.expression}"
			value="${status.value}" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
			<openmrs:message code="User.confirm.description" />
		</td>
	</tr>
	<tr><td colspan="2"><br/></td></tr>
	<tr><td colspan="2"><openmrs:message code="options.login.secretQuestion.about" /></td></tr>
	<tr>
		<td><openmrs:message code="options.login.password.old" /></td>
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
		<td><openmrs:message code="options.login.secretQuestionNew" /></td>
		<td>
			<spring:bind path="opts.secretQuestionNew">
				<input type="text" name="${status.expression}"
					value="${status.value}" size="35"/>
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="options.login.secretAnswerNew" /></td>
		<td>
			<spring:bind path="opts.secretAnswerNew">
				<input type="password" name="${status.expression}"
					value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<td><openmrs:message code="options.login.secretAnswerConfirm" /></td>
		<td>
			<spring:bind path="opts.secretAnswerConfirm">
				<input type="password" name="${status.expression}"
					value="${status.value}" size="35" />
				<c:if test="${status.errorMessage != ''}">
					<span class="error">${status.errorMessage}</span>
				</c:if>
			</spring:bind>
		</td>
	</tr>
</table>
<br /><br />
<br />
</fieldset>

<fieldset><legend><openmrs:message code="options.notify.legend" /></legend>
<table>
	<tr>
		<td><input type="radio" name="notification" value="internalOnly" id="internalOnly" <c:if test="${opts.notification == 'internalOnly'}">checked</c:if> /></td>
		<td><label for="internalOnly"><openmrs:message code="options.notify.internalOnly" /></label></td>
	</tr>
	<tr>
		<td><input type="radio" name="notification" value="internal" id="internal" <c:if test="${opts.notification == 'internal'}">checked</c:if> /></td>
		<td><label for="internal"><openmrs:message code="options.notify.internal" /></label></td>
	</tr>
	<tr>
		<td><input type="radio" name="notification" value="internalProtected" id="internalProtected" <c:if test="${opts.notification == 'internalProtected'}">checked</c:if> /></td>
		<td><label for="internalProtected"><openmrs:message code="options.notify.internalProtected" /></label></td>
	</tr>
	<tr>
		<td><input type="radio" name="notification" value="email" id="email" <c:if test="${opts.notification == 'email'}">checked</c:if> /></td>
		<td><label for="email"><openmrs:message code="options.notify.email" /></label></td>
	</tr>
</table>
<table>
	<tr>
		<td><openmrs:message code="options.notify.notificationAddress" /></td>
		<td>
			<spring:bind path="opts.notificationAddress">
				<input type="text" name="${status.expression}" value="${status.value}" size="35"/>
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
<openmrs:extensionPoint pointId="org.openmrs.userOptionExtension" requiredClass="org.openmrs.module.web.extension.UserOptionExtension"  type="html">
	<openmrs:hasPrivilege privilege="${extension.requiredPrivilege}">
		<c:catch var="ex">
			<c:choose>
				<c:when
					test="${extension.portletUrl == '' || extension.portletUrl == null}">
							portletId is null: '${extension.extensionId}'
						</c:when>
				<c:otherwise>
					<fieldset><legend>${extension.tabName}</legend> <openmrs:portlet
						url="${extension.portletUrl}" id="${extension.tabId}"
						moduleId="${extension.moduleId}" parameters="${extension.portletParameters}" /></fieldset>
				</c:otherwise>
			</c:choose>
		</c:catch>
		<c:if test="${not empty ex}">
			<div class="error"><openmrs:message code="fix.error.plain" /> <br />
			<b>${ex}</b>
			<div style="height: 200px; width: 800px; overflow: scroll"><c:forEach
				var="row" items="${ex.cause.stackTrace}">
								${row}<br />
			</c:forEach></div>
			</div>
		</c:if>


	</openmrs:hasPrivilege>
</openmrs:extensionPoint></div>
<br />
<input type="submit" value="<openmrs:message code="options.save"/>">
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
