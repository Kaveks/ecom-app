package org.example.ecomapp.service.user;

import org.example.ecomapp.model.User;
import org.example.ecomapp.repository.UserRepository;
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

    @Override
    public User fetchUser(Long id) {
        // Old List-based implementation
        // return userList.stream()
        //         .filter(user -> user.getId().equals(id))
        //         .findFirst()
        //         .orElse(null);

        // Repository-based implementation
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> fetchUsers() {
        // Old List-based implementation
        // return userList;

        // Repository-based implementation
        return userRepository.findAll();
    }

    @Override
    public String addUser(User user) {
        // Old List-based implementation
        // user.setId(nextId++);
        // userList.add(user);

        // Repository-based implementation
        userRepository.save(user);

        return "User added successfully";
    }

    @Override
    public User updateUser(Long id, User updateUser) {
        User existingUser = fetchUser(id);

        if (existingUser == null) {
            return null;
        }

        // Old implementation using if conditions
        /*
        if (updateUser.getFirstName() != null) {
            existingUser.setFirstName(updateUser.getFirstName());
        }

        if (updateUser.getLastName() != null) {
            existingUser.setLastName(updateUser.getLastName());
        }

        if (updateUser.getEmail() != null) {
            existingUser.setEmail(updateUser.getEmail());
        }

        if (updateUser.getPhone() != null) {
            existingUser.setPhone(updateUser.getPhone());
        }

        if (updateUser.getRole() != null) {
            existingUser.setRole(updateUser.getRole());
        }
        */

        // Current implementation using a Map
        Map<String, Consumer<User>> fieldUpdaters = Map.of(
                "firstName", user -> user.setFirstName(updateUser.getFirstName()),
                "lastName", user -> user.setLastName(updateUser.getLastName()),
                "email", user -> user.setEmail(updateUser.getEmail()),
                "phone", user -> user.setPhone(updateUser.getPhone()),
                "role", user -> user.setRole(updateUser.getRole())
        );

        Map<String, Object> updatedFields = Map.of(
                "firstName", updateUser.getFirstName() != null
                        ? updateUser.getFirstName() : "",
                "lastName", updateUser.getLastName() != null
                        ? updateUser.getLastName() : "",
                "email", updateUser.getEmail() != null
                        ? updateUser.getEmail() : "",
                "phone", updateUser.getPhone() != null
                        ? updateUser.getPhone() : "",
                "role", updateUser.getRole() != null
                        ? updateUser.getRole() : ""
        );

        updatedFields.forEach((field, value) -> {
            if (value != null && !value.equals("")) {
                fieldUpdaters.get(field).accept(existingUser);
            }
        });

        // Old List-based implementation
        // return existingUser;

        // Repository-based implementation
        return userRepository.save(existingUser);
    }

    @Override
    public Boolean deleteUser(Long id) {
        User user = fetchUser(id);

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