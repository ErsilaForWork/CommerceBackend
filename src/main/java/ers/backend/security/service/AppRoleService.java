package ers.backend.security.service;

import ers.backend.security.model.AppRole;
import ers.backend.security.model.AppRoleEnum;
import ers.backend.security.repo.RoleRepo;
import org.springframework.stereotype.Service;

@Service
public class AppRoleService {

    private final RoleRepo roleRepo;

    public AppRoleService(RoleRepo roleRepo) {
        this.roleRepo = roleRepo;
    }

    public AppRole getUserRole() {
        return roleRepo.findByEnumRole(AppRoleEnum.ROLE_USER);
    }

    public AppRole getAdminRole() {
        return roleRepo.findByEnumRole(AppRoleEnum.ROLE_ADMIN);
    }
}
