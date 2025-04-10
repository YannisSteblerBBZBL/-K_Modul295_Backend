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
            ClientResource clientResource = realmResource.clients().get("466dc5d7-368d-4e29-9668-a4abf7460d98");
           
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
            /* RoleRepresentation roleRepresentation = realmResource.roles().get(role).toRepresentation();
            usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation)); */
            RoleRepresentation roleRepresentation = clientResource.roles().get(role).toRepresentation();
            usersResource.get(userId).roles().clientLevel(clientResource.toRepresentation().getId()).add(Collections.singletonList(roleRepresentation));

            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserIdByUsername(String username) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();
        
        List<UserRepresentation> users = usersResource.search(username, true);
        
        if (users == null || users.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        String userId = users.get(0).getId();
        
        return userId;
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
