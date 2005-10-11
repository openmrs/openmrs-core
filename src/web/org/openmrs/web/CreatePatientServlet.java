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
		PatientService ps = context.getPatientService();

		// =Identifier===
		PatientIdentifier pIdent = new PatientIdentifier();
		pIdent.setIdentifier(request.getParameter("identifier"));
		pIdent.setIdentifierType(ps.getPatientIdentifierType(Integer.valueOf(request.getParameter("identiferType"))));
		pIdent.setLocation(ps.getLocation(Integer.valueOf(request.getParameter("location"))));
		
		// =Address======
		PatientAddress pAdd = new PatientAddress();
		pAdd.setAddress1(request.getParameter("address1"));
		pAdd.setAddress2(request.getParameter("address2"));
		pAdd.setCityVillage(request.getParameter("cityVillage"));
		pAdd.setStateProvince(request.getParameter("stateProvince"));
		pAdd.setCountry(request.getParameter("country"));
		pAdd.setLatitude(request.getParameter("latitude"));
		pAdd.setLongitude(request.getParameter("longitude"));
		
		// =Name=========
		PatientName pName = new PatientName();
		pName.setPreferred(Boolean.valueOf(request.getParameter("preferred")));
		pName.setGivenName(request.getParameter("givenName"));
		pName.setMiddleName(request.getParameter("middleName"));
		pName.setFamilyNamePrefix(request.getParameter("familyNamePrefix"));
		pName.setFamilyName(request.getParameter("familyName"));
		pName.setFamilyName2(request.getParameter("familyName2"));
		pName.setFamilyNameSuffix(request.getParameter("familyNameSuffix"));
		pName.setDegree(request.getParameter("degree"));
		
		// =Patient======
		Patient patient = new Patient();
		patient.setGender(request.getParameter("gender"));
		patient.setRace(request.getParameter("race"));
		patient.setBirthdateEstimated(Boolean.valueOf(request.getParameter("birthdateEstimated")));
		patient.setBirthplace(request.getParameter("birthplace"));
		patient.setTribe(ps.getTribe(Integer.valueOf(request.getParameter(""))));
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
			patient.setBirthdate(DateFormat.getDateInstance().parse(request.getParameter("birthdate")));
		}
		catch (ParseException e) {
			throw new IOException("Unable to parse birthdate");
		}
		try {
			patient.setDeathDate(DateFormat.getDateInstance().parse(request.getParameter("deathDate")));
		}
		catch (ParseException e) {
			throw new IOException("Unable to parse the death date");
		}

		try {
			if (context.isAuthenticated()) {
				ps.createPatient(patient);
				httpSession.setAttribute(Constants.OPENMRS_MSG_ATTR, "Patient '" + patient.getPatientId() + "' updated");
				response.sendRedirect("index.jsp?id=" + patient.getPatientId());
				return;
			}
		} catch (Exception e) {
			response.sendRedirect(request.getContextPath() + "/login.jsp?msg=Invalid+credentials.+Try again.");
		}
	}
}
