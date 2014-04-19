<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Manage Address Templates" otherwise="/login.htm"
                 redirect="/admin/locations/addressTemplate.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><openmrs:message code="AddressTemplate.Manage"/></h2>

<div class="boxHeader">
    <openmrs:message code="AddressTemplate.Manage"/>
</div>
<div class="box">
    <form method="post" action="addressTemplateAdd.form">
        <table>

            <tr>
                <td valign="top">
                    <textarea name="xml" rows="20" cols="60">${addressTemplateXml}</textarea><span class="required">*</span>
                </td>
                <td valign="top">
                    <span class="description"> <p><openmrs:message htmlEscape="false" code="AddressTemplate.copy.form.wiki"/>: <a
                            href="http://wiki.openmrs.org/display/docs/Administering+Address+Templates" TARGET="_blank"><openmrs:message
                            code="AddressTemplate.wiki.title"/></a></p> </span>
                </td>
            </tr>


            <tr>
                <td>
                    <input type="submit" value="<openmrs:message code="general.save"/>"/>
                    <input type="button" value="<openmrs:message code="general.cancel"/>"
                           onClick="window.location = 'addressTemplate.form'"/>
                </td>
            </tr>
        </table>
    </form>
</div>

<openmrs:extensionPoint pointId="org.openmrs.admin.locations.addressTemplateList.footer" type="html"/>

<%@ include file="/WEB-INF/template/footer.jsp" %>