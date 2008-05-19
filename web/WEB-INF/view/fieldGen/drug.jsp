<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs_tag:drugField formFieldName="${model.formFieldName}" initialValue="${model.initialValue}" drugs="${model.drugs}" optionHeader="${model.optionHeader}" onChange="${model.onChange}" includeVoided="${model.includeVoided}" />
