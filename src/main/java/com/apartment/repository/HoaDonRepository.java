package com.apartment.repository;

import com.apartment.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    
    List<HoaDon> findByMaHo(String maHo);
    
    List<HoaDon> findByTrangThai(String trangThai);
    
    @Query("SELECT hd FROM HoaDon hd WHERE hd.maHo = :maHo AND hd.trangThai = 'chua_thanh_toan'")
    List<HoaDon> findUnpaidByMaHo(String maHo);
    
    @Query("SELECT SUM(hd.soTien) FROM HoaDon hd WHERE hd.trangThai = 'da_thanh_toan'")
    BigDecimal sumPaidAmount();
    
    @Query("SELECT SUM(hd.soTien) FROM HoaDon hd WHERE hd.trangThai = 'chua_thanh_toan'")
    BigDecimal sumUnpaidAmount();
    
    @Query("SELECT hd FROM HoaDon hd ORDER BY hd.ngayTao DESC LIMIT 10")
    List<HoaDon> findRecentInvoices();
    
    Page<HoaDon> findAll(Pageable pageable);
    
    @Query("SELECT hd FROM HoaDon hd WHERE " +
           "LOWER(hd.maHo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(hd.loaiHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<HoaDon> searchByKeyword(String keyword, Pageable pageable);
}


