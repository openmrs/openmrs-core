package org.openmrs.api.storage;

import org.openmrs.api.StorageService;
import org.openmrs.api.stream.StreamDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

@Service
@Conditional(StorageServiceCondition.class)
@Qualifier("s3Storage")
public class S3StorageService  extends BaseStorageService implements StorageService {
	
	//TODO : Implement S3 storage service , S3 bucket configuration should be from application.properties / data source.
	
	public S3StorageService(@Autowired StreamDataService streamService) {
		super(streamService);
		//TODO : Implement S3 storage service
	}

	@Override
	public InputStream getData(String key) throws IOException {
		//TODO : get file from give key path and Convert file into input stream
		return null;
	}

	@Override
	public ObjectMetadata getMetadata(String key) throws IOException {
		//TODO : get file metadata from given key path
		return null;
	}

	@Override
	public Stream<String> getKeys(String moduleIdOrGroup, String keyPrefix) throws IOException {
		//TODO : get all keys from give moduleIdOrGroup and keyPrefix also don't include hidden file.
		return Stream.empty();
	}

	@Override
	public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix) throws IOException {
		//TODO : save file to S3 bucket and return the key
		return "";
	}

	@Override
	public boolean purgeData(String key) throws IOException {
		//TODO : delete file from S3 bucket
		return false;
	}

	@Override
	public boolean exists(String key) {
		//TODO : check if file exists in S3 bucket
		return false;
	}
}
