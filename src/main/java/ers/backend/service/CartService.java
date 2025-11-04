package ers.backend.service;

import ers.backend.exceptions.NotEnoughMoneyException;
import ers.backend.exceptions.OwnProductBuyingException;
import ers.backend.model.CartItem;
import ers.backend.model.Product;
import ers.backend.repo.CartRepo;
import ers.backend.security.model.AppUser;
import ers.backend.security.service.AppUserService;
import ers.backend.service.cloud.CloudinaryService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepo cartRepo;
    private final AppUserService userService;
    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    public CartService(CartRepo cartRepo, AppUserService userService, ProductService productService, CloudinaryService cloudinaryService) {
        this.cartRepo = cartRepo;
        this.userService = userService;
        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
    }


    public CartItem save(CartItem cartItem) {
        return cartRepo.save(cartItem);
    }

    public boolean isOwner(Long cartItemId, String username) {
        if(!cartRepo.existsById(cartItemId))
            return false;
        CartItem cartItem = cartRepo.findById(cartItemId).get();
        return cartItem.getOwner().getUsername().equals(username);
    }

    public void pursache(CartItem cartItem, AppUser buyer) throws NotEnoughMoneyException, OwnProductBuyingException {

        AppUser seller = cartItem.getProduct().getOwner();

        if(seller.equals(buyer))
            throw new OwnProductBuyingException("Buying Own Product!");

        Product product = cartItem.getProduct();

        float price = cartItem.getCartQuantity() * product.getPrice();

        if(buyer.getBalance() < price)
            throw new NotEnoughMoneyException("Cant buy a product " + product.getName());

        buyer.setBalance(buyer.getBalance() - price);
        seller.setBalance(seller.getBalance() + price);
        product.setStockQuantity(product.getStockQuantity() - cartItem.getCartQuantity());

        if(product.getStockQuantity() <= 0) {
            String imageId = product.getImageId();

            try {
                cloudinaryService.delete(imageId);
                productService.delete(product);
                buyer.getCartItems().remove(cartItem);
            } catch (IOException e) {
                product.setAvailable(false);
                System.out.println("Disable to delete Image of the product with product ID" + product.getProductId());
            }
        }

        userService.save(buyer);
        userService.save(seller);

    }

    public boolean existById(Long cartItemId) {
        return cartRepo.existsById(cartItemId);
    }

    public Optional<CartItem> findById(Long cartItemId) {
        return cartRepo.findById(cartItemId);
    }

    public void delete(CartItem cartItem) {
        cartRepo.delete(cartItem);
    }
}
