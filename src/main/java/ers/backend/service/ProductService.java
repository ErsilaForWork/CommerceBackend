package ers.backend.service;

import ers.backend.DTO.ProductDTO;
import ers.backend.exceptions.ProductNotFoundException;
import ers.backend.model.Product;
import ers.backend.repo.ProductRepo;
import ers.backend.security.model.AppUser;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> getAll() {
        return productRepo.findAll();
    }

    public Product save(ProductDTO productDTO, AppUser owner) {
        Product product = new Product(productDTO, owner);
        return productRepo.save(product);
    }

    public Product save(Product product, Map dataMap) {
        product.setImageId(
                (String) dataMap.get("public_id")
        );
        product.setImageName(
                (String) dataMap.get("original_filename")
        );
        product.setImageUrl(
                (String) dataMap.get("url")
        );
        return productRepo.save(product);
    }

    public boolean isOwner(String username, Long productId) {
        if(!productRepo.existsById(productId))
            return false;

        Product product = productRepo.findById(productId).get();
        return product.getOwner().getUsername().equals(username);
    }

    public void delete(Product product) {
        productRepo.delete(product);
    }

    public void delete(Long productId) throws ProductNotFoundException {

        Product product = productRepo.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("Product not found!")
        );

        productRepo.delete(product);

    }

    public Product update(Long productId,ProductDTO productData) throws ProductNotFoundException, BadRequestException {

        if(!productRepo.existsById(productId))
            throw new ProductNotFoundException("Product Not Found!");
        if(!Objects.equals(productId, productData.getProductId()))
            throw new BadRequestException("Bad Request, ID for product and Actual product are different!");

        Product product = productRepo.findById(productId).get();
        product.update(productData);
        product.setProductId(productId);
        return productRepo.save(product);
    }

    public Product findById(Long productId) throws ProductNotFoundException {

        if(!productRepo.existsById(productId))
            throw new ProductNotFoundException("Product Not Found!");

        return productRepo.findById(productId).get();

    }

    public void deleteAll() {
        productRepo.deleteAll();
    }

    public List<Product> search(String query) {
        return productRepo.search(query);
    }
}
