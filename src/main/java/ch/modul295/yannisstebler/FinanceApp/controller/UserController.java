package ch.modul295.yannisstebler.FinanceApp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.modul295.yannisstebler.FinanceApp.model.User;
import ch.modul295.yannisstebler.FinanceApp.security.Roles;
import ch.modul295.yannisstebler.FinanceApp.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @RolesAllowed(Roles.Admin)
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @RolesAllowed(Roles.Admin)
    public Optional<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @RolesAllowed(Roles.User)
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    @RolesAllowed(Roles.User)
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Optional<User> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}
