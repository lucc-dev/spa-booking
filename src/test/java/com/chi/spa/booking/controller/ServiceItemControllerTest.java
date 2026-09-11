package com.chi.spa.booking.controller;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.model.ServiceItem;
import com.chi.spa.booking.repository.ServiceItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = SpaBookingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class ServiceItemControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).build();
        serviceItemRepository.deleteAll();
    }

    @Test
    void createServiceItem_returnsBadRequestWhenServiceRejects() throws Exception {
        ServiceItem item = ServiceItem.builder()
                .name("Test Massage A")
                .durationMinutes(0)
                .price(1200)
                .build();

        mockMvc.perform(post("/service-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createServiceItem_returnsOkWhenSaved() throws Exception {
        ServiceItem item = ServiceItem.builder()
                .name("Test Massage B")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build();

        mockMvc.perform(post("/service-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Massage B"))
                .andExpect(jsonPath("$.durationMinutes").value(60));
    }

    @Test
    void getAllServiceItems_returnsOk() throws Exception {
        serviceItemRepository.save(ServiceItem.builder()
                .name("Test Massage C")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build());

        mockMvc.perform(get("/service-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Massage C"));
    }

    @Test
    void getServiceItemById_returnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get("/service-items/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateServiceItem_returnsOkAndUsesPathId() throws Exception {
        ServiceItem saved = serviceItemRepository.save(ServiceItem.builder()
                .name("Test Massage D")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build());
        ServiceItem item = ServiceItem.builder()
                .name("Test Massage D")
                .description("Updated description")
                .durationMinutes(60)
                .price(1200)
                .build();

        mockMvc.perform(put("/service-items/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().intValue()))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void deleteServiceItem_returnsNoContent() throws Exception {
        ServiceItem saved = serviceItemRepository.save(ServiceItem.builder()
                .name("Test Massage E")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build());

        mockMvc.perform(delete("/service-items/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }
}
