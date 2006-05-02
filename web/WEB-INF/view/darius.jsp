<%@ include file="/WEB-INF/template/include.jsp" %>

All Patients: <c:out value="${model.all_patients}"/>

<br />

<c:out value="${model.filter_description}"/>: <c:out value="${model.filtered_patients}" escapeXml="false"/>

<br />
Age frequency distribution <c:out value="${model.age_frequency}" escapeXml="false"/>

<br />
Birthdate table <c:out value="${model.birth_table}" escapeXml="false"/>