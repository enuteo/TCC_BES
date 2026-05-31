package tcc.bes.api_monolito.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tcc.bes.api_monolito.dto.LoginResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<LoginResponseDTO> handleInvalidCredentials(InvalidCredentialsException ex) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setUser(null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<LoginResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setSuccess(false);
        response.setMessage(ex.getMessage());
        response.setUser(null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<LoginResponseDTO> handleGenericException(Exception ex) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setSuccess(false);
        response.setMessage("An error occurred: " + ex.getMessage());
        response.setUser(null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
