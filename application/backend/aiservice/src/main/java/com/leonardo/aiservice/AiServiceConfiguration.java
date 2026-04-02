package com.leonardo.aiservice;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.leonardo.aiservice.internal.AiGatewayClient;
import com.leonardo.aiservice.internal.KubernetesTokenManager;
import com.leonardo.aiservice.internal.NoOpTokenProvider;
import com.leonardo.aiservice.internal.TokenProvider;
import com.leonardo.aiservice.request.AiRequest;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import reactor.netty.http.client.HttpClient;

@AutoConfiguration
@ConditionalOnClass(AiRequest.class) // un po' superfluo
@EnableConfigurationProperties(AiServiceProperties.class)
public class AiServiceConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AiServiceConfiguration.class);


    @Bean
    @ConditionalOnMissingBean(AiService.class)
    AiService aiService(AiGatewayClient aiWebClient) {
        DefaultAiService defaultAiService = new DefaultAiService(aiWebClient);
        log.debug("Bean AiService inizializzato");
        return defaultAiService;
    }

    @Bean
    @ConditionalOnMissingBean(AiGatewayClient.class)
    AiGatewayClient aiGatewayClient(TokenProvider tokenProvider, WebClient webClient, AiServiceProperties props) {
        AiGatewayClient aiGatewayClient = new AiGatewayClient(tokenProvider, webClient, props.uri(), props.endpoint(), props.k8s().enabled());
        log.debug("Bean AiGatewayClient inizializzato con URI {}", props.uri());
        return aiGatewayClient;
    }

    @Bean
    @ConditionalOnMissingBean(WebClient.class)
    WebClient webClient(WebClient.Builder builder, AiServiceProperties props) {
      return builder
    	.exchangeStrategies(ExchangeStrategies.builder()
    		            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(200 * 1024 * 1024)) // 20 MB
    		            .build())
         .clientConnector(new ReactorClientHttpConnector(
              HttpClient.create()
                  .responseTimeout(Duration.ofSeconds(props.timeoutSeconds()))
          ))
          .build();
  }

    @Bean
    @ConditionalOnMissingBean(TokenProvider.class)
    @ConditionalOnProperty(prefix = "aiservice.k8s", name = "enabled", havingValue = "false")
    TokenProvider noOpTokenProvider() {
        log.warn("Nessuna connessione con client Kubernetes API configurata");
        return new NoOpTokenProvider();
    }

    // Kubernetes beans; qui configuriamo il client per parlare con la Kubernetes API, usiamo la configurazione di default

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(KubernetesClient.class)
    @ConditionalOnProperty(
        prefix = "aiservice.k8s",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    static class KubernetesConfiguration {

        private static final Logger log = LoggerFactory.getLogger(KubernetesConfiguration.class);

        @Bean(destroyMethod = "close")
        @ConditionalOnMissingBean(KubernetesClient.class)
        KubernetesClient kubernetesClient() {
            KubernetesClient client = new KubernetesClientBuilder().build();
            log.info("Client Kubernetes API configurato con successo");
            return client;
        }

        @Bean
        @ConditionalOnMissingBean(TokenProvider.class)
        TokenProvider tokenManager(KubernetesClient kubernetesClient, AiServiceProperties props) {
            KubernetesTokenManager kubernetesTokenManager = new KubernetesTokenManager(kubernetesClient, props.k8s().namespace(), props.k8s().serviceAccount());
            log.debug("AiService TokenManager bean è stato istanziato con successo");
            return kubernetesTokenManager;
        }

    }
}
