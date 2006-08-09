<%@ include file="/WEB-INF/template/include.jsp" %>
	
	<div class="boxHeader">Graphs</div>
	<div class="box">
		<table>
			<tr>
				<td>
					<img src="/openmrs/showGraphServlet?patientId=${patient.patientId}&conceptId=5089&width=500&height=300"/>
				</td>
			</tr>
			<tr>
				<td>
					<img src="/openmrs/showGraphServlet?patientId=${patient.patientId}&conceptId=5497&width=500&height=300"/>
				</td>
			</tr>
		</table>
	</div>	
	
