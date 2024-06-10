package au.com.galatexcollection.my_task_tracker.security;

import au.com.galatexcollection.my_task_tracker.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final Integer id;
    private final String username;
    private final String password;
    private final String name;
    private final Set<? extends GrantedAuthority> grantedAuthorities;

    public CustomUserDetails(Integer id, String username, String password, String name, Set<? extends GrantedAuthority> grantedAuthorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.grantedAuthorities = grantedAuthorities;
    }

    public static CustomUserDetails createUserDetails(User user) {
        Set<GrantedAuthority> authorities = user.getRoles()
            .stream().map(roleName -> new SimpleGrantedAuthority(roleName.name()))
            .collect(Collectors.toSet());

        return  new CustomUserDetails(user.getId(), user.getUsername(), user.getPassword(), user.getName(), authorities);
    }

    public Integer getId() {
        return id;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.grantedAuthorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
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
        return true;
    }
}
