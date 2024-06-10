package au.com.galatexcollection.my_task_tracker.security;

import au.com.galatexcollection.my_task_tracker.entity.RoleName;
import au.com.galatexcollection.my_task_tracker.exception.CustomException;
import au.com.galatexcollection.my_task_tracker.security.repository.CustomSecurityContextRepository;
import au.com.galatexcollection.my_task_tracker.security.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String [] ALLOWED_PATTERN = {"/api/v1/users/auth/**"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


    @Bean
    @Profile("prod")
    public AuthenticationManager authenticationManager(
        final PasswordEncoder passwordEncoder,              //autowire PasswordEncoder
        final CustomUserDetailsService userDetailsService   //autowire CustomUserDetailsService
        )
    {
        var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setUserDetailsService(userDetailsService);

        return new ProviderManager(authenticationProvider);
    }


    @Bean
    @Profile("prod")
    public SecurityFilterChain securityFilterChain(final HttpSecurity http, final AuthenticationManager authenticationManager,
                                                   final CustomSecurityContextRepository securityContextRepository,
                                                   final ObjectMapper mapper /**autowire**/) throws Exception {

        return http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.POST, ALLOWED_PATTERN).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/va/tasks").hasAuthority(RoleName.ROLE_ADMIN.name())
                    //.anyRequest().authenticated())
                .anyRequest().hasAuthority(RoleName.ROLE_USER.name()))
            .authenticationManager(authenticationManager)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(context -> context.securityContextRepository(securityContextRepository))
            .exceptionHandling(ex -> ex  //401
                .authenticationEntryPoint((request, response, authException) -> {
                    LOG.error(authException.getMessage());
                    var errResponse = new CustomException(401, HttpStatus.UNAUTHORIZED.name(), authException.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(mapper.writeValueAsString(errResponse));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    LOG.error(accessDeniedException.getMessage());
                    var errResponse = new CustomException(403, HttpStatus.FORBIDDEN.name(), accessDeniedException.getMessage());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(mapper.writeValueAsString(errResponse));
                })
            )    // 403)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    @Profile("dev")
    public SecurityFilterChain securityFilterChainDev(HttpSecurity http /* autowire HttpSecurity*/) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(request -> request.anyRequest().permitAll())
            .build();
    }
}
