package utc.englishlearning.Encybara.service;

import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("adminDetailsService")
public class AdminDetailsCustom implements UserDetailsService {

    private final AdminService adminService;

    public AdminDetailsCustom(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        utc.englishlearning.Encybara.domain.Admin admin = this.adminService.handleGetAdminByUsername(username);
        if (admin == null) {
            throw new UsernameNotFoundException("Username/password không hợp lệ");
        }

        return new User(
                admin.getEmail(),
                admin.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

    }

}