<#ftl strip_whitespace=true>
<#macro renderStat stats name class=""><#assign value = stats.get(name)!0><#if (value != 0)><span class="${class}">
${value}
</span><#else>${value}</#if></#macro>
<#function zebra index>
  <#if (index % 2) == 0>
    <#return "even" />
  <#else>
    <#return "odd" />
  </#if>
</#function>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
    <title>JBehave Reports</title>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <style type="text/css" media="all">
    @import url( "./style/jbehave-core.css" );
    </style>
</head>

<body>
    <div class="clear"></div>
</div>

<div class="reports">

    <h2>Story Reports</h2>

    <table>
        <colgroup span="2" class="stories"></colgroup>
        <colgroup span="4" class="scenarios"></colgroup>
        <colgroup span="6" class="steps"></colgroup>
        <tr class="topHeader">
            <th colspan="2">Stories</th>
            <th colspan="4">Scenarios</th>
            <th colspan="6">Steps</th>
        </tr>
        <tr>
            <th class="storyNameColumn">Name</th>
            <th>Not Allowed</th>
            <th>Total</th>
            <th>Successful</th>
            <th>Failed</th>
            <th>Not Allowed</th>
            <th>Total</th>
            <th>Successful</th>
            <th>Pending</th>
            <th>Not Performed</th>
            <th>Failed</th>
            <th>Ignored</th>
        </tr>
        <#assign reportNames = reportsTable.getReportNames()>
        <#assign totalReports = reportNames.size() - 1>
        <#assign counter = 0>
        <#list reportNames as name>
        <#assign reports = reportsTable.getReport(name)>
        <#if name != "Totals">
        <#assign stats = reports.getStats()>
        <#assign notAllowed = stats.get("notAllowed")!0>
        <#if notAllowed != 0>
            <#assign rowClass = "hidden">
        <#else>
            <#assign rowClass = "">
            <#assign counter = counter+1>
        </#if>
    <tr class="${rowClass} ${zebra(counter)}">
        <#assign failed = stats.get("scenariosFailed")!0>
        <#assign storyClass = "story">
        <#if failed != 0>
        <#assign storyClass = storyClass + " failed">
        <#else>
        <#assign storyClass = storyClass + " successful">
    </#if>
    <td class="${storyClass}">
        <#assign filesByFormat = reports.filesByFormat>
        <#assign file = filesByFormat.get("html")>
        <a href="${file.name}">${reports.name}</a>
    </td>
    <td>
        <@renderStat stats "notAllowed" "failed"/>
    </td>
    <td>
        <@renderStat stats "scenarios"/>
    </td>
    <td>
        <@renderStat stats "scenariosSuccessful" "successful"/>
    </td>
    <td>
        <@renderStat stats "scenariosFailed" "failed"/>
    </td>
    <td>
        <@renderStat stats "scenariosNotAllowed" "failed"/>
    </td>
    <td>
        <@renderStat stats "steps" />
    </td>
    <td>
        <@renderStat stats "stepsSuccessful" "successful"/>
    </td>
    <td>
        <@renderStat stats "stepsPending" "pending"/>
    </td>
    <td>
        <@renderStat stats "stepsNotPerformed" "notPerformed" />
    </td>
    <td>
        <@renderStat stats "stepsFailed" "failed"/>
    </td>
    <td>
        <@renderStat stats "stepsIgnorable" "ignorable"/>
    </td>
    </tr>
    </#if>
    </#list>
<tr class="totals">
<td>${counter}</td>
<#assign stats = reportsTable.getReport("Totals").getStats()>
<td>
    <@renderStat stats "notAllowed" "failed"/>
</td>
<td>
    <@renderStat stats "scenarios"/>
</td>
<td>
    <@renderStat stats "scenariosSuccessful" "successful"/>
</td>
<td>
    <@renderStat stats "scenariosFailed" "failed"/>
</td>
<td>
    <@renderStat stats "scenariosNotAllowed" "failed"/>
</td>
<td>
    <@renderStat stats "steps" />
</td>
<td>
    <@renderStat stats "stepsSuccessful" "successful"/>
</td>
<td>
    <@renderStat stats "stepsPending" "pending"/>
</td>
<td>
    <@renderStat stats "stepsNotPerformed" "notPerformed" />
</td>
<td>
    <@renderStat stats "stepsFailed" "failed"/>
</td>
<td>
    <@renderStat stats "stepsIgnorable" "ignorable"/>
</td>
</tr>
        </table>
<br/>
        </div>

<div class="clear"></div>
<div id="footer">
<div class="left">Generated on ${date?string("dd/MM/yyyy HH:mm:ss")}</div>
<div class="clear"></div>
</div>

        </body>

        </html>