package org.openmrs.formentry;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.hl7.HL7InQueue;
import org.w3c.dom.Document;

/**
 * Processes FormEntryQueue entries. Each entry is translated into an HL7
 * message, using the transform associated with the form used to make the entry.
 * When the transform is successful, the queue entry is converted into the
 * FormEntryArchive; for unsuccessful transforms, the queue entry is converted
 * to a FormEntryError.
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class FormEntryQueueProcessor /* implements Runnable */ {

	private static Log log = LogFactory.getLog( FormEntryQueueProcessor.class );

	private Context context;
	private DocumentBuilderFactory documentBuilderFactory;
	private XPathFactory xPathFactory;
	private TransformerFactory transformerFactory;

	/**
	 * Empty constructor (requires context to be set before any other calls are
	 * made)
	 */
	public FormEntryQueueProcessor() {
	}

	/**
	 * Constructs a FormEntryQueueProcessor
	 * 
	 * @param context
	 *            OpenMRS context for accessing the database API
	 */
	public FormEntryQueueProcessor(Context context) {
		this.context = context;
	}

	/**
	 * Transform a FormEntryQueue entry (converts the XML data into HL7 and
	 * places it into the HL7 inbound queue for further processing). Once
	 * transformed, then FormEntryQueue entry is flagged as completed (the
	 * status is updated).
	 * 
	 * The XSLT from the appropriate form (the form used to generate the
	 * FormEntryQueue data in the first place) is used to perform the
	 * transformation into HL7.
	 * 
	 * @param formEntryQueue
	 *            entry to be transformed
	 */
	public void transformFormEntryQueue(FormEntryQueue formEntryQueue) {		log.debug("Transforming form entry queue");
		String formData = formEntryQueue.getFormData();
		FormService formService = context.getFormService();
		Integer formId = null;
		String errorDetails = null;

		// First we parse the FormEntry xml data to obtain the formId of the
		// form that was used to create the xml data
		try {
			DocumentBuilderFactory dbf = getDocumentBuilderFactory();
			DocumentBuilder db = dbf.newDocumentBuilder();
			XPathFactory xpf = getXPathFactory();
			XPath xp = xpf.newXPath();
			Document doc = db.parse(IOUtils.toInputStream(formData));
			formId = Integer.parseInt(xp.evaluate("/form/@id", doc));
		} catch (Exception e) {
			errorDetails = e.getMessage();
			log.error(e);
		}

		// If we failed to obtain the formId, move the queue entry into the
		// error bin and abort
		if (formId == null) {
			setFatalError(formEntryQueue, "Error retrieving form ID from data",
					errorDetails);
			return;
		}

		// Now that we've determined the form used to create the XML data,
		// we can obtain the associated XSLT to perform the transform to HL7.
		String xsltDoc = formService.getForm(formId).getXslt();

		StringWriter outWriter = new StringWriter();
		Source source = new StreamSource(IOUtils.toInputStream(formData));
		Source xslt = new StreamSource(IOUtils.toInputStream(xsltDoc));
		Result result = new StreamResult(outWriter);

		TransformerFactory tf = getTransformerFactory();
		String out = null;
		errorDetails = null;
		try {
			Transformer t = tf.newTransformer(xslt);
			t.transform(source, result);
			out = outWriter.toString();
		} catch (TransformerConfigurationException e) {
			errorDetails = e.getMessage();
			log.error(e);
		} catch (TransformerException e) {
			errorDetails = e.getMessage();
			log.error(e);
		}

		// If the transform failed, move the queue entry into the error bin
		// and exit
		if (out == null) {
			setFatalError(formEntryQueue, "Unable to transform to HL7",
					errorDetails);
			return;
		}

		// At this point, we have successfully transformed the XML data into
		// HL7. Create a new entry in the HL7 inbound queue and move the
		// current FormEntry queue item into the archive.
		HL7InQueue hl7InQueue = new HL7InQueue();
		hl7InQueue.setHL7Data(out.toString());
		hl7InQueue.setHL7Source(context.getHL7Service().getHL7Source(1));
		hl7InQueue.setHL7SourceKey(String.valueOf(formEntryQueue
				.getFormEntryQueueId()));
		context.getHL7Service().createHL7InQueue(hl7InQueue);

		FormEntryArchive formEntryArchive = new FormEntryArchive(formEntryQueue);
		context.getFormEntryService().createFormEntryArchive(formEntryArchive);
		context.getFormEntryService().deleteFormEntryQueue(formEntryQueue);

		// clean up memory
		context.getFormEntryService().garbageCollect();
	}

	/**
	 * Transform the next pending FormEntryQueue entry. If there are no pending
	 * items in the queue, this method simply returns quietly.
	 * 
	 * @return true if a queue entry was processed, false if queue was empty
	 */
	public boolean transformNextFormEntryQueue() {
		boolean transformOccurred = false;
		FormEntryService fes = context.getFormEntryService();
		FormEntryQueue feq;
		if ((feq = fes.getNextFormEntryQueue()) != null) {
			transformFormEntryQueue(feq);
			transformOccurred = true;
		}
		return transformOccurred;
	}

	/**
	 * @return DocumentBuilderFactory to be used for parsing XML
	 */
	private DocumentBuilderFactory getDocumentBuilderFactory() {
		if (documentBuilderFactory == null)
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
		return documentBuilderFactory;
	}

	/**
	 * @return XPathFactory to be used for obtaining data from the parsed XML
	 */
	private XPathFactory getXPathFactory() {
		if (xPathFactory == null)
			xPathFactory = XPathFactory.newInstance();
		return xPathFactory;
	}

	/**
	 * @return TransformerFactory used to perform the transform to HL7
	 */
	private TransformerFactory getTransformerFactory() {
		if (transformerFactory == null)
			transformerFactory = TransformerFactory.newInstance();
		return transformerFactory;
	}

	/**
	 * Convenience method to handle fatal errors. In this case, a FormEntryError
	 * object is built and stored based on the current queue entry and then the
	 * current queue entry is removed from the queue.
	 * 
	 * @param formEntryQueue
	 *            queue entry with fatal error
	 * @param error
	 *            name and/or brief description of the error
	 * @param errorDetails
	 *            specifics for the fatal error
	 */
	private void setFatalError(FormEntryQueue formEntryQueue, String error, String errorDetails) {
		FormEntryError formEntryError = new FormEntryError();
		formEntryError.setFormData(formEntryQueue.getFormData());
		formEntryError.setError(error);
		formEntryError.setErrorDetails(errorDetails);
		context.getFormEntryService().createFormEntryError(formEntryError);
		context.getFormEntryService().deleteFormEntryQueue(formEntryQueue);
	}

	/**
	 * Convenience method to allow for dependency injection
	 * 
	 * @param context
	 *            OpenMRS context to be used by the processor
	 */
	public void setContext(Context context) {
		this.context = context;
	}


	/**
	 * Starts up a thread to process all existing FormEntryQueue entries
	 * 
	 * @param context
	 *            context from which process should start (required for
	 *            authentication)
	 */
	public synchronized void processFormEntryQueue() throws APIException {
		try {
			while (transformNextFormEntryQueue()) {
				// loop until queue is empty
			}
		} catch (Exception e) {
			log.error("Error while processing FormEntryQueue", e);
		}
	}

	/*
	 * Run method for processing all entries in the FormEntry queue
	public void run() {
		try {
			while (transformNextFormEntryQueue()) {
				// loop until queue is empty
			}
		} catch (Exception e) {
			log.error("Error while processing FormEntryQueue", e);
		}
	}
	 */
	
	/*
	private static Hashtable<Context, Thread> threadCache = new Hashtable<Context, Thread>();

	private static Thread getThreadForContext(Context context) {
		Thread thread;
		if (threadCache.containsKey(context))
			thread = threadCache.get(context);
		else {
			thread = new Thread(new FormEntryQueueProcessor(context));
			threadCache.put(context, thread);
		}
		return thread;
	}
	*/

}
