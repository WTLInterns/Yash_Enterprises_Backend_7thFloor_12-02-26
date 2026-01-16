package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Deal;
import com.company.attendance.crm.repository.DealRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DealService {
    private final DealRepository dealRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public Optional<Deal> findById(Long id) {
        return dealRepository.findById(id);
    }

    public Page<Deal> list(Pageable pageable) {
        return dealRepository.findAll(pageable);
    }

    public List<Deal> getAllDeals() {
        return dealRepository.findAll();
    }

    public Deal create(Deal deal) {
        return dealRepository.save(deal);
    }

    public Deal update(Deal deal) {
        return dealRepository.save(deal);
    }

    public void delete(Long id) {
        dealRepository.deleteById(id);
    }
}
