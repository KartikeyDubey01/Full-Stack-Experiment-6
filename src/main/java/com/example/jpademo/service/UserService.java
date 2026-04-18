package com.example.jpademo.service;

import com.example.jpademo.entity.Role;
import com.example.jpademo.entity.User;
import com.example.jpademo.repository.RoleRepository;
import com.example.jpademo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public User createUserWithRoles(String username, String email, List<String> roleNames) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(null, roleName)));
            user.addRole(role);
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(String roleName) {
        return userRepository.findUsersByRoleName(roleName);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        return userRepository.searchByUsername(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}