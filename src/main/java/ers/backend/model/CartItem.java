package ers.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ers.backend.security.model.AppUser;
import jakarta.persistence.*;

@Entity
@Table(
        name = "cart_items"
)
@SequenceGenerator(
        name = "seq",
        sequenceName = "cartSeqDemo",
        allocationSize = 1
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    private Long itemId;

    private Integer cartQuantity;

    @ManyToOne
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "product_id"
    )
    @JsonManagedReference
    private Product product;

    @ManyToOne
    @JoinColumn(
            name = "owner_id",
            referencedColumnName = "user_id"
    )
    @JsonManagedReference
    private AppUser owner;

    public CartItem(Product product, AppUser owner) {
        this.product = product;
        this.owner = owner;
        this.cartQuantity = 1;
    }

    public CartItem() {}

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getCartQuantity() {
        return cartQuantity;
    }

    public void setCartQuantity(Integer cartQuantity) {
        this.cartQuantity = cartQuantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }


}
