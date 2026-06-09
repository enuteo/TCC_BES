package tcc.bes.api_monolito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tcc.bes.api_monolito.dto.ApiResponseDTO;
import tcc.bes.api_monolito.dto.StatusDTO;
import tcc.bes.api_monolito.service.StatusService;

@RestController
@RequestMapping("/api/status")
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<StatusDTO>> getStatus() {
        StatusDTO status = statusService.getStatus();
        ApiResponseDTO<StatusDTO> response = new ApiResponseDTO<>(true, "API operational", status);
        return ResponseEntity.ok(response);
    }
}
