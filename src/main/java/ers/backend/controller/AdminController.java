package ers.backend.controller;

import ers.backend.model.Product;
import ers.backend.security.model.AppUser;
import ers.backend.security.service.AppUserService;
import ers.backend.service.ProductService;
import ers.backend.service.cloud.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173",
        allowCredentials = "true",
        allowedHeaders = "*"
)
public class AdminController {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService;
    private final AppUserService userService;

    public AdminController(ProductService productService, CloudinaryService cloudinaryService, AppUserService userService) {
        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
        this.userService = userService;
    }

    @GetMapping("/users/all")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok()
                .body(userService.getAllUsers());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long userId) {
        AppUser user;

        try {
            user = userService.getById(userId);
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId) {
        AppUser user;

        try {
            user = userService.getById(userId);
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Set<Product> userProducts = user.getProducts();

        for (Product product : userProducts) {
            String imageId = product.getImageId();

            try {
                cloudinaryService.delete(imageId);
            }catch (IOException e) {
                System.out.println("Enable to delete the Image from cloudinary with product ID:"+ product.getProductId()+", check the DB");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        userService.delete(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/products/all")
    public void deleteAllProducts() {
        List<Product> products = productService.getAll();

        for(Product product : products) {
            String imageId = product.getImageId();
            try {
                cloudinaryService.delete(imageId);
            }catch (IOException e) {
                System.out.println("Enable to delete the Image from cloudinary with product ID:"+ product.getProductId()+", check the DB");
                continue;
            }

            productService.delete(product);
        }
    }

}
