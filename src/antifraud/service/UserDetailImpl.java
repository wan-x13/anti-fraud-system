package antifraud.service;

import antifraud.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;



public class UserDetailImpl implements UserDetails {
    private final String username;
    private  final String password;
    private final List<GrantedAuthority> rolesAuthorities ;
    private final boolean isAccountNonLocked;


    public UserDetailImpl(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.rolesAuthorities = List.of(new SimpleGrantedAuthority(user.getRole()));
        this.isAccountNonLocked = user.isAccountNonLocked();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolesAuthorities;
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }



}
