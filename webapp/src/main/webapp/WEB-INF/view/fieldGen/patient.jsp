<!-- Patient View for FieldGen module -->
<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs_tag:patientField formFieldName="${model.formFieldName}" initialValue="${model.obj.patientId}" linkUrl="${pageContext.request.contextPath}/admin/patients/patient.form" />
<!-- Patient View for FieldGen module -->
