package com.azarenka.evebuilders.rest.api.ui;

import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.service.api.IOrderFilterService;
import com.azarenka.evebuilders.service.api.IProfileService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileRestControllerTest {

    private IProfileService profileService;
    private IOrderFilterService orderFilterService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        profileService = mock(IProfileService.class);
        orderFilterService = mock(IOrderFilterService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProfileRestController(profileService, orderFilterService))
            .build();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "pilot",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void saveFilterPersistsSingleFilterObject() throws Exception {
        mockMvc.perform(put("/api/profile/filter")
                .contentType("application/json")
                .content("""
                    {
                      "userId":"pilot",
                      "statuses":["IN_PROGRESS","NEW"],
                      "orderTypes":["MARKET","INDUSTRIAL"],
                      "minFreeCount":5,
                      "distributed":true
                    }
                    """))
            .andExpect(status().isNoContent());

        verify(orderFilterService).saveFilter(org.mockito.ArgumentMatchers.argThat(filter ->
            "pilot".equals(filter.getUserId())
                && filter.getStatuses() != null
                && filter.getStatuses().size() == 2
                && filter.getOrderTypes() != null
                && filter.getOrderTypes().size() == 2
                && Integer.valueOf(5).equals(filter.getMinFreeCount())
                && Boolean.TRUE.equals(filter.isDistributed())
        ));
    }

    @Test
    void getFilterReturnsStoredFilter() throws Exception {
        OrderFilter filter = new OrderFilter();
        filter.setUserId("pilot");
        filter.setStatuses(List.of(OrderStatusEnum.NEW, OrderStatusEnum.IN_PROGRESS));
        filter.setOrderTypes(List.of("MARKET"));
        filter.setMinFreeCount(3);
        filter.setDistributed(false);
        when(orderFilterService.getOrderFilter()).thenReturn(filter);

        mockMvc.perform(get("/api/profile/filter"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("pilot"))
            .andExpect(jsonPath("$.statuses[0]").value("NEW"))
            .andExpect(jsonPath("$.statuses[1]").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.orderTypes[0]").value("MARKET"))
            .andExpect(jsonPath("$.minFreeCount").value(3))
            .andExpect(jsonPath("$.distributed").value(false));
    }
}
