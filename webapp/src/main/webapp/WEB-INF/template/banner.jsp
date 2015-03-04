<div id="banner" xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:spring="http://www.springframework.org/tags"
	xmlns:openmrs="urn:jsptld:/WEB-INF/taglibs/openmrs.tld">
<a href="<%= request.getContextPath() %><spring:theme code="url.homepage" />">
  <div id="logosmall"><img src="<%= request.getContextPath() %><spring:theme code="image.logo.text.small" />" alt="OpenMRS Logo" border="0"/></div>
</a>  
<table id="bannerbar">
  <tr>
    <td id="logocell"> <img src="<%= request.getContextPath() %><spring:theme code="image.logo.small" />" alt="" class="logo-reduced61" />
    </td>
	<td id="barcell">
        <div class="barsmall" id="barsmall">
        <img align="left" src="<%= request.getContextPath() %><spring:theme code="image.logo.bar" />" alt="" class="bar-round-reduced50" id="bar-round-reduced50"/>
         <openmrs:hasPrivilege privilege="View Navigation Menu">
 				<%@ include file="/WEB-INF/template/gutter.jsp" %>
 		</openmrs:hasPrivilege>
         </div>
        <script type="text/javascript">
        	function resize(){
			document.getElementById('bar-round-reduced50').style.height = document.getElementById('barsmall').offsetHeight+"px";
        	}
        	window.onload=resize;
			window.onresize=resize;
		</script>
        </div>
    </td>
  </tr>
</table>
</div>
