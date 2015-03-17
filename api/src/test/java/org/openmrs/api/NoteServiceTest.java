/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Note;
import org.openmrs.notification.NoteService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

@SuppressWarnings("deprecation")
@Ignore("TRUNK-4106")
public class NoteServiceTest extends BaseContextSensitiveTest {
	
	protected static final String INITIAL_NOTE_XML = "org/openmrs/api/include/NoteServiceTest-initial.xml";
	
	/**
	 * @see {@link NoteService#voidNote(Note,String)}
	 */
	@Test
	@Verifies(value = "should void the Note and set the voidReason", method = "voidNote(Note,String)")
	public void voidNote_shouldVoidTheNoteAndSetTheVoidReason() throws Exception {
		executeDataSet(INITIAL_NOTE_XML);
		
		NoteService noteService = Context.getNoteService();
		
		Note note = noteService.getNote(8);
		
		Assert.assertFalse(note.isVoided());
		Assert.assertNull(note.getVoidReason());
		Assert.assertNull(note.getDateVoided());
		Assert.assertNull(note.getDateVoided());
		
		noteService.voidNote(note, "test reason");
		
		assertTrue(note.isVoided());
		assertEquals("test reason", note.getVoidReason());
		assertEquals(Context.getAuthenticatedUser(), note.getVoidedBy());
		assertNotNull(note.getDateVoided());
	}
}
