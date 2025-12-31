package com.apartment.service;

import com.apartment.entity.DoiTuong;
import com.apartment.repository.DoiTuongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DoiTuongService {
    
    @Autowired
    private DoiTuongRepository doiTuongRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<DoiTuong> findAll() {
        return doiTuongRepository.findAll();
    }
    
    public Page<DoiTuong> findAll(Pageable pageable) {
        return doiTuongRepository.findAll(pageable);
    }
    
    public Optional<DoiTuong> findByCccd(String cccd) {
        return doiTuongRepository.findByCccd(cccd);
    }
    
    public List<DoiTuong> findAllActiveCuDan() {
        return doiTuongRepository.findAllActiveCuDan();
    }
    
    public List<DoiTuong> findByVaiTroAndActive(String vaiTro) {
        return doiTuongRepository.findByVaiTroAndActive(vaiTro);
    }
    
    public List<DoiTuong> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
         return doiTuongRepository.searchAllByKeyword(keyword.trim());
    }
    
    public Page<DoiTuong> searchByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return findAll(pageable);
        }
        return doiTuongRepository.searchAllByKeyword(keyword.trim(), pageable);
    }
    
    @Transactional
    public DoiTuong save(DoiTuong doiTuong) {
        // Khi cập nhật, nếu không nhập mật khẩu thì giữ nguyên mật khẩu cũ để tránh null constraint
        if (doiTuong.getCccd() != null) {
            doiTuongRepository.findByCccd(doiTuong.getCccd()).ifPresent(existing -> {
                if (doiTuong.getMatKhau() == null || doiTuong.getMatKhau().isBlank()) {
                    doiTuong.setMatKhau(existing.getMatKhau());
                }
            });
        }
        // Mã hóa mật khẩu nếu là tạo mới hoặc thay đổi mật khẩu (chưa mã hóa)
        if (doiTuong.getMatKhau() != null && !doiTuong.getMatKhau().startsWith("$2a$")) {
            doiTuong.setMatKhau(passwordEncoder.encode(doiTuong.getMatKhau()));
        }
        return doiTuongRepository.save(doiTuong);
    }
    
    @Transactional
    public void updateLastLogin(String cccd) {
        doiTuongRepository.findByCccd(cccd).ifPresent(doiTuong -> {
            doiTuong.setLanDangNhapCuoi(LocalDateTime.now());
            doiTuongRepository.save(doiTuong);
        });
    }
    
    @Transactional
    public void delete(String cccd) {
        doiTuongRepository.deleteById(cccd);
    }
    
    public Long countCuDan() {
        return doiTuongRepository.countCuDan();
    }
    
    public boolean existsByCccd(String cccd) {
        return doiTuongRepository.existsById(cccd);
    }
}


