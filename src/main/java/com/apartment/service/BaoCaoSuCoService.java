package com.apartment.service;

import com.apartment.entity.BaoCaoSuCo;
import com.apartment.repository.BaoCaoSuCoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BaoCaoSuCoService {
    
    @Autowired
    private BaoCaoSuCoRepository baoCaoSuCoRepository;
    
    public List<BaoCaoSuCo> findAll() {
        return baoCaoSuCoRepository.findAll();
    }
    
    public Page<BaoCaoSuCo> findAll(Pageable pageable) {
        return baoCaoSuCoRepository.findAll(pageable);
    }
    
    public Optional<BaoCaoSuCo> findById(Integer id) {
        return baoCaoSuCoRepository.findById(id);
    }
    
    public List<BaoCaoSuCo> findByCccdNguoiBaoCao(String cccd) {
        return baoCaoSuCoRepository.findByCccdNguoiBaoCao(cccd);
    }
    
    public List<BaoCaoSuCo> findPendingReports() {
        return baoCaoSuCoRepository.findPendingReports();
    }
    
    public List<BaoCaoSuCo> findByTrangThai(String trangThai) {
        return baoCaoSuCoRepository.findByTrangThai(trangThai);
    }
    
    public Page<BaoCaoSuCo> findByTrangThai(String trangThai, Pageable pageable) {
        if (trangThai == null || trangThai.isEmpty()) {
            return findAll(pageable);
        }
        return baoCaoSuCoRepository.findByTrangThai(trangThai, pageable);
    }
    
    @Transactional
    public BaoCaoSuCo save(BaoCaoSuCo baoCaoSuCo) {
        return baoCaoSuCoRepository.save(baoCaoSuCo);
    }
    
    @Transactional
    public void delete(Integer id) {
        baoCaoSuCoRepository.deleteById(id);
    }
    
    public Long countNewReports() {
        return baoCaoSuCoRepository.countNewReports();
    }
}


