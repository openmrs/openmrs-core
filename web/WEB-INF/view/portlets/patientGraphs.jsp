<%@ include file="/WEB-INF/template/include.jsp" %>

<c:if test="${model.authenticatedUser != null}">
	
	<h2>Patient graphs</h2> 
	<table>
		<tr>
			<td>
				<img src="/openmrs/showGraphServlet?patientId=${patient.patientId}&conceptId=5089"/><br><br>
			</td>
			<td>
				<img src="/openmrs/showGraphServlet?patientId=${patient.patientId}&conceptId=5497"/><br><br>
			</td>
		</tr>
	</table>
</c:if>