package org.example.ecomapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@Entity(name="user_table")
public class User {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    // use lombok to add constructors instead

//    public User() {
//    }
//
//    public User(Long id, String firstName, String lastName) {
//        this.id = id;
//        this.firstName = firstName;
//        this.lastName = lastName;
//    }
}
