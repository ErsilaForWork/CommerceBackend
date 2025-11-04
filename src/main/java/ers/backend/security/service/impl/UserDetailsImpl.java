package ers.backend.security.service.impl;

import ers.backend.security.model.AppRole;
import ers.backend.security.model.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final String username;
    private String password;
    private final List<GrantedAuthority> authorities;


    public UserDetailsImpl(String username, String password, AppRole role) {
        this.username = username;
        this.password = password;
        this.authorities = List.of(new SimpleGrantedAuthority(role.toString()));
    }

    public static  UserDetailsImpl build(AppUser user) {
        return new UserDetailsImpl(user.getUsername(), user.getPassword(), user.getRole());
    }

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
}
