package ch.modul295.yannisstebler.financeapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

/**
 * Controller class for managing user-related actions.
 * Provides endpoints for creating, retrieving, updating, and deleting users.
 */
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Endpoint to retrieve all users. Admins can view all users.
     * Normal users can only view their own information.
     * 
     * @param auth The authentication object containing the user's details.
     * @return A list of users that the requesting user has access to.
     */
    @GetMapping
    @RolesAllowed(Roles.USER)
    public ResponseEntity<List<User>> getAllUsers(Authentication auth) {
        String username = ((Jwt) auth.getPrincipal()).getClaim("preferred_username");

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can access all users
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        }

        // Normal users can only access their own data
        List<User> filteredUsers = userService.getAllUsers().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .toList();

        return ResponseEntity.ok(filteredUsers);
    }

    /**
     * Endpoint to retrieve a specific user by ID. Admins can view any user.
     * Normal users can only view their own data.
     *
     * @param id The ID of the user to be retrieved.
     * @param auth The authentication object containing the user's details.
     * @return The requested user or HTTP status NOT_FOUND if the user does not exist.
     */
    @GetMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<User> getUserById(@PathVariable Long id, Authentication auth) {
        String username = ((Jwt) auth.getPrincipal()).getClaim("preferred_username");

        // Admins can view any user
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            Optional<User> user = userService.getUserById(id);
            return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } else {
            // Normal users can only view themselves
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent() && user.get().getUsername().equals(username)) {
                return ResponseEntity.ok(user.get());
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden if the user doesn't own the account
    }

    /**
     * Endpoint to create a new user.
     * This endpoint does not require any authentication or authorization, so it could be publicly accessible.
     * 
     * @param user The user data to be created.
     * @return The created user, or an appropriate error status.
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        ResponseEntity<User> response = userService.createUser(user);
        if (response.getStatusCode() == HttpStatus.CREATED) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response.getBody());  // 201 Created
        } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);  // 409 Conflict - User already exists
        } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // 400 Bad Request - Invalid user data
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);  // 500 Internal Server Error - Unexpected issue
    }

    /**
     * Endpoint to update a user by ID. Only admins are allowed to update user data.
     *
     * @param id The ID of the user to be updated.
     * @param user The updated user data.
     * @return The updated user or HTTP status NOT_FOUND if the user does not exist.
     */
    @PutMapping("/{id}")
    @RolesAllowed(Roles.ADMIN)
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);  // 200 OK
    }

    /**
     * Endpoint to delete a user by ID. Admins can delete any user.
     * Normal users can only delete their own account.
     *
     * @param id The ID of the user to be deleted.
     * @param auth The authentication object containing the user's details.
     * @return HTTP status indicating the result of the deletion.
     */
    @DeleteMapping("/{id}")
    @RolesAllowed({Roles.ADMIN, Roles.USER})
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication auth) {
        String username = ((Jwt) auth.getPrincipal()).getClaim("preferred_username");

        // Admins can delete any user
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();  // 204 No Content
        } else {
            // Normal users can only delete themselves
            Optional<User> user = userService.getUserById(id);
            if (user.isPresent() && user.get().getUsername().equals(username)) {
                userService.deleteUser(id);
                return ResponseEntity.noContent().build();  // 204 No Content
            }
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // Forbidden if user tries to delete another user
    }
}
