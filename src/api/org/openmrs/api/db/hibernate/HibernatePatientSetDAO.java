package org.openmrs.api.db.hibernate;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

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
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientName;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.Modifier;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientSetDAO;
import org.openmrs.reporting.PatientSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class HibernatePatientSetDAO implements PatientSetDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate sessionFactory.getCurrentSession() factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernatePatientSetDAO() { }
	
	/**
	 * Set sessionFactory.getCurrentSession() factory
	 * 
	 * @param sessionFactory.getCurrentSession()Factory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
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
		Locale locale = Context.getLocale();
		
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    Document doc = null;
	    
		PatientService patientService = Context.getPatientService();
		EncounterService encounterService = Context.getEncounterService();

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
				patientNode.setAttribute("cause_of_death", p.getCauseOfDeath().getName(locale, false).getName());
			}
			if (p.getHealthDistrict() != null) {
				patientNode.setAttribute("health_district", p.getHealthDistrict());
			}
			if (p.getHealthCenter() != null) {
				patientNode.setAttribute("health_center", p.getHealthCenter().getName());
				patientNode.setAttribute("health_center_id", p.getHealthCenter().getLocationId().toString());
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
						if (order.getDiscontinued() != null) {
							orderNode.setAttribute("discontinued", order.getDiscontinued().toString());
						}
						if (order.getDiscontinuedDate() != null) {
							orderNode.setAttribute("discontinued_date", df.format(order.getDiscontinuedDate()));
						}
						if (order.getDiscontinuedReason() != null) {
							orderNode.setAttribute("discontinued_reason", order.getDiscontinuedReason().getName(locale, false).getName());
						}

						ordersNode.appendChild(orderNode);
					}
				}
				
				patientNode.appendChild(encounterNode);
			}
			
			ObsService obsService = Context.getObsService();
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
		
		Query query = sessionFactory.getCurrentSession().createQuery("select distinct patientId from Patient p where p.voided = 0 order by patientId");
		
		List<Integer> ids = new ArrayList<Integer>();
		ids.addAll(query.list());
		
		PatientSet patientSet = new PatientSet();
		patientSet.setPatientIds(ids);
		
		return patientSet;
	}

	public PatientSet getPatientsHavingObs(Integer conceptId, PatientSetService.TimeModifier timeModifier, PatientSetService.Modifier modifier, Object value, Date fromDate, Date toDate) {
		Concept concept = Context.getConceptService().getConcept(conceptId);
		Number numericValue = null;
		String stringValue = null;
		Concept codedValue = null;
		String valueSql = null;
		if (value != null) {
			if (concept.getDatatype().getHl7Abbreviation().equals("NM")) {
				if (value instanceof Number)
					numericValue = (Number) value;
				else
					numericValue = new Double(value.toString());
				valueSql = "o.value_numeric";
			} else if (concept.getDatatype().getHl7Abbreviation().equals("ST")) {
				stringValue = value.toString();
				valueSql = "o.value_text";
				if (modifier == null)
					modifier = Modifier.EQUAL;
			} else if (concept.getDatatype().getHl7Abbreviation().equals("CWE")) {
				if (value instanceof Concept)
					codedValue = (Concept) value;
				else
					codedValue = Context.getConceptService().getConceptByName(value.toString());
				valueSql = "o.value_coded";
			}
		}

		Query query;
		StringBuilder sb = new StringBuilder();
		boolean useValue = value != null;
		boolean doSqlAggregation = timeModifier == TimeModifier.MIN || timeModifier == TimeModifier.MAX || timeModifier == TimeModifier.AVG;
		boolean doInvert = false;
		
		String dateSql = "";
		if (fromDate != null)
			dateSql += " and o.obs_datetime >= :fromDate ";
		if (toDate != null)
			dateSql += " and o.obs_datetime <= :toDate ";

		if (timeModifier == TimeModifier.ANY || timeModifier == TimeModifier.NO) {
			if (timeModifier == TimeModifier.NO)
				doInvert = true;
			sb.append("select o.patient_id from obs o " +
					"where concept_id = :concept_id ");
			sb.append(dateSql);

		} else if (timeModifier == TimeModifier.FIRST || timeModifier == TimeModifier.LAST) {
			boolean isFirst = timeModifier == PatientSetService.TimeModifier.FIRST;
			sb.append("select o.patient_id " +
					"from obs o inner join (" +
					"    select patient_id, " + (isFirst ? "min" : "max") + "(obs_datetime) as obs_datetime" +
					"    from obs" +
					"    where concept_id = :concept_id " +
					dateSql +
					"    group by patient_id" +
					") subq on o.patient_id = subq.patient_id and o.obs_datetime = subq.obs_datetime " +
					"where o.concept_id = :concept_id ");	

		} else if (doSqlAggregation) {
			String sqlAggregator = timeModifier.toString();
			valueSql = sqlAggregator + "(" + valueSql + ")";
			sb.append("select o.patient_id " +
					"from obs o where concept_id = :concept_id " +
					dateSql +
					"group by o.patient_id ");

		} else {
			throw new IllegalArgumentException("TimeModifier '" + timeModifier + "' not recognized");
		}

		if (useValue) {
			sb.append(doSqlAggregation ? "having " : " and ");
			sb.append(valueSql + " ");
			sb.append(modifier.getSqlRepresentation() + " :value");
		}
		if (!doSqlAggregation)
			sb.append(" group by o.patient_id ");
		
		log.debug("query: " + sb);
		query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
		query.setInteger("concept_id", conceptId);
		if (useValue) {
			if (numericValue != null)
				query.setDouble("value", numericValue.doubleValue());
			else if (codedValue != null)
				query.setInteger("value", codedValue.getConceptId());
			else if (stringValue != null)
				query.setString("value", stringValue);
			else
				throw new IllegalArgumentException("useValue is true, but numeric, coded, and string values are all null");
		}
		if (fromDate != null)
			query.setDate("fromDate", fromDate);
		if (toDate != null)
			query.setDate("toDate", fromDate);

		PatientSet ret;
		if (doInvert) {
			ret = getAllPatients();
			ret.removeAllIds(query.list());
		} else {
			ret = new PatientSet();
			List patientIds = query.list();
			ret.setPatientIds(new ArrayList<Integer>(patientIds));
		}

		return ret;
	}
	
	/**
	 * Gets all patients with an obs's value_date column value within <code>startTime</code>
	 * and <code>endTime</code>
	 *  
	 * @param conceptId
	 * @param startTime
	 * @param endTime
	 * @return PatientSet
	 */
	@SuppressWarnings("unchecked")
	public PatientSet getPatientsHavingDateObs(Integer conceptId, Date startTime, Date endTime) {
		Query query;
		StringBuffer sb = new StringBuffer();
		sb.append("select o.patient_id from obs o " +
		"where concept_id = :concept_id ");
		sb.append(" and o.value_datetime between :startValue and :endValue");
		sb.append(" and o.voided = 0");
		
		query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
		query.setInteger("concept_id", conceptId);
		query.setDate("startValue", startTime);
		query.setDate("endValue", endTime);
		
		PatientSet ret = new PatientSet();
		List patientIds = query.list();
		ret.setPatientIds(new ArrayList<Integer>(patientIds));

		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public PatientSet getPatientsHavingNumericObs(Integer conceptId, PatientSetService.TimeModifier timeModifier, PatientSetService.Modifier modifier, Number value, Date fromDate, Date toDate) {
		
		Concept concept = Context.getConceptService().getConcept(conceptId);
		if (!concept.isNumeric()) {
			// throw new IllegalArgumentException(concept + " is not numeric");
		}
		
		Query query;
		StringBuffer sb = new StringBuffer();
		boolean useValue = modifier != null && value != null;
		boolean doSqlAggregation = timeModifier == TimeModifier.MIN || timeModifier == TimeModifier.MAX || timeModifier == TimeModifier.AVG;
		String valueSql = "o.value_numeric";
		boolean doInvert = false;
		
		String dateSql = "";
		if (fromDate != null)
			dateSql += " and o.obs_datetime >= :fromDate ";
		if (toDate != null)
			dateSql += " and o.obs_datetime <= :toDate ";
		
		if (timeModifier == TimeModifier.ANY || timeModifier == TimeModifier.NO) {
			if (timeModifier == TimeModifier.NO)
				doInvert = true;
			sb.append("select o.patient_id from obs o " +
					"where concept_id = :concept_id ");
			sb.append(dateSql);
		} else if (timeModifier == TimeModifier.FIRST || timeModifier == TimeModifier.LAST) {
			boolean isFirst = timeModifier == PatientSetService.TimeModifier.FIRST;
			sb.append("select o.patient_id " +
					"from obs o inner join (" +
					"    select patient_id, " + (isFirst ? "min" : "max") + "(obs_datetime) as obs_datetime" +
					"    from obs" +
					"    where concept_id = :concept_id " +
					dateSql +
					"    group by patient_id" +
					") subq on o.patient_id = subq.patient_id and o.obs_datetime = subq.obs_datetime " +
					"where o.concept_id = :concept_id ");		
		} else if (doSqlAggregation) {
			String sqlAggregator = timeModifier.toString();
			valueSql = sqlAggregator + "(o.value_numeric)";
			sb.append("select o.patient_id " +
					"from obs o where concept_id = :concept_id " +
					dateSql +
					"group by o.patient_id ");
		} else {
			throw new IllegalArgumentException("TimeModifier '" + timeModifier + "' not recognized");
		}
		
		if (useValue) {
			sb.append(doSqlAggregation ? "having " : " and ");
			sb.append(valueSql + " ");
			sb.append(modifier.getSqlRepresentation() + " :value");
		}
		if (!doSqlAggregation)
			sb.append(" group by o.patient_id ");
		
		log.debug("query: " + sb);
		query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
		query.setInteger("concept_id", conceptId);
		if (useValue) {
			query.setDouble("value", value.doubleValue());
		}
		if (fromDate != null)
			query.setDate("fromDate", fromDate);
		if (toDate != null)
			query.setDate("toDate", fromDate);

		PatientSet ret;
		if (doInvert) {
			ret = getAllPatients();
			ret.removeAllIds(query.list());
		} else {
			ret = new PatientSet();
			List patientIds = query.list();
			ret.setPatientIds(new ArrayList<Integer>(patientIds));
		}

		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate,
			Integer minAge, Integer maxAge, Boolean aliveOnly, Boolean deadOnly) throws DAOException {
		
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
		if (aliveOnly != null && aliveOnly) {
			clauses.add("patient.dead = false");
		}
		if (deadOnly != null && deadOnly) {
			clauses.add("patient.dead = true");
		}

		Date maxBirthFromAge = null;
		if (minAge != null) {
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.YEAR, -minAge);
			maxBirthFromAge = cal.getTime();
			clauses.add("patient.birthdate <= :maxBirthFromAge");
		}
		Date minBirthFromAge = null;
		if (maxAge != null) {
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.YEAR, -(maxAge + 1));
			minBirthFromAge = cal.getTime();
			clauses.add("patient.birthdate > :minBirthFromAge");
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
		query = sessionFactory.getCurrentSession().createQuery(queryString.toString());
		if (gender != null) {
			query.setString("gender", gender);
		}
		if (minBirthdate != null) {
			query.setDate("minBirthdate", minBirthdate);
		}
		if (maxBirthdate != null) {
			query.setDate("maxBirthdate", maxBirthdate);
		}
		if (minAge != null) {
			query.setDate("maxBirthFromAge", maxBirthFromAge);
		}
		if (maxAge != null) {
			query.setDate("minBirthFromAge", minBirthFromAge);
		}
		
		List<Integer> patientIds = query.list();
		
		PatientSet ret = new PatientSet();
		ret.setPatientIds(new ArrayList<Integer>(patientIds));

		return ret;
	}

	private static final long MS_PER_YEAR = 365l * 24 * 60 * 60 * 1000l; 
	
	@SuppressWarnings("unchecked")
	public Map<Integer, String> getShortPatientDescriptions(Collection<Integer> patientIds) throws DAOException {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		
		Query query = sessionFactory.getCurrentSession().createQuery("select patient.patientId, patient.gender, patient.birthdate from Patient patient");
		
		List<Object[]> temp = query.list();
		
		long now = System.currentTimeMillis();
		for (Object[] results : temp) {
			if (!patientIds.contains(results[0])) { continue; }
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
		
		
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Map<String, Object>> getCharacteristics(PatientSet patients) throws DAOException {
		Map<Integer, Map<String, Object>> ret = new HashMap<Integer, Map<String, Object>>();
		Collection<Integer> ids = patients.getPatientIds();
		Query query = sessionFactory.getCurrentSession().createQuery("select patient.patientId, patient.gender, patient.birthdate from Patient patient");
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

		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * fromDate and toDate are both inclusive
	 * TODO: finish this. 
	 */
	public Map<Integer, List<Obs>> getObservations(PatientSet patients, Concept concept, Date fromDate, Date toDate) throws DAOException {
		Map<Integer, List<Obs>> ret = new HashMap<Integer, List<Obs>>();
		
		Collection<Integer> ids = patients.getPatientIds();
		
		/*
		Query query = sessionFactory.getCurrentSession().createQuery("select obs, obs.patientId " +
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
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.add(Restrictions.eq("concept", concept));
		criteria.add(Restrictions.in("patient.patientId", ids));
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(org.hibernate.criterion.Order.desc("obsDatetime"));
		log.debug("criteria: " + criteria);
		List<Obs> temp = criteria.list();
		for (Obs obs : temp) {
			Integer ptId = obs.getPatientId();
			List<Obs> forPatient = ret.get(ptId);
			if (forPatient == null) {
				forPatient = new ArrayList<Obs>();
				ret.put(ptId, forPatient);
			}
			forPatient.add(obs);
		}
		
		return ret;
	}
	
	public Map<Integer, List<Object>> getObservationsValues(PatientSet patients, Concept c, String attribute) {
		Map<Integer, List<Object>> ret = new HashMap<Integer, List<Object>>();
		
		Collection<Integer> ids = patients.getPatientIds();
		if (ids.size() == 0)
			return ret;
		
		String className = "";
		
		List<String> columns = new Vector<String>();
		columns.add(attribute);
		
		if (attribute == null) {
			columns = findObsValueColumnName(c);
			//log.debug("c: " + c.getConceptId() + " attribute: " + attribute);
		}
		else if (attribute.equals("valueDatetime")) {
			// pass -- same column name
		}
		else if (attribute.equals("obsDatetime")) {
			// pass -- same column name
		}
		else if (attribute.equals("location")) {
			// pass -- same column name
		}
		else if (attribute.equals("comment")) {
			// pass -- same column name
		}
		else if (attribute.equals("encounterType")) {
			className = "encounter";
		}
		else if (attribute.equals("provider")) {
			className = "encounter";
		}
		else {
			throw new DAOException("Attribute: " + attribute + " is not recognized. Please add reference in " + this.getClass());
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria("org.openmrs.Obs", "obs");
		
		String aliasName = "obs";
		
		if (className.length() > 0) {
			aliasName = "other";
			criteria.createAlias("obs.encounter", aliasName);
		}
		
		// set up the query
		ProjectionList projections = Projections.projectionList();
		projections.add(Projections.property("obs.patientId"));
		for (String col : columns)
			projections.add(Projections.property(aliasName + "." + col));
		criteria.setProjection(projections);
		
		if (ids.size() != getAllPatients().size())
			criteria.add(Restrictions.in("obs.patientId", ids));
		
		criteria.add(Expression.eq("obs.concept", c));
		criteria.add(Expression.eq("obs.voided", false));
		
		criteria.addOrder(org.hibernate.criterion.Order.desc("obs.obsDatetime"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("obs.voided"));
		
		log.debug("criteria: " + criteria);
		
		List<Object[]> rows = criteria.list();
		
		// set up the return map
		for (Object[] row : rows) {
			//log.debug("row[0]: " + row[0] + " row[1]: " + row[1] + (row.length > 2 ? " row[2]: " + row[2] : ""));
			Integer ptId = (Integer)row[0];
			
			// get the first non-null value column
			int index = 1;
			Object columnValue = null;
			while (index < row.length && columnValue == null)
				columnValue = row[index++];
			
			if (!ret.containsKey(ptId)) {
				List<Object> arr = new Vector<Object>();
				arr.add(columnValue);
				ret.put(ptId, arr);
			}
			else {
				List<Object> oldArr = ret.get(ptId);
				oldArr.add(columnValue);
				ret.put(ptId, oldArr);
			}
		}
		
		return ret;
		
	}
	
	// TODO this should be in some sort of central place...but where?
	public static List<String> findObsValueColumnName(Concept c) {
		String abbrev = c.getDatatype().getHl7Abbreviation();
		List<String> columns = new Vector<String>();
		
		if (abbrev.equals("BIT"))
			columns.add("valueNumeric");
		else if (abbrev.equals("CWE")) {
			columns.add("valueDrug");
			columns.add("valueCoded");
		}
		else if (abbrev.equals("NM") || abbrev.equals("SN"))
			columns.add("valueNumeric");
		else if (abbrev.equals("DT") || abbrev.equals("TM") || abbrev.equals("TS"))
			columns.add("valueDatetime");
		else if (abbrev.equals("ST"))
			columns.add("valueText");
		
		return columns;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Encounter> getEncountersByType(PatientSet patients, EncounterType encType) {
		Map<Integer, Encounter> ret = new HashMap<Integer, Encounter>();
		
		Collection<Integer> ids = patients.getPatientIds();
		
		// default query
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		criteria.add(Restrictions.in("patient.patientId", ids));
		criteria.add(Restrictions.eq("voided", false));
		
		if (encType != null)
			criteria.add(Restrictions.eq("encounterType", encType));
		
		criteria.addOrder(org.hibernate.criterion.Order.desc("patient.patientId"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("encounterDatetime"));
		
		List<Encounter> encounters = criteria.list();
		
		// set up the return map
		for (Encounter enc : encounters) {
			Integer ptId = enc.getPatientId();
			if (!ret.containsKey(ptId))
				ret.put(ptId, enc);
		}
		
		return ret;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Encounter> getFirstEncountersByType(PatientSet patients, EncounterType encType) {
		Map<Integer, Encounter> ret = new HashMap<Integer, Encounter>();
		
		Collection<Integer> ids = patients.getPatientIds();
		
		// default query
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		criteria.add(Restrictions.in("patient.patientId", ids));
		criteria.add(Restrictions.eq("voided", false));
		
		if (encType != null)
			criteria.add(Restrictions.eq("encounterType", encType));
		
		criteria.addOrder(org.hibernate.criterion.Order.desc("patient.patientId"));
		criteria.addOrder(org.hibernate.criterion.Order.asc("encounterDatetime"));
		
		List<Encounter> encounters = criteria.list();
		
		// set up the return map
		for (Encounter enc : encounters) {
			Integer ptId = enc.getPatientId();
			if (!ret.containsKey(ptId))
				ret.put(ptId, enc);
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Object> getPatientAttributes(PatientSet patients, String className, String property, boolean returnAll) throws DAOException {
		Map<Integer, Object> ret = new HashMap<Integer, Object>();
		
		Collection<Integer> ids = patients.getPatientIds();
		if (ids.size() == 0)
			return ret;
		
		className = "org.openmrs." + className;
		
		// default query
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(className);
		
		// make 'patient.**' reference 'patient' like alias instead of object
		if (className.equals("org.openmrs.Patient"))
			criteria = sessionFactory.getCurrentSession().createCriteria(className, "patient");
		
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
		log.debug("criteria: " + criteria);
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
		query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
		query.setInteger("concept_id", conceptId);
		if (useVal) {
			query.setString("value", value);
		}

		PatientSet ret = new PatientSet();
		List patientIds = query.list();
		ret.setPatientIds(new ArrayList<Integer>(patientIds));

		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public PatientSet getPatientsHavingLocation(Integer locationId) throws DAOException {
		Query query;
		StringBuffer sb = new StringBuffer();
		sb.append("select patient_id from Patient p " +
				"where health_center = :location_id ");
		query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
		query.setInteger("location_id", locationId);

		PatientSet ret = new PatientSet();
		List<Integer> patientIds = query.list();
		ret.setPatientIds(new ArrayList<Integer>(patientIds));
		
		return ret;
	}

	public PatientSet convertPatientIdentifier(List<String> identifiers) throws DAOException {
		
		Query query;
		StringBuffer sb = new StringBuffer();
		sb.append("select distinct(patient_id) from patient_identifier p " +
				"where identifier in (:identifiers) ");
		query = sessionFactory.getCurrentSession().createSQLQuery(sb.toString());
		query.setParameterList("identifiers", identifiers, new StringType());
		PatientSet ret = new PatientSet();
		List<Integer> patientIds = query.list();
		ret.setPatientIds(new ArrayList<Integer>(patientIds));
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public List<Patient> getPatients(Collection<Integer> patientIds) throws DAOException {
		List<Patient> ret = new ArrayList<Patient>();
		
		if (!patientIds.isEmpty()) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
			criteria.add(Restrictions.in("patientId", patientIds));
			criteria.add(Restrictions.eq("voided", false));
			log.debug("criteria: " + criteria);
			List<Patient> temp = criteria.list();
			for (Patient p : temp) {
				ret.add(p);
			}
		}
		
		return ret;
	}
	
	/**
	 * Returns a Map from patientId to a Collection of drugIds for drugs active for the patients on that date
	 * If patientIds is null then do this for all patients
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer, Collection<Integer>> getActiveDrugIds(Collection<Integer> patientIds, Date onDate) throws DAOException {
		HashSet<Integer> idsLookup = patientIds == null ? null :
			(patientIds instanceof HashSet ? (HashSet<Integer>) patientIds : new HashSet<Integer>(patientIds));
		if (onDate == null) {
			onDate = new Date();
		}
		Map<Integer, Collection<Integer>> ret = new HashMap<Integer, Collection<Integer>>();
		
		
		String sql = "select patient_id, drug_inventory_id " +
				"from encounter e" +
				"    inner join orders o on e.encounter_id = o.encounter_id " +
				"    inner join drug_order d on o.order_id = d.order_id " +
				"where o.start_date <= :onDate" +
				"  and (o.auto_expire_date is null or o.auto_expire_date > :onDate) " +
				"  and (o.discontinued_date is null or o.discontinued_date > :onDate) ";
		log.debug("onDate=" + onDate + " sql= " + sql);
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		query.setDate("onDate", onDate);
		List<Object[]> results = (List<Object[]>) query.list();
		for (Object[] row : results) {
			Integer patientId = (Integer) row[0];
			if (idsLookup == null || idsLookup.contains(patientId)) {
				Integer drugId = (Integer) row[1];
				Collection<Integer> drugIds = ret.get(patientId);
				if (drugIds == null) {
					drugIds = new HashSet<Integer>();
					ret.put(patientId, drugIds);
				}
				drugIds.add(drugId);
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, PatientState> getCurrentStates(PatientSet ps, ProgramWorkflow wf) throws DAOException {
		Map<Integer, PatientState> ret = new HashMap<Integer, PatientState>();
		Collection<Integer> ids = ps.getPatientIds();
		if (ids.size() == 0)
			return ret;
		
		Date now = new Date();
			
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientState.class);
		//criteria.add(Restrictions.in("patientProgram.patient.patientId", ids));
		criteria.createCriteria("patientProgram").add(Restrictions.in("patient.patientId", ids));
		//criteria.add(Restrictions.eq("state.programWorkflow", wf));
		criteria.createCriteria("state").add(Restrictions.eq("programWorkflow", wf));
		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.or(Restrictions.isNull("startDate"), Restrictions.le("startDate", now)));
		criteria.add(Restrictions.or(Restrictions.isNull("endDate"), Restrictions.ge("endDate", now)));
		log.debug("criteria: " + criteria);
		List<PatientState> temp = criteria.list();
		for (PatientState state : temp) {
			Integer ptId = state.getPatientProgram().getPatient().getPatientId();
			ret.put(ptId, state);
		}
				
		return ret;
	}

	/**
	 * This method assumes the patient is not simultaneously enrolled in the program more than once.
	 * if (includeVoided == true) then include voided programs
	 * if (includePast == true) then include program which are already complete
	 * In all cases this only returns the latest program enrollment for each patient.
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer, PatientProgram> getPatientPrograms(PatientSet ps, Program program,
			boolean includeVoided, boolean includePast) throws DAOException {
		Map<Integer, PatientProgram> ret = new HashMap<Integer, PatientProgram>();
		Collection<Integer> ids = ps.getPatientIds();
		if (ids.size() == 0)
			return ret;
		
		Date now = new Date();
			
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PatientProgram.class);
		criteria.add(Restrictions.in("patient.patientId", ids));
		criteria.add(Restrictions.eq("program", program));
		if (!includeVoided)
			criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.or(Restrictions.isNull("dateEnrolled"), Restrictions.le("dateEnrolled", now)));
		if (!includePast)
			criteria.add(Restrictions.or(Restrictions.isNull("dateCompleted"), Restrictions.ge("dateCompleted", now)));
		log.debug("criteria: " + criteria);
		List<PatientProgram> temp = criteria.list();
		for (PatientProgram prog : temp) {
			Integer ptId = prog.getPatient().getPatientId(); 
			ret.put(ptId, prog);
		}
				
		return ret;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, List<DrugOrder>> getCurrentDrugOrders(PatientSet ps, List<Concept> drugConcepts) throws DAOException {
		Map<Integer, List<DrugOrder>> ret = new HashMap<Integer, List<DrugOrder>>();
		Collection<Integer> ids = ps.getPatientIds();
		if (ids.size() == 0)
			return ret;
		
		Date now = new Date();

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DrugOrder.class);
		//criteria.add(Restrictions.in("encounter.patient.patientId", ids));
		criteria.createCriteria("encounter").add(Restrictions.in("patient.patientId", ids));
		if (drugConcepts != null)
			criteria.add(Restrictions.in("concept", drugConcepts));
		criteria.add(Restrictions.eq("voided", false));
		criteria.add(Restrictions.le("startDate", now));
		criteria.add(Restrictions.or(
						Restrictions.and(Restrictions.eq("discontinued", false), Restrictions.or(Restrictions.isNull("autoExpireDate"), Restrictions.gt("autoExpireDate", now))),
						Restrictions.and(Restrictions.eq("discontinued", true), Restrictions.gt("discontinuedDate", now))
				));
		criteria.addOrder(org.hibernate.criterion.Order.asc("startDate"));
		log.debug("criteria: " + criteria);
		List<DrugOrder> temp = criteria.list();
		for (DrugOrder regimen : temp) {
			Integer ptId = regimen.getEncounter().getPatientId();
			List<DrugOrder> list = ret.get(ptId);
			if (list == null) {
				list = new ArrayList<DrugOrder>();
				ret.put(ptId, list);
			}
			list.add(regimen);
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, List<DrugOrder>> getDrugOrders(PatientSet ps, List<Concept> drugConcepts) throws DAOException {
		Map<Integer, List<DrugOrder>> ret = new HashMap<Integer, List<DrugOrder>>();
		Collection<Integer> ids = ps.getPatientIds();
		if (ids.size() == 0)
			return ret;
		
		Date now = new Date();

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DrugOrder.class);
		//criteria.add(Restrictions.in("encounter.patient.patientId", ids));
		criteria.createCriteria("encounter").add(Restrictions.in("patient.patientId", ids));
		if (drugConcepts != null)
			criteria.add(Restrictions.in("concept", drugConcepts));
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(org.hibernate.criterion.Order.asc("startDate"));
		log.debug("criteria: " + criteria);
		List<DrugOrder> temp = criteria.list();
		for (DrugOrder regimen : temp) {
			Integer ptId = regimen.getEncounter().getPatientId();
			List<DrugOrder> list = ret.get(ptId);
			if (list == null) {
				list = new ArrayList<DrugOrder>();
				ret.put(ptId, list);
			}
			list.add(regimen);
		}
		return ret;
	}
	
	// TODO: Reimplement this method if we revise the meanings/names of the relationship fields
	@SuppressWarnings("unchecked")
	public Map<Integer, List<Relationship>> getRelationships(PatientSet ps, RelationshipType relType) {
		Map<Integer, List<Relationship>> ret = new HashMap<Integer, List<Relationship>>();
		Collection<Integer> ids = ps.getPatientIds();
		if (ids.size() == 0)
			return ret;
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Relationship.class);
		if (relType != null)
			criteria.add(Restrictions.eq("relationship", relType));
		//criteria.add(Restrictions.in("relative.patient.patientId", ids));
		criteria.createCriteria("relative").add(Restrictions.in("patient.patientId", ids));
		criteria.add(Restrictions.eq("voided", false));
		log.debug("criteria: " + criteria);
		List<Relationship> temp = criteria.list();
		for (Relationship rel : temp) {
			Integer ptId = rel.getRelative().getPatient().getPatientId();
			List<Relationship> rels = ret.get(ptId);
			if (rels == null) {
				rels = new ArrayList<Relationship>();
				ret.put(ptId, rels);
			}
			rels.add(rel);
		}
		return ret;
	}
	
}
