package ru.kata.spring.boot_security.demo.service;



import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserDetailsService, UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users;
    }

    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Override
    public boolean isAdmin(User user) {
        return true;
    }

    @Override
    public boolean hasRole(User user, String role) {
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Пользователь не найден ");
        }
        return user;
    }

    @Override
    public User updateUser(User updatedUser, String newPassword) {

        try {
            User existingUser = userRepository.findById(updatedUser.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
                User userByEmail = userRepository.findByUsername(updatedUser.getUsername());
                if (userByEmail != null && !userByEmail.getId().equals(existingUser.getId())) {
                    throw new IllegalArgumentException("Email already taken");
                }
            }
            existingUser.setUsername(updatedUser.getUsername());

            if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
                existingUser.setRoles(updatedUser.getRoles());
            }

            if (newPassword != null && !newPassword.isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(newPassword));
            }
            return userRepository.save(existingUser);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    };

}
