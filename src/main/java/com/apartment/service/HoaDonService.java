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
        String finalLoaiHoaDon = (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) ? loaiHoaDon : null;
        String finalTrangThai = (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai : null;
        
        boolean hasLoai = finalLoaiHoaDon != null;
        boolean hasTrangThai = finalTrangThai != null;
        boolean hasTuNgay = tuNgay != null;
        boolean hasDenNgay = denNgay != null;
        
        if (!hasLoai && !hasTrangThai && !hasTuNgay && !hasDenNgay) {
            return findAll(pageable);
        }
        
        java.util.List<HoaDon> allHoaDon = hoaDonRepository.findAll();
        java.util.stream.Stream<HoaDon> stream = allHoaDon.stream();
        
        if (hasLoai) {
            stream = stream.filter(hd -> finalLoaiHoaDon.equals(hd.getLoaiHoaDon()));
        }
        
        if (hasTrangThai) {
            stream = stream.filter(hd -> finalTrangThai.equals(hd.getTrangThai()));
        }
        
        if (hasTuNgay) {
            stream = stream.filter(hd -> hd.getHanThanhToan() != null && 
                !hd.getHanThanhToan().isBefore(tuNgay));
        }
        
        if (hasDenNgay) {
            stream = stream.filter(hd -> hd.getHanThanhToan() != null && 
                !hd.getHanThanhToan().isAfter(denNgay));
        }
        
        java.util.List<HoaDon> filteredList = stream.collect(java.util.stream.Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        java.util.List<HoaDon> pageContent = start < filteredList.size() 
            ? filteredList.subList(start, end) 
            : java.util.Collections.emptyList();
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            filteredList.size()
        );
    }
    
    public Page<HoaDon> searchAndFilter(String keyword, String loaiHoaDon, String trangThai, LocalDate tuNgay, LocalDate denNgay, Pageable pageable) {
        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String finalLoaiHoaDon = (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) ? loaiHoaDon : null;
        String finalTrangThai = (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai : null;
        
        boolean hasKeyword = trimmedKeyword != null;
        boolean hasLoai = finalLoaiHoaDon != null;
        boolean hasTrangThai = finalTrangThai != null;
        boolean hasTuNgay = tuNgay != null;
        boolean hasDenNgay = denNgay != null;
        
        if (!hasKeyword && !hasLoai && !hasTrangThai && !hasTuNgay && !hasDenNgay) {
            return findAll(pageable);
        }
        
        if (hasKeyword && !hasLoai && !hasTrangThai && !hasTuNgay && !hasDenNgay) {
            return searchByKeyword(trimmedKeyword, pageable);
        }
        
        java.util.List<HoaDon> allHoaDon = hoaDonRepository.findAll();
        java.util.stream.Stream<HoaDon> stream = allHoaDon.stream();
        
        if (hasKeyword) {
            String lowerKeyword = trimmedKeyword.toLowerCase();
            stream = stream.filter(hd -> 
                (hd.getMaHo() != null && hd.getMaHo().toLowerCase().contains(lowerKeyword)) ||
                (hd.getLoaiHoaDon() != null && hd.getLoaiHoaDon().toLowerCase().contains(lowerKeyword))
            );
        }
        
        if (hasLoai) {
            stream = stream.filter(hd -> finalLoaiHoaDon.equals(hd.getLoaiHoaDon()));
        }
        
        if (hasTrangThai) {
            stream = stream.filter(hd -> finalTrangThai.equals(hd.getTrangThai()));
        }
        
        if (hasTuNgay) {
            stream = stream.filter(hd -> hd.getHanThanhToan() != null && 
                !hd.getHanThanhToan().isBefore(tuNgay));
        }
        
        if (hasDenNgay) {
            stream = stream.filter(hd -> hd.getHanThanhToan() != null && 
                !hd.getHanThanhToan().isAfter(denNgay));
        }
        
        java.util.List<HoaDon> filteredList = stream.collect(java.util.stream.Collectors.toList());
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        java.util.List<HoaDon> pageContent = start < filteredList.size() 
            ? filteredList.subList(start, end) 
            : java.util.Collections.emptyList();
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            filteredList.size()
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


