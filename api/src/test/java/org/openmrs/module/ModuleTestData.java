/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ModuleTestData {
	
	private Map<String, Integer> willRefreshContextCallCount = new HashMap<>();
	
	private Map<String, Integer> contextRefreshedCallCount = new HashMap<>();
	
	private Map<String, Integer> willStartCallCount = new HashMap<>();
	
	private Map<String, Integer> startedCallCount = new HashMap<>();
	
	private Map<String, Integer> willStopCallCount = new HashMap<>();
	
	private Map<String, Integer> stoppedCallCount = new HashMap<>();
	
	private Map<String, Integer> setupOnVersionChangeBeforeSchemaChangesCallCount = new HashMap<>();

	private Map<String, Integer> setupOnVersionChangeCallCount = new HashMap<>();
	
	private Map<String, Long> willRefreshContextCallTime = new HashMap<>();
	
	private Map<String, Long> contextRefreshedCallTime = new HashMap<>();
	
	private Map<String, Long> willStartCallTime = new HashMap<>();
	
	private Map<String, Long> startedCallTime = new HashMap<>();
	
	private Map<String, Long> willStopCallTime = new HashMap<>();
	
	private Map<String, Long> stoppedCallTime = new HashMap<>();
	
	private Map<String, Long> setupOnVersionChangeBeforeSchemaChangesCallTime = new HashMap<>();

	private Map<String, Long> setupOnVersionChangeCallTime = new HashMap<>();
	
	private ModuleTestData() {
		
	}
	
	private static class ModuleTestDataHolder {
		
		private static final ModuleTestData INSTANCE = new ModuleTestData();
	}
	
	public static ModuleTestData getInstance() {
		return ModuleTestDataHolder.INSTANCE;
	}
	
	public synchronized void init(String moduleId) {
		willRefreshContextCallCount.put(moduleId, 0);
		contextRefreshedCallCount.put(moduleId, 0);
		willStartCallCount.put(moduleId, 0);
		startedCallCount.put(moduleId, 0);
		willStopCallCount.put(moduleId, 0);
		stoppedCallCount.put(moduleId, 0);
		setupOnVersionChangeBeforeSchemaChangesCallCount.put(moduleId, 0);
		setupOnVersionChangeCallCount.put(moduleId, 0);
		
		willRefreshContextCallTime.put(moduleId, 0L);
		contextRefreshedCallTime.put(moduleId, 0L);
		willStartCallTime.put(moduleId, 0L);
		startedCallTime.put(moduleId, 0L);
		willStopCallTime.put(moduleId, 0L);
		stoppedCallTime.put(moduleId, 0L);
		setupOnVersionChangeBeforeSchemaChangesCallTime.put(moduleId, 0L);
		setupOnVersionChangeCallTime.put(moduleId, 0L);
	}
	
	public synchronized Integer getWillRefreshContextCallCount(String moduleId) {
		Integer count = willRefreshContextCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public synchronized Integer getContextRefreshedCallCount(String moduleId) {
		Integer count = contextRefreshedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public synchronized Integer getWillStartCallCount(String moduleId) {
		Integer count = willStartCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public synchronized Integer getStartedCallCount(String moduleId) {
		Integer count = startedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public synchronized Integer getWillStopCallCount(String moduleId) {
		Integer count = willStopCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public synchronized Integer getStoppedCallCount(String moduleId) {
		Integer count = stoppedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public synchronized Integer getSetupOnVersionChangeBeforeSchemaChangesCallCount(String moduleId) {
		Integer count = setupOnVersionChangeBeforeSchemaChangesCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}

	public synchronized Integer getSetupOnVersionChangeCallCount(String moduleId) {
		Integer count = setupOnVersionChangeCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		return count;
	}
	
	public synchronized void willRefreshContext(String moduleId) {
		willRefreshContextCallTime.put(moduleId, new Date().getTime());
		
		Integer count = willRefreshContextCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		willRefreshContextCallCount.put(moduleId, count + 1);
	}
	
	public synchronized void contextRefreshed(String moduleId) {
		contextRefreshedCallTime.put(moduleId, new Date().getTime());
		
		Integer count = contextRefreshedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		contextRefreshedCallCount.put(moduleId, count + 1);
	}
	
	public synchronized void willStart(String moduleId) {
		willStartCallTime.put(moduleId, new Date().getTime());
		
		Integer count = willStartCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		willStartCallCount.put(moduleId, count + 1);
	}
	
	public synchronized void started(String moduleId) {
		startedCallTime.put(moduleId, new Date().getTime());
		
		Integer count = startedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		startedCallCount.put(moduleId, count + 1);
	}
	
	public synchronized void willStop(String moduleId) {
		willStopCallTime.put(moduleId, new Date().getTime());
		
		Integer count = willStopCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		willStopCallCount.put(moduleId, count + 1);
	}
	
	public synchronized void stopped(String moduleId) {
		stoppedCallTime.put(moduleId, new Date().getTime());
		
		Integer count = stoppedCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		stoppedCallCount.put(moduleId, count + 1);
	}
	
	public synchronized void setupOnVersionChangeBeforeSchemaChanges(String moduleId) {
		setupOnVersionChangeBeforeSchemaChangesCallTime.put(moduleId, new Date().getTime());

		Integer count = setupOnVersionChangeBeforeSchemaChangesCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		setupOnVersionChangeBeforeSchemaChangesCallCount.put(moduleId, count + 1);
	}

	public synchronized void setupOnVersionChange(String moduleId) {
		setupOnVersionChangeCallTime.put(moduleId, new Date().getTime());

		Integer count = setupOnVersionChangeCallCount.get(moduleId);
		if (count == null) {
			count = 0;
		}
		setupOnVersionChangeCallCount.put(moduleId, count + 1);
	}
	
	public synchronized Long getWillRefreshContextCallTime(String moduleId) {
		return willRefreshContextCallTime.get(moduleId);
	}
	
	public synchronized Long getContextRefreshedCallTime(String moduleId) {
		return contextRefreshedCallTime.get(moduleId);
	}
	
	public synchronized Long getWillStartCallTime(String moduleId) {
		return willStartCallTime.get(moduleId);
	}
	
	public synchronized Long getStartedCallTime(String moduleId) {
		return startedCallTime.get(moduleId);
	}
	
	public synchronized Long getWillStopCallTime(String moduleId) {
		return willStopCallTime.get(moduleId);
	}
	
	public synchronized Long getStoppedCallTime(String moduleId) {
		return stoppedCallTime.get(moduleId);
	}
	
	public synchronized Long getSetupOnVersionChangeBeforeSchemaChangesCallTime(String moduleId) {
		return setupOnVersionChangeBeforeSchemaChangesCallTime.get(moduleId);
	}

	public synchronized Long getSetupOnVersionChangeCallTime(String moduleId) {
		return setupOnVersionChangeCallTime.get(moduleId);
	}
}
