<br />
<table>
	<tr>
		<td><spring:message code="DataExport.columnName"/></td>
		<td><input type="text" name="conceptColumnName" size="30" /></td>
	</tr>
	<tr>
		<td><spring:message code="DataExport.columnModifier"/></td>
		<td>
			<input type="radio" name="conceptModifier" value="any" checked="checked" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.columnModifier.any"/></span>
			<input type="radio" name="conceptModifier" value="first" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.columnModifier.first"/></span>
			<input type="radio" name="conceptModifier" value="mostRecent" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.columnModifier.mostRecent"/></span>
			<input type="radio" name="conceptModifier" value="mostRecentNum" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.columnModifier.mostRecentNum"/></span>		
		</td>
	</tr>
	<tr>
		<td><spring:message code="DataExport.columnValue"/></td>
		<td>
			<input type="text" name="conceptName" size="50"/>
			<input type="button" name="conceptButton" class="smallButton" 
				value="<spring:message code="general.change"/>" 
				onclick="showSearch(this, 'concept')" />
		</td>
	</tr>
	<tr>
		<td><spring:message code="DataExport.conceptExtras"/></td>
		<td>
			<input type="checkbox" name="conceptExtra" value="obsDatetime" checked="checked" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.conceptExtra.obsDatetime"/></span>
			<input type="checkbox" name="conceptExtra" value="location" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.conceptExtra.location"/></span>
			<input type="checkbox" name="conceptExtra" value="comment" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.conceptExtra.comment"/></span>
			<input type="checkbox" name="conceptExtra" value="encounterType" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.conceptExtra.encounterType"/></span>
			<input type="checkbox" name="conceptExtra" value="provider" /><span onclick="this.previousSibling.click()"><spring:message code="DataExport.conceptExtra.provider"/></span>
</table>
<br/>