<%@ include file="/WEB-INF/template/include.jsp" %>

All Patients: <c:out value="${model.all_patients}"/>

<br />

<c:out value="${model.filter_description}"/>: <c:out value="${model.filtered_patients}"/>

Age table <c:out value="${model.output}"/>