package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;

public class LocationFilter extends AbstractPatientFilter implements PatientFilter {
	
	public enum Method {
		LAST_ENCOUNTER,
		ANY_ENCOUNTER,
		PATIENT_TABLE;
	}
	
	private Location location;
	private Method method;

	public LocationFilter() {
		method = Method.PATIENT_TABLE;
	}
	
	public boolean isReadyToRun() {
		return getMethod() != null && getLocation() != null;
	}
	
	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Method getMethod() {
		return method;
	}
	
	public void setMethod(Method method) {
		this.method = method;
	}
	
	private PatientSet filterHelper(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		String locIdStr = location.getLocationId().toString();
		if (method == Method.PATIENT_TABLE) {
			List<Integer> ret = new ArrayList<Integer>();
			Map<Integer, Object> temp = service.getPersonAttributes(input, "Health Center", "Location", "locationId", "locationId", false);
			for (Map.Entry<Integer, Object> e : temp.entrySet()) {
				if (e.getValue() == null)
					continue;
				if (locIdStr.equals(e.getValue().toString()))
					ret.add(e.getKey());
			}
			return new PatientSet().copyPatientIds(ret);
		} else {
			throw new IllegalArgumentException(method + " NOT YET IMPLEMENTED");
		}
		
	}

	public PatientSet filter(PatientSet input) {
		return input.intersect(filterHelper(input));
	}

	public PatientSet filterInverse(PatientSet input) {
		return input.subtract(filterHelper(input));
	}

	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		if (method.equals(Method.PATIENT_TABLE))
			sb.append("Health center");
		else if (method.equals(Method.LAST_ENCOUNTER))
			sb.append("Last encounter at");
		else if (method.equals(Method.ANY_ENCOUNTER))
			sb.append("Any encounter at");
		sb.append(": ").append(location);
		return sb.toString();
	}
}
