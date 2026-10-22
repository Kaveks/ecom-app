package org.example.ecomapp;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



@Service
public class UserService implements UserServiceInterface {
    private final List<User> userList=new ArrayList<>();
    private Long nextId=1L;

    @Override
    public User fetchUser(Long id) {
        return userList.stream().filter(user->user.getId().equals(id)).findFirst().orElse(null);
    }
    @Override
    public List<User> fetchUsers(){
        return userList;
    }
    @Override
    public String addUser(User user){
        user.setId(nextId++);
        userList.add(user);
        return "User added successfully";
    }
    @Override
    public User updateUser(Long id, User updateUser) {
        User existingUser = fetchUser(id);

        if (existingUser == null) {
            return null;
        }

        if (updateUser.getFirstName() != null) {
            existingUser.setFirstName(updateUser.getFirstName());
        }

        if (updateUser.getLastName() != null) {
            existingUser.setLastName(updateUser.getLastName());
        }

        return existingUser;
    }


    @Override
    public Boolean deleteUser(Long id) {
        User user=fetchUser(id);
        if(user ==null){
            return false;
        }
        userList.remove(user);
        return true;
    }

}

