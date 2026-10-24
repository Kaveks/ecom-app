package org.example.ecomapp.service.user;

import org.example.ecomapp.dto.address.AddressRequest;
import org.example.ecomapp.dto.address.AddressResponse;
import org.example.ecomapp.dto.user.UserRequest;
import org.example.ecomapp.dto.user.UserResponse;
import org.example.ecomapp.model.Address;
import org.example.ecomapp.model.User;
import org.example.ecomapp.repository.UserRepository;
import org.example.ecomapp.util.FieldUtils;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class UserService implements UserServiceInterface {

    private final UserRepository userRepository;

    // Old in-memory implementation using List
    // private final List<User> userList = new ArrayList<>();
    // private Long nextId = 1L;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // Convert User entity to UserResponse DTO
    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(String.valueOf(user.getId()));
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        if (user.getAddress() != null) {
            Address address = user.getAddress();
            AddressResponse addressResponse = new AddressResponse();
            addressResponse.setCity(address.getCity());
            addressResponse.setStreet(address.getStreet());
            addressResponse.setCounty(address.getCounty());
            addressResponse.setPostalCode(address.getPostalCode());
            addressResponse.setPostalAddress(address.getPostalAddress());

            response.setAddress(addressResponse);
        }
        return response;
    }
    // Convert UserRequest DTO to User entity
    private User toUserEntity(UserRequest request) {
        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        // Role defaults to CUSTOMER in the User entity.
        // Do not accept an elevated role from a public request.

        if (request.getAddress() != null) {
            user.setAddress(toAddressEntity(request.getAddress()));
        }

        return user;
    }

    // Convert AddressRequest DTO to Address entity
    private Address toAddressEntity(AddressRequest request) {
        Address address = new Address();

        address.setCity(request.getCity());
        address.setStreet(request.getStreet());
        address.setCounty(request.getCounty());
        address.setPostalCode(request.getPostalCode());
        address.setPostalAddress(request.getPostalAddress());

        return address;
    }

    // check for text
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Override
    public UserResponse fetchUser(Long id) {
        // Old List-based implementation
        // return userList.stream()
        //         .filter(user -> user.getId().equals(id))
        //         .findFirst()
        //         .orElse(null);

        // Repository-based implementation
        return userRepository.findById(id)
                .map(this::toUserResponse)
                .orElse(null);
    }

    @Override
    public List<UserResponse> fetchUsers() {
        // Old List-based implementation
        // return userList;

        // Repository-based implementation
        return userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse addUser(UserRequest request) {
        // Old List-based implementation
        // user.setId(nextId++);
        // userList.add(user);

        // Repository-based implementation
        User user= toUserEntity(request);

        User savedUser= userRepository.save(user);

        //return "User added successfully";
        return toUserResponse(savedUser) ;
    }


    @Override
    public UserResponse updateUser(Long id, UserRequest request) {

        // Find the existing user
        User existingUser = userRepository.findById(id).orElse(null);

        // Return null if the user does not exist
        if (existingUser == null) {
            return null;
        }

        // Update User fields
        if (FieldUtils.hasText(request,UserRequest::getFirstName)) {
            existingUser.setFirstName(request.getFirstName());
        }

        if (FieldUtils.hasText(request,UserRequest::getLastName)) {
            existingUser.setLastName(request.getLastName());
        }

        if (FieldUtils.hasText(request,UserRequest::getEmail)) {
            existingUser.setEmail(request.getEmail());
        }

        if (FieldUtils.hasText(request,UserRequest::getPhone)) {
            existingUser.setPhone(request.getPhone());
        }


        // Update Address fields
        AddressRequest updateAddress = request.getAddress();

        if (updateAddress != null) {

            // Get the user's existing address
            Address existingAddress = existingUser.getAddress();

            // Create an address if the user does not have one
            if (existingAddress == null) {
                existingAddress = new Address();
                existingUser.setAddress(existingAddress);
            }

            // Update the address fields supplied in the request

            if (FieldUtils.hasText(updateAddress, AddressRequest::getCity)) {
                existingAddress.setCity(updateAddress.getCity());
            }

            if (FieldUtils.hasText(updateAddress, AddressRequest::getStreet)) {
                existingAddress.setStreet(updateAddress.getStreet());
            }

            if (FieldUtils.hasText(updateAddress, AddressRequest::getCounty)) {
                existingAddress.setCounty(updateAddress.getCounty());
            }

            if (FieldUtils.hasText(updateAddress, AddressRequest::getPostalCode)) {
                existingAddress.setPostalCode(updateAddress.getPostalCode());
            }

            if (FieldUtils.hasText(updateAddress, AddressRequest::getPostalAddress)) {
                existingAddress.setPostalAddress(updateAddress.getPostalAddress());
            }

        }

        // Save the updated user and associated address
        User savedUser = userRepository.save(existingUser);

        // Convert the saved user entity into a response DTO
        return toUserResponse(savedUser);
    }


    @Override
    public Boolean deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return false;
        }

        // Old List-based implementation
        // userList.remove(user);

        // Repository-based implementation
        userRepository.delete(user);

        return true;
    }
}