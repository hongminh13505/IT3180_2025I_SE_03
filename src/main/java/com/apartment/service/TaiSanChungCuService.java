package com.apartment.service;

import com.apartment.entity.TaiSanChungCu;
import com.apartment.repository.TaiSanChungCuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TaiSanChungCuService {
    
    @Autowired
    private TaiSanChungCuRepository taiSanRepository;
    
    public List<TaiSanChungCu> findAll() {
        return taiSanRepository.findAll();
    }
    
    public Page<TaiSanChungCu> findAll(Pageable pageable) {
        return taiSanRepository.findAll(pageable);
    }
    
    public Optional<TaiSanChungCu> findById(Integer maTaiSan) {
        return taiSanRepository.findById(maTaiSan);
    }
    
    public List<TaiSanChungCu> findAllCanHo() {
        return taiSanRepository.findAllCanHo();
    }
    
    public List<TaiSanChungCu> findAllCanHoActive() {
        return taiSanRepository.findAllCanHoActive();
    }
    
    public List<TaiSanChungCu> findByLoaiTaiSan(String loaiTaiSan) {
        return taiSanRepository.findByLoaiTaiSan(loaiTaiSan);
    }
    
    public Page<TaiSanChungCu> findByLoaiTaiSan(String loaiTaiSan, Pageable pageable) {
        if (loaiTaiSan == null || loaiTaiSan.isEmpty()) {
            return findAll(pageable);
        }
        return taiSanRepository.findByLoaiTaiSan(loaiTaiSan, pageable);
    }
    
    public List<TaiSanChungCu> findByTrangThai(String trangThai) {
        return taiSanRepository.findByTrangThai(trangThai);
    }
    
    public List<TaiSanChungCu> findByMaHo(String maHo) {
        return taiSanRepository.findByMaHo(maHo);
    }
    
    public Optional<TaiSanChungCu> findCanHoByTen(String tenCanHo) {
        return taiSanRepository.findByLoaiTaiSanAndTenTaiSanIgnoreCase("can_ho", tenCanHo);
    }
    
    @Transactional
    public TaiSanChungCu save(TaiSanChungCu taiSan) {
        return taiSanRepository.save(taiSan);
    }
    
    @Transactional
    public void delete(Integer maTaiSan) {
        taiSanRepository.deleteById(maTaiSan);
    }
    
    public Long countCanHo() {
        return taiSanRepository.countCanHo();
    }
}

