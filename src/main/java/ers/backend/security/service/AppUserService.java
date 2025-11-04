package ers.backend.security.service;

import ers.backend.DTO.UserDTO;
import ers.backend.exceptions.UserAlreadyExistException;
import ers.backend.security.model.AppRoleEnum;
import ers.backend.security.model.AppUser;
import ers.backend.security.repo.AppUserRepo;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AppUserService {

    private final AppUserRepo userRepo;

    public AppUserService(AppUserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public AppUser getByUsername(String username) {
        return userRepo.getByUsername(username);
    }


    public void save(AppUser user) throws UserAlreadyExistException {
        try {
            userRepo.save(user);
        }catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistException("User with username "+user.getUsername()+" already exists!");
        }
    }


    public boolean checkExistsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    public List<AppUser> getAllUsers() {
        return userRepo.findAllByRole_EnumRole(AppRoleEnum.ROLE_USER);
    }

    public AppUser getById(Long userId) throws NoSuchElementException {
        return userRepo.findById(userId).get();
    }

    public void delete(AppUser user) {
        userRepo.delete(user);
    }
}
