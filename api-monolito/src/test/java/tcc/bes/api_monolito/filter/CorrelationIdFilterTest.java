package tcc.bes.api_monolito.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tcc.bes.api_monolito.controller.StatusController;
import tcc.bes.api_monolito.service.StatusService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tcc.bes.api_monolito.filter.CorrelationIdFilter.CORRELATION_ID_HEADER;

@WebMvcTest(StatusController.class)
@Import({StatusService.class, CorrelationIdFilter.class})
class CorrelationIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateCorrelationIdWhenHeaderIsMissing() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(header().exists(CORRELATION_ID_HEADER))
                .andReturn();

        String correlationId = result.getResponse().getHeader(CORRELATION_ID_HEADER);

        assertNotNull(correlationId);
        assertDoesNotThrow(() -> UUID.fromString(correlationId));
    }

    @Test
    void shouldReuseIncomingCorrelationIdWhenHeaderIsValid() throws Exception {
        String correlationId = "teste-123";

        mockMvc.perform(get("/api/status").header(CORRELATION_ID_HEADER, correlationId))
                .andExpect(status().isOk())
                .andExpect(header().string(CORRELATION_ID_HEADER, correlationId));
    }
}
