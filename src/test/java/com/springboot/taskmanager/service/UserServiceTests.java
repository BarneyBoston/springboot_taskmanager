package com.springboot.taskmanager.service;

import com.springboot.taskmanager.dto.UserRegistrationDto;
import com.springboot.taskmanager.entity.User;
import com.springboot.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void userServiceLoadUserByUsername() {
        User user = User.builder()
                .username("loadUser")
                .password("pw")
                .email("load@example.com")
                .role("ROLE_USER")
                .build();

        // stub repository to accept any string username
        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(user);

        UserDetails userDetails = userService.loadUserByUsername("loadUser");

        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(user.getRole())));
    }

    @Test
    void userServiceRegisterNewUser() {
        UserRegistrationDto registrationDto = UserRegistrationDto.builder()
                .username("userDtoToRegister")
                .password("pw")
                .confirmPassword("pw")
                .email("registeredDto@example.com")
                .build();

        // repository should report username not existing
        when(userRepository.findByUsername(registrationDto.getUsername())).thenReturn(null);
        // password encoder returns encoded value
        when(passwordEncoder.encode(registrationDto.getPassword())).thenReturn("encodedPw");
        // return the passed entity from save
        when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredNewUser = userService.registerNewUser(registrationDto);

        // capture what was saved
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        // verify saved fields and returned object
        assertEquals(registrationDto.getUsername(), savedUser.getUsername());
        assertEquals("encodedPw", savedUser.getPassword());
        assertEquals("ROLE_USER", savedUser.getRole());

        assertEquals(savedUser.getUsername(), registeredNewUser.getUsername());
        assertEquals(savedUser.getPassword(), registeredNewUser.getPassword());
    }
}
