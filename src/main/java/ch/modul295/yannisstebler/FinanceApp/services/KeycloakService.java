package ch.modul295.yannisstebler.financeapp.services;

import java.util.Collections;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;

/**
 * Service class for interacting with Keycloak to manage users and roles.
 */
@Service
public class KeycloakService {
    
    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm; 

    @Value("${keycloak.clientLongId}")
    private String clientLongId; 

    /**
     * Creates a new user in Keycloak with the specified details.
     * 
     * @param username the username of the new user
     * @param password the password of the new user
     * @param email the email of the new user
     * @param firstName the first name of the new user
     * @param lastName the last name of the new user
     * @param role the role to assign to the user
     * @return the user ID if successful, or null if there was an error
     */
    public String createKeycloakUser(String username, String password, String email, String firstName, String lastName, String role) {
        try {
            // Get realm and resources for managing users and clients
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();
            ClientResource clientResource = realmResource.clients().get(clientLongId);
            
            // Validate and create user representation
            if (username == null || username.isEmpty()) {
                deleteKeycloakUser(username); 
                throw new IllegalArgumentException("Username cannot be null or empty");
            }
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Create user in Keycloak
            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                System.err.println("User creation failed: " + response.getStatusInfo());
                response.close();
                return null;
            }

            response.close();


            String userId = getUserIdByUsername(username);

            // Set password
            if (password == null || password.isEmpty()) {
                deleteKeycloakUser(username);
                throw new IllegalArgumentException("Password cannot be null or empty");
            }
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            usersResource.get(userId).resetPassword(credential);

            // Assign role to user
            RoleRepresentation roleRepresentation = clientResource.roles().get(role).toRepresentation();
            usersResource.get(userId).roles().clientLevel(clientResource.toRepresentation().getId()).add(Collections.singletonList(roleRepresentation));

            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves the user ID by username.
     * 
     * @param username the username of the user
     * @return the user ID
     * @throws RuntimeException if the user is not found
     */
    public String getUserIdByUsername(String username) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        
        List<UserRepresentation> users = usersResource.search(username, true);
        
        if (users == null || users.isEmpty()) {
            throw new RuntimeException("User not found"); 
        }

        return users.get(0).getId();
    }

    /**
     * Deletes a user from Keycloak by their username.
     * 
     * @param username the username of the user to be deleted
     * @return true if the user was deleted, false if the user was not found
     */
    public boolean deleteKeycloakUser(String username) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> users = usersResource.search(username, true);
            if (users.isEmpty()) {
                return false; 
            }

            String userId = users.get(0).getId();
            usersResource.delete(userId);
            return true; 
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
