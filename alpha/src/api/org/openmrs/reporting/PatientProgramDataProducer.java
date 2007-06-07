package org.openmrs.reporting;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

public class PatientProgramDataProducer extends AbstractReportObject implements PatientDataProducer {

	public enum WhichField {
		ENROLLMENT_DATE (new DataTransformer() {
				public Object transform(Object o) {
					return ((PatientProgram) o).getDateEnrolled();
				}
			}),
		COMPLETION_DATE (new DataTransformer() {
				public Object transform(Object o) {
					return ((PatientProgram) o).getDateCompleted();
				}
			});
		
		private final DataTransformer transformer;

		WhichField(DataTransformer transformer) {
			this.transformer = transformer;
		}
		
		public DataTransformer getTransformer() {
			return transformer;
		}
	}
	
	private String name;
	private Program program;
	private WhichField field;
	
	public PatientProgramDataProducer(Program program, WhichField field) {
		this.program = program;
		this.field = field;
	}
	
	public Map<Integer, Object> produceData(Collection<Integer> patientIds) {
		PatientSet ps = new PatientSet();
		ps.copyPatientIds(patientIds);
		Map<Integer, PatientProgram> programs = Context.getPatientSetService().getPatientPrograms(ps, program);
		Map<Integer, Object> ret = new HashMap<Integer, Object>();
		DataTransformer trans = field.getTransformer();
		for (Map.Entry<Integer, PatientProgram> e : programs.entrySet()) {
			ret.put(e.getKey(), trans.transform(e.getValue()));
		}
		return ret;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getReportObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setReportObjectId(Integer id) {
		// TODO Auto-generated method stub
		
	}

}
