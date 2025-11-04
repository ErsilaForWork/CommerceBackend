package ers.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import ers.backend.DTO.ProductDTO;
import ers.backend.security.model.AppUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "products"
)
@SequenceGenerator(
        name = "seq",
        sequenceName = "product_seq",
        allocationSize = 1
)
public class Product {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq"
    )
    @Column(name = "product_id")
    private Long productId;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "owner_id",
            referencedColumnName = "user_id",
            updatable = false
    )
    @JsonManagedReference
    private AppUser owner;

    private String brand;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    private String description;

    private Float price;

    @Column(name = "product_available")
    private boolean isAvailable;

    @Column(name = "product_realese_date")
    private LocalDate productReleaseDate;

    @CreationTimestamp
    @Column(
            name = "creation_date",
            updatable = false
    )
    private LocalDate creationDate;

    @Column(name = "quantity")
    private int stockQuantity;

    private String imageName;

    private String imageUrl;

    private String imageId;

    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JsonBackReference
    private Set<CartItem> cartItems = new HashSet<>();

    public Product() {}


    public Product(ProductDTO productDTO, AppUser owner) {
        this.name = productDTO.getName();
        this.brand = productDTO.getBrand();
        this.description = productDTO.getDescription();
        this.price = productDTO.getPrice();
        this.stockQuantity = productDTO.getStockQuantity();
        this.owner = owner;
        this.productReleaseDate = productDTO.getProductReleaseDate();
        this.categories = productDTO.getCategories();
        this.imageId = productDTO.getImageId();
        this.imageUrl = productDTO.getImageUrl();
        this.imageName = productDTO.getImageName();
        this.isAvailable = true;
    }

    public void update(ProductDTO productDTO) {
        this.name = productDTO.getName();
        this.brand = productDTO.getBrand();
        this.categories = productDTO.getCategories();
        this.description = productDTO.getDescription();
        this.price = productDTO.getPrice();
        this.stockQuantity = productDTO.getStockQuantity();
        this.productReleaseDate = productDTO.getProductReleaseDate();
        this.isAvailable = true;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public LocalDate getProductReleaseDate() {
        return productReleaseDate;
    }

    public void setProductReleaseDate(LocalDate productReleaseDate) {
        this.productReleaseDate = productReleaseDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public AppUser getOwner() {
        return owner;
    }


}
