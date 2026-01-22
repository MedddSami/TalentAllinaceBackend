package com.jobrecuruitmentplatform.service;

import com.jobrecuruitmentplatform.dto.*;
import com.jobrecuruitmentplatform.exception.BadRequestException;
import com.jobrecuruitmentplatform.model.Company;
import com.jobrecuruitmentplatform.model.User;
import com.jobrecuruitmentplatform.model.UserRole;
import com.jobrecuruitmentplatform.repository.CompanyRepository;
import com.jobrecuruitmentplatform.repository.UserRepository;
import com.jobrecuruitmentplatform.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final CompanyRepository companyRepository;

    @Transactional
    public AuthResponse registerEmployer(RegisterEmployerRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Create employer user
        User employer = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.EMPLOYER)
                .build();

        employer = userRepository.save(employer);

        // Create company
        Company company = Company.builder()
                .name(request.getCompanyName())
                .description(request.getCompanyDescription())
                .website(request.getCompanyWebsite())
                .location(request.getCompanyLocation())
                .employer(employer)
                .build();

        companyRepository.save(company);

        // Generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(employer.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDTO(employer))
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .build();

        userRepository.save(user);


        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDTO(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadRequestException("User not found"));

            return AuthResponse.builder()
                    .token(token)
                    .user(mapToUserDTO(user))
                    .build();
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password");
        }
    }

    public UserDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        return mapToUserDTO(user);
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}