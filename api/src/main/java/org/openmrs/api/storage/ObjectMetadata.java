/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.storage;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to store and retrieve metadata.
 * 
 * @since 2.8.0
 */
public class ObjectMetadata {
	private Map<String, String> otherMetadata = new HashMap<>();
	private Long length;
	private String mimeType;
	private String filename;
	private Instant creationTime;
	
	public ObjectMetadata() {}

	public ObjectMetadata(Long length, String mimeType, String filename, Instant creationTime, 
						  Map<String, String> otherMetadata) {
		this();
		this.length = length;
		this.mimeType = mimeType;
		this.filename = filename;
		this.creationTime = creationTime;
		this.otherMetadata = otherMetadata;
	}
	
	public static ObjectMetadata.Builder builder() {
		return new ObjectMetadata.Builder();
	}

	public Map<String, String> getOtherMetadata() {
		return otherMetadata;
	}

	public void setOtherMetadata(Map<String, String> otherMetadata) {
		this.otherMetadata = otherMetadata;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Instant getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Instant creationTime) {
		this.creationTime = creationTime;
	}

	public static class Builder {
		private Long length;
		private String mimeType;
		private String filename;
		private Instant creationTime;
		private Map<String, String> otherMetadata;

		public Builder setLength(Long length) {
			this.length = length;
			return this;
		}

		public Builder setMimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}

		public Builder setFilename(String filename) {
			this.filename = filename;
			return this;
		}

		public Builder setCreationTime(Instant creationTime) {
			this.creationTime = creationTime;
			return this;
		}
		
		public Builder setOtherMetadata(Map<String, String> otherMetadata) {
			this.otherMetadata = otherMetadata;
			return this;
		}

		public ObjectMetadata build() {
			return new ObjectMetadata(length, mimeType, filename, creationTime, otherMetadata);
		}
	}
}
