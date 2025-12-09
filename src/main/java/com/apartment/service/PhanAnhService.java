package com.apartment.service;

import com.apartment.entity.PhanAnh;
import com.apartment.repository.PhanAnhRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    public List<PhanAnh> findByCccdNguoiPhanAnh(String cccd) {
        return phanAnhRepository.findByCccdNguoiPhanAnhOrderByNgayTaoDesc(cccd);
    }
    
    public List<PhanAnh> findByTrangThai(String trangThai) {
        return phanAnhRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai);
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

