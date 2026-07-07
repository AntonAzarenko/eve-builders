package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FitRestControllerTest {

    private IFitLoaderService fitLoaderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        fitLoaderService = mock(IFitLoaderService.class);
        mockMvc = MockMvcBuilders
            .standaloneSetup(new FitRestController(fitLoaderService))
            .build();
    }

    @Test
    void getAllFitsReturnsFits() throws Exception {
        Fit fit = fit("fit-1", "Rifter fit", "text-fit");
        when(fitLoaderService.getAll()).thenReturn(List.of(fit));

        mockMvc.perform(get("/api/fits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("fit-1"))
            .andExpect(jsonPath("$[0].name").value("Rifter fit"))
            .andExpect(jsonPath("$[0].textFit").value("text-fit"));
    }

    @Test
    void getFitByIdReturnsFit() throws Exception {
        Fit fit = fit("fit-1", "Rifter fit", "text-fit");
        when(fitLoaderService.getFitById("fit-1")).thenReturn(fit);

        mockMvc.perform(get("/api/fits/fit-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("fit-1"))
            .andExpect(jsonPath("$.name").value("Rifter fit"))
            .andExpect(jsonPath("$.textFit").value("text-fit"));
    }

    @Test
    void uploadFitReturnsResult() throws Exception {
        when(fitLoaderService.upload("fit text")).thenReturn(true);

        mockMvc.perform(post("/api/fits/upload")
                .contentType(MediaType.TEXT_PLAIN)
                .content("fit text"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));

        verify(fitLoaderService).upload("fit text");
    }

    private Fit fit(String id, String name, String textFit) {
        Fit fit = new Fit();
        fit.setId(id);
        fit.setName(name);
        fit.setTextFit(textFit);
        return fit;
    }
}
