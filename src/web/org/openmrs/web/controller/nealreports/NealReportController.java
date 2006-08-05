package org.openmrs.web.controller.nealreports;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientSet;
import org.openmrs.web.WebConstants;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.AbstractView;

import reports.ReportMaker;
import reports.keys.General;
import reports.keys.Hiv;
import reports.keys.TB;

public class NealReportController implements Controller {

	protected final Log log = LogFactory.getLog(getClass());
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (context == null) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "auth.session.expired");
			response.sendRedirect(request.getContextPath() + "/logout");
		}

		String reportType = request.getParameter("reportType");
		
		ReportMaker maker = new ReportMaker();
		maker.setParameter("report_type", reportType);
		
		Locale locale = context.getLocale();
		ConceptService cs = context.getConceptService();
		PatientSetService pss = context.getPatientSetService();
		
		String patientSetParameter = request.getParameter("patientIds");
		PatientSet ps;
		if (patientSetParameter != null && patientSetParameter.length() > 0) {
			ps = PatientSet.parseCommaSeparatedPatientIds(patientSetParameter.trim());
		} else {
			ps = pss.getAllPatients();
		}
		
		Map<Integer, Map<String, String>> patientDataHolder = new HashMap<Integer, Map<String, String>>();
		
		List<String> attributesToGet = new ArrayList<String>();
		Map<String, String> attributeNamesForReportMaker = new HashMap<String, String>();
		attributeHelper(attributesToGet, attributeNamesForReportMaker, "Patient.patientId", General.ID);
		attributeHelper(attributesToGet, attributeNamesForReportMaker, "PatientName.givenName", General.FIRST_NAME);
		attributeHelper(attributesToGet, attributeNamesForReportMaker, "PatientName.familyName", General.LAST_NAME);
		attributeHelper(attributesToGet, attributeNamesForReportMaker, "Patient.birthdate", General.BIRTHDAY);
		attributeHelper(attributesToGet, attributeNamesForReportMaker, "Patient.gender", General.SEX);
		
		// General.SITE
		// General.HIV_POSITIVE_P
		// General.TB_ACTIVE_P
		// Hiv.TREATMENT_STATUS
		// General.ADDRESS
		// General.ENROLL_DATE
		// General.USER_ID (this is actually patient identifier)
		// General.DUE_DATE if pregnant
		// Hiv.ACCOMP_FIRST_NAME
		// Hiv.ACCOMP_LAST_NAME
		// General.PREGNANT_P
		// General.PMTCT (get meds for ptme? ask CA)
		// General.FORMER_GROUP (previous ARV group)
		
		// --- for every arv drug, addDynamic() a holder with
		// Hiv.OBS_TYPE == Hiv.ARV or "arv"
		// General.DOSE_PER_DAY == total dose per day == ddd
		// Hiv.OBS_DATE == start date of arvs
		// Hiv.ARV == the name of the drug as 3-letter abbreviation, or whatever   
		// "stop_date" == stop date for ARVS
		// "ddd_quotient"  == number of times taken per day (i think) 
		// "strength_unit" == unit for strength, e.g, "tab"
		// "strength_dose" == amount given per time
		
		// --- for every tb drug, addDynamic() a holder with
		// Hiv.OBS_TYPE == TB.TB_REGIMEN or "atb"
		// TB.TB_REGIMEN == the name of the drug
		// General.DOSE_PER_DAY == total dose per day == ddd
		// Hiv.OBS_DATE == start date of arvs
		// "stop_date" == stop date for ARVS
		// "ddd_quotient"  == number of times taken per day (i think) 
		// "strength_unit" == unit for strength, e.g, "tab"
		// "strength_dose" == amount given per time
				
		List<Concept> conceptsToGet = new ArrayList<Concept>();
		Map<Concept, String> namesForReportMaker = new HashMap<Concept, String>();
		conceptHelper(cs, conceptsToGet, namesForReportMaker, "ANTIRETROVIRAL TREATMENT GROUP", Hiv.TREATMENT_GROUP);
		conceptHelper(cs, conceptsToGet, namesForReportMaker, "TUBERCULOSIS TREATMENT GROUP", TB.TB_GROUP);
		conceptHelper(cs, conceptsToGet, namesForReportMaker, "CURRENT WHO HIV STAGE", Hiv.WHO_STAGE);
		
		List<Concept> dynamicConceptsToGet = new ArrayList<Concept>();
		Map<Concept, String> obsTypesForDynamicConcepts = new HashMap<Concept, String>();
		dynamicConceptHelper(cs, dynamicConceptsToGet, obsTypesForDynamicConcepts, "WEIGHT (KG)", "weight");
		//dynamicConceptHelper(cs, dynamicConceptsToGet, obsTypesForDynamicConcepts, "HEIGHT (CM)", "height");
		dynamicConceptHelper(cs, dynamicConceptsToGet, obsTypesForDynamicConcepts, "CD4 COUNT", Hiv.CD4COUNT);
		//dynamicConceptHelper(cs, dynamicConceptsToGet, obsTypesForDynamicConcepts, "CD4%", Hiv.CD4PERCENT);
		
		long l = System.currentTimeMillis();

		for (String attr : attributesToGet) {
			String nameToUse = attributeNamesForReportMaker.get(attr);
			Map<Integer, Object> temp = pss.getPatientAttributes(ps, attr, false);
			for (Map.Entry<Integer, Object> e : temp.entrySet()) {
				Integer ptId = e.getKey();
				Map<String, String> holder = (Map<String, String>) patientDataHolder.get(ptId);
				if (holder == null) {
					holder = new HashMap<String, String>();
					patientDataHolder.put(ptId, holder);
				}
				if (e.getValue() != null) {
					Object obj = e.getValue();
					String valToUse = null;
					if (obj instanceof Date) {
						valToUse = formatDate((Date) obj);
					} else {
						valToUse = obj.toString();
					}
					holder.put(nameToUse, valToUse);
				}
			}
		}
		
		log.debug("Pulled attributesToGet in " + (System.currentTimeMillis() - l) + " ms");
		l = System.currentTimeMillis();
		
		for (Concept c : conceptsToGet) {
			long l1 = System.currentTimeMillis();
			String nameToUse = namesForReportMaker.get(c);
			Map<Integer, List<Obs>> temp = pss.getObservations(ps, c);
			long l2 = System.currentTimeMillis();
			for (Map.Entry<Integer, List<Obs>> e : temp.entrySet()) {
				Integer ptId = e.getKey();
				Map<String, String> holder = (Map<String, String>) patientDataHolder.get(ptId);
				if (holder == null) {
					holder = new HashMap<String, String>();
					patientDataHolder.put(ptId, holder);
				}
				holder.put(nameToUse, e.getValue().get(0).getValueAsString(locale));
			}
			long l3 = System.currentTimeMillis();
			log.debug("\t" + nameToUse + " " + c + " step 1: " + (l2 - l1) + " ms. step 2: " + (l3 - l2) + " ms.");
		}
		
		log.debug("Pulled conceptsToGet in " + (System.currentTimeMillis() - l) + " ms");
		l = System.currentTimeMillis();
		
		for (Concept c : dynamicConceptsToGet) {
			long l1 = System.currentTimeMillis();
			String typeToUse = obsTypesForDynamicConcepts.get(c);
			Map<Integer, List<Obs>> temp = pss.getObservations(ps, c);
			long l2 = System.currentTimeMillis();
			for (Map.Entry<Integer, List<Obs>> e : temp.entrySet()) {
				Integer ptId = e.getKey();
				List<Obs> obs = e.getValue();
				for (Obs o : obs) {
					Map<String, String> holder = new HashMap<String, String>();
					holder.put(General.ID, ptId.toString());
					holder.put(Hiv.OBS_DATE, formatDate(o.getObsDatetime()));
					holder.put(Hiv.RESULT, o.getValueAsString(locale));
					holder.put(Hiv.OBS_TYPE, typeToUse);
					maker.addDynamic(holder);
				}
			}
			long l3 = System.currentTimeMillis();
			log.debug("\t" + typeToUse + " " + c + " step 1: " + (l2 - l1) + " ms. step 2: " + (l3 - l2) + " ms.");
		}
		
		log.debug("Pulled dynamicConceptsToGet in " + (System.currentTimeMillis() - l) + " ms");
		l = System.currentTimeMillis();
		
		// hack for demo in capetown using Kenya data
		{
			// arv start date
			Map<Integer, List<Obs>> observs = pss.getObservations(ps, context.getConceptService().getConcept(1255));
			for (Map.Entry<Integer, List<Obs>> e : observs.entrySet()) {
				Date date = null;
				for (Obs observ : e.getValue()) {
					if (observ.getValueCoded().getConceptId() == 1256) {
						date = observ.getObsDatetime();
						if (date == null) {
							date = observ.getEncounter().getEncounterDatetime();
						}
						break;
					}
				}
				if (date != null) {
					patientDataHolder.get(e.getKey()).put(Hiv.FIRST_ARV_DATE, formatDate(date));
				}
			}
			
			// tb tx start date
			observs = pss.getObservations(ps, context.getConceptService().getConcept(1268));
			for (Map.Entry<Integer, List<Obs>> e : observs.entrySet()) {
				Date date = null;
				for (Obs observ : e.getValue()) {
					if (observ.getValueCoded().getConceptId() == 1256) {
						date = observ.getObsDatetime();
						if (date == null) {
							date = observ.getEncounter().getEncounterDatetime();
						}
						break;
					}
				}
				if (date != null) {
					patientDataHolder.get(e.getKey()).put(TB.FIRST_TB_REGIMEN_DATE, formatDate(date));
				}
			}

			// location of most recent encounter
			Map<Integer, Encounter> encs = pss.getEncountersByType(ps, null);
			for (Map.Entry<Integer, Encounter> e : encs.entrySet()) {
				String locName = null;
				Location encLocation = e.getValue().getLocation();
				if (encLocation != null) {
					locName = encLocation.getName();
				}
				if (locName != null && locName.length() > 0) {
					patientDataHolder.get(e.getKey()).put(General.SITE, locName);
				}
			}
		}

		int cnt = 0;
		for (Map<String, String> patient : patientDataHolder.values()) {
			// patient.put("BIRTH_YEAR", "1978");
			patient.put(General.HIV_POSITIVE_P, "t");
			maker.addStatic(patient);
		}
		
		log.debug("Loaded data into report-maker in " + (System.currentTimeMillis() - l) + " ms");
		l = System.currentTimeMillis();
	   
	    File dir = new File("NEAL_REPORT_DIR");
	    dir.mkdir();
	    String filename = maker.generateReport(dir.getAbsolutePath() + "/");
	    
	    log.debug("ran maker.generateReport() in " + (System.currentTimeMillis() - l) + " ms");
		l = System.currentTimeMillis();
	    
	    Map<String, Object> model = new HashMap<String, Object>();
	    model.put("dir", dir);
	    model.put("filename", filename);
	    
	    AbstractView view = new AbstractView() {
				protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
					File f = new File((File) model.get("dir"), (String) model.get("filename"));
					response.setHeader("Content-Disposition", "attachment; filename=" + f.getName());
					response.setHeader("Pragma", "no-cache");
					response.setContentType("application/pdf");
					response.setContentLength((int) f.length());
					BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
					BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));				
					for (int i = in.read(); i >= 0; i = in.read()) {
						out.write(i);
					}
					in.close();
					out.flush();
					f.delete();
				}
	    	};
	    
	    return new ModelAndView(view, model);
	}

	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private String formatDate(Date d) {
		return d == null ? null : df.format(d);
	}

	private void dynamicConceptHelper(ConceptService cs, List<Concept> dynamicConceptsToGet, Map<Concept, String> obsTypesForDynamicConcepts,
			String conceptName, String typeToUse) {
		Concept c = cs.getConceptByName(conceptName);
		if (c == null) {
			throw new IllegalArgumentException("Cannot find concept named " + conceptName);
		}
		dynamicConceptsToGet.add(c);
		obsTypesForDynamicConcepts.put(c, typeToUse);
	}

	private void attributeHelper(List<String> attributesToGet, Map<String, String> attributeNamesForReportMaker, String attrName, String nameForReportMaker) {
		attributesToGet.add(attrName);
		attributeNamesForReportMaker.put(attrName, nameForReportMaker);
	}

	private void conceptHelper(ConceptService cs, List<Concept> conceptsToGet, Map<Concept, String> namesForReportMaker, String conceptName, String nameForReportMaker) {
		Concept c = cs.getConceptByName(conceptName);
		if (c == null) {
			throw new IllegalArgumentException("Cannot find concept named " + conceptName);
		}
		conceptsToGet.add(c);
		namesForReportMaker.put(c, nameForReportMaker);
	}

}
