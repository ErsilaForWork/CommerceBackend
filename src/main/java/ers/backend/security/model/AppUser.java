package ers.backend.security.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ers.backend.DTO.UserDTO;
import ers.backend.model.CartItem;
import ers.backend.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
        }
)
@SequenceGenerator(
        name = "seq",
        sequenceName = "user_seq",
        allocationSize = 1
)
public class AppUser {

    public AppUser() {}

    public AppUser(String username, String password, AppRole appRole) {
        this.username = username;
        this.password = password;
        this.role = appRole;
    }

    public AppUser(String username, String password, AppRole appRole, float balance) {
        this.username = username;
        this.password = password;
        this.role = appRole;
        this.balance = balance;
    }

    public AppUser(UserDTO userDTO, AppRole role) {
        this.username = userDTO.getUsername();
        this.password = userDTO.getPassword();
        this.role = role;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Size(max = 100)
    private String username;

    @NotBlank
    @Size(min = 8)
    @JsonIgnore
    private String password;

    private Float balance = 0F;

    @OneToMany(
            mappedBy = "owner",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @JsonBackReference
    private final Set<Product> products = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    @JsonManagedReference
    private AppRole role;

    @OneToMany(
            mappedBy = "owner",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonBackReference
    private Set<CartItem> cartItems = new HashSet<>();

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDate creationTime;

    public LocalDate getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDate creationTime) {
        this.creationTime = creationTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public AppRole getRole() {
        return role;
    }

    public void setRole(AppRole role) {
        this.role = role;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public Set<CartItem> getCartItems() {
        return cartItems;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass()) return false;
        AppUser appUser = (AppUser) o;

        if(this.userId == null || appUser.getUserId() == null) return false;

        return this.userId == appUser.getUserId();
    }

    @Override
    public int hashCode() {
        if(userId != null)
            return userId.hashCode();
        return System.identityHashCode(this);
    }
}
