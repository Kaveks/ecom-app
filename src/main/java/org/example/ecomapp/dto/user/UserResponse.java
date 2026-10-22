package org.example.ecomapp.dto.user;

import lombok.Data;
import org.example.ecomapp.dto.address.AddressResponse;
import org.example.ecomapp.enums.UserRole;

@Data
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserRole role;
    private AddressResponse address;
}
