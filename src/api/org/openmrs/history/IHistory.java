package org.openmrs.history;

/**
 * History interface
*/
public interface IHistory
{
	public Integer getHistoryId();
	public void setHistoryId(Integer id);
    
    // need methods to represent specific history
    // data and also a way to deal with converting primary
    // keys
}
