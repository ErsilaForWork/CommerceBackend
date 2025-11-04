package ers.backend.controller;

import ers.backend.DTO.ProductDTO;
import ers.backend.DTO.ResponseMessage;
import ers.backend.exceptions.ProductNotFoundException;
import ers.backend.model.Product;
import ers.backend.security.model.AppUser;
import ers.backend.security.service.AppUserService;
import ers.backend.service.ProductService;
import ers.backend.service.cloud.CloudinaryService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173",
        allowCredentials = "true",
        allowedHeaders = "*"
)

public class MainController {

    private final ProductService productService;
    private final AppUserService userService;
    private final CloudinaryService cloudService;

    public MainController(ProductService productService, AppUserService userService, CloudinaryService cloudService) {
        this.productService = productService;
        this.userService = userService;
        this.cloudService = cloudService;
    }

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAll();
    }

    @GetMapping("/product/owner/{ownerId}")
    public ResponseEntity<?> getOwnersProduct(@PathVariable("ownerId") Long ownerId) {
        AppUser owner;

        try{
            owner = userService.getById(ownerId);
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(owner.getProducts(), HttpStatus.OK);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProduct(@PathVariable("id") Long productId) {
        Product product;

        try {
            product = productService.findById(productId);
            return new ResponseEntity<>(product, HttpStatus.OK);
        }catch (ProductNotFoundException e) {
            return new ResponseEntity<>(new ResponseMessage("Product ID not found!"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/product")
    public List<Product> searchForProduct(@RequestParam("q") String keyword) {
        System.out.println("Searching for : "+keyword);
        return productService.search(keyword);
    }

    @DeleteMapping("/product/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @productService.isOwner(authentication.name, #productId)")
    @Transactional
    public ResponseEntity<?> deleteProduct(@PathVariable("id") @P("productId") Long productId) {

        try{
            Product product = productService.findById(productId);
            if(product.getImageId() != null)
                cloudService.delete(product.getImageId());
            productService.delete(product);

            return new ResponseEntity<>(HttpStatus.OK);
        }catch (ProductNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (IOException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @PutMapping("/product-image/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @productService.isOwner(authentication.name, #productId)")
    @Transactional
    public ResponseEntity<?> changeImage(@PathVariable("id") @P("productId") Long productId,@RequestPart MultipartFile multipartFile) throws IOException {

        BufferedImage bi;
        Product product;

        try{
            bi = ImageIO.read(multipartFile.getInputStream());
            product = productService.findById(productId);
            cloudService.delete(product.getImageId());
        }catch (IOException | ProductNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if(bi == null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Map result = cloudService.upload(multipartFile);
        Product savedProduct = productService.save(product, result);
        return new ResponseEntity<>(savedProduct, HttpStatus.OK);
    }


    //Must be done! There confusion between productId and productData.getproductId()
    @PutMapping("/product/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @productService.isOwner(authentication.name, #productId)")
    @Transactional
    public ResponseEntity<?> changeParams(@PathVariable("id") @P("productId") Long productId, @Valid @RequestBody ProductDTO productData, @AuthenticationPrincipal UserDetails ownerDetails, BindingResult br) {
        if(br.hasErrors())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        productData.setProductId(productId);

        System.out.println(productData);

        try{
            AppUser owner = userService.getByUsername(ownerDetails.getUsername());

            if(owner.getRole().toString().equals("ROLE_ADMIN")) {
                productData.setOwner(productService.findById(productId).getOwner());
            }else {
                productData.setOwner(owner);
            }
            Product product = productService.update(productId,productData);
            System.out.println(productData);
            return new ResponseEntity<>(product, HttpStatus.OK);

        }catch (ProductNotFoundException | BadRequestException e) {
            System.out.println("EXCEPTION!" + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/product")
    @Transactional
    public ResponseEntity<?> createProduct(@Valid @RequestPart ProductDTO productDTO, @RequestPart MultipartFile multipartFile, @AuthenticationPrincipal UserDetails userDetails, BindingResult br) throws IOException {

        BufferedImage bi;
        try {
            bi = ImageIO.read(multipartFile.getInputStream());
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseMessage("Cant upload an Image!") ,HttpStatus.BAD_REQUEST);
        }
        System.out.println("1) We get Image");
        if(br.hasErrors() || bi == null)
            return new ResponseEntity<>(new ResponseMessage("Enter the essential fields!"),HttpStatus.BAD_REQUEST);

        Map result;

        try {
             result = cloudService.upload(multipartFile);
        }catch (IOException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        productDTO.setImageId(
                (String) result.get("public_id")
        );
        productDTO.setImageName(
                (String) result.get("original_filename")
        );
        productDTO.setImageUrl(
                (String) result.get("url")
        );

        AppUser owner = userService.getByUsername(userDetails.getUsername());

        Product product = productService.save(productDTO,owner);

        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

}
