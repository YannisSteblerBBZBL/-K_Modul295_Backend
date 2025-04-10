package ch.modul295.yannisstebler.FinanceApp.services;

import java.util.Collections;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KeycloakService {
    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public String createKeycloakUser(String username, String password, String role) {
        try {
            // Get realm
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            // Create user representation
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEnabled(true);
            user.setEmailVerified(true);

            // Create user
            usersResource.create(user);
            String userId = getUserIdByUsername(username);

            // Set password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            usersResource.get(userId).resetPassword(credential);

            // Assign role
            RoleRepresentation roleRepresentation = realmResource.roles().get(role).toRepresentation();
            usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));

            // Retrieve and return the created user
            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserIdByUsername(String username) {
        try {
            RealmResource realmResource = keycloak.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(username, true);
            if (users == null || users.isEmpty()) {
                throw new RuntimeException("User not found");
            }
            String userId = users.getFirst().getId();
            return userId;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user ID", e);
        }
    }

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
