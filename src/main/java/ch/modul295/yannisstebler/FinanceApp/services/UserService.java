package ch.modul295.yannisstebler.financeapp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ch.modul295.yannisstebler.financeapp.model.User;
import ch.modul295.yannisstebler.financeapp.repository.UserRepository;
import jakarta.validation.Valid;

/**
 * Service class for managing users, including interaction with Keycloak for authentication.
 */
@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository; 

    @Autowired
    private KeycloakService keycloakService;  

    /**
     * Constructs the UserService with the required dependencies.
     * 
     * @param userRepository the user repository to interact with the database
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all users from the database.
     * 
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a specific user by their ID.
     * 
     * @param id the ID of the user to retrieve
     * @return an Optional containing the user if found, otherwise empty
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id); 
    }

    /**
     * Creates a new user, including creation in Keycloak and saving to the database.
     * 
     * @param user the user to be created
     * @return a ResponseEntity containing the created user or an error message
     */
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
            return ResponseEntity.status(500).body(null);
        }

        // Set the Keycloak user ID and mark the user as active
        user.setKeycloakID(keycloakUserId);
        user.setActive(true);

        // Save the user in the database
        try {
            User savedUser = userRepository.save(user);
            return ResponseEntity.status(201).body(savedUser);
        } catch (Exception e) {
            // If saving user in the database fails
            return ResponseEntity.status(500).body(null); 
        }
    }

    /**
     * Updates an existing user in the database.
     * 
     * @param id the ID of the user to update
     * @param user the user object containing updated details
     * @return the updated user
     * @throws IllegalArgumentException if the user is not found
     */
    public User updateUser(Long id, User user) {
        if (userRepository.existsById(id)) {  
            user.setId(id);  
            User updatedUser = userRepository.save(user);
            return updatedUser;
        } else {
            throw new IllegalArgumentException("User not found");  
        }
    }

    /**
     * Deletes a user, deactivating them in the database and removing them from Keycloak.
     * 
     * @param id the ID of the user to delete
     * @return an Optional containing the deleted user, or empty if not found
     * @throws IllegalArgumentException if the user is not found
     */
    public Optional<User> deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id); 

        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");  
        }

        // Delete the user from Keycloak
        keycloakService.deleteKeycloakUser(user.get().getUsername());

        // Deactivate the user in the database
        User deletedUser = user.get();
        deletedUser.setActive(false);
        userRepository.save(deletedUser); 

        return user;  // Return the deleted user
    }
}
