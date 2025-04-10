package ch.modul295.yannisstebler.financeapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.modul295.yannisstebler.financeapp.model.User;
import ch.modul295.yannisstebler.financeapp.security.Roles;
import ch.modul295.yannisstebler.financeapp.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    // Get all users - Only for admins
    @GetMapping
    @RolesAllowed(Roles.ADMIN)
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get a specific user by ID - Admin can see any user, normal user can only see themselves
    @GetMapping("/{id}")
    @RolesAllowed({Roles.ADMIN, Roles.USER})
    public Optional<User> getUserById(@PathVariable Long id, Authentication auth) {
        String username = ((Jwt) auth.getPrincipal()).getClaim("preferred_username");

        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(Roles.ADMIN))) {
            return userService.getUserById(id);
        } else {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent() && user.get().getUsername().equals(username)) {
                return user;
            }
        }

        return Optional.empty();
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Update user - No option to update user data for normal users
    @PutMapping("/{id}")
    @RolesAllowed(Roles.ADMIN)
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    // Delete user - Normal users can only delete themselves, admins can delete any user
    @DeleteMapping("/{id}")
    @RolesAllowed({Roles.ADMIN, Roles.USER})
    public Optional<User> deleteUser(@PathVariable Long id, Authentication auth) {
        String username = ((Jwt) auth.getPrincipal()).getClaim("preferred_username");

        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(Roles.ADMIN))) {
            return userService.deleteUser(id);
        } else {
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent() && user.get().getUsername().equals(username)) {
                return userService.deleteUser(id);
            }
        }

        return Optional.empty();
    }
}
