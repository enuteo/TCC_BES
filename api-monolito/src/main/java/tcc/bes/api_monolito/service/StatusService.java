package tcc.bes.api_monolito.service;

import org.springframework.stereotype.Service;
import tcc.bes.api_monolito.dto.StatusDTO;

@Service
public class StatusService {

    public StatusDTO getStatus() {
        return new StatusDTO("UP", "api-monolito");
    }
}
