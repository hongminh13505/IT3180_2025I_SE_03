package com.apartment.service;

import com.apartment.entity.DangKyDichVu;
import com.apartment.repository.DangKyDichVuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DangKyDichVuService {
    
    @Autowired
    private DangKyDichVuRepository dangKyDichVuRepository;
    
    public List<DangKyDichVu> findAll() {
        return dangKyDichVuRepository.findAll();
    }
    
    public Optional<DangKyDichVu> findById(Integer id) {
        return dangKyDichVuRepository.findById(id);
    }
    
    public List<DangKyDichVu> findByCccdNguoiDung(String cccd) {
        return dangKyDichVuRepository.findByCccdNguoiDung(cccd);
    }
    
    public List<DangKyDichVu> findByTrangThai(String trangThai) {
        return dangKyDichVuRepository.findByTrangThai(trangThai);
    }
    
    public List<DangKyDichVu> findPendingRegistrations() {
        return dangKyDichVuRepository.findPendingRegistrations();
    }
    
    @Transactional
    public DangKyDichVu save(DangKyDichVu dangKyDichVu) {
        return dangKyDichVuRepository.save(dangKyDichVu);
    }
    
    @Transactional
    public void delete(Integer id) {
        dangKyDichVuRepository.deleteById(id);
    }
}

