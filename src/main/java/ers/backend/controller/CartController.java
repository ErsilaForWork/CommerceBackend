package ers.backend.controller;

import ers.backend.DTO.CartItemDTO;
import ers.backend.DTO.QuantityDTO;
import ers.backend.DTO.ResponseMessage;
import ers.backend.exceptions.NotEnoughMoneyException;
import ers.backend.exceptions.OwnProductBuyingException;
import ers.backend.exceptions.ProductNotFoundException;
import ers.backend.model.CartItem;
import ers.backend.model.Product;
import ers.backend.security.model.AppUser;
import ers.backend.security.service.AppUserService;
import ers.backend.service.CartService;
import ers.backend.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(
        origins = "http://localhost:5173",
        allowCredentials = "true",
        allowedHeaders = "*"
)
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final AppUserService userService;

    public CartController(CartService cartService, ProductService productService, AppUserService userService) {
        this.cartService = cartService;
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping
    public Set<CartItem> getCartOfUser(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userService.getByUsername(userDetails.getUsername());
        return user.getCartItems();
    }

    //Maybe we can do it with the PathVariable instead of DTO?
    @PostMapping
    public ResponseEntity<?> createCartItem(@RequestBody CartItemDTO cartItemData, @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Product product = productService.findById(cartItemData.getProductId());

            if(!product.isAvailable())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            AppUser owner = userService.getByUsername(userDetails.getUsername());

            CartItem cartItem = new CartItem(product, owner);
            CartItem saved = cartService.save(cartItem);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        }catch (ProductNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @cartService.isOwner(#cartItemId, authentication.name)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCartItem(@PathVariable("id") @P("cartItemId") Long cartItemId) {
        if(!cartService.existById(cartItemId))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        CartItem cartItem = cartService.findById(cartItemId).get();
        cartService.delete(cartItem);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @cartService.isOwner(#cartItemId, authentication.name)")
    @PutMapping("/{id}")
    public ResponseEntity<?> changeQuantity(@PathVariable("id") @P("cartItemId") Long cartItemId, @RequestBody QuantityDTO quantityDTO) {
        if(!cartService.existById(cartItemId))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        CartItem cartItem = cartService.findById(cartItemId).get();

        if(cartItem.getProduct().getStockQuantity() < quantityDTO.getQuantity())
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        cartItem.setCartQuantity(Math.max(quantityDTO.getQuantity(), 1));
        return new ResponseEntity<>(cartService.save(cartItem), HttpStatus.OK);
    }


    @PostMapping("/pursache")
    @Transactional
    public ResponseEntity<?> pursacheAllCart(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser cartOwner = userService.getByUsername(userDetails.getUsername());
        Set<CartItem> cartItems = cartOwner.getCartItems();
        Set<CartItem> cartItemSet = new HashSet<>(cartItems);

        System.out.println("Getting users carts succesfull!");

        boolean ownBuyingFlag = false;
        for (CartItem cartItem : cartItemSet) {
            System.out.println("Pursaching cartitem with id:" + cartItem.getItemId());
            try{
                cartService.pursache(cartItem, cartOwner);
            }catch (NotEnoughMoneyException e) {
                return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.CONFLICT);
            }catch (OwnProductBuyingException e) {
                ownBuyingFlag = true;
                continue;
            }
            cartOwner.getCartItems().remove(cartItem);
        }

        if(ownBuyingFlag)
            return new ResponseEntity<>(new ResponseMessage("There was users own products!"), HttpStatus.OK);
        return new ResponseEntity<>(new ResponseMessage("Successfull!"), HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ROLE_ADMIN') or @cartService.isOwner(#cartItemId, authentication.name)")
    @PostMapping("/pursache/{id}")
    @Transactional
    public ResponseEntity<?> pursacheProduct(@PathVariable("id") @P("cartItemId") Long cartItemId, @AuthenticationPrincipal UserDetails userDetails) {

        if(!cartService.existById(cartItemId))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        AppUser client = userService.getByUsername(userDetails.getUsername());
        CartItem cartItem = cartService.findById(cartItemId).get();
        cartService.pursache(cartItem, client);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
