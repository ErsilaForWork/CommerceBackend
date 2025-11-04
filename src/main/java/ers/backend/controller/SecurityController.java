package ers.backend.controller;

import ers.backend.DTO.ResponseMessage;
import ers.backend.DTO.UserDTO;
import ers.backend.exceptions.UserAlreadyExistException;
import ers.backend.security.model.AppRole;
import ers.backend.security.model.AppUser;
import ers.backend.security.service.AppRoleService;
import ers.backend.security.service.AppUserService;
import ers.backend.jwt.JwtUtils;
import ers.backend.security.service.impl.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "http://localhost:5173",
        allowCredentials = "true",
        allowedHeaders = "*"
)
public class SecurityController {

    private final AppUserService userService;
    private final AppRoleService roleService;
    private final AppRole ROLE_USER;
    private final AppRole ROLE_ADMIN;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final AuthenticationManager AUTH_MANAGER;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityController(AppUserService userService, AppRoleService roleService, AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.roleService = roleService;
        this.AUTH_MANAGER = authenticationManager;
        this.ROLE_USER = roleService.getUserRole();
        this.ROLE_ADMIN = roleService.getAdminRole();
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie expire = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofSeconds(0))
                .sameSite("Strict")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expire.toString())
                .body(null);
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult br) {
        if(br.hasErrors())
            return new ResponseEntity<>(new ResponseMessage("Not Valid Data!"),HttpStatus.BAD_REQUEST);

        AppUser user = new AppUser(userDTO, ROLE_USER);
        user.setPassword(encoder.encode(user.getPassword()));
        try{
            userService.save(user);
            String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());

            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new ResponseMessage("Succusfull!"));

        }catch (UserAlreadyExistException e) {
            return new ResponseEntity<>(new ResponseMessage("User Already Exists!"),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/data")
    public ResponseEntity<?> getDataAboutUser(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userService.getByUsername(userDetails.getUsername());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDTO userDTO, BindingResult br) {
        if(br.hasErrors() || !userService.checkExistsByUsername(userDTO.getUsername())) {
            return new ResponseEntity<>(new ResponseMessage("Not Valid Data"),HttpStatus.BAD_REQUEST);
        }

        System.out.println(userDTO.getUsername()+" " + userDTO.getPassword());

        Authentication authentication = AUTH_MANAGER.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword())
        );


        if(authentication.isAuthenticated()){
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUsername());

            String jwt = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(24*60*60)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new ResponseMessage("Succecfull!"));
        }

        return new ResponseEntity<>("Failure!", HttpStatus.UNAUTHORIZED);
    }

}
