package org.openmrs.api.db.hibernate;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientName;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientSetDAO;
import org.openmrs.reporting.PatientSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HibernatePatientSetDAO implements PatientSetDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernatePatientSetDAO() { }
	
	public HibernatePatientSetDAO(Context c) {
		this.context = c;
	}
	
	public String exportXml(PatientSet ps) throws DAOException {
		// TODO: This is inefficient for large patient sets.
		StringBuffer ret = new StringBuffer("<patientset>");
		for (Integer patientId : ps.getPatientIds()) {
			ret.append(exportXml(patientId));
		}
		ret.append("</patientset>");
		return ret.toString();
	}

	private String formatUserName(User u) {
		StringBuilder sb = new StringBuilder();
		boolean any = false;
		if (u.getFirstName() != null) {
			if (any) {
				sb.append(" ");
			} else {
				any = true;
			}
			sb.append(u.getFirstName());
		}
		if (u.getMiddleName() != null) {
			if (any) {
				sb.append(" ");
			} else {
				any = true;
			}
			sb.append(u.getMiddleName());
		}
		if (u.getLastName() != null) {
			if (any) {
				sb.append(" ");
			} else {
				any = true;
			}
			sb.append(u.getLastName());
		}
		return sb.toString();
	}
	
	private String formatUser(User u) {
		StringBuilder ret = new StringBuilder();
		ret.append(u.getUserId() + "^" + formatUserName(u));
		return ret.toString();
	}

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Element obsElementHelper(Document doc, Locale locale, Obs obs) {
		Element obsNode = doc.createElement("obs");
		Concept c = obs.getConcept();

		obsNode.setAttribute("obs_id", obs.getObsId().toString());
		obsNode.setAttribute("concept_id", c.getConceptId().toString());
		obsNode.setAttribute("concept_name", c.getName(locale).getName());
		
		if (obs.getObsDatetime() != null) {
			obsNode.setAttribute("datetime", df.format(obs.getObsDatetime()));
		}
		if (obs.getAccessionNumber() != null) {
			obsNode.setAttribute("accession_number", obs.getAccessionNumber());
		}
		if (obs.getComment() != null) {
			obsNode.setAttribute("comment", obs.getComment());
		}
		if (obs.getDateStarted() != null) {
			obsNode.setAttribute("date_started", df.format(obs.getDateStarted()));
		}
		if (obs.getDateStopped() != null) {
			obsNode.setAttribute("date_stopped", df.format(obs.getDateStopped()));
		}
		if (obs.getObsGroupId() != null) {
			obsNode.setAttribute("obs_group_id", obs.getObsGroupId().toString());
		}
		if (obs.getValueGroupId() != null) {
			obsNode.setAttribute("value_group_id", obs.getValueGroupId().toString());
		}

		String value = null;
		String dataType = null;
		
		if (obs.getValueCoded() != null) {
			Concept valueConcept = obs.getValueCoded();
			obsNode.setAttribute("value_coded_id", valueConcept.getConceptId().toString());
			obsNode.setAttribute("value_coded", valueConcept.getName(locale).getName());
			dataType = "coded";
			value = valueConcept.getName(locale).getName();
		}
		if (obs.getValueAsBoolean() != null) {
			obsNode.setAttribute("value_boolean", obs.getValueAsBoolean().toString());
			dataType = "boolean";
			value = obs.getValueAsBoolean().toString();
		}
		if (obs.getValueDatetime() != null) {
			obsNode.setAttribute("value_datetime", df.format(obs.getValueDatetime()));
			dataType = "datetime";
			value = obs.getValueDatetime().toString();
		}
		if (obs.getValueNumeric() != null) {
			obsNode.setAttribute("value_numeric", obs.getValueNumeric().toString());
			dataType = "numeric";
			value = obs.getValueNumeric().toString();
		}
		if (obs.getValueText() != null) {
			obsNode.setAttribute("value_text", obs.getValueText());
			dataType = "text";
			value = obs.getValueText();
		}
		if (obs.getValueModifier() != null) {
			obsNode.setAttribute("value_modifier", obs.getValueModifier());
			if (value != null) {
				value = obs.getValueModifier() + " " + value;
			}
		}
		obsNode.setAttribute("data_type", dataType);
		obsNode.appendChild(doc.createTextNode(value));
		
		return obsNode;
	}
	
	/**
	 * Note that the formatting may depend on locale
	 */
	public String exportXml(Integer patientId) throws DAOException {
		Locale locale = context.getLocale();
		
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    Document doc = null;
	    
		PatientService patientService = context.getPatientService();
		EncounterService encounterService = context.getEncounterService();

		Patient p = patientService.getPatient(patientId);
		List<Encounter> encounters = encounterService.getEncountersByPatientId(patientId, false);
	    
	    try {
	    	DocumentBuilder builder = factory.newDocumentBuilder();
	        doc = builder.newDocument();
	        
			Element root = (Element) doc.createElement("patient_data");
			doc.appendChild(root);
			
			Element patientNode = doc.createElement("patient");
			patientNode.setAttribute("patient_id", p.getPatientId().toString());
			
			boolean firstName = true;
			Element namesNode = doc.createElement("names");
			for (PatientName name : p.getNames()) {
				if (firstName) {
					if (name.getGivenName() != null) {
						patientNode.setAttribute("given_name", name.getGivenName());
					}
					if (name.getMiddleName() != null) {
						patientNode.setAttribute("middle_name", name.getMiddleName());
					}
					if (name.getFamilyName() != null) {
						patientNode.setAttribute("family_name", name.getFamilyName());
					}
					if (name.getFamilyName2() != null) {
						patientNode.setAttribute("family_name2", name.getFamilyName2());
					}
					firstName = false;
				}
				Element nameNode = doc.createElement("name");
				if (name.getGivenName() != null) {
					nameNode.setAttribute("given_name", name.getGivenName());
				}
				if (name.getMiddleName() != null) {
					nameNode.setAttribute("middle_name", name.getMiddleName());
				}
				if (name.getFamilyName() != null) {
					nameNode.setAttribute("family_name", name.getFamilyName());
				}
				if (name.getFamilyName2() != null) {
					nameNode.setAttribute("family_name2", name.getFamilyName2());
				}
				namesNode.appendChild(nameNode);
			}
			patientNode.appendChild(namesNode);
			patientNode.setAttribute("gender", p.getGender());
			if (p.getRace() != null) {
				patientNode.setAttribute("race", p.getRace());
			}
			if (p.getBirthdate() != null) {
				patientNode.setAttribute("birthdate", df.format(p.getBirthdate()));
			}
			if (p.getBirthdateEstimated() != null) {
				patientNode.setAttribute("birthdate_estimated", p.getBirthdateEstimated().toString());
			}
			if (p.getBirthplace() != null) {
				patientNode.setAttribute("birthplace", p.getBirthplace());
			}
			if (p.getCitizenship() != null) {
				patientNode.setAttribute("citizenship", p.getCitizenship());
			}
			if (p.getTribe() != null) {
				patientNode.setAttribute("tribe", p.getTribe().getName());
			}
			if (p.getMothersName() != null) {
				patientNode.setAttribute("mothers_name", p.getMothersName());
			}
			if (p.getCivilStatus() != null) {
				patientNode.setAttribute("civil_status", p.getCivilStatus().getName(locale, false).getName());
			}
			if (p.getDeathDate() != null) {
				patientNode.setAttribute("death_date", df.format(p.getDeathDate()));
			}
			if (p.getCauseOfDeath() != null) {
				patientNode.setAttribute("cause_of_death", p.getCauseOfDeath());
			}
			if (p.getHealthDistrict() != null) {
				patientNode.setAttribute("health_district", p.getHealthDistrict());
			}
			if (p.getHealthCenter() != null) {
				patientNode.setAttribute("health_center", encounterService.getLocation(p.getHealthCenter()).getName());
				patientNode.setAttribute("health_center_id", p.getHealthCenter().toString());
			}
			
			for (Encounter e : encounters) {
				Element encounterNode = doc.createElement("encounter");
				if (e.getEncounterDatetime() != null) {
					encounterNode.setAttribute("datetime", df.format(e.getEncounterDatetime()));
				}
				
				Element metadataNode = doc.createElement("metadata");
				{
					Location l = e.getLocation();
					if (l != null) {
						Element temp = doc.createElement("location");
						temp.setAttribute("location_id", l.getLocationId().toString());
						temp.appendChild(doc.createTextNode(l.getName()));
						metadataNode.appendChild(temp);
					}
					EncounterType t = e.getEncounterType();
					if (t != null) {
						Element temp = doc.createElement("encounter_type");
						temp.setAttribute("encounter_type_id", t.getEncounterTypeId().toString());
						temp.appendChild(doc.createTextNode(t.getName()));
						metadataNode.appendChild(temp);
					}
					Form f = e.getForm();
					if (f != null) {
						Element temp = doc.createElement("form");
						temp.setAttribute("form_id", f.getFormId().toString());
						temp.appendChild(doc.createTextNode(f.getName()));
						metadataNode.appendChild(temp);
					}
					User u = e.getProvider();
					if (u != null) {
						Element temp = doc.createElement("provider");
						temp.setAttribute("provider_id", u.getUserId().toString());
						temp.appendChild(doc.createTextNode(formatUserName(u)));
						metadataNode.appendChild(temp);
					}
				}
				encounterNode.appendChild(metadataNode);

				Collection<Obs> observations = e.getObs();
				if (observations != null && observations.size() > 0) {
					Element observationsNode = doc.createElement("observations");
					for (Obs obs : observations) {
						Element obsNode = obsElementHelper(doc, locale, obs);
						observationsNode.appendChild(obsNode);
					}
					encounterNode.appendChild(observationsNode);
				}
				
				Set<Order> orders = e.getOrders();
				if (orders != null && orders.size() != 0) {
					Element ordersNode = doc.createElement("orders");
					for (Order order : orders) {
						Element orderNode = doc.createElement("order");
						orderNode.setAttribute("order_id", order.getOrderId().toString());
						orderNode.setAttribute("order_type", order.getOrderType().getName());

						Concept concept = order.getConcept();
						orderNode.setAttribute("concept_id", concept.getConceptId().toString());
						orderNode.appendChild(doc.createTextNode(concept.getName(locale).getName()));

						if (order.getInstructions() != null) {
							orderNode.setAttribute("instructions", order.getInstructions());
						}
						if (order.getStartDate() != null) {
							orderNode.setAttribute("start_date", df.format(order.getStartDate()));
						}
						if (order.getAutoExpireDate() != null) {
							orderNode.setAttribute("auto_expire_date", df.format(order.getAutoExpireDate()));
						}
						if (order.getOrderer() != null) {
							orderNode.setAttribute("orderer", formatUser(order.getOrderer()));
						}
						if (order.isDiscontinued() != null) {
							orderNode.setAttribute("discontinued", order.isDiscontinued().toString());
						}
						if (order.getDiscontinuedDate() != null) {
							orderNode.setAttribute("discontinued_date", df.format(order.getDiscontinuedDate()));
						}
						if (order.getDiscontinuedReason() != null) {
							orderNode.setAttribute("discontinued_reason", order.getDiscontinuedReason());
						}

						ordersNode.appendChild(orderNode);
					}
				}
				
				patientNode.appendChild(encounterNode);
			}
			
			ObsService obsService = context.getObsService();
			Set<Obs> allObservations = obsService.getObservations(p);
			if (allObservations != null && allObservations.size() > 0) {
				log.debug("allObservations has " + allObservations.size() + " obs");
				Set<Obs> undoneObservations = new HashSet<Obs>();
				for (Obs obs : allObservations) {
					if (obs.getEncounter() == null) {
						undoneObservations.add(obs);
					}
				}
				log.debug("undoneObservations has " + undoneObservations.size() + " obs");

				if (undoneObservations.size() > 0) {
					Element observationsNode = doc.createElement("observations");
					for (Obs obs : undoneObservations) {
						Element obsNode = obsElementHelper(doc, locale, obs);
						observationsNode.appendChild(obsNode);
						log.debug("added node " + obsNode + " to observationsNode");
					}
					patientNode.appendChild(observationsNode);
				}
			}

			// TODO: put in orders that don't belong to any encounter
			
			root.appendChild(patientNode);

	    } catch (Exception ex) {
			throw new DAOException(ex);
		}
				
		String ret = null;

		try {
			Source source = new DOMSource(doc);
			StringWriter sw = new StringWriter();
			Result result = new StreamResult(sw);
			
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, result);
			ret = sw.toString();
		} catch (Exception ex) {
			throw new DAOException(ex);
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public PatientSet getAllPatients() {
		Session session = HibernateUtil.currentSession();
		Query query = session.createQuery("select distinct patientId from Patient p where p.voided = 0");
		
		Set<Integer> ids = new HashSet<Integer>();
		ids.addAll(query.list());
		
		PatientSet patientSet = new PatientSet();
		patientSet.setPatientIds(ids);
		
		return patientSet;
	}
	
	@SuppressWarnings("unchecked")
	public PatientSet getPatientsHavingNumericObs(Integer conceptId, PatientSetService.Modifier modifier, Number value) {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		
		Query query;
		StringBuffer sb = new StringBuffer();
		sb.append("select patient_id from obs o " +
				"where concept_id = :concept_id ");
		boolean useVal = false;
		if (value != null && modifier != PatientSetService.Modifier.EXISTS) {
			sb.append("and value_numeric " + modifier.getSqlRepresentation() + " :value ");
			useVal = true;
		} else {
			sb.append("and value_numeric is not null ");
		}
		sb.append("group by patient_id ");
		query = session.createSQLQuery(sb.toString());
		query.setInteger("concept_id", conceptId);
		if (useVal) {
			query.setDouble("value", value.doubleValue());
		}

		PatientSet ret = new PatientSet();
		List patientIds = query.list();
		ret.setPatientIds(new HashSet<Integer>(patientIds));

		HibernateUtil.commitTransaction();
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate) throws DAOException {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		
		Query query;
		
		StringBuffer queryString = new StringBuffer("select patientId from Patient patient");
		List<String> clauses = new ArrayList<String>();

		clauses.add("patient.voided = false");
		
		if (gender != null) {
			gender = gender.toUpperCase();
			clauses.add("patient.gender = :gender");
		}
		if (minBirthdate != null) {
			clauses.add("patient.birthdate >= :minBirthdate");
		}
		if (maxBirthdate != null) {
			clauses.add("patient.birthdate <= :maxBirthdate");
		}
		
		boolean first = true;
		for (String clause : clauses) {
			if (first) {
				queryString.append(" where ").append(clause);
				first = false;
			} else {
				queryString.append(" and ").append(clause);
			}
		}
		query = session.createQuery(queryString.toString());
		if (gender != null) {
			query.setString("gender", gender);
		}
		if (minBirthdate != null) {
			query.setDate("minBirthdate", minBirthdate);
		}
		if (maxBirthdate != null) {
			query.setDate("maxBirthdate", maxBirthdate);
		}
		
		List<Integer> patientIds = query.list();
		
		PatientSet ret = new PatientSet();
		ret.setPatientIds(new HashSet<Integer>(patientIds));

		HibernateUtil.commitTransaction();
		
		return ret;
	}

	private static final long MS_PER_YEAR = 365l * 24 * 60 * 60 * 1000l; 
	
	@SuppressWarnings("unchecked")
	public Map<Integer, String> getShortPatientDescriptions(PatientSet patients) throws DAOException {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		
		Map<Integer, String> ret = new HashMap<Integer, String>();
		
		Set<Integer> ids = patients.getPatientIds();
		Query query = session.createQuery("select patient.patientId, patient.gender, patient.birthdate from Patient patient");
		
		List<Object[]> temp = query.list();
		
		long now = System.currentTimeMillis();
		for (Object[] results : temp) {
			if (!ids.contains(results[0])) { continue; }
			StringBuffer sb = new StringBuffer();
			if ("M".equals(results[1])) {
				sb.append("Male");
			} else {
				sb.append("Female");
			}
			Date bd = (Date) results[2];
			if (bd != null) {
				int age = (int) ((now - bd.getTime()) / MS_PER_YEAR);
				sb.append(", ").append(age).append(" years old");
			}
			ret.put((Integer) results[0], sb.toString()); 
		}
		
		HibernateUtil.commitTransaction();
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Map<String, Object>> getCharacteristics(PatientSet patients) throws DAOException {
		Map<Integer, Map<String, Object>> ret = new HashMap<Integer, Map<String, Object>>();

		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		Set<Integer> ids = patients.getPatientIds();
		Query query = session.createQuery("select patient.patientId, patient.gender, patient.birthdate from Patient patient");
		List<Object[]> temp = query.list();

		long now = System.currentTimeMillis();
		for (Object[] results : temp) {
			Integer patientId = (Integer) results[0];
			if (!ids.contains(patientId)) { continue; }
			Map<String, Object> holder = new HashMap<String, Object>();
			holder.put("gender", results[1]);
			Date bd = (Date) results[2];
			if (bd != null) {
				int age = (int) ((now - bd.getTime()) / MS_PER_YEAR);
				holder.put("age_years", age);
				holder.put("birthdate", bd);
			}
			ret.put(patientId, holder); 
		}

		HibernateUtil.commitTransaction();
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, List<Obs>> getObservations(PatientSet patients, Concept concept) throws DAOException {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();
		
		Map<Integer, List<Obs>> ret = new HashMap<Integer, List<Obs>>();
		
		Set<Integer> ids = patients.getPatientIds();
		
		/*
		Query query = session.createQuery("select obs, obs.patientId " +
										  "from Obs obs where obs.conceptId = :conceptId " +
										  " and obs.patientId in :ids " +
										  "order by obs.obsDatetime asc");
		query.setInteger("conceptId", conceptId);
		query.set
	
		List<Object[]> temp = query.list();
		for (Object[] holder : temp) {
			Obs obs = (Obs) holder[0];
			Integer ptId = (Integer) holder[1];
			List<Obs> forPatient = ret.get(ptId);
			if (forPatient == null) {
				forPatient = new ArrayList<Obs>();
				ret.put(ptId, forPatient);
			}
			forPatient.add(obs);
		}
		*/
		Criteria criteria = session.createCriteria(Obs.class);
		criteria.add(Restrictions.eq("concept", concept));
		criteria.add(Restrictions.in("patient.patientId", ids));
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(org.hibernate.criterion.Order.desc("obsDatetime"));
		log.debug("criteria: " + criteria);
		List<Obs> temp = criteria.list();
		for (Obs obs : temp) {
			Integer ptId = obs.getPatient().getPatientId();
			List<Obs> forPatient = ret.get(ptId);
			if (forPatient == null) {
				forPatient = new ArrayList<Obs>();
				ret.put(ptId, forPatient);
			}
			forPatient.add(obs);
		}
				
		HibernateUtil.commitTransaction();
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Encounter> getEncountersByType(PatientSet patients, EncounterType encType) {
		Session session = HibernateUtil.currentSession();
		
		Map<Integer, Encounter> ret = new HashMap<Integer, Encounter>();
		
		Set<Integer> ids = patients.getPatientIds();
		
		// default query
		Criteria criteria = session.createCriteria(Encounter.class);
		criteria.add(Restrictions.in("patient.patientId", ids));
		criteria.add(Restrictions.eq("voided", false));
		
		if (encType != null)
			criteria.add(Restrictions.eq("encounterType", encType));
		
		criteria.addOrder(org.hibernate.criterion.Order.desc("patient.patientId"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("encounterDatetime"));
		
		List<Encounter> encounters = criteria.list();
		
		// set up the return map
		for (Encounter enc : encounters) {
			Integer ptId = enc.getPatient().getPatientId();
			if (!ret.containsKey(ptId))
				ret.put(ptId, enc);
		}
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Object> getPatientAttributes(PatientSet patients, String className, String property, boolean returnAll) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		Map<Integer, Object> ret = new HashMap<Integer, Object>();
		
		Set<Integer> ids = patients.getPatientIds();
		
		className = "org.openmrs." + className;
		
		// default query
		Criteria criteria = session.createCriteria(className);
		
		// make 'patient.**' reference 'patient' like alias instead of object
		if (className.equals("org.openmrs.Patient"))
			criteria = session.createCriteria(className, "patient");
		
		// set up the query
		criteria.setProjection(Projections.projectionList().add(
				Projections.property("patient.patientId")).add(
				Projections.property(property)));
		criteria.add(Restrictions.in("patient.patientId", ids));
		criteria.addOrder(org.hibernate.criterion.Order.desc("voided"));
		
		// add 'preferred' sort order if necessary
		try {
			boolean hasPreferred = false;
			for(Field f : Class.forName(className).getDeclaredFields()) {
				if (f.getName().equals("preferred"))
					hasPreferred = true;
			}
			
			if (hasPreferred)
				criteria.addOrder(org.hibernate.criterion.Order.desc("preferred"));
		} catch (ClassNotFoundException e) {
			log.warn("Class not found: " + className);
		}
		
		criteria.addOrder(org.hibernate.criterion.Order.desc("dateCreated"));
		List<Object[]> rows = criteria.list();
		
		// set up the return map
		if (returnAll) {
			for (Object[] row : rows) {
				Integer ptId = (Integer)row[0];
				Object columnValue = row[1];
				if (!ret.containsKey(ptId)) {
					Object[] arr = {columnValue};
					ret.put(ptId, arr);
				}
				else {
					Object[] oldArr = (Object[])ret.get(ptId);
					Object[] newArr = new Object[oldArr.length + 1];
					System.arraycopy(oldArr,0,newArr,0,oldArr.length);
					newArr[oldArr.length] = columnValue;
					ret.put(ptId, newArr);
				}
			}
		}
		else {
			for (Object[] row : rows) {
				Integer ptId = (Integer)row[0];
				Object columnValue = row[1];
				if (!ret.containsKey(ptId))
					ret.put(ptId, columnValue);
			}
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public PatientSet getPatientsHavingTextObs(Integer conceptId, String value) throws DAOException {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();

		Query query;
		StringBuffer sb = new StringBuffer();
		sb.append("select patient_id from obs o " +
				"where concept_id = :concept_id ");
		boolean useVal = false;
		if (value != null) {
			sb.append("and value_text = :value ");
			useVal = true;
		} else {
			sb.append("and value_text is not null ");
		}
		sb.append("group by patient_id ");
		query = session.createSQLQuery(sb.toString());
		query.setInteger("concept_id", conceptId);
		if (useVal) {
			query.setString("value", value);
		}

		PatientSet ret = new PatientSet();
		List patientIds = query.list();
		ret.setPatientIds(new HashSet<Integer>(patientIds));

		HibernateUtil.commitTransaction();
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public PatientSet getPatientsHavingLocation(Integer locationId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		HibernateUtil.beginTransaction();

		Query query;
		StringBuffer sb = new StringBuffer();
		sb.append("select distinct patient_id from encounter e " +
				"where location_id = :location_id ");
		query = session.createSQLQuery(sb.toString());
		query.setInteger("location_id", locationId);

		PatientSet ret = new PatientSet();
		List<Integer> patientIds = query.list();
		ret.setPatientIds(new HashSet<Integer>(patientIds));

		HibernateUtil.commitTransaction();
		
		return ret;
	}
	
}
