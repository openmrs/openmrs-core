<div id="banner">
<a href="<spring:theme code="url.organization" text="http://openmrs.org" />">
  <div id="logosmall"><img src="<%= request.getContextPath() %><spring:theme code="image.logo.text.small" text="/images/openmrs_logo_text_small.gif" />" alt="OpenMRS Logo" border="0"/></div>
</a>  
<table id="bannerbar">
  <tr>
    <td id="logocell"> <img src="<%= request.getContextPath() %><spring:theme code="image.logo.white" text="/images/openmrs_logo_white.gif" />" alt="" class="logo-reduced61" />
    </td>
	<td id="barcell">
        <div class="barsmall">
        <img align="left" src="<%= request.getContextPath() %><spring:theme code="image.logo.bar" text="/images/openmrs_green_bar.gif" />" alt="" class="bar-round-reduced50"/>
        <openmrs:hasPrivilege privilege="View Navigation Menu">
				<%@ include file="/WEB-INF/template/gutter.jsp" %>
		</openmrs:hasPrivilege>
        </div>
    </td>
  </tr>
</table>
</div>