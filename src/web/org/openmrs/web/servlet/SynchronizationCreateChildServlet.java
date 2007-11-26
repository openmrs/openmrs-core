/**
 * Auto generated file comment
 */
package org.openmrs.web.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;

/**
 *
 */
public class SynchronizationCreateChildServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        
        if (!Context.hasPrivilege(OpenmrsConstants.PRIV_BACKUP_ENTIRE_DATABASE)) {
            session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Privilege required: " + OpenmrsConstants.PRIV_BACKUP_ENTIRE_DATABASE);
            session.setAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, request.getRequestURI() + "?" + request.getQueryString());
            response.sendRedirect(request.getContextPath() + "/login.htm");
            return;
        }

        response.setContentType("text/sql");
        response.setHeader("Content-Disposition", "attachment; filename=createSyncChild_mysql.sql");
        response.setHeader("Pragma", "no-cache");

        String guidToUse = request.getParameter("guid");
        Context.getSynchronizationService().createDatabaseForChild(guidToUse, response.getOutputStream());
    }

    
    
}
