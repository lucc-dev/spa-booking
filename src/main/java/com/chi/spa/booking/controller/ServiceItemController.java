package com.chi.spa.booking.controller;

import com.chi.spa.booking.model.ServiceItem;
import com.chi.spa.booking.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-items")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceItemService;

    @PostMapping
    public ResponseEntity<ServiceItem> createServiceItem(@RequestBody ServiceItem serviceItem) {
        try {
            ServiceItem created = serviceItemService.createServiceItem(serviceItem);
            if (created == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ServiceItem>> getAllServiceItems() {
        try {
            List<ServiceItem> items = serviceItemService.getAllServiceItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceItem> getServiceItemById(@PathVariable Long id) {
        try {
            ServiceItem item = serviceItemService.getServiceItemById(id);
            if (item == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceItem> updateServiceItem(@PathVariable Long id, @RequestBody ServiceItem serviceItem) {
        try {
            serviceItem.setId(id);
            ServiceItem updated = serviceItemService.updateServiceItem(serviceItem);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteServiceItemById(@PathVariable Long id) {
        try {
            boolean deleted = serviceItemService.deleteServiceItemById(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}