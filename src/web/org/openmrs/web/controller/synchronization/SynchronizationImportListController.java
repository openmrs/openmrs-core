package org.openmrs.web.controller.synchronization;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.ingest.SyncDeserializer;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncRecordIngest;
import org.openmrs.synchronization.ingest.SyncTransmissionResponse;
import org.openmrs.web.WebConstants;
import org.openmrs.web.WebUtil;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class SynchronizationImportListController extends SimpleFormController {

    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
     *      org.springframework.web.bind.ServletRequestDataBinder)
     */
    protected void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) throws Exception {
        super.initBinder(request, binder);
    }

	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		ModelAndView result = new ModelAndView(new RedirectView(getSuccessView()));
		
		log.debug("in onSubmit method");

		HttpSession httpSession = request.getSession();
		boolean isUpload = ServletRequestUtils.getBooleanParameter(request, "upload", false);
		String contents = "";
		String error = "";
		String success = "";
		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		if (isUpload && request instanceof MultipartHttpServletRequest) {
			log.debug("seems we have a file object");
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
			MultipartFile multipartSyncFile = multipartRequest.getFile("syncDataFile");
			if (multipartSyncFile != null && !multipartSyncFile.isEmpty()) {
				InputStream inputStream = null;

				try {
					inputStream = multipartSyncFile.getInputStream();
					BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
					String line = "";
					while ((line = in.readLine()) != null) {
						contents += line;
					}
				} catch (Exception e) {
					log.warn("Unable to read in sync data file", e);
					error = e.getMessage();
				} finally {
					try {
						if (inputStream != null)
							inputStream.close();
					}
					catch (IOException io) {
						log.warn("Unable to close temporary input stream", io);
					}
				}
			}
		} else {
			log.debug("seems we DO NOT have a file object");
		}
		
		if ( contents.length() > 0 ) {
			SyncTransmission st = SyncDeserializer.xmlToSyncTransmission(contents);
			SyncTransmissionResponse str = new SyncTransmissionResponse(st);
			List<SyncImportRecord> importRecords = new ArrayList<SyncImportRecord>();
			
			if ( st == null ) log.debug("st is null");
			else {
				//log.debug("st is NOT null, and has " + st.getGuid());

				for ( SyncRecord record : st.getSyncRecords() ) {
					SyncImportRecord importRecord = SyncRecordIngest.processSyncRecord(record);
					importRecords.add(importRecord);
				}
			}
			if ( importRecords.size() > 0 ) str.setSyncImportRecords(importRecords);
			str.CreateFile();
            InputStream in = new ByteArrayInputStream(str.getFileOutput().getBytes());
            response.setContentType("text/xml; charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + str.getFileName() + ".xml");
            OutputStream out = response.getOutputStream();
            IOUtils.copy(in, out);
            out.flush();
            out.close();
            result = null;
		}
		
		if (!success.equals(""))
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
		
		if (!error.equals(""))
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);

		return result;
	}

    /**
     * 
     * This is called prior to displaying a form for the first time. It tells
     * Spring the form/command object to load into the request
     * 
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    protected Object formBackingObject(HttpServletRequest request)
            throws ServletException {
        // default empty Object
    	String ret = "";
    	
        return ret;
    }
}