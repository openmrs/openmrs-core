package org.openmrs.api;

import org.openmrs.*;

public interface ObsService {

	public void createObs(Obs obs);

	public Obs getObs(Long obsId);

	public void saveOrUpdate(Obs obs);

	public void voidObs(Obs obs);

}
