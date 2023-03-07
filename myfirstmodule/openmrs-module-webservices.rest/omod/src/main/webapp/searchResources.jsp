<%@ include file="/WEB-INF/template/include.jsp"%>

<style type="text/css">
	table.queryTable, table.queryTable th, table.queryTable td
	{
		border: solid 1px gray;
		  border-collapse: collapse;
	}
	
</style>

<c:forEach var="resource" items="${searchHandlersData}">
	
<h3>
  ${resource.resourceName} 
</h3>
      
     <ul>
		<li>
		   <b><spring:message code="webservices.rest.help.url"/>: </b> ${resource.resourceURL}
		</li>
		<li>
		  OpenMRS versions: 
		  <c:forEach var="ver" items="${resource.supportedVersions}" varStatus="status">


                    
		            <c:set value="${status.count}" var="size"></c:set>
		             ${ver}
		            <c:if test="${ status.index != size - 1 }"> ,</c:if>
		  </c:forEach>
		</li>
	    <li>
	       <spring:message code="webservices.rest.help.availableHandlers"/> :
	          <ol type="1">
	         <c:forEach var="searchQuery" items="${resource.searchQueriesDoc}">
	             <li>
	               <table class="queryTable">
	                  <tr>
	                  <th><spring:message code="webservices.rest.help.requiredParameters"/></th>
	                  <th> <spring:message code="webservices.rest.help.optionalParameters"/></th>
	                  <th> <spring:message code="webservices.rest.help.description"/></th>
	                 </tr>
	                 <tr>
	                   <td>
	                     <c:forEach var="requiredParameter" items="${searchQuery.requiredParameters}">
	                               ${requiredParameter}<br>
	                     </c:forEach>
	                   </td>
	                   	<td>
	                   	 <c:forEach var="optionalParameter" items="${searchQuery.optionalParameters}">
	                              ${optionalParameter}<br>
	                     </c:forEach>
	                   </td>
	                    <td>
	                    ${searchQuery.description}
	                   </td>
	                  </tr>
	               </table>
	             </li>
	         </c:forEach>
	          </ol>
		</li>
    </ul>
</c:forEach>