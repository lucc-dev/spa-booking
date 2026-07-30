package com.chi.spa.booking.service;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.model.ServiceItem;
import com.chi.spa.booking.repository.ServiceItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpaBookingApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class ServiceItemServiceTest {

    @Autowired
    private ServiceItemService serviceItemService;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @BeforeEach
    void clearData() {
        serviceItemRepository.deleteAll();
    }

    @Test
    void createServiceItem_savesValidItem() {
        ServiceItem item = ServiceItem.builder()
                .name("Test Massage A")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build();

        ServiceItem result = serviceItemService.createServiceItem(item);

        assertThat(result.getId()).isNotNull();
        assertThat(serviceItemRepository.findById(result.getId())).isPresent();
    }

    @Test
    void createServiceItem_returnsNullWhenRequiredFieldsMissing() {
        assertThat(serviceItemService.createServiceItem(new ServiceItem())).isNull();
    }

    @Test
    void createServiceItem_returnsNullWhenDurationInvalid() {
        ServiceItem item = ServiceItem.builder()
                .name("Test Massage B")
                .durationMinutes(0)
                .price(1200)
                .build();

        assertThat(serviceItemService.createServiceItem(item)).isNull();
    }

    @Test
    void getAllServiceItems_returnsAllItems() {
        serviceItemRepository.save(ServiceItem.builder()
                .name("Test Massage C")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build());

        List<ServiceItem> result = serviceItemService.getAllServiceItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Massage C");
    }

    @Test
    void getServiceItemById_returnsItemWhenFound() {
        ServiceItem saved = serviceItemRepository.save(ServiceItem.builder()
                .name("Test Massage D")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build());

        ServiceItem result = serviceItemService.getServiceItemById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Massage D");
    }

    @Test
    void getServiceItemById_returnsNullWhenMissing() {
        assertThat(serviceItemService.getServiceItemById(999L)).isNull();
    }

    @Test
    void updateServiceItem_returnsNullWhenIdMissingOrNotExists() {
        ServiceItem item = ServiceItem.builder()
                .name("Test Massage E")
                .durationMinutes(60)
                .price(1200)
                .build();

        assertThat(serviceItemService.updateServiceItem(item)).isNull();
    }

    @Test
    void updateServiceItem_savesWhenExists() {
        ServiceItem saved = serviceItemRepository.save(ServiceItem.builder()
                .name("Test Massage F")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build());

        saved.setDescription("Updated description");

        ServiceItem result = serviceItemService.updateServiceItem(saved);

        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(serviceItemRepository.findById(saved.getId()).orElseThrow().getDescription())
                .isEqualTo("Updated description");
    }

    @Test
    void deleteServiceItemById_deletesWhenExists() {
        ServiceItem saved = serviceItemRepository.save(ServiceItem.builder()
                .name("Test Massage G")
                .description("Full body massage")
                .durationMinutes(60)
                .price(1200)
                .build());

        boolean result = serviceItemService.deleteServiceItemById(saved.getId());

        assertThat(result).isTrue();
        assertThat(serviceItemRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteServiceItemById_returnsFalseWhenMissing() {
        assertThat(serviceItemService.deleteServiceItemById(999L)).isFalse();
    }
}
