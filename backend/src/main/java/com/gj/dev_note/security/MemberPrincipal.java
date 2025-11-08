package com.gj.dev_note.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record MemberPrincipal(Long id, String username, String password,
                              Collection<? extends GrantedAuthority> authorities,
                              boolean enabled) implements UserDetails {
    @Override public Collection<? extends GrantedAuthority> getAuthorities(){ return authorities; }
    @Override public String getPassword(){ return password; }
    @Override public String getUsername(){ return username; }
    @Override public boolean isAccountNonExpired(){ return true; }
    @Override public boolean isAccountNonLocked(){ return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled(){ return enabled; }

    public static MemberPrincipal fromJwt(Long id, String username,
                                          Collection<? extends GrantedAuthority> authorities) {
        return new MemberPrincipal(id, username, "", authorities, true);
    }
}
