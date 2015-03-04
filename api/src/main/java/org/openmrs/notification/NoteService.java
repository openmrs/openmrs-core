/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification;

import java.util.List;

import org.openmrs.User;

public interface NoteService {
	
	public List<Note> getNotes(User user) throws Exception;
	
	public void createNote(Note note) throws Exception;
	
	public Note getNote(Integer noteId) throws Exception;
	
	public void updateNote(Note note) throws Exception;
	
}
