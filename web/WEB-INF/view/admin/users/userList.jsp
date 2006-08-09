<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Edit Users" otherwise="/login.htm" redirect="/admin/users/user.list" />

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="User.manage.title"/></h2>

<a href="user.form"><spring:message code="User.add"/></a>

<br/><br/>

<b class="boxHeader"><spring:message code="User.list.title"/></b>

<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/dojo/dojo.js"></script>

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.UserSearch");
	
	var searchWidget;
	
	var getSystemId = function(u) {
		if (typeof u == 'string') return u;
		s = " &nbsp; " + u.systemId;;
		if (u.voided)
			s = "<span class='retired'>" + s + "</span>";
		return s;
	}
	
	var getUsername = function(u) {
		if (typeof u == 'string' || u.username == null) return '';
		return " &nbsp; " + u.username;
	}
	
	var getFirst = function(u) {
		if (typeof u == 'string' || u.firstName == null) return '';
		return " &nbsp; " + u.firstName;
	}
	
	var getLast = function(u) {
		if (typeof u == 'string' || u.lastName == null) return '';
		return " &nbsp; " + u.lastName;
	}
	
	var getRoles = function(u) {
		if (typeof u == 'string') return '';	
		return " &nbsp; " + u.roles;
	}
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("uSearch");			
		
		dojo.event.topic.subscribe("uSearch/select", 
			function(msg) {
				document.location = "user.form?userId=" + msg.objs[0].userId;
			}
		);
		
		searchWidget.getCellFunctions = function() {
			return [searchWidget.simpleClosure(this, "getNumber"), 
					getSystemId, 
					getUsername, 
					getFirst, 
					getLast,
					getRoles
					];
		};
		
		searchWidget.setHeaderCellContent (
			['', '<spring:message code="User.systemId"/>', '<spring:message code="User.username"/>', '<spring:message code="User.firstName"/>', '<spring:message code="User.lastName"/>', '<spring:message code="User.roles"/>']
		);
		
		searchWidget.showAll();
		
	});
	
</script>

<div class="box">
	<div dojoType="UserSearch" widgetId="uSearch" searchLabel='<spring:message code="User.find"/>' showIncludeVoided="true"></div>
</div>

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>