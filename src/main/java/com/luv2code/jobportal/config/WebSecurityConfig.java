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
public class WebSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Autowired
    public WebSecurityConfig(CustomUserDetailsService customUserDetailsService, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

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



    @Bean
  protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
  {
      http.authenticationProvider(autheticationProvider());


      http.authorizeHttpRequests(auth -> {
          auth.requestMatchers(publicUrl).permitAll();
          auth.anyRequest().authenticated();
      });

      http.formLogin(form->form.loginPage("/login").permitAll()
              .successHandler(customAuthenticationSuccessHandler))
              .logout(logout ->
              {
                  logout.logoutUrl("/logout");
              logout.logoutSuccessUrl("/");
              }).cors(Customizer.withDefaults())
              .csrf(csrf ->csrf.disable());
      return http.build();
  }
   @Bean
    public AuthenticationProvider autheticationProvider() {


        DaoAuthenticationProvider autheticationProvider = new DaoAuthenticationProvider();
        autheticationProvider.setPasswordEncoder(passwordEncoder());
        autheticationProvider.setUserDetailsService(customUserDetailsService);

        return autheticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
