package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.UserRegistrationDto;
import com.ecommerce.userservice.dto.UserResponseDto;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword()); // In production, hash this password
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        
        User savedUser = userRepository.save(user);
        return new UserResponseDto(savedUser);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDto::new);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponseDto::new);
    }
    
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllActiveUsers() {
        return userRepository.findAllActiveUsers()
                .stream()
                .map(UserResponseDto::new)
                .collect(Collectors.toList());
    }
    
    public UserResponseDto updateUser(Long id, UserRegistrationDto updateDto) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOptional.get();
        
        // Update fields
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        if (updateDto.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDto.getPhoneNumber());
        }
        
        User updatedUser = userRepository.save(user);
        return new UserResponseDto(updatedUser);
    }
    
    public void deactivateUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOptional.get();
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public long getTotalActiveUsers() {
        return userRepository.countActiveUsers();
    }
}
