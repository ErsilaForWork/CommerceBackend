package ers.backend.security.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@SequenceGenerator(
        name = "seq",
        sequenceName = "role_seq",
        allocationSize = 1
)
@Table(
        name = "roles"
)
public class AppRole {

    public AppRole() {}

    public AppRole(AppRoleEnum appRoleEnum) {
        this.enumRole = appRoleEnum;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @Column(name = "role_id")
    private int roleId;

    @Column(name = "role_name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private AppRoleEnum enumRole;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @JsonBackReference
    private final Set<AppUser> users = new HashSet<>();



    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public AppRoleEnum getEnumRole() {
        return enumRole;
    }

    public void setEnumRole(AppRoleEnum enumRole) {
        this.enumRole = enumRole;
    }

    public Set<AppUser> getUsers() {
        return users;
    }

    @Override
    public String toString() {
        return switch (enumRole) {
            case ROLE_USER -> "ROLE_USER";
            case ROLE_ADMIN -> "ROLE_ADMIN";
        };
    }
}
