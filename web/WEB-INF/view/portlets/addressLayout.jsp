<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript">

    // The originally defined onSubmit function
    // We replace this with "return false;" until all errors are fixed
    var origOnSubmit = null;

    // to know if we have overwritten the onsubmit method already
    var overwrittenOnSubmit = false;

    /**
     * Validate the input format according to the regular expression.
     * If not valid, the background is highlighted and a formatting Hint is displayed.
     *
     * @param obj the input dom object
     * @param regex regular expression defined in the localized AddressTemplate in openmrs-servlet.xml
     * @param codeName the token.codeName (e.g.: "latitude")
     */
    function validateFormat(obj, regex, codeName) {
        var formatMsg = "formatMsg_" + codeName;
        var resultArray = obj.value.match(regex);
        var tips = document.getElementsByName(formatMsg);
        if (resultArray || obj.value == null || obj.value == "") {
            obj.style.background="";
            for (var i=0; i<tips.length; i++) {
                tips[i].style.display = "none";
            }
            if (overwrittenOnSubmit) {
            	// replace the parent form's onsubmit with the one
            	// we saved because we put in a temporary "return false" in the onsubmit
            	obj.form.onsubmit = origOnSubmit;
            	origOnSubmit = null;
        		overwrittenOnSubmit = false;
            }
        }
        else {
            obj.style.background="yellow";
            for (var i=0; i<tips.length; i++) {
                tips[i].style.display = "";
            }

            if (!overwrittenOnSubmit) {
        		// this is the first time there was an error, save the current
        		// onSubmit for the form and replace it with a popup error msg
        		origOnSubmit = obj.form.onsubmit;
        		obj.form.onsubmit = function() { alert('<spring:message code="fix.error.plain" javaScriptEscape="true"/>'); return false; };
        		overwrittenOnSubmit = true;
        	}
        }
        
    }

</script>

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'columnHeaders'}">
			<c:if test="${model.layoutShowExtended == 'true'}">
				<th></th>
			</c:if>
			
			<c:forEach items="${model.layoutTemplate.lines}" var="line">
				<c:forEach items="${line}" var="token">
					<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
						<th><spring:message code="${token.displayText}"/></th>
					</c:if>
				</c:forEach>
			</c:forEach>	
		</c:when>

		<c:when test="${model.size == 'inOneRow'}">
			<tr>
				<c:if test="${model.layoutShowExtended == 'true'}">
					<td>
						<spring:bind path="preferred">
							<c:if test="${status.value}">*</c:if>
						</spring:bind>
					</td>
				</c:if>
				<c:forEach items="${model.layoutTemplate.lines}" var="line">
					<c:forEach items="${line}" var="token">
						<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
							<td>
								<spring:bind path="${token.codeName}">
									${status.value}
								</spring:bind>
							</td>
						</c:if>
					</c:forEach>
				</c:forEach>	
			</tr>
		</c:when>
		<c:when test="${model.size == 'compact'}">
			<div id="${model.portletDivName}">
				<table>
					<c:forEach items="${model.layoutTemplate.lines}" var="line">
						<tr>
							<td>
								<c:forEach items="${line}" var="token">
									<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
										<spring:bind path="${token.codeName}">
											${status.value}
										</spring:bind>
									</c:if>
									<c:if test="${token.isToken == model.layoutTemplate.nonLayoutToken}">
										${token.displayText}
									</c:if>
								</c:forEach>
							</td>
						</tr>
					</c:forEach>
				</table>
			</div>
		</c:when>
		<c:when test="${model.size == 'full'}">
			<c:choose>
				<c:when test="${model.layoutMode == 'view'}">
					<div id="${model.portletDivName}">
						<table>
							<c:forEach items="${model.layoutTemplate.lines}" var="line">
								<tr>
									<td>
										<c:forEach items="${line}" var="token">
											<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
												<spring:message code="${token.displayText}" />:
												<spring:bind path="${token.codeName}">
													${status.value}
												</spring:bind>
											</c:if>
											<c:if test="${token.isToken == model.layoutTemplate.nonLayoutToken}">
												${token.displayText}
											</c:if>
										</c:forEach>
									</td>
								</tr>
							</c:forEach>
						</table>
					</div>
				</c:when>
				<c:otherwise>
					<c:if test="${model.layoutShowTable != 'false'}">
						<div id="${model.portletDivName}">
							<table>
					</c:if>
					<c:if test="${model.layoutShowExtended == 'true'}">
							<tr>
								<td><spring:message code="general.preferred"/></td>
								<td>
									<spring:bind path="preferred">
										<input type="hidden" name="_${status.expression}">
										<input type="checkbox" name="${status.expression}" onclick="if (preferredBoxClick) preferredBoxClick(this)" value="true" alt="personAddress" <c:if test="${status.value == true}">checked</c:if> />
										<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
									</spring:bind>
								</td>
							</tr>
					</c:if>
					<c:forEach items="${model.layoutTemplate.lines}" var="line">
						<tr>
							<c:forEach items="${line}" var="token" varStatus="tokenStatus">
								<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
									<td><spring:message code="${token.displayText}" /></td>
									<td <c:if test="${tokenStatus.last && tokenStatus.index < model.layoutTemplate.maxTokens}">colspan="${model.layoutTemplate.maxTokens - tokenStatus.index}"</c:if>>
										<spring:bind path="${token.codeName}">
                                            <input type="text"   name="${status.expression}"  value="${status.value}" size="${token.displaySize}"
                                                onkeyup="<c:if test='${model.layoutTemplate.elementRegex[token.codeName] !="" }'>validateFormat(this, '${model.layoutTemplate.elementRegex[token.codeName]}','${token.codeName}' )</c:if>"
                                            />
                                            <i name="formatMsg_${token.codeName}" style="font-weight: normal; font-size: xx-small; color: red; display: none">
                                                 <c:choose>
                                                     <c:when test="${model.layoutTemplate.elementRegexFormats[token.codeName] != null }" >
                                                        (<spring:message code="general.format" />: ${model.layoutTemplate.elementRegexFormats[token.codeName]})
                                                    </c:when>
                                                    <c:otherwise>
                                                        <spring:message code="general.invalid" />&nbsp;<spring:message code="general.format" />
                                                    </c:otherwise>
                                                </c:choose>
                                            </i>
											<c:if test="${model.layoutShowErrors != 'false'}">
												<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
											</c:if>
										</spring:bind>
									</td>
								</c:if>
							</c:forEach>
						</tr>
					</c:forEach>
					<c:if test="${model.layoutShowExtended == 'true'}">
							<spring:bind path="creator">
								<c:if test="${!(status.value == null)}">
									<tr>
										<td><spring:message code="general.createdBy" /></td>
										<td colspan="4">
											${status.value.personName} -
											<openmrs:formatDate path="dateCreated" type="long" />
										</td>
									</tr>
								</c:if>
							</spring:bind>
                            <c:if test="${model.layoutHideVoidOption != 'true'}">
								<tr>
									<td><spring:message code="general.voided"/></td>
									<td>
										<spring:bind path="voided">
											<input type="hidden" name="_${status.expression}"/>
											<input type="checkbox" name="${status.expression}" 
												   <c:if test="${status.value == true}">checked="checked"</c:if> 
										</spring:bind>
												   onClick="toggleLayer('<spring:bind path="personAddressId">voidReasonRow-${status.value}</spring:bind>'); if (voidedBoxClicked) voidedBoxClicked(this); "
											/>
									</td>
								</tr>
								<tr id="<spring:bind path="personAddressId">voidReasonRow-${status.value}</spring:bind>"
									style="<spring:bind path="voided"><c:if test="${status.value == false}">display: none;</c:if></spring:bind>">
									<td><spring:message code="general.voidReason"/></td>
									<spring:bind path="voidReason">
										<td colspan="4">
											<input type="text" name="${status.expression}" value="${status.value}" size="43" />
											<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
										</td>
									</spring:bind>
								</tr>
								<spring:bind path="voidedBy">
									<c:if test="${!(status.value == null)}">
										<tr>
											<td><spring:message code="general.voidedBy" /></td>
											<td colspan="4">
												${status.value.personName} -
												<openmrs:formatDate path="dateVoided" type="long" />
											</td>
										</tr>
									</c:if>
								</spring:bind>
                            </c:if>
					</c:if>
					<c:if test="${model.layoutShowTable != 'false'}">
							</table>
						</div>
					</c:if>
				</c:otherwise>
			</c:choose>
		</c:when>
		<c:otherwise>
			<spring:message code="Portlet.addressLayout.error" arguments="${model.size}"/>
		</c:otherwise>
	</c:choose>
</c:if>
