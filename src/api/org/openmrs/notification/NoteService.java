package org.openmrs.notification;

import java.util.Collection;
import java.util.List;
import org.openmrs.User;
import org.openmrs.notification.Note;


public interface NoteService { 
	
	public List<Note> getNotes(User user) throws Exception;
	public void createNote(Note note) throws Exception;
	public Note getNote(Integer noteId) throws Exception;
	public void updateNote(Note note) throws Exception;
	
}