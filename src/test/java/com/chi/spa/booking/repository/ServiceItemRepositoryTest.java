package com.chi.spa.booking.repository;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.model.ServiceItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpaBookingApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class ServiceItemRepositoryTest {

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Test
    void saveAndFindById_works() {
        ServiceItem item = ServiceItem.builder()
                .name("Test Massage R")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build();

        ServiceItem saved = serviceItemRepository.save(item);

        assertThat(saved.getId()).isNotNull();
        assertThat(serviceItemRepository.findById(saved.getId())).isPresent();
        assertThat(serviceItemRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Test Massage R");
    }
}
