package ers.backend.security.service.impl;

import ers.backend.security.repo.AppUserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepo userRepo;

    public UserDetailsServiceImpl(AppUserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return UserDetailsImpl.build(userRepo.findByUsername(username).get());
        }catch (NoSuchElementException e){
            throw new UsernameNotFoundException("Username not found!");
        }
    }
}
