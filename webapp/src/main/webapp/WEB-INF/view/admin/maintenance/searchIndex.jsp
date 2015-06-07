<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Search Index" otherwise="/login.htm"
                 redirect="/admin/maintenance/searchIndex.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>
<script type="text/javascript">

    function inProgress() {
        $j("#rebuildButton").prop("disabled", true);
        $j("#failure").hide();
        $j("#success").hide();
        $j("#progress").show();
    }

    function onSuccess(data) {
        $j("#progress").hide();
        if (data && data.success) {
            $j("#success").show();
        } else {
            $j("#failure").show();
        }
        $j("#rebuildButton").prop("disabled", false);
    }

    function onError(data) {
        $j("#progress").hide();
        $j("#success").hide();
        $j("#failure").show();
        $j("#rebuildButton").prop("disabled", false);
    }

    function rebuildIndex() {
        inProgress();
        $j.ajax({
            "type": "POST",
            "url": "${pageContext.request.contextPath}/admin/maintenance/rebuildSearchIndex.htm",
            "data": {},
            "dataType": "json",
            "success": onSuccess,
            "error": onError
        });
    }

    $j(document).ready(function () {
        $j("#progress").hide();
        $j("#success").hide();
        $j("#failure").hide();
    });
</script>

<h2><openmrs:message code="SearchIndex.title"/></h2>

<div class="dashedAndHighlighted">
    <p><openmrs:message code="RebuildSearchIndex.message"/></p>
</div>
<br>
<input id="rebuildButton" type="submit" value='<openmrs:message code="RebuildSearchIndex.title"/>' onclick="rebuildIndex()">
<br>
<div id="progress">
    <p><openmrs:message code="RebuildSearchIndex.inProgress.message"/></p>
    <img id="indexing_progress_img" src="<openmrs:contextPath/>/images/loading.gif"/>
</div>
<br>
<div id="success">
    <p><openmrs:message code="RebuildSearchIndex.completed.message"/></p>
</div>
<div class="error" id="failure">
    <p><openmrs:message code="RebuildSearchIndex.failure.message"/></p>
</div>


<%@ include file="/WEB-INF/template/footer.jsp" %>