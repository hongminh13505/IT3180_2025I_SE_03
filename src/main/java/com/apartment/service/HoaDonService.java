package com.apartment.service;

import com.apartment.entity.HoaDon;
import com.apartment.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HoaDonService {
    
    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    public List<HoaDon> findAll() {
        return hoaDonRepository.findAll();
    }
    
    public Page<HoaDon> findAll(Pageable pageable) {
        return hoaDonRepository.findAll(pageable);
    }
    
    public Page<HoaDon> searchByKeyword(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return findAll(pageable);
        }
        return hoaDonRepository.searchByKeyword(keyword.trim(), pageable);
    }
    
    public Page<HoaDon> filter(String loaiHoaDon, String trangThai, LocalDate tuNgay, LocalDate denNgay, Pageable pageable) {
        return hoaDonRepository.filter(
            (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) ? loaiHoaDon : null,
            (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai : null,
            tuNgay,
            denNgay,
            pageable
        );
    }
    
    public Page<HoaDon> searchAndFilter(String keyword, String loaiHoaDon, String trangThai, LocalDate tuNgay, LocalDate denNgay, Pageable pageable) {
        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return hoaDonRepository.searchAndFilter(
            trimmedKeyword,
            (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) ? loaiHoaDon : null,
            (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai : null,
            tuNgay,
            denNgay,
            pageable
        );
    }
    
    public Optional<HoaDon> findById(Integer id) {
        return hoaDonRepository.findById(id);
    }
    
    public List<HoaDon> findByMaHo(String maHo) {
        return hoaDonRepository.findByMaHo(maHo);
    }
    
    public List<HoaDon> findUnpaidByMaHo(String maHo) {
        return hoaDonRepository.findUnpaidByMaHo(maHo);
    }
    
    public List<HoaDon> findByTrangThai(String trangThai) {
        return hoaDonRepository.findByTrangThai(trangThai);
    }
    
    @Transactional
    public HoaDon save(HoaDon hoaDon) {
        return hoaDonRepository.save(hoaDon);
    }
    
    @Transactional
    public void delete(Integer id) {
        hoaDonRepository.deleteById(id);
    }
    
    public BigDecimal sumPaidAmount() {
        BigDecimal sum = hoaDonRepository.sumPaidAmount();
        return sum != null ? sum : BigDecimal.ZERO;
    }
    
    public BigDecimal sumUnpaidAmount() {
        BigDecimal sum = hoaDonRepository.sumUnpaidAmount();
        return sum != null ? sum : BigDecimal.ZERO;
    }
    
    public List<HoaDon> findRecentInvoices() {
        return hoaDonRepository.findAll().stream()
                .sorted((a, b) -> b.getNgayTao().compareTo(a.getNgayTao()))
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public long countPaidInvoices() {
        return hoaDonRepository.findByTrangThai("da_thanh_toan").size();
    }
    
    public long countUnpaidInvoices() {
        return hoaDonRepository.findByTrangThai("chua_thanh_toan").size();
    }
}


