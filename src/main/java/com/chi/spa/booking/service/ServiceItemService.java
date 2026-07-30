package com.chi.spa.booking.service;

import com.chi.spa.booking.model.ServiceItem;
import com.chi.spa.booking.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceItemService {

    private final ServiceItemRepository serviceItemRepository;

    public ServiceItem createServiceItem(ServiceItem item) {
        if (item.getName() == null || item.getDurationMinutes() == null || item.getPrice() == null) {
            return null;
        }

        if (item.getDurationMinutes() <= 0 || item.getPrice() < 0) {
            return null;
        }
        return serviceItemRepository.save(item);
    }

    public List<ServiceItem> getAllServiceItems() {
        return serviceItemRepository.findAll();
    }

    public ServiceItem getServiceItemById(Long id) {
        return serviceItemRepository.findById(id).orElse(null);
    }

    public ServiceItem updateServiceItem(ServiceItem item) {
        if (item.getId() == null || !serviceItemRepository.existsById(item.getId())) {
            return null;
        }
        return serviceItemRepository.save(item);
    }

    public boolean deleteServiceItemById(Long id) {
        if (serviceItemRepository.existsById(id)) {
            serviceItemRepository.deleteById(id);
            return true;
        }
        return false;
    }
}