
package org.example.ecomapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.ecomapp.enums.UserRole;

@Data
@NoArgsConstructor
// @AllArgsConstructor
@Entity
@Table(name = "user_table")
public class User {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role=UserRole.CUSTOMER;


    // Use Lombok to generate constructors instead.

    // Old constructor implementation, kept for reference:
    //
    // public User() {
    // }
    //
    // public User(Long id, String firstName, String lastName) {
    //     this.id = id;
    //     this.firstName = firstName;
    //     this.lastName = lastName;
    // }
}
