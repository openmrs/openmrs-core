<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Address Templates" otherwise="/login.htm"
                 redirect="/admin/locations/addressTemplate.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="AddressTemplate.Manage"/></h2>

<div class="boxHeader">
    <spring:message code="AddressTemplate.add"/>
</div>
<div class="box">
    <form method="post" action="addressTemplateAdd.form">
        <table>

            <tr>
                <td valign="top">
                    <textarea name="xml" rows="20" cols="60">${addressTemplateXml}</textarea>
                </td>
                <td valign="top">
                    <span class="description"> <p><spring:message code="AddressTemplate.copy.form.wiki"/>: <a
                            href="http://wiki.openmrs.org/display/docs/Administering+Address+Templates" TARGET="_blank"><spring:message
                            code="AddressTemplate.wiki.title"/></a></p> </span>
                </td>
            </tr>


            <tr>
                <td>
                    <input type="submit" value="<spring:message code="general.save"/>"/>
                    <input type="button" value="<spring:message code="general.cancel"/>"
                           onClick="window.location = 'addressTemplate.form'"/>
                </td>
            </tr>
        </table>
    </form>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.addressTemplateList.footer" type="html"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>