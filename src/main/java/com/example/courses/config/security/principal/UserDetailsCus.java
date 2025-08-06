package com.example.courses.config.security.principal;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Builder
@Getter
public class UserDetailsCus implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Chưa implement logic expired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Chưa implement logic lock
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Chưa implement logic password expired
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}