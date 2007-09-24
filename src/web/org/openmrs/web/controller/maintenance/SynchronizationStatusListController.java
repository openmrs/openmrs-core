package org.openmrs.web.controller.maintenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.SynchronizationService;
import org.openmrs.api.context.Context;
import org.openmrs.serial.Item;
import org.openmrs.serial.Record;
import org.openmrs.synchronization.engine.SyncItem;
import org.openmrs.synchronization.engine.SyncRecord;
import org.openmrs.synchronization.engine.SyncSource;
import org.openmrs.synchronization.engine.SyncSourceJournal;
import org.openmrs.synchronization.engine.SyncStrategyFile;
import org.openmrs.synchronization.engine.SyncTransmission;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
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
                SyncTransmission tx = strategy.createSyncTransmission(source);
                Object[] args = new Object[] {tx.getFileName()};
                success = msa.getMessage("SynchronizationStatus.createTx.success", args);
            }
        }
        catch(Exception e) {
            Object[] args = new Object[] {e.getStackTrace()};
            error = msa.getMessage("SynchronizationStatus.createTx.error",args);  
        }
        		
        
        view = getSuccessView();

        if (!success.equals(""))
            httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
        
        if (!error.equals(""))
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
		
		return new ModelAndView(new RedirectView(view));
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
        List<SyncRecord> recordList = (ArrayList<SyncRecord>)obj;

        // warning: right now we are assuming there is only 1 item per record
        for ( SyncRecord record : recordList ) {
			for ( SyncItem item : record.getItems() ) {
				String syncItem = item.getContent();
				Record xml = Record.create(syncItem);
				Item root = xml.getRootItem();
				String className = root.getNode().getNodeName();
				recordTypes.put(record.getGuid(), className.substring("org.openmrs.".length()));
				
				// now we have to go through the item nodes to find the real GUID that we want
				NodeList nodes = root.getNode().getChildNodes();
				for ( int i = 0; i < nodes.getLength(); i++ ) {
					Node n = nodes.item(i);
					String propName = n.getNodeName();
					if ( propName.equalsIgnoreCase("guid") ) {
						itemGuids.put(record.getGuid(), n.getTextContent());
					}
				}
			}
        	
        }
        
        ret.put("recordTypes", recordTypes);
        ret.put("itemGuids", itemGuids);
        
	    return ret;
    }

}