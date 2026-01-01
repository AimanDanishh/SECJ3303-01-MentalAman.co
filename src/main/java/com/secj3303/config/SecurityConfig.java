package com.secj3303.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        // DEMO users (replace with DB later if needed)
        auth.inMemoryAuthentication()
            .withUser("student@demo.com").password("{noop}demo123").roles("STUDENT")
            .and()
            .withUser("faculty@demo.com").password("{noop}demo123").roles("FACULTY")
            .and()
            .withUser("counsellor@demo.com").password("{noop}demo123").roles("COUNSELLOR")
            .and()
            .withUser("admin@demo.com").password("{noop}demo123").roles("ADMINISTRATOR");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .csrf().disable()   // 🔴 FIX AUTO LOGOUT
            .authorizeRequests()
                .antMatchers("/login", "/resources/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll();
    }
}