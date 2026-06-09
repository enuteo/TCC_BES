package tcc.bes.api_monolito.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "Username cannot be empty")
    @Size(max = 100, message = "Username must have at most 100 characters")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(max = 255, message = "Password must have at most 255 characters")
    private String password;
}
