package org.example.ecomapp.service.user;

import org.example.ecomapp.model.User;

import java.util.List;

public interface UserServiceInterface {
    List<User> fetchUsers();
    String addUser(User user);
    User fetchUser(Long id);
    User updateUser(Long id ,User updateUser);
    Boolean deleteUser(Long id);
}
