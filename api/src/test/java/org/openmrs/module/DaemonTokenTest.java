package org.openmrs.module;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by freddy on 20.05.18.
 */
public class DaemonTokenTest {
	private DaemonToken daemonToken;
	/*
	**
	* Check that DaemonTokenId is as it was set
	*/
	@Test
	public void getId_shouldReturnIdSetInConstructor(){
		String tokenId = "DaemonTokenId12";
		daemonToken = new DaemonToken(tokenId);
		
		assertEquals(daemonToken.getId(), tokenId);
	}
	
	
}
