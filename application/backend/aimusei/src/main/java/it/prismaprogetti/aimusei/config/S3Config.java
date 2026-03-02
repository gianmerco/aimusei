package it.prismaprogetti.aimusei.config;

import java.net.URI;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.s3.InMemoryBufferingS3OutputStreamProvider;
import io.awspring.cloud.s3.Jackson2JsonS3ObjectConverter;
import io.awspring.cloud.s3.S3ObjectConverter;
import io.awspring.cloud.s3.S3OutputStreamProvider;
import io.awspring.cloud.s3.S3Template;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Configurazione S3 con SSL trust-all.
 * Workaround per certificato S3 PSN scaduto lato server.
 *
 * NOTA: creiamo esplicitamente S3Client, S3Presigner e S3Template
 * per evitare che l'auto-configurazione di Spring Cloud AWS
 * crei un S3Client col TrustManager di default del JDK.
 */
@Configuration
public class S3Config {

    @Value("${spring.cloud.aws.s3.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.aws.s3.region}")
    private String region;

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    private TrustManager createTrustAllManager() {
        return new X509TrustManager() {
            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            @Override public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            @Override public void checkServerTrusted(X509Certificate[] certs, String authType) { }
        };
    }

    private SdkHttpClient createTrustAllHttpClient() {
        return ApacheHttpClient.builder()
                .tlsTrustManagersProvider(() -> new TrustManager[]{ createTrustAllManager() })
                .build();
    }

    @Bean
    @Primary
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(true)
                        .checksumValidationEnabled(false)
                        .build())
                .httpClient(createTrustAllHttpClient())
                .build();
    }

    @Bean
    @Primary
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .checksumValidationEnabled(false)
                        .build())
                .build();
    }

    @Bean
    @Primary
    public S3Template s3Template() {
        S3Client client = s3Client();
        S3OutputStreamProvider outputStreamProvider =
                new InMemoryBufferingS3OutputStreamProvider(client, null);
        S3ObjectConverter objectConverter =
                new Jackson2JsonS3ObjectConverter(new ObjectMapper());
        return new S3Template(client, outputStreamProvider, objectConverter, s3Presigner());
    }
}
