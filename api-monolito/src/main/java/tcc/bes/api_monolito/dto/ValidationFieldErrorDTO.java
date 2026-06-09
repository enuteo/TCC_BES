package tcc.bes.api_monolito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationFieldErrorDTO {
    private String field;
    private String message;
}
