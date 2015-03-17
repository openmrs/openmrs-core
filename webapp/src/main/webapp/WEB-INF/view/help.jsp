<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:message var="pageTitle" code="help.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><openmrs:message code="help.title"/></h2>

<br />
<openmrs:message code="help.wiki.text" htmlEscape="false" arguments="<a href='https://wiki.openmrs.org/x/GAAJ'>,</a>"/>
<br /><br/>
<ul>
	<li><h4><openmrs:message code="help.overview.text" /></h4></li>
	<br/>
	<ul>
	<li><openmrs:message code="help.about.text" htmlEscape="false" arguments="<a href='https://wiki.openmrs.org/x/3gNN'>,</a>"/></li>
	<br/>
	<li><openmrs:message code="help.technical.text" htmlEscape="false" arguments="<a href='https://wiki.openmrs.org/x/-g4z'>,</a>"/></li>
	<br/>
	</ul>
	<li><h4><openmrs:message code="help.guides.text"/></h4></li>
	<br/>
	<ul>
	<li><a href="http://go.openmrs.org/guide"><openmrs:message code="help.getting.started.guide.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/GwAJ"><openmrs:message code="help.user.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/FgAJ"><openmrs:message code="help.developer.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/2YFE"><openmrs:message code="help.administrator.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/RAEr"><openmrs:message code="help.modules.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/HwAJ"><openmrs:message code="help.troubleshoot.text"/></a></li>
	</ul>
</ul>
<br />
<openmrs:message code="help.contact.text"/>
<br />
<ul>
	<li><openmrs:message code="help.irc.text" htmlEscape="false" arguments="<a href='https://wiki.openmrs.org/x/EQAP'>,</a>"/></li>
	<br>
	<li><openmrs:message code="help.mailing.text" htmlEscape="false" arguments="<a href='https://wiki.openmrs.org/display/RES/Mailing+Lists'>,</a>"/></li>
</ul>


<br/>

<openmrs:extensionPoint pointId="org.openmrs.help" type="html" />


<%@ include file="/WEB-INF/template/footer.jsp" %>