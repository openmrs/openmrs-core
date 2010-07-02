<a href="<spring:theme code="url.organization"/>">
  <div style="margin-left: 49px; margin-top:2px;"><img src="<%= request.getContextPath() %><spring:theme code="image.logo.text.small"/>" alt="OpenMRS Logo" border="0"/></div>
</a>  <table style="text-align:center; line-height:40px; margin-top:-18px; width:100%">
  <tr>
    <td width="61px"> <img src="<%= request.getContextPath() %><spring:theme code="image.logo.white"/>" alt="css 61px by 61px logo" class="logo-reduced61" />
    </td>
	<td>
        <div class="barsmall">
        <img align="left" src="<%= request.getContextPath() %><spring:theme code="image.logo.bar"/>" alt="css bar-round" class="bar-round-reduced50"/>
        <openmrs:hasPrivilege privilege="View Navigation Menu">
				<%@ include file="/WEB-INF/template/gutter.jsp" %>
		</openmrs:hasPrivilege>
        </div>
    </td>
  </tr>
  </table>
