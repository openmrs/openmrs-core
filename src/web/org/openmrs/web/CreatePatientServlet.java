package org.openmrs.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientName;
import org.openmrs.api.PatientService;
import org.openmrs.context.Context;

public class CreatePatientServlet extends HttpServlet {

	public static final long serialVersionUID = 1123432457L;

	/**
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession httpSession = request.getSession();
		Context context = (Context)httpSession.getAttribute(Constants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		if (context == null) {
			httpSession.setAttribute(Constants.OPENMRS_ERROR_ATTR, "Your session has expired.");
			response.sendRedirect(request.getContextPath() + "/logout");
			return;
		}
		PatientService ps = context.getPatientService();

		// =Identifier===
		String identifier  = request.getParameter("identifier");
		String identifierType = request.getParameter("identifierType");
		String identifierLoc  = request.getParameter("identifierLocation");
		if (identifier == null || identifierType == null || identifierLoc == null)
			throw new ServletException("Illegal patient identifier");
		PatientIdentifier pIdent = new PatientIdentifier();
		pIdent.setIdentifier(identifier);
		pIdent.setIdentifierType(ps.getPatientIdentifierType(Integer.valueOf(identifierType)));
		pIdent.setLocation(ps.getLocation(Integer.valueOf(identifierLoc)));
		
		// =Address======
		PatientAddress pAdd = null;
		String address1 = request.getParameter("address1");
		String address2 = request.getParameter("address2");
		String cityVillage = request.getParameter("cityVillage");
		String stateProvince = request.getParameter("stateProvince");
		String country   = request.getParameter("country");
		String latitude  = request.getParameter("latitude");
		String longitude = request.getParameter("longitude");
		if (address1 != null || address2 != null || cityVillage != null || 
				stateProvince != null || country != null ||
				latitude != null || longitude != null) {
			pAdd = new PatientAddress();
			pAdd.setAddress1(address1);
			pAdd.setAddress2(address2);
			pAdd.setCityVillage(cityVillage);
			pAdd.setStateProvince(stateProvince);
			pAdd.setCountry(country);
			pAdd.setLatitude(latitude);
			pAdd.setLongitude(longitude);
		}
		
		// =Name=========
		PatientName pName = new PatientName();
		String givenName = request.getParameter("givenName");
		String familyNamePrefix = request.getParameter("familyNamePrefix");
		String familyName = request.getParameter("familyName");
		String familyName2= request.getParameter("familyName2");
		String familyNameSuffix = request.getParameter("familyNameSuffix");
		if (givenName == "")
			throw new ServletException("Patient contains illegal given name");
		if (familyNamePrefix == "" && familyName == "" && familyName2 == "" && familyNameSuffix == "")
			throw new ServletException("Patient contains illegal family name");
		pName.setPreferred(Boolean.valueOf(request.getParameter("preferred")));
		pName.setGivenName(givenName);
		pName.setMiddleName(request.getParameter("middleName"));
		pName.setFamilyNamePrefix(familyNamePrefix);
		pName.setFamilyName(familyName);
		pName.setFamilyName2(familyName2);
		pName.setFamilyNameSuffix(familyNameSuffix);
		pName.setDegree(request.getParameter("degree"));
		
		// =Patient======
		Patient patient = new Patient();
		patient.setGender(request.getParameter("gender"));
		patient.setRace(request.getParameter("race"));
		patient.setBirthdateEstimated(Boolean.valueOf(request.getParameter("birthdateEstimated")));
		patient.setBirthplace(request.getParameter("birthplace"));
		patient.setTribe(ps.getTribe(Integer.valueOf(request.getParameter("tribe"))));
		patient.setCitizenship(request.getParameter("citizenship"));
		patient.setMothersName(request.getParameter("mothersName"));
		patient.setCivilStatus(Integer.valueOf(request.getParameter("civilStatus")));
		patient.setCauseOfDeath(request.getParameter("causeOfDeath"));
		patient.setHealthDistrict(request.getParameter("healthDistrict"));
		patient.setHealthCenter(Integer.valueOf(request.getParameter("healthCenter")));
		patient.addAddress(pAdd);
		patient.addIdentifier(pIdent);
		patient.addName(pName);
		
		try {
			patient.setBirthdate(DateFormat.getDateInstance(DateFormat.SHORT).parse(request.getParameter("birthdate")));
		}
		catch (ParseException e) {
			throw new ServletException("Unable to parse birthdate");
		}
		String deathDate = request.getParameter("deathDate");
		if (deathDate != "") {
			try {
				patient.setDeathDate(DateFormat.getDateInstance().parse(deathDate));
			}
			catch (ParseException e) {
				throw new ServletException("Unable to parse the death date");
			}
		}

		if (context.isAuthenticated()) {
			ps.createPatient(patient);
			httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "Patient '" + patient.getPatientId() + "' created");
			response.sendRedirect("index.jsp?patientId=" + patient.getPatientId());
			//request.getRequestDispatcher("createPatientForm.jsp").forward(request, response);
			return;
		}
	}
}
