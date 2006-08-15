<%@ include file="/WEB-INF/template/include.jsp" %>
	
	<div class="boxHeader">Graphs</div>
	<div class="box">
		<table width="100%">
			<tr>
				<td align="center">
					<img src="/openmrs/showGraphServlet?patientId=${patient.patientId}&conceptId=<openmrs:globalProperty key="weight"/>&width=500&height=300"/>
				</td>
			</tr>
			<tr>
				<td align="center">
					<img src="/openmrs/showGraphServlet?patientId=${patient.patientId}&conceptId=<openmrs:globalProperty key="cd4.count"/>&width=500&height=300"/>
				</td>
			</tr>
		</table>
	</div>	
	
