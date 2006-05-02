<input type="hidden" name="fieldId" id="fieldId" value=""/>
<table id="field">
	<tr>
		<td valign="top">
			<spring:message code="Field.name"/>
			<div id="searchForm" class="searchForm">
				<div class="wrapper">
					<div style="text-align: right">
						<input type="checkbox" id="verboseListing" <c:if test="${defaultVerbose == true}">checked</c:if> onclick="showVerbose()">
						<label for="verbose"><spring:message code="dictionary.verboseListing"/></label> &nbsp; 
						<input type="button" onClick="return closeBox();" id="closeButton" value="X"/>
					</div>
					<div id="searchResults" class="searchResults">
						<table width="100%">
							<tbody id="searchBody"></tbody>
						</table>
					</div>
				</div>
			</div>
		</td>
		<td>
			<input type="text" name="name" id="name" size="30" onKeyDown="showSearchForm(this, event)"/><br/>
			<spring:message code="general.id"/>: <span id="fieldIdDisplay"></span> &nbsp; 
			<spring:message code="Field.forms"/> <span id="forms"></span>
		</td>
	</tr>
	<tr>
		<td valign="top"><spring:message code="general.description"/></td>
		<td><textarea name="description" id="description" rows="2" cols="30"></textarea></td>
	</tr>
	<tr>
		<td><spring:message code="Field.type"/></td>
		<td>
			<select name="fieldType" id="fieldType" onChange="chooseFieldType(this.value)">
				<c:forEach items="${fieldTypes}" var="ft">
					<option value="${ft.fieldTypeId}">${ft.name}</option>
				</c:forEach>
			</select>
		</td>
	</tr>
	<tr id="concept">
		<td valign="top"><spring:message code="Field.concept"/><br/>&nbsp;</td>
		<input type="hidden" name="conceptId" id="conceptId" />
		<td id="conceptName" valign="top"></td>
	</tr>
	<tr id="database">
		<td valign="top"><spring:message code="Field.database"/></td>
		<td>
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
						<spring:message code="Field.tableName"/> <br/>
						<input type="input" name="tableName" id="tableName" size="15"/>
					</td>
					<td>
						<spring:message code="Field.attributeName"/> <br/>
						<input type="input" name="attributeName" id="attributeName" size="15"/>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr id="other">
		<td colspan="2">&nbsp;<br/>&nbsp;</td>
	</tr>
	<tr>
		<td style="white-space: nowrap;"><spring:message code="Field.selectMultiple"/></td>
		<td><input type="checkbox" name="selectMultiple" id="selectMultiple" value="on"/></td>
	</tr>
	<tr>
		<td><spring:message code="general.createdBy"/></td>
		<td id="creator"></td>
	</tr>
</table>


<hr>


<table>
	<tr>
		<td colspan="3"><spring:message code="FormField.id"/> <span id="ff_formFieldId"></span></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.parent"/></td>
		<td colspan='2'>
			<select name="parent" id="ff_parent" style="font-size: .8em"></select>
		</td>
	</tr>
</table>
<table>
	<tr>
		<td align="left"><spring:message code="FormField.fieldNumber"/></td>
		<td align="left"><spring:message code="FormField.fieldPart"/></td>
		<td align="left"><spring:message code="FormField.pageNumber"/></td>
	</tr>
	<tr>
		<td align="left"><input type="text" name="fieldNumber" size="5" id="ff_fieldNumber"/></td>
		<td align="left"><input type="text" name="fieldPart" size="5" id="ff_fieldPart" /></td>
		<td align="left"><input type="text" name="pageNumber" size="5" id="ff_pageNumber" /></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.minOccurs"/></td>
		<td><spring:message code="FormField.maxOccurs"/></td>
	</tr>
	<tr>
		<td><input type="text" name="minOccurs" size="5" id="ff_minOccurs" /></td>
		<td><input type="text" name="maxOccurs" size="5" id="ff_maxOccurs" /></td>
	</tr>
	<tr>
		<td><spring:message code="FormField.required"/></td>
		<td><input type="checkbox" name="required" id="ff_required" /></td>
	</tr>
	<tr>
		<td><spring:message code="general.createdBy" /></td>
		<td id="ff_creator" colspan="2"></td>
	</tr>
	<tr>
		<td><spring:message code="general.changedBy" /></td>
		<td id="ff_changedBy" colspan="2"></td>
	</tr>
</table>	