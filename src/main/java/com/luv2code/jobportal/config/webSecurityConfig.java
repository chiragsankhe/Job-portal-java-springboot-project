package com.luv2code.jobportal.config;


import com.luv2code.jobportal.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class webSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final String[] publicUrl = {"/",
            "/global-search/**",
            "/register",
            "/register/**",
            "/webjars/**",
            "/resources/**",
            "/assets/**",
            "/css/**",
            "/summernote/**",
            "/js/**",
            "/*.css",
            "/*.js",
            "/*.js.map",
            "/fonts**", "/favicon.ico", "/resources/**", "/error"};

    @Autowired
    public webSecurityConfig(CustomUserDetailsService customUserDetailsService, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // your custom AuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // public / protected URLs
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(publicUrl).permitAll();
                    auth.anyRequest().authenticated();
                })

                // ── form login ───────────────────────────────────────
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(customAuthenticationSuccessHandler)
                )   // ← close the formLogin lambda here

                // ── logout ──────────────────────────────────────────
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                )

                // ── CORS & CSRF ─────────────────────────────────────
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEndcoder());
        authenticationProvider.setUserDetailsService(customUserDetailsService); // use this instead
        return authenticationProvider;
    }


    @Bean
    public PasswordEncoder passwordEndcoder() {

        return new BCryptPasswordEncoder();
    }
}
