package com.br.ats.consult.api.security.config;

import com.br.ats.consult.api.security.RestAccessDeniedHandler;
import com.br.ats.consult.api.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityAuthConfig {

    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Value("${app.security.csrf.enabled:true}")
    private boolean csrfEnabled;

    @Value("${app.security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.security.cookie.same-site:Strict}")
    private String cookieSameSite;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(parseAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-XSRF-TOKEN", "X-Requested-With", "Accept", "X-Request-Id"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .secure(cookieSecure)
                .sameSite(cookieSameSite));
        return repository;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository()))
                .csrf(csrf -> {
                    if (!csrfEnabled) {
                        csrf.disable();
                        return;
                    }
                    csrf.csrfTokenRepository(csrfTokenRepository());
                    csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
                })
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                        .requestMatchers(HttpMethod.GET, "/auth/csrf", "/api/v1/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/auth/login", "/api/v1/auth/login",
                                "/auth/register", "/api/v1/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/me", "/api/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/auth/logout", "/api/v1/auth/logout").authenticated()

                        .requestMatchers("/api/v1/admin/users", "/api/v1/admin/users/**")
                        .hasAnyRole("ADMIN", "TECH_RECRUITER")

                        .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**",
                                "/*.js", "/*.css", "/*.map", "/*.ico", "/*.png", "/*.svg", "/*.webmanifest").permitAll()
                        .requestMatchers(HttpMethod.GET, "/login", "/register", "/home", "/jobs", "/jobs/**",
                                "/candidates", "/clients", "/processes", "/processes/**",
                                "/users", "/users/**", "/privacidade").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/job-positions/*/applications").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/job-positions/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/job-positions/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/job-positions/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/job-positions/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/technologies/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/technologies/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/technologies/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/technologies/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/v1/clients/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/clients/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/clients/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/clients/**").hasAnyRole("ADMIN", "TECH_RECRUITER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/candidates/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/candidates/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/candidates/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidates/**").hasAnyRole("ADMIN", "TECH_RECRUITER")

                        .requestMatchers(HttpMethod.POST, "/api/v1/selection-processes/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/selection-processes/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/selection-processes/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/selection-processes/**").hasAnyRole("ADMIN", "TECH_RECRUITER")

                        .requestMatchers(HttpMethod.GET, "/api/v1/applications/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/applications/**").hasAnyRole("ADMIN", "TECH_RECRUITER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/applications/*/comments").hasAnyRole("ADMIN", "TECH_RECRUITER")

                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    private List<String> parseAllowedOrigins() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
