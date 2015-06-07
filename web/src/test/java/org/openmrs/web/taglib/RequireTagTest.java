/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.taglib;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.Verifies;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPageContext;

/**
 * Tests for the {@link RequireTag} taglib controller.
 */
public class RequireTagTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link RequireTag#doStartTag()}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should allow user to have any privilege", method = "doStartTag()")
	public void doStartTag_shouldAllowUserToHaveAnyPrivilege() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/RequireTagTest.xml");
		Context.authenticate("whirleygiguser", "test");
		
		RequireTag tag = new RequireTag();
		tag.setPageContext(new MockPageContext());
		tag.setAnyPrivilege("Manage WhirleyGigs, Manage WhoopDeDoos");
		
		// the tag passes
		Assert.assertEquals(Tag.SKIP_BODY, tag.doStartTag());
		
		Context.logout();
	}
	
	/**
	 * @see {@link RequireTag#doStartTag()}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should allow user with all privileges", method = "doStartTag()")
	public void doStartTag_shouldAllowUserWithAllPrivileges() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/RequireTagTest.xml");
		Context.authenticate("overallmanager", "test");
		
		RequireTag tag = new RequireTag();
		tag.setPageContext(new MockPageContext());
		tag.setAllPrivileges("Manage WhirleyGigs, Manage WhoopDeDoos, Manage Thingamajigs");
		
		// the tag passes
		Assert.assertEquals(Tag.SKIP_BODY, tag.doStartTag());
		
		Context.logout();
	}
	
	/**
	 * @see {@link RequireTag#doStartTag()}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should allow user with the privilege", method = "doStartTag()")
	public void doStartTag_shouldAllowUserWithThePrivilege() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/RequireTagTest.xml");
		Context.authenticate("whirleygiguser", "test");
		
		RequireTag tag = new RequireTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Manage WhirleyGigs");
		
		// the tag passes
		Assert.assertEquals(Tag.SKIP_BODY, tag.doStartTag());
		
		Context.logout();
	}
	
	/**
	 * @see {@link RequireTag#doStartTag()}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should reject user without all of the privileges", method = "doStartTag()")
	public void doStartTag_shouldRejectUserWithoutAllOfThePrivileges() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/RequireTagTest.xml");
		Context.authenticate("whirleygiguser", "test");
		
		RequireTag tag = new RequireTag();
		tag.setPageContext(new MockPageContext());
		tag.setAllPrivileges("Manage WhirleyGigs, Manage WhoopDeDoos, Manage Thingamajigs");
		
		// the tag passes
		Assert.assertEquals(Tag.SKIP_PAGE, tag.doStartTag());
		
		Context.logout();
	}
	
	/**
	 * @see {@link RequireTag#doStartTag()}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should reject user without any of the privileges", method = "doStartTag()")
	public void doStartTag_shouldRejectUserWithoutAnyOfThePrivileges() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/RequireTagTest.xml");
		Context.authenticate("whirleygiguser", "test");
		
		RequireTag tag = new RequireTag();
		tag.setPageContext(new MockPageContext());
		tag.setAnyPrivilege("Random Privilege, Other Random Privilege");
		
		// the tag passes
		Assert.assertEquals(Tag.SKIP_PAGE, tag.doStartTag());
		
		Context.logout();
	}
	
	/**
	 * @see {@link RequireTag#doStartTag()}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should reject user without the privilege", method = "doStartTag()")
	public void doStartTag_shouldRejectUserWithoutThePrivilege() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/RequireTagTest.xml");
		Context.authenticate("overallmanager", "test");
		
		RequireTag tag = new RequireTag();
		tag.setPageContext(new MockPageContext());
		tag.setPrivilege("Some Random Privilege");
		
		// the tag passes
		Assert.assertEquals(Tag.SKIP_PAGE, tag.doStartTag());
		
		Context.logout();
	}
	
	/**
	 * @see {@link RequireTag#doStartTag()}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should set the right session attributes if the authenticated user misses some privileges", method = "doStartTag()")
	public void doStartTag_shouldSetTheRightSessionAttributesIfTheAuthenticatedUserMissesSomePrivileges() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/RequireTagTest.xml");
		Context.authenticate("whirleygiguser", "test");
		
		RequireTag tag = new RequireTag();
		MockPageContext pageContext = new MockPageContext();
		final String referer = "/denied.htm";
		((MockHttpServletRequest) pageContext.getRequest()).addHeader("Referer", referer);
		tag.setPageContext(pageContext);
		tag.setAllPrivileges("Manage WhirleyGigs,Manage Thingamajigs");
		String redirect = "/myRedirect.html";
		tag.setRedirect(redirect);
		
		Assert.assertEquals(Tag.SKIP_PAGE, tag.doStartTag());
		Assert.assertEquals(true, pageContext.getAttribute(WebConstants.INSUFFICIENT_PRIVILEGES, PageContext.SESSION_SCOPE));
		Assert.assertNotNull(pageContext.getAttribute(WebConstants.REQUIRED_PRIVILEGES, PageContext.SESSION_SCOPE));
		Assert.assertEquals(redirect, pageContext.getAttribute(WebConstants.DENIED_PAGE, PageContext.SESSION_SCOPE)
		        .toString());
		
		Context.logout();
	}
	
	/**
	 * @see {@link RequireTag#doStartTag()}
	 */
	@Test
	@SkipBaseSetup
	@Verifies(value = "should set the referer as the denied page url if no redirect url is specified", method = "doStartTag()")
	public void doStartTag_shouldSetTheRefererAsTheDeniedPageUrlIfNoRedirectUrlIsSpecified() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/web/taglib/include/RequireTagTest.xml");
		Context.authenticate("whirleygiguser", "test");
		
		RequireTag tag = new RequireTag();
		MockPageContext pageContext = new MockPageContext();
		final String referer = "/denied.htm";
		((MockHttpServletRequest) pageContext.getRequest()).addHeader("Referer", referer);
		tag.setPageContext(pageContext);
		tag.setAllPrivileges("Manage WhirleyGigs,Manage Thingamajigs");
		tag.setRedirect("");
		
		Assert.assertEquals(Tag.SKIP_PAGE, tag.doStartTag());
		Assert.assertEquals(true, pageContext.getAttribute(WebConstants.INSUFFICIENT_PRIVILEGES, PageContext.SESSION_SCOPE));
		Assert.assertNotNull(pageContext.getAttribute(WebConstants.REQUIRED_PRIVILEGES, PageContext.SESSION_SCOPE));
		Assert.assertEquals(referer, pageContext.getAttribute(WebConstants.DENIED_PAGE, PageContext.SESSION_SCOPE)
		        .toString());
		
		Context.logout();
	}
}
