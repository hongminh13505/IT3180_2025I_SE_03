package com.apartment.repository;

import com.apartment.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    
    @Query("SELECT hd FROM HoaDon hd WHERE " +
           "(:loaiHoaDon IS NULL OR hd.loaiHoaDon = :loaiHoaDon) AND " +
           "(:trangThai IS NULL OR hd.trangThai = :trangThai) AND " +
           "(:tuNgay IS NULL OR hd.hanThanhToan >= :tuNgay) AND " +
           "(:denNgay IS NULL OR hd.hanThanhToan <= :denNgay)")
    Page<HoaDon> filter(@Param("loaiHoaDon") String loaiHoaDon,
                        @Param("trangThai") String trangThai,
                        @Param("tuNgay") LocalDate tuNgay,
                        @Param("denNgay") LocalDate denNgay,
                        Pageable pageable);
    
    @Query("SELECT hd FROM HoaDon hd WHERE " +
           "(LOWER(hd.maHo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(hd.loaiHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:loaiHoaDon IS NULL OR hd.loaiHoaDon = :loaiHoaDon) AND " +
           "(:trangThai IS NULL OR hd.trangThai = :trangThai) AND " +
           "(:tuNgay IS NULL OR hd.hanThanhToan >= :tuNgay) AND " +
           "(:denNgay IS NULL OR hd.hanThanhToan <= :denNgay)")
    Page<HoaDon> searchAndFilter(@Param("keyword") String keyword,
                                 @Param("loaiHoaDon") String loaiHoaDon,
                                 @Param("trangThai") String trangThai,
                                 @Param("tuNgay") LocalDate tuNgay,
                                 @Param("denNgay") LocalDate denNgay,
                                 Pageable pageable);
    
    @Query(value = "SELECT YEAR(hd.ngay_tao) as year, MONTH(hd.ngay_tao) as month, " +
           "SUM(CASE WHEN hd.trang_thai = 'da_thanh_toan' THEN hd.so_tien ELSE 0 END) as tongThu, " +
           "SUM(CASE WHEN hd.trang_thai = 'chua_thanh_toan' THEN hd.so_tien ELSE 0 END) as tongNo, " +
           "COUNT(hd.ma_hoa_don) as soLuong " +
           "FROM hoa_don hd " +
           "WHERE YEAR(hd.ngay_tao) = :year " +
           "GROUP BY YEAR(hd.ngay_tao), MONTH(hd.ngay_tao) " +
           "ORDER BY month", nativeQuery = true)
    List<Object[]> thongKeTheoThang(@Param("year") int year);
    
    @Query(value = "SELECT YEAR(hd.ngay_tao) as year, " +
           "SUM(CASE WHEN hd.trang_thai = 'da_thanh_toan' THEN hd.so_tien ELSE 0 END) as tongThu, " +
           "SUM(CASE WHEN hd.trang_thai = 'chua_thanh_toan' THEN hd.so_tien ELSE 0 END) as tongNo, " +
           "COUNT(hd.ma_hoa_don) as soLuong " +
           "FROM hoa_don hd " +
           "GROUP BY YEAR(hd.ngay_tao) " +
           "ORDER BY year", nativeQuery = true)
    List<Object[]> thongKeTheoNam();
    
    @Query(value = "SELECT hd.loai_hoa_don, " +
           "SUM(CASE WHEN hd.trang_thai = 'da_thanh_toan' THEN hd.so_tien ELSE 0 END) as tongThu, " +
           "SUM(CASE WHEN hd.trang_thai = 'chua_thanh_toan' THEN hd.so_tien ELSE 0 END) as tongNo, " +
           "COUNT(hd.ma_hoa_don) as soLuong " +
           "FROM hoa_don hd " +
           "WHERE YEAR(hd.ngay_tao) = :year " +
           "AND (:month IS NULL OR MONTH(hd.ngay_tao) = :month) " +
           "GROUP BY hd.loai_hoa_don", nativeQuery = true)
    List<Object[]> thongKeTheoLoai(@Param("year") int year, @Param("month") Integer month);
}


