package ch.modul295.yannisstebler.FinanceApp.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.username}")
    private String username;

    @Value("${keycloak.password}")
    private String password;

    @Bean
    public Keycloak keycloak() {

        try {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .clientId("admin-cli")
                    .username(username)
                    .password(password)
                    .build();

            // Test the connection
            keycloak.serverInfo().getInfo();

            return keycloak;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Keycloak client", e);
        }
    }
}
