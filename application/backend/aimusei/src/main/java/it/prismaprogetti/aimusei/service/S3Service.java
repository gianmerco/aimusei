package it.prismaprogetti.aimusei.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.awspring.cloud.s3.S3Template;
import lombok.SneakyThrows;

@Service
public class S3Service {
	
	@Autowired
	private S3Template s3Template;
	
	private static final String BUCKET_NAME = "museiitaliani-schedemuseo";

	public void saveInBucket(byte[] document, String idMuseo) {
		InputStream inputStream = new ByteArrayInputStream(document);
		s3Template.upload(BUCKET_NAME, idMuseo+".pdf", inputStream);
	}

	public URL getSignedGetUrl(String idMuseo) {
		return s3Template.createSignedGetURL(BUCKET_NAME, idMuseo+".pdf", Duration.ofMinutes(15));
	}

	public Boolean objectExists(String idMuseo) {
		return s3Template.objectExists(BUCKET_NAME, idMuseo+".pdf");
	}

	@SneakyThrows
	public byte[] getImage(String idMuseo) {
		return s3Template.download(BUCKET_NAME, idMuseo + ".pdf").getInputStream().readAllBytes();
	}
}