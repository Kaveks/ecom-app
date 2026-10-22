package org.example.ecomapp.service.user;

import org.example.ecomapp.dto.user.UserRequest;
import org.example.ecomapp.dto.user.UserResponse;

import java.util.List;

public interface UserServiceInterface {

    UserResponse fetchUser(Long id);

    List<UserResponse> fetchUsers();

    UserResponse addUser(UserRequest request);

    UserResponse updateUser(Long id, UserRequest updateRequest);

    Boolean deleteUser(Long id);
}