package com.enigcode.frozen_backend.batches.controller;

import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.batches.service.BatchTraceabilityService;
import com.enigcode.frozen_backend.common.exceptions_configs.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BatchControllerTraceabilityTest {

    @Mock
    private BatchTraceabilityService traceabilityService;

    @Mock
    private BatchService batchService;

    @InjectMocks
    private BatchController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void downloadTraceabilityPDF_returnsPdfAndHeaders() throws Exception {
        byte[] pdf = new byte[]{1,2,3};
        when(traceabilityService.generateTraceabilityPDF(1L)).thenReturn(pdf);

        mockMvc.perform(get("/batches/1/traceability-pdf"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", "application/pdf"))
            .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("trazabilidad_lote_1.pdf")))
            .andExpect(header().longValue("Content-Length", 3L))
            .andExpect(content().bytes(pdf));
    }
}
