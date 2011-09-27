package org.openmrs.web.controller.provider;

import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.handler.AttributeHandler;
import org.openmrs.propertyeditor.PersonEditor;
import org.openmrs.validator.ProviderValidator;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/admin/provider/provider.form")
public class ProviderFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) throws Exception {
		binder.registerCustomEditor(org.openmrs.Person.class, new PersonEditor());
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public String onSubmit(WebRequest request, @RequestParam(required = false) String saveProviderButton,
	        @RequestParam(required = false) String retireProviderButton,
	        @RequestParam(required = false) String unretireProviderButton,
	        @RequestParam(required = false) boolean linkToPerson, @ModelAttribute("provider") Provider provider,
	        BindingResult errors, ModelMap model) throws Exception {
		
		//For existing providers, switch between linking to person or use name
		if (provider.getProviderId() != null) {
			if (linkToPerson)
				provider.setName(null);
			else
				provider.setPerson(null);
		}
		
		handleAttributeParameteres(request, provider, model);
		new ProviderValidator().validate(provider, errors);
		
		if (!errors.hasErrors()) {
			if (Context.isAuthenticated()) {
				ProviderService service = Context.getProviderService();
				
				String message = "Provider.saved";
				if (saveProviderButton != null) {
					service.saveProvider(provider);
				} else if (retireProviderButton != null) {
					service.retireProvider(provider, provider.getRetireReason());
					message = "Provider.retired";
				} else if (unretireProviderButton != null) {
					service.unretireProvider(provider);
					message = "Provider.unretired";
				}
				
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, message, WebRequest.SCOPE_SESSION);
				return "redirect:index.htm";
			}
		}
		
		return showForm();
	}
	
	@ModelAttribute("provider")
	public Provider formBackingObject(@RequestParam(required = false) Integer providerId) throws ServletException {
		Provider provider = new Provider();
		if (Context.isAuthenticated()) {
			if (providerId != null) {
				ProviderService ps = Context.getProviderService();
				return ps.getProvider(providerId);
			}
		}
		return provider;
	}
	
	@ModelAttribute("providerAttributeTypes")
	public List<ProviderAttributeType> getProviderAttributeTypes() throws Exception {
		return Context.getProviderService().getAllProviderAttributeTypes();
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String showForm() {
		return "admin/provider/providerForm";
	}
	
	public String showForm(Integer providerId) {
		return "redirect:provider.form?providerId=" + providerId;
	}
	
	//TODO This method is copied from VisitFormController. Need to find a common place to reuse this code.
	private void handleAttributeParameteres(WebRequest request, Provider provider, ModelMap model) {
		// manually handle the attribute parameters
		for (ProviderAttributeType providerAttributeType : (List<ProviderAttributeType>) model.get("providerAttributeTypes")) {
			if (providerAttributeType.getMaxOccurs() == null || providerAttributeType.getMaxOccurs() != 1)
				throw new RuntimeException("For now only attributes with maxOccurs=1 are supported");
			AttributeHandler<?> handler = Context.getAttributeService().getHandler(providerAttributeType);
			// look for parameters starting with attribute.${ providerAttributeType.id }
			for (Iterator<String> iter = request.getParameterNames(); iter.hasNext();) {
				String paramName = iter.next();
				if (paramName.startsWith("attribute." + providerAttributeType.getId())) {
					String paramValue = request.getParameter(paramName);
					if (StringUtils.hasText(paramValue)) {
						handler.deserialize(paramValue);
						//handler.validate(realValue);
						setAttribute(provider, providerAttributeType, paramValue);
					} else {
						for (ProviderAttribute providerAttribute : provider.getActiveAttributes(providerAttributeType))
							providerAttribute.setVoided(true);
					}
				}
			}
		}
	}
	
	private void setAttribute(Provider provider, ProviderAttributeType providerAttributeType, String paramValue) {
		ProviderAttribute providerAttribute = new ProviderAttribute();
		providerAttribute.setAttributeType(providerAttributeType);
		providerAttribute.setSerializedValue(paramValue);
		provider.setAttribute(providerAttribute);
	}
	
}
