package com.secj3303.config;

import java.util.Collections;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.secj3303.dao.PersonDao;
import com.secj3303.model.Person;

@Configuration
@EnableWebSecurity
@SuppressWarnings("deprecation")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final PersonDao personDao;

    public SecurityConfig(PersonDao personDao) {
        this.personDao = personDao;
    }

    // ===============================
    // AUTHENTICATION (DAO + HIBERNATE)
    // ===============================
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(username -> {

            // username = email
            Person person = personDao.findByEmail(username);

            if (person == null) {
                throw new UsernameNotFoundException(
                        "User not found with email: " + username);
            }

            return new org.springframework.security.core.userdetails.User(
                person.getEmail(),
                person.getPassword(),
                true,   // enabled
                true,   // accountNonExpired
                true,   // credentialsNonExpired
                true,   // accountNonLocked
                Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_" + person.getRole())
                )
            );
        });
    }

    // ===============================
    // AUTHORIZATION
    // ===============================
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .csrf().disable()

            .authorizeRequests()
                .antMatchers("/login", "/resources/**", "/css/**", "/js/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/member/**").hasRole("MEMBER")
                .anyRequest().authenticated()
            .and()

            .formLogin()
                .loginPage("/login")
                .usernameParameter("username") // email
                .passwordParameter("password")
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