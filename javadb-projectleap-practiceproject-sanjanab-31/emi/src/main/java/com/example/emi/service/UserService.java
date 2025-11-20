package com.example.emi.service;
import com.example.emi.model.User;
import com.example.emi.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
    public User createUser(User user) {
        user.setJoinDate(LocalDateTime.now());
        User saved = userRepository.save(user);
    emailService.sendWelcomeEmail(saved);
        return saved;
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}
