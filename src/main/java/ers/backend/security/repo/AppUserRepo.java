package ers.backend.security.repo;

import ers.backend.security.model.AppRoleEnum;
import ers.backend.security.model.AppUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    AppUser getByUsername(String username);

    List<AppUser> findAllByRole_EnumRole(AppRoleEnum enumRole);

}
