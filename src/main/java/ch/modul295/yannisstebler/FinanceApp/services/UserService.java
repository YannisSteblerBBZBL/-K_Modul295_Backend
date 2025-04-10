package ch.modul295.yannisstebler.financeapp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ch.modul295.yannisstebler.financeapp.model.User;
import ch.modul295.yannisstebler.financeapp.repository.UserRepository;

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

    public ResponseEntity<User> createUser(User user) {

        String keycloakUserId = keycloakService.createKeycloakUser(user.getUsername(), user.getPassword(), "ROLE_user");
        
        if (keycloakUserId == null) {
            return ResponseEntity.status(500).body(null);
        }
  
        user.setKeycloakID(keycloakUserId);
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
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