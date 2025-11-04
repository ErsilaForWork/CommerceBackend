package ers.backend.security.repo;

import ers.backend.security.model.AppRole;
import ers.backend.security.model.AppRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepo extends JpaRepository<AppRole, Integer> {
    boolean existsAppRoleByEnumRole(AppRoleEnum enumRole);

    AppRole findByEnumRole(AppRoleEnum enumRole);

    AppRole getByEnumRole(AppRoleEnum appRoleEnum);
}
