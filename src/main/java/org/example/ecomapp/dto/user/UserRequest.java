package org.example.ecomapp.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.ecomapp.dto.address.AddressRequest;

@Data
public class UserRequest {
    @NotNull(message = "firstName is required.")
    private String firstName;
    @NotNull(message = "lastName is required.")
    private String lastName;
    @NotNull(message = "email is required.")
    private String email;
    @NotNull(message = "phone is required.")
    private String phone;
    private AddressRequest address;
}
