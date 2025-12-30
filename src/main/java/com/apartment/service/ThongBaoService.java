package com.apartment.service;

import com.apartment.entity.ThongBao;
import com.apartment.repository.ThongBaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ThongBaoService {
    
    @Autowired
    private ThongBaoRepository thongBaoRepository;
    
    public List<ThongBao> findAll() {
        return thongBaoRepository.findAll();
    }
    
    public Page<ThongBao> findAll(Pageable pageable) {
        return thongBaoRepository.findAll(pageable);
    }
    
    public Page<ThongBao> searchByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return findAll(pageable);
        }
        return thongBaoRepository.searchByKeyword(keyword.trim(), pageable);
    }
    
    public Optional<ThongBao> findById(Integer id) {
        return thongBaoRepository.findById(id);
    }
    
    public List<ThongBao> findAllVisible() {
        return thongBaoRepository.findAllVisible();
    }
    
    public List<ThongBao> findPublicNotifications() {
        return thongBaoRepository.findPublicNotifications();
    }
    
    @Transactional
    public ThongBao save(ThongBao thongBao) {
        return thongBaoRepository.save(thongBao);
    }
    
    @Transactional
    public void delete(Integer id) {
        thongBaoRepository.deleteById(id);
    }
}


