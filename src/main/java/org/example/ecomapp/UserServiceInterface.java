package org.example.ecomapp;

import java.util.List;

public interface UserServiceInterface {
    List<User> fetchUsers();
    String addUser(User user);
    User fetchUser(Long id);
    User updateUser(Long id ,User updateUser);
    Boolean deleteUser(Long id);
}
