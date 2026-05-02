package com.personalProject.code_reviewer.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Slf4j
@Configuration
public class AppConfig {

    @Bean
    public RestClient githubRestClient(@Value("${app.github.token}") String githubToken) {
        log.atDebug().addArgument(githubToken).log("AppConfig.githubRestClient | GitHubToken: {}");
        HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .build();
    }
}
