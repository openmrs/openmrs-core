package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;

public class Analysis {

	List<DataFilter<Patient>> patientFilters;
	List<DataProducer<Patient>> producers;
	List<DataTableGrouper> groupers;
	Comparator<DataRow> sorter;
	
	public Analysis() {
		patientFilters = new ArrayList<DataFilter<Patient>>();
		producers = new ArrayList<DataProducer<Patient>>();
		groupers = new ArrayList<DataTableGrouper>();
	}
	
	public void addFilter(DataFilter<Patient> f) {
		patientFilters.add(f);
	}
	
	public void addProducer(DataProducer<Patient> p) {
		producers.add(p);
	}
	
	public void addGrouper(DataTableGrouper g) {
		groupers.add(g);
	}

	public void setSorter(Comparator<DataRow> s) {
		sorter = s;
	}
		
	public DataTable run(Set<Patient> input) {
		DataSet<Patient> data = new SimpleDataSet<Patient>(input);
		for (DataFilter<Patient> f : patientFilters) {
			data = f.filter(data);
		}
		for (DataProducer<Patient> p : producers) {
			p.produceData(data);
		}
		DataTable table = new SimpleDataTable(data);
		for (DataTableGrouper g : groupers) {
			table = g.group(table);
		}
		if (sorter != null) {
			table.sort(sorter);
		}
		return table;
	}
	
}
