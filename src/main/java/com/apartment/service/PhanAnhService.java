package com.apartment.service;

import com.apartment.entity.PhanAnh;
import com.apartment.repository.PhanAnhRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PhanAnhService {
    
    @Autowired
    private PhanAnhRepository phanAnhRepository;
    
    public List<PhanAnh> findAll() {
        return phanAnhRepository.findAllOrderByNgayTaoDesc();
    }
    
    public Page<PhanAnh> findAll(Pageable pageable) {
        return phanAnhRepository.findAllOrderByNgayTaoDesc(pageable);
    }
    
    public List<PhanAnh> findByCccdNguoiPhanAnh(String cccd) {
        return phanAnhRepository.findByCccdNguoiPhanAnhOrderByNgayTaoDesc(cccd);
    }
    
    public List<PhanAnh> findByTrangThai(String trangThai) {
        return phanAnhRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai);
    }
    
    public Page<PhanAnh> findByTrangThai(String trangThai, Pageable pageable) {
        return phanAnhRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai, pageable);
    }
    
    public Page<PhanAnh> searchByKeyword(String keyword, String trangThai, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            if (trangThai != null && !trangThai.isEmpty()) {
                return findByTrangThai(trangThai, pageable);
            }
            return findAll(pageable);
        }
        if (trangThai != null && !trangThai.isEmpty()) {
            return phanAnhRepository.searchByKeywordAndTrangThai(keyword.trim(), trangThai, pageable);
        }
        return phanAnhRepository.searchByKeyword(keyword.trim(), pageable);
    }
    
    public List<PhanAnh> findMoi() {
        return phanAnhRepository.findByTrangThaiOrderByNgayTaoDesc("moi");
    }
    
    public Long countMoi() {
        return phanAnhRepository.countMoi();
    }
    
    public Optional<PhanAnh> findById(Integer id) {
        return phanAnhRepository.findById(id);
    }
    
    public PhanAnh save(PhanAnh phanAnh) {
        return phanAnhRepository.save(phanAnh);
    }
    
    public void delete(Integer id) {
        phanAnhRepository.deleteById(id);
    }
    
    public PhanAnh phanHoi(Integer maPhanAnh, String noiDungPhanHoi, String cccdNguoiPhanHoi) {
        PhanAnh phanAnh = phanAnhRepository.findById(maPhanAnh)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phản ánh"));
        
        phanAnh.setTrangThai("da_phan_hoi");
        phanAnh.setNoiDungPhanHoi(noiDungPhanHoi);
        phanAnh.setCccdNguoiPhanHoi(cccdNguoiPhanHoi);
        phanAnh.setNgayPhanHoi(LocalDateTime.now());
        
        return phanAnhRepository.save(phanAnh);
    }
    
    public PhanAnh danhDauDaXem(Integer maPhanAnh) {
        PhanAnh phanAnh = phanAnhRepository.findById(maPhanAnh)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phản ánh"));
        
        if ("moi".equals(phanAnh.getTrangThai())) {
            phanAnh.setTrangThai("da_xem");
        }
        
        return phanAnhRepository.save(phanAnh);
    }
}

