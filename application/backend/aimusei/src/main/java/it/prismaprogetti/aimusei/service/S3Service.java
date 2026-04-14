package it.prismaprogetti.aimusei.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.awspring.cloud.s3.S3Template;
import lombok.SneakyThrows;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
public class S3Service {
	
	@Autowired
	private S3Template s3Template;
	
	@Autowired
    private S3Presigner s3Presigner;  
	
	private static final String BUCKET_NAME = "museiitaliani-schedemuseo";

	public void saveInBucket(byte[] document, String idMuseo) throws IOException {
		try (InputStream inputStream = new ByteArrayInputStream(document)){
			s3Template.upload(BUCKET_NAME, idMuseo+".pdf", inputStream);
		}
	}

    public URL getSignedGetUrl(String idMuseo) {
        String key = idMuseo + ".pdf";

        // Costruisce la richiesta con gli header di risposta per inline
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .responseContentDisposition("inline; filename=\"" + key + "\"")
                .responseContentType("application/pdf")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(15))
                .build();

        PresignedGetObjectRequest signedRequest = s3Presigner.presignGetObject(presignRequest);
        return signedRequest.url();
    }

	public Boolean objectExists(String idMuseo) {
		return s3Template.objectExists(BUCKET_NAME, idMuseo+".pdf");
	}

	@SneakyThrows
	public byte[] getImage(String idMuseo) {
	    try (InputStream is = s3Template.download(BUCKET_NAME, idMuseo + ".pdf").getInputStream()) {
	        return is.readAllBytes();
	    }
	}
}