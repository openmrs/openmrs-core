package org.openmrs.oldreporting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;

public class Analysis {

	private final Log log = LogFactory.getLog(getClass());
	
	private List<DataFilter<Patient>> patientFilters;
	private List<DataProducer<Patient>> producers;
	private List<SeriesTransformer> seriesTransformers;
	private List<DataTableGrouper> groupers;
	private Comparator<DataRow> sorter;
	
	public Analysis() {
		patientFilters = new ArrayList<DataFilter<Patient>>();
		producers = new ArrayList<DataProducer<Patient>>();
		seriesTransformers = new ArrayList<SeriesTransformer>();
		groupers = new ArrayList<DataTableGrouper>();
	}
	
	public void addFilter(DataFilter<Patient> f) {
		patientFilters.add(f);
	}
	
	public void addProducer(DataProducer<Patient> p) {
		producers.add(p);
	}
	
	public void addSeriesTransformer(SeriesTransformer st) {
		seriesTransformers.add(st);
	}
	
	public void addGrouper(DataTableGrouper g) {
		groupers.add(g);
	}

	public void setSorter(Comparator<DataRow> s) {
		sorter = s;
	}
		
	public DataTable run(Set<Patient> input) {
		log.debug("input = " + input);
		DataSet<Patient> data = new SimpleDataSet<Patient>(input);
		for (DataFilter<Patient> f : patientFilters) {
			data = f.filter(data);
			log.debug("filtered by " + f + " to " + data);
		}
		for (DataProducer<Patient> p : producers) {
			p.produceData(data);
			log.debug("produced data with " + p);
		}
		DataTable table = new SimpleDataTable(data);
		if (seriesTransformers != null && seriesTransformers.size() > 0) {
			List<DataSeries> series = new ArrayList<DataSeries>();
			for (SeriesTransformer trans : seriesTransformers) {
				DataSeries temp = trans.transform(table);
				log.debug("transformed to series with " + trans + " to " + temp);
				series.add(temp);
			}
			table = SimpleDataTable.fromSeries("", series);
			log.debug("combined series into " + table);
		}
		for (DataTableGrouper g : groupers) {
			table = g.group(table);
			log.debug("grouped using " + g + " into " + table);
		}
		if (sorter != null) {
			table.sort(sorter);
			log.debug("sorted with " + sorter + " to " + table);
		}
		return table;
	}


	/**
	 * @return Returns the groupers.
	 */
	public List<DataTableGrouper> getGroupers() {
		return groupers;
	}

	/**
	 * @param groupers The groupers to set.
	 */
	public void setGroupers(List<DataTableGrouper> groupers) {
		this.groupers = groupers;
	}

	/**
	 * @return Returns the patientFilters.
	 */
	public List<DataFilter<Patient>> getPatientFilters() {
		return patientFilters;
	}

	/**
	 * @param patientFilters The patientFilters to set.
	 */
	public void setPatientFilters(List<DataFilter<Patient>> patientFilters) {
		this.patientFilters = patientFilters;
	}

	/**
	 * @return Returns the producers.
	 */
	public List<DataProducer<Patient>> getProducers() {
		return producers;
	}

	/**
	 * @param producers The producers to set.
	 */
	public void setProducers(List<DataProducer<Patient>> producers) {
		this.producers = producers;
	}

	/**
	 * @return Returns the seriesTransformers.
	 */
	public List<SeriesTransformer> getSeriesTransformers() {
		return seriesTransformers;
	}

	/**
	 * @param seriesTransformers The seriesTransformers to set.
	 */
	public void setSeriesTransformers(List<SeriesTransformer> seriesTransformers) {
		this.seriesTransformers = seriesTransformers;
	}

	/**
	 * @return Returns the sorter.
	 */
	public Comparator<DataRow> getSorter() {
		return sorter;
	}
	
}
