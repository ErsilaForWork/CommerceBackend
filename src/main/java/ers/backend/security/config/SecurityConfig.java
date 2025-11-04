package ers.backend.security.config;

import ers.backend.jwt.AuthEntryPointJwt;
import ers.backend.jwt.JwtFilter;
import ers.backend.security.model.AppRole;
import ers.backend.security.model.AppRoleEnum;
import ers.backend.security.model.AppUser;
import ers.backend.security.repo.AppUserRepo;
import ers.backend.security.repo.RoleRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserRepo userRepo;
    private final RoleRepo roleRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final JwtFilter jwtFilter;
    private final AuthEntryPointJwt unauthorizedHandler;

    public SecurityConfig(AppUserRepo userRepo, RoleRepo roleRepo, JwtFilter jwtFilter, AuthEntryPointJwt unauthorizedHandler) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.jwtFilter = jwtFilter;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public SecurityFilterChain secConfig(HttpSecurity http) throws Exception {

        http.csrf(cuztomizer -> cuztomizer.disable());
        http.httpBasic(Customizer.withDefaults());
        http.authorizeHttpRequests(requests
                ->
                requests
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/security/login").permitAll()
                        .requestMatchers("/api/security/register").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
        );
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler));
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CommandLineRunner initData() {

        return line -> {
            if(!roleRepo.existsAppRoleByEnumRole(AppRoleEnum.ROLE_USER)){
                roleRepo.save(new AppRole(AppRoleEnum.ROLE_USER));
            }

            if(!roleRepo.existsAppRoleByEnumRole(AppRoleEnum.ROLE_ADMIN)){
                roleRepo.save(new AppRole(AppRoleEnum.ROLE_ADMIN));
            }

            if(!userRepo.existsByUsername("user")) {
                userRepo.save(new AppUser("user", encoder.encode("user1234"), roleRepo.getByEnumRole(AppRoleEnum.ROLE_USER),Integer.MAX_VALUE));
            }

            if(!userRepo.existsByUsername("admin")) {
                userRepo.save(new AppUser("admin", encoder.encode("admin123"), roleRepo.getByEnumRole(AppRoleEnum.ROLE_ADMIN),Integer.MAX_VALUE));
            }
        };

    }

}
