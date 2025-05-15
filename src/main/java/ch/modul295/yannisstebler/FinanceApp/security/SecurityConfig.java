package ch.modul295.yannisstebler.financeapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    @Value("${spring.application.name}")
    private String appName;

    // Whitelisted paths that don't require authentication
    private static final String[] AUTH_WHITELIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs.yaml",
        "/swagger"
    };

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(AUTH_WHITELIST).permitAll() // Existing whitelist
                .requestMatchers(new AntPathRequestMatcher("/api/users", "POST")).permitAll() // Allow POST to /api/users
                .anyRequest().authenticated()) // Require authentication for all other requests
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(new AuthenticationRoleConverter(appName))))
                .csrf(csrf -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/api/users", "POST"))
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler))
                .cors(cors -> corsConfigurer());

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS")
                        .allowedOrigins("http://localhost:4200", "http://localhost:8080");
            }
        };
    }
}
