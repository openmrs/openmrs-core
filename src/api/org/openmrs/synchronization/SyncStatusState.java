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
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.synchronization;

/**
 * State enum for sync status global property.
 * 
 * @see org.openmrs.synchronization.SyncConstants.SYNC_STATUS
 */
public enum SyncStatusState {

    /**
     * Sync is enabled in continue on error mode : potential data inconsistencies during both journaling and record processing 
     * are reported as warning, but do not abort or prevent other openMRS operations from completing.
     */
    ENABLED_CONTINUE_ON_ERROR,
    /**
     * Sync is enabled in a strict mode: all potential data inconsistencies during both journaling and record processing are treated as errors.
     */
    ENABLED_STRICT,
    /**
     * Sync feature is disabled. No journalling of local changes will be performed.
     */
    DISABLED,
    }