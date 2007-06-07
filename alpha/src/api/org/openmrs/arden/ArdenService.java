package org.openmrs.arden;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ArdenService {

	/**
	 * 
	 * @param file - mlm file to be parsed
	 */
	public void compileFile(String file);

}