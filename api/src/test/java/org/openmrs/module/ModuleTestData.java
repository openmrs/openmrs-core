package org.openmrs.module;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ModuleTestData {
	
	private Map<String, Integer> willRefreshContextCallCount = new HashMap<String, Integer>();
	
	private Map<String, Integer> contextRefreshedCallCount = new HashMap<String, Integer>();
	
	private Map<String, Integer> willStartCallCount = new HashMap<String, Integer>();
	
	private Map<String, Integer> startedCallCount = new HashMap<String, Integer>();
	
	private Map<String, Integer> willStopCallCount = new HashMap<String, Integer>();
	
	private Map<String, Integer> stoppedCallCount = new HashMap<String, Integer>();
	
	private Map<String, Long> willRefreshContextCallTime = new HashMap<String, Long>();
	
	private Map<String, Long> contextRefreshedCallTime = new HashMap<String, Long>();
	
	private Map<String, Long> willStartCallTime = new HashMap<String, Long>();
	
	private Map<String, Long> startedCallTime = new HashMap<String, Long>();
	
	private Map<String, Long> willStopCallTime = new HashMap<String, Long>();
	
	private Map<String, Long> stoppedCallTime = new HashMap<String, Long>();
	
	private ModuleTestData() {
		
	}
	
	private static class ModuleTestDataHolder {
		
		private static ModuleTestData INSTANCE = null;
	}
	
	public static ModuleTestData getInstance() {
		if (ModuleTestDataHolder.INSTANCE == null)
			ModuleTestDataHolder.INSTANCE = new ModuleTestData();
		
		return ModuleTestDataHolder.INSTANCE;
	}
	
	public void init(String moduleId) {
		willRefreshContextCallCount.put(moduleId, 0);
		contextRefreshedCallCount.put(moduleId, 0);
		willStartCallCount.put(moduleId, 0);
		startedCallCount.put(moduleId, 0);
		willStopCallCount.put(moduleId, 0);
		stoppedCallCount.put(moduleId, 0);
		
		willRefreshContextCallTime.put(moduleId, 0l);
		contextRefreshedCallTime.put(moduleId, 0l);
		willStartCallTime.put(moduleId, 0l);
		startedCallTime.put(moduleId, 0l);
		willStopCallTime.put(moduleId, 0l);
		stoppedCallTime.put(moduleId, 0l);
	}
	
	public Integer getWillRefreshContextCallCount(String moduleId) {
		Integer count = willRefreshContextCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public Integer getContextRefreshedCallCount(String moduleId) {
		Integer count = contextRefreshedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public Integer getWillStartCallCount(String moduleId) {
		Integer count = willStartCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public Integer getStartedCallCount(String moduleId) {
		Integer count = startedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public Integer getWillStopCallCount(String moduleId) {
		Integer count = willStopCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public Integer getStoppedCallCount(String moduleId) {
		Integer count = stoppedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public void willRefreshContext(String moduleId) {
		willRefreshContextCallTime.put(moduleId, new Date().getTime());
		
		Integer count = willRefreshContextCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		willRefreshContextCallCount.put(moduleId, count + 1);
	}
	
	public void contextRefreshed(String moduleId) {
		contextRefreshedCallTime.put(moduleId, new Date().getTime());
		
		Integer count = contextRefreshedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		contextRefreshedCallCount.put(moduleId, count + 1);
	}
	
	public void willStart(String moduleId) {
		willStartCallTime.put(moduleId, new Date().getTime());
		
		Integer count = willStartCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		willStartCallCount.put(moduleId, count + 1);
	}
	
	public void started(String moduleId) {
		startedCallTime.put(moduleId, new Date().getTime());
		
		Integer count = startedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		startedCallCount.put(moduleId, count + 1);
	}
	
	public void willStop(String moduleId) {
		willStopCallTime.put(moduleId, new Date().getTime());
		
		Integer count = willStopCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		willStopCallCount.put(moduleId, count + 1);
	}
	
	public void stopped(String moduleId) {
		stoppedCallTime.put(moduleId, new Date().getTime());
		
		Integer count = stoppedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		stoppedCallCount.put(moduleId, count + 1);
	}
	
	public Long getWillRefreshContextCallTime(String moduleId) {
		return willRefreshContextCallTime.get(moduleId);
	}
	
	public Long getContextRefreshedCallTime(String moduleId) {
		return contextRefreshedCallTime.get(moduleId);
	}
	
	public Long getWillStartCallTime(String moduleId) {
		return willStartCallTime.get(moduleId);
	}
	
	public Long getStartedCallTime(String moduleId) {
		return startedCallTime.get(moduleId);
	}
	
	public Long getWillStopCallTime(String moduleId) {
		return willStopCallTime.get(moduleId);
	}
	
	public Long getStoppedCallTime(String moduleId) {
		return stoppedCallTime.get(moduleId);
	}
}
