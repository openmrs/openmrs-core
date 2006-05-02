<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Alerts" otherwise="/login.htm" redirect="/admin/users/alert.form" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<script src="<%= request.getContextPath() %>/scripts/calendar/calendar.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/prototype.lite.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.js"></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/moo.fx.pack.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/engine.js'></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/util.js'></script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/openmrsSearch.js"></script>
<script type="text/javascript" src='<%= request.getContextPath() %>/dwr/interface/DWRUserService.js'></script>

<script type="text/javascript">
var init = function() {
		mySearch = new fx.Resize("searchForm", {duration: 100});
		mySearch.hide();
	};
	
	var findObjects = function(txt) {
		DWRUserService.findUsers(fillTable, txt, [], false);
		return false;
	}
	
	var onSelect = function(objs) {
		var obj = objs[0];
		$("user").value = obj.userId;
		$("userName").innerHTML = getName(obj);
		changeButton.focus();
		mySearch.hide();
		return false;
	}
	
	function showSearch(btn) {
		mySearch.hide();
		setPosition(btn, $("searchForm"), 465, 350);
		resetForm();
		DWRUtil.removeAllRows("searchBody");
		$('searchTitle').innerHTML = '<spring:message code="User.find"/>';
		searchType = 'user';
		mySearch.toggle();
		$("searchText").value = '';
		$("searchText").select();
		changeButton = btn;
	}
	
	var getName = function(obj) {
		if (typeof obj == 'string') return '';
		str = obj.firstName;
		str += ' ';
		str += obj.lastName;
		return str;
	}
	
	function closeBox() {
		mySearch.toggle();
		return false;
	}
	
	var customCellFunctions = [getNumber, getName];
	
	var oldonload = window.onload;
	if (typeof window.onload != 'function') {
		window.onload = init;
	} else {
		window.onload = function() {
			oldonload();
			init();
		}
	}
	
	function gotoUser(tagName, userId) {
		if (userId == null)
			userId = $(tagName).value;
		window.location = "${pageContext.request.contextPath}/admin/users/user.form?userId=" + userId;
		return false;
	}
</script>

<style>
	.searchForm {
		width: 450px;
		position: absolute;
		z-index: 10;
		margin: 5px;
	}
	.searchForm .wrapper {
		padding: 2px;
		background-color: whitesmoke;
		border: 1px solid grey;
		height: 330px;
	}
	.searchResults {
		height: 270px;
		overflow: auto;
	}
	th { text-align: left; }
</style>

<h2><spring:message code="Alert.manage.title"/></h2>	

<spring:hasBindErrors name="alert">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<form method="post">
<table>
	<tr>
		<th><spring:message code="Alert.text"/></th>
		<td>
			<spring:bind path="alert.text">
				<input type="text" id="text" name="${status.expression}" value="${status.value}" size="55">
				<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="Alert.user"/></th>
		<td valign="top">
			<spring:bind path="alert.user">
				<table>
					<tr>
						<td><a id="userName" href="#View User" onclick="return gotoUser('user')">${status.value.firstName} ${status.value.middleName} ${status.value.lastName}</a></td>
						<td>
							&nbsp;
							<input type="hidden" id="user" value="${status.value.userId}" name="userId" />
							<input type="button" id="userButton" class="smallButton" value="<spring:message code="general.change"/>" onclick="showSearch(this)" />
							<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
						</td>
					</tr>
				</table>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="Alert.role"/></th>
		<td valign="top">
			<spring:bind path="alert.role">
				<select name="roleStr">
					<option value=""><spring:message code="general.none"/></option>
					<c:forEach items="${allRoles}" var="role">
						<option value="${role.role}" <c:if test="${role == status.value}">selected</c:if>>${role.role}</option>
					</c:forEach>
				</select>
			</spring:bind>
		</td>
	</tr>
	<tr>
		<th valign="top"><spring:message code="Alert.dateToExpire"/></th>
		<td valign="top">
			<spring:bind path="alert.dateToExpire">
				<input type="text" name="${status.expression}" size="10" 
					   value="${status.value}" onClick="showCalendar(this)" />
			</spring:bind>
		</td>
	</tr>
	<c:if test="${!(encounter.changedBy == null)}">
		<tr>
			<td><spring:message code="general.changedBy" /></td>
			<td>
				<a href="#View User" onclick="return gotoUser(null, '${alert.changedBy.userId}')">${alert.changedBy.firstName} ${alert.changedBy.lastName}</a> -
				<openmrs:formatDate date="${alert.dateChanged}" type="medium" />
			</td>
		</tr>
	</c:if>
</table>

<input type="submit" value="<spring:message code="Alert.save"/>">
</form>

<script type="text/javascript">
 document.forms[0].elements[0].focus();
</script>

<div id="searchForm" class="searchForm">
	<div class="wrapper">
		<input type="button" onClick="return closeBox();" class="closeButton" value="X"/>
		<form method="get" onSubmit="return searchBoxChange('searchBody', searchText, null, false, 0); return false;">
			<h3 id="searchTitle"></h3>
			<input type="text" id="searchText" size="35" onkeyup="return searchBoxChange('searchBody', this, event, false, 400);">
		</form>
		<div id="searchResults" class="searchResults">
			<table cellpadding="2" cellspacing="0">
				<tbody id="searchBody">
					<tr>
						<td></td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>

<%@ include file="/WEB-INF/template/footer.jsp" %>