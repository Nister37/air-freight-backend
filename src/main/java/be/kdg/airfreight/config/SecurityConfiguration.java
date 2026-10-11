package be.kdg.airfreight.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableMethodSecurity
class SecurityConfiguration {
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .formLogin(login -> login.permitAll())
                .exceptionHandling(errors -> errors.defaultAuthenticationEntryPointFor(
                        (request, response, exception) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()),
                        request -> request.getRequestURI().startsWith(request.getContextPath() + "/api/")))
                .build();
    }

    @Bean
    @Profile("!local")
    UserDetailsService users() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    @Profile("local")
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Public development fixtures, available only with the local profile. */
    @Bean
    @Profile("local")
    UserDetailsService localUsers(BCryptPasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("planner").password(encoder.encode("local123")).roles("flight-planner").build(),
                User.withUsername("loader").password(encoder.encode("local123")).roles("load-planner").build(),
                User.withUsername("admin").password(encoder.encode("local123")).roles("admin").build());
    }

    @Bean
    WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
