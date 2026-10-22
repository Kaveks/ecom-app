package org.example.ecomapp.dto.user;

import lombok.Data;
import org.example.ecomapp.dto.address.AddressRequest;

@Data
public class UserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private AddressRequest address;
}
