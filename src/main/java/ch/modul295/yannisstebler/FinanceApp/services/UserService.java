package ch.modul295.yannisstebler.financeapp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ch.modul295.yannisstebler.financeapp.model.User;
import ch.modul295.yannisstebler.financeapp.repository.UserRepository;
import jakarta.validation.Valid;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private KeycloakService keycloakService;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user;
    }

    public ResponseEntity<User> createUser(@Valid User user) {
        // Create the user in Keycloak
        String keycloakUserId = keycloakService.createKeycloakUser(
            user.getUsername(),
            user.getPassword(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            "ROLE_user"
        );

        // If Keycloak user creation failed
        if (keycloakUserId == null) {
            return ResponseEntity.status(500).body(null); // 500 Internal Server Error: Keycloak user creation failed
        }

        // Set the Keycloak user ID and mark the user as active
        user.setKeycloakID(keycloakUserId);
        user.setActive(true);

        // Save the user in the database
        try {
            User savedUser = userRepository.save(user);
            return ResponseEntity.status(201).body(savedUser); // 201 Created: User successfully created
        } catch (Exception e) {
            // If saving user in the database fails
            return ResponseEntity.status(500).body(null); // 500 Internal Server Error: Database error
        }
    }

    public User updateUser(Long id, User user) {
        if (userRepository.existsById(id)) {
            user.setId(id);
            User updatedUser = userRepository.save(user);
            return updatedUser;
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    public Optional<User> deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        keycloakService.deleteKeycloakUser(user.get().getUsername());

        User deletedUser = user.get();
        deletedUser.setActive(false);

        userRepository.save(deletedUser);

        return user;
    }
}