package org.example.ecomapp;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

        // Update only the fields supplied by the client.
        if (updateUser.getFirstName() != null) {
            existingUser.setFirstName(updateUser.getFirstName());
        }

        if (updateUser.getLastName() != null) {
            existingUser.setLastName(updateUser.getLastName());
        }

        // Old List-based implementation:
        // The modified object was already in the userList,
        // so updating its fields updated the in-memory object.
        // return existingUser;

        // Repository-based implementation:
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
