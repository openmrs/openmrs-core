package org.openmrs.web.controller.synchronization;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.serial.Item;
import org.openmrs.serial.Record;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncRecordState;
import org.openmrs.synchronization.engine.SyncSource;
import org.openmrs.synchronization.engine.SyncSourceJournal;
import org.openmrs.synchronization.engine.SyncStrategyFile;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.synchronization.ingest.SyncDeserializer;
import org.openmrs.synchronization.ingest.SyncImportRecord;
import org.openmrs.synchronization.ingest.SyncRecordIngest;
import org.openmrs.synchronization.ingest.SyncTransmissionResponse;
import org.openmrs.web.WebConstants;
import org.openmrs.web.WebUtil;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SynchronizationStatusListController extends SimpleFormController {

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

    /**
     * 
     * The onSubmit function receives the form/command object that was modified
     * by the input form and saves it to the db
     * 
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, java.lang.Object,
     *      org.springframework.validation.BindException)
     */
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {

    	ModelAndView result = new ModelAndView(new RedirectView(getSuccessView()));
    	
        // TODO - replace with privilage check
        if (!Context.isAuthenticated())
            throw new APIAuthenticationException("Not authenticated!");
        
        HttpSession httpSession = request.getSession();
        String view = getFormView();
        String success = "";
        String error = "";
        MessageSourceAccessor msa = getMessageSourceAccessor();
        
        String action = ServletRequestUtils.getStringParameter(request, "action", "");
        
        try {
            // handle transmission generation
            if ("createTx".equals(action)) {
                SyncSource source = new SyncSourceJournal();
                SyncStrategyFile strategy = new SyncStrategyFile();
                //SyncTransmission tx = strategy.createSyncTransmission(source);
                SyncTransmission tx = strategy.createStateBasedSyncTransmission(source);
                //Object[] args = new Object[] {tx.getFileName()};
                
                // Write sync transmission to response
                InputStream in = new ByteArrayInputStream(tx.getFileOutput().getBytes());
                response.setContentType("text/xml; charset=utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + tx.getFileName() + ".xml");
                OutputStream out = response.getOutputStream();
                IOUtils.copy(in, out);
                //response.flushBuffer();
                out.flush();
                out.close();
                
                // let's update SyncRecords to reflect the fact that we now have tried to sync them
                for ( SyncRecord record : tx.getSyncRecords() ) {
                	record.setRetryCount(record.getRetryCount() + 1);
                	record.setState(SyncRecordState.SENT);
                	Context.getSynchronizationService().updateSyncRecord(record);
                }
                                
                // don't return a model/view
                result = null;
                
                //success = msa.getMessage("SynchronizationStatus.createTx.success", args);
            } else if ( "uploadResponse".equals(action) && request instanceof MultipartHttpServletRequest) {

            	String contents = "";
            	
    			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest)request;
    			MultipartFile multipartSyncFile = multipartRequest.getFile("syncResponseFile");
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
        		
        		if ( contents.length() > 0 ) {
        			SyncTransmissionResponse str = SyncDeserializer.xmlToSyncTransmissionResponse(contents);
        			
        			if ( str == null ) log.debug("st is null");
        			else {
        				// process each incoming syncImportRecord
        				for ( SyncImportRecord importRecord : str.getSyncImportRecords() ) {
        					SyncRecord record = Context.getSynchronizationService().getSyncRecord(importRecord.getGuid());
        					record.setState(importRecord.getState());
        					Context.getSynchronizationService().updateSyncRecord(record);
        				}
        			}
        		}

            }
        }
        catch(Exception e) {
            Object[] args = new Object[] {e.getStackTrace()};
            error = msa.getMessage("SynchronizationStatus.createTx.error",args);  
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
        List<SyncRecord> recordList = new ArrayList<SyncRecord>();

        // only fill the Object if the user has authenticated properly
        if (Context.isAuthenticated()) {
            SynchronizationService ss = Context.getSynchronizationService();
            recordList.addAll(ss.getSyncRecords());
        }

        return recordList;
    }

	@Override
    protected Map referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		Map<String,Object> ret = new HashMap<String,Object>();
		
		Map<String,String> recordTypes = new HashMap<String,String>();
		Map<String,String> itemGuids = new HashMap<String,String>();
		Map<String,String> itemInfo = new HashMap<String,String>();
		Map<String,String> itemInfoKeys = new HashMap<String,String>();
        List<SyncRecord> recordList = (ArrayList<SyncRecord>)obj;

        //itemInfoKeys.put("Patient", "gender,birthdate");
        itemInfoKeys.put("PersonName", "name");
        itemInfoKeys.put("User", "username");
        
        // warning: right now we are assuming there is only 1 item per record
        for ( SyncRecord record : recordList ) {
			for ( SyncItem item : record.getItems() ) {
				String syncItem = item.getContent();
				Record xml = Record.create(syncItem);
				Item root = xml.getRootItem();
				String className = root.getNode().getNodeName().substring("org.openmrs.".length());
				recordTypes.put(record.getGuid(), className);
				String itemInfoKey = itemInfoKeys.get(className);
				
				// now we have to go through the item nodes to find the real GUID that we want
				NodeList nodes = root.getNode().getChildNodes();
				for ( int i = 0; i < nodes.getLength(); i++ ) {
					Node n = nodes.item(i);
					String propName = n.getNodeName();
					if ( propName.equalsIgnoreCase("guid") ) {
						itemGuids.put(record.getGuid(), n.getTextContent());
					}
					if ( propName.equalsIgnoreCase(itemInfoKey) ) {
						itemInfo.put(record.getGuid(), n.getTextContent());
					}
				}
			}
        	
        }
        
        ret.put("recordTypes", recordTypes);
        ret.put("itemGuids", itemGuids);
        ret.put("itemInfo", itemInfo);
        
	    return ret;
    }

}