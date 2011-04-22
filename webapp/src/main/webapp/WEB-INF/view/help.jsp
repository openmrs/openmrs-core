<%@ include file="/WEB-INF/template/include.jsp" %>

<spring:message var="pageTitle" code="help.title" scope="page"/>
<%@ include file="/WEB-INF/template/header.jsp" %>

<h2><spring:message code="help.title"/></h2>

<br />
<spring:message code="help.wiki.text" arguments="<a href='https://wiki.openmrs.org/x/GAAJ'>,</a>"/>
<br /><br/>
<ul>
	<li><h4><spring:message code="help.overview.text" /></h4></li>
	<br/>
	<ul>
	<li><spring:message code="help.about.text" arguments="<a href='https://wiki.openmrs.org/x/3gNN'>,</a>"/></li>
	<br/>
	<li><spring:message code="help.technical.text" arguments="<a href='https://wiki.openmrs.org/x/-g4z'>,</a>"/></li>
	<br/>
	</ul>
	<li><h4><spring:message code="help.guides.text"/></h4></li>
	<br/>
	<ul>
	<li><a href="https://wiki.openmrs.org/x/GwAJ"><spring:message code="help.user.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/FgAJ"><spring:message code="help.developer.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/2YFE"><spring:message code="help.administrator.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/RAEr"><spring:message code="help.modules.text"/></a></li>
	<br/>
	<li><a href="https://wiki.openmrs.org/x/HwAJ"><spring:message code="help.troubleshoot.text"/></a></li>
	</ul>
</ul>
<br />
<spring:message code="help.contact.text"/>
<br />
<ul>
	<li><spring:message code="help.irc.text" arguments="<a href='https://wiki.openmrs.org/x/EQAP'>,</a>"/></li>
	<br>
	<li><spring:message code="help.mailing.text" arguments="<a href='https://wiki.openmrs.org/x/SQAr'>,</a>"/></li>
</ul>


<br/>

<openmrs:extensionPoint pointId="org.openmrs.help" type="html" />


<%@ include file="/WEB-INF/template/footer.jsp" %>