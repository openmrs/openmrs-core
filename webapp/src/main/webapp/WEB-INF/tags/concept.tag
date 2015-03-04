<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ attribute name="conceptId" required="true" type="java.lang.Integer" %>

<openmrs:concept conceptId="${conceptId}" var="c" nameVar="n" numericVar="num">${n.name}</openmrs:concept>