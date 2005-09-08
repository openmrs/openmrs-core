package org.openmrs.formentry.web;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.Patient;
import org.openmrs.PatientName;
import org.openmrs.api.hibernate.HibernateUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class FormEntryController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		String shutdownFlag = request.getParameter("shutdown");
		if (shutdownFlag != null && shutdownFlag.equals("1")) {
			HibernateUtil.shutdown();
			response.getWriter().println("shutdown performed");
			return null;
		}

		response.getWriter().println("made it!<br>");

		Session session = HibernateUtil.currentSession();
		Transaction tx = null;

		try {
			tx = session.beginTransaction();

			List patients = session.createQuery(
					"from Patient as p where p.names.familyName >= 'D' "
					+ "and p.names.familyName < 'E' "
					+ "order by p.names.familyName, p.names.givenName"
					).list();

			response.getWriter().println(
					patients.size() + " patients found<br>");

			response.getWriter().println("<hr>");

			int count = 0;
			for (Iterator i = patients.iterator(); i.hasNext();) {
				Patient patient = (Patient) i.next();
				List<PatientName> names = patient.getNames();
				if (names != null) {
					for (Iterator n = names.iterator(); n.hasNext();) {
						PatientName name = (PatientName) n.next();
						if (names != null) {
							response.getWriter().println(
									++count + ": " + name.getGivenName() + " "
											+ name.getFamilyName() + "<br>");
						}
					}
				}
			}
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			HibernateUtil.closeSession();
			session = null;
			tx = null;
			throw (e);
		} finally {
			response.getWriter().println("end of line<br>");
			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
				tx.commit();
		}

		HibernateUtil.closeSession();
		session = null;
		tx = null;
		
		return null;
	}

}
