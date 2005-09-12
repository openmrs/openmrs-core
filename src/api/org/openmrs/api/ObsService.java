package org.openmrs.api;

import org.openmrs.*;

public interface ObsService {

	/**
	 * Create an observation 
	 * @param Obs
	 * @return newly created observation
	 * @throws APIException
	 */
	public Obs createObs(Obs obs) throws APIException;

	/**
	 * Get an observation
	 * @param integer obsId of observation desired
	 * @return matching Obs
	 * @throws APIException
	 */
	public Obs getObs(Integer obsId) throws APIException;

	/**
	 * Save changes to observation
	 * @param Obs
	 * @throws APIException
	 */
	public void updateObs(Obs obs) throws APIException;

	/**
	 * Equivalent to deleting an observation
	 * @param obs
	 * @throws APIException
	 */
	public void voidObs(Obs obs) throws APIException;
	
	/**
	 * Revive an observation (pull a Lazarus)
	 * @param Obs
	 * @throws APIException
	 */
	public void unVoidObs(Obs obs) throws APIException;

	/**
	 * Delete an observation.  SHOULD NOT BE CALLED unless caller is lower-level.
	 * @param Obs
	 * @throws APIException
	 * @see voidObs(Obs)
	 */
	public void deleteObs(Obs obs) throws APIException;
}
