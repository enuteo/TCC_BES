package tcc.bes.api_monolito.service;

import org.springframework.stereotype.Service;
import tcc.bes.api_monolito.dto.LoginRequestDTO;
import tcc.bes.api_monolito.dto.LoginResponseDTO;
import tcc.bes.api_monolito.dto.UserDTO;
import tcc.bes.api_monolito.exception.InvalidCredentialsException;
import tcc.bes.api_monolito.exception.UserNotFoundException;
import tcc.bes.api_monolito.model.User;
import tcc.bes.api_monolito.repository.UserRepository;

@Service
public class LoginService {

    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
            throw new InvalidCredentialsException("Username cannot be empty");
        }

        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            throw new InvalidCredentialsException("Password cannot be empty");
        }

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UserDTO userDTO = mapToUserDTO(user);
        return new LoginResponseDTO(true, "Login successful", userDTO);
    }

    private UserDTO mapToUserDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
