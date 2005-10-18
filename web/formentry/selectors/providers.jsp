<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.jsp" />

<%@ include file="/WEB-INF/template/forms/header.jsp" %>

mode request/mode | string:add

<h1>Select a Provider</h1>

<table border="0" tal:define="provider_list here/sql_select_providers">
<tr tal:condition="not:provider_list">
  <td>
    <p class="no_hit">
	  Matata!  I was unable to find any providers.  Please
	  <a href="javascript:reloadPage()">try again</a>.  If this problem persists,
	  then contact a computer geek immediately!
	</p>
  </td>
</tr>
<tr tal:repeat="provider provider_list" tal:attributes="class python:test(repeat['provider'].even(),'even','odd')">
  <td tal:define="fname python:provider.first_name or '';
                  lname python:provider.last_name or ''">
    <a class="hit" href="#top" tal:content="string:${fname} ${lname} (${provider/username})"
       tal:attributes="onclick string:javascript:pick(this);
	                   value provider/user_id"></a>
  </td>
</tr>
</table>

<%@ include file="/WEB-INF/template/forms/footer.jsp" %>

