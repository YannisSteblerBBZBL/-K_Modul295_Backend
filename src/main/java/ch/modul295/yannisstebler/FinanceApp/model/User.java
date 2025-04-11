package ch.modul295.yannisstebler.financeapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String firstName;

    @Column(nullable = false)
    String lastName;

    @NotBlank(message = "Username cannot be empty")
    @Column(nullable = false, unique = true)
    String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    String password;

    @NotBlank(message = "Keycloak ID cannot be empty")
    @Column(nullable = false)
    String keycloakID;

    @Email(message = "Email should be valid")
    @Column(unique = true)
    String email;

    @Column(nullable = false)
    Boolean active;

    @PrePersist
    public void setDefaults() {
        if (this.active == null) {
            this.active = true;
        }
    }
}