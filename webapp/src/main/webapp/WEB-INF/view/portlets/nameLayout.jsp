<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">
	<c:choose>
		<c:when test="${model.size == 'columnHeaders'}">
			<c:if test="${model.layoutShowExtended == 'true'}">
				<th></th>
			</c:if>
			
			<c:forEach items="${model.layoutTemplate.lines}" var="line">
				<c:forEach items="${line}" var="token">
					<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
						<th><openmrs:message code="${token.displayText}"/>
						    <c:if test="${token.displayText == 'PersonName.givenName' || token.displayText == 'PersonName.familyName'}">
						        <span class="required">*</span>
						    </c:if>
						</th>
					</c:if>
				</c:forEach>
			</c:forEach>	
		</c:when>
		
		<c:when test="${model.size == 'quickView'}">
			<c:if test="${model.layoutShowExtended == 'true'}">
				<spring:bind path="preferred">
					<c:if test="${status.value}">*</c:if>
				</spring:bind>
			</c:if>
			<c:forEach items="${model.layoutTemplate.lines}" var="line">
				<c:forEach items="${line}" var="token">
					<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
						<spring:bind path="${token.codeName}">
							<c:out value="${status.value}"/>
						</spring:bind>
					</c:if>
				</c:forEach>
			</c:forEach>
		</c:when>
		
		<c:when test="${model.size == 'inOneRow'}">
			<tr>
				<c:choose>
					<c:when test="${model.layoutMode == 'view'}">
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
											<c:out value="${status.value}"/>
										</spring:bind>
									</td>
								</c:if>
							</c:forEach>
						</c:forEach>
					</c:when>
					<c:otherwise>
						<c:if test="${model.layoutShowExtended == 'true'}">
							<td>
								<spring:bind path="preferred">
									<input type="hidden" name="_${status.expression}">
									<input type="checkbox" name="${status.expression}" onclick="preferredBoxClick(this)" alt="personName" value="true" <c:if test="${status.value == true}">checked</c:if> />
									<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
								</spring:bind>
							</td>
						</c:if>
						<c:forEach items="${model.layoutTemplate.lines}" var="line">
							<c:forEach items="${line}" var="token">
								<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
									<td>
										<spring:bind path="${token.codeName}">
											<c:if test="${status.value == null}">
												<input type="text" name="${status.expression}" value="${model.layoutTemplate.elementDefaults[token.codeName]}" size="${token.displaySize}" />
											</c:if>
											<c:if test="${status.value != null}">
												<input type="text" name="${status.expression}" value="<c:out value="${status.value}" />" size="${token.displaySize}" />
											</c:if>
											<c:if test="${model.layoutShowErrors != 'false'}">
												<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
											</c:if>
										</spring:bind>
									</td>
								</c:if>
							</c:forEach>
						</c:forEach>
					</c:otherwise>
				</c:choose>
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
											<c:out value="${status.value}" />
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
												<openmrs:message code="${token.displayText}" />:
												<spring:bind path="${token.codeName}">
													<c:out value="${status.value}" />
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
								<td><openmrs:message code="general.preferred"/></td>
								<td>
									<spring:bind path="preferred">
										<input type="hidden" name="_${status.expression}">
										<input type="checkbox" name="${status.expression}" onclick="if (preferredBoxClick) preferredBoxClick(this)" alt="personName" value="true" <c:if test="${status.value == true}">checked</c:if> />
										<c:if test="${status.errorMessage != ''}"><span class="error">${status.errorMessage}</span></c:if>
									</spring:bind>
								</td>
							</tr>
					</c:if>
							<c:forEach items="${model.layoutTemplate.lines}" var="line">
								<tr>
									<c:forEach items="${line}" var="token" varStatus="tokenStatus">
										<c:if test="${token.isToken == model.layoutTemplate.layoutToken}">
											<td><openmrs:message code="${token.displayText}" />
											    <c:if test="${token.displayText == 'PersonName.givenName' || token.displayText == 'PersonName.familyName'}">
											        <span class="required">*</span>
											    </c:if>
											</td>
											<td <c:if test="${tokenStatus.last && tokenStatus.index < model.layoutTemplate.maxTokens}">colspan="${model.layoutTemplate.maxTokens - tokenStatus.index}"</c:if>>
												<spring:bind path="${token.codeName}">
													<c:if test="${status.value == null}">
														<input type="text" name="${status.expression}" value="${model.layoutTemplate.elementDefaults[token.codeName]}" size="${token.displaySize}" />
													</c:if>
													<c:if test="${status.value != null}">
														<input type="text" name="${status.expression}" value="<c:out value="${status.value}" />" size="${token.displaySize}" />
													</c:if>
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
										<td><openmrs:message code="general.createdBy" /></td>
										<td colspan="4">
                                            <c:out value="${status.value.personName}" /> -
											<openmrs:formatDate path="dateCreated" type="long" />
										</td>
									</tr>
								</c:if>
							</spring:bind>
							<spring:bind path="changedBy">
								<c:if test="${!(status.value == null)}">
									<tr>
										<td><openmrs:message code="general.changedBy" /></td>
										<td colspan="4">
                                            <c:out value="${status.value.personName}" /> -
											<openmrs:formatDate path="dateChanged" type="long" />
										</td>
									</tr>
								</c:if>
							</spring:bind>
                            <c:if test="${model.layoutHideVoidOption != 'true'}">
								<tr>
									<td><openmrs:message code="general.voided"/></td>
									<td>
										<spring:bind path="voided">
											<input type="hidden" name="_${status.expression}"/>
											<input type="checkbox" name="${status.expression}" 
												   <c:if test="${status.value == true}">checked="checked"</c:if>
												   onClick="toggleLayer('<spring:bind path="personNameId">voidReasonNameRow-${status.value}</spring:bind>'); if (voidedBoxClicked) voidedBoxClicked(this); "
											/>
										</spring:bind>
									</td>
								</tr>
								<tr id="<spring:bind path="personNameId">voidReasonNameRow-${status.value}</spring:bind>"
									style="<spring:bind path="voided"><c:if test="${status.value == false}">display: none;</c:if></spring:bind>">
									<td><openmrs:message code="general.voidReason"/></td>
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
											<td><openmrs:message code="general.voidedBy" /></td>
											<td colspan="4">
                                                <c:out value="${status.value.personName}" /> -
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
			<openmrs:message code="Portlet.nameLayout.error" arguments="${model.size}"/>
		</c:otherwise>
	</c:choose>
</c:if>
