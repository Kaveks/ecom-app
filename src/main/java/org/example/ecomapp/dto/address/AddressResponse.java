package org.example.ecomapp.dto.address;

import lombok.Data;

@Data
public class AddressResponse {

    private String city;
    private String street;
    private String county;
    private String postalCode;
    private String postalAddress;
}
