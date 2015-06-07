<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs_tag:userField formFieldName="${model.formFieldName}" initialValue="${model.obj.userId}" roles="${model.roles}" linkUrl="${pageContext.request.contextPath}/admin/users/user.form" />
