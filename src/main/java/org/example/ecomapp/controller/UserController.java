
package org.example.ecomapp.controller;

import org.example.ecomapp.dto.user.UserRequest;
import org.example.ecomapp.dto.user.UserResponse;
import org.example.ecomapp.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> fetchUser(
            @PathVariable Long id) {

        UserResponse user = userService.fetchUser(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> fetchUsers() {
        return ResponseEntity.ok(userService.fetchUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponse> addUser(
            @RequestBody UserRequest request) {

        UserResponse createdUser = userService.addUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest request) {

        UserResponse updatedUser =
                userService.updateUser(id, request);

        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id) {

        boolean deleted = userService.deleteUser(id);

        if (deleted) {
            return ResponseEntity.ok(
                    "User id " + id + " deleted"
            );
        }

        return ResponseEntity.notFound().build();
    }
}
