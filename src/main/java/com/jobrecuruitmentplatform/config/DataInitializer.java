package com.jobrecuruitmentplatform.config;

import com.jobrecuruitmentplatform.model.User;
import com.jobrecuruitmentplatform.model.UserRole;
import com.jobrecuruitmentplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${ADMIN_FIRST_NAME:Admin}")
    private String adminFirstName;

    @Value("${ADMIN_LAST_NAME:User}")
    private String adminLastName;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (!userRepository.existsByEmail(adminEmail)) {

            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFirstName(adminFirstName);
            admin.setLastName(adminLastName);
            admin.setRole(UserRole.ADMIN);

            userRepository.save(admin);

            System.out.println("Created admin user: " + adminEmail);
        } else {
            System.out.println("Admin already exists: " + adminEmail);
        }
    }
}

