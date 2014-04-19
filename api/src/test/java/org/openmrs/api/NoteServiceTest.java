/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
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
