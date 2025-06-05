package com.arhiva_digitala.digital_archive_api.security.services;

import com.arhiva_digitala.digital_archive_api.model.Utilizator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String numeUtilizator;
    private String email;
    @JsonIgnore
    private String parola;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String numeUtilizator, String email, String parola,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.numeUtilizator = numeUtilizator;
        this.email = email;
        this.parola = parola;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Utilizator utilizator) {
        // Hardcoding role to "USER" for now. This can be enhanced later if roles are stored in the database.
        GrantedAuthority authority = new SimpleGrantedAuthority("USER");

        return new UserDetailsImpl(
                utilizator.getId(), // Using getId() from Utilizator entity
                utilizator.getNumeUtilizator(),
                utilizator.getEmail(),
                utilizator.getParola(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return parola;
    }

    @Override
    public String getUsername() {
        return numeUtilizator;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Puteti folosi campul 'esteActiv' din entitatea Utilizator aici
        // return utilizator.isEsteActiv(); 
        return true; // Pentru simplitate, il lasam true
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
