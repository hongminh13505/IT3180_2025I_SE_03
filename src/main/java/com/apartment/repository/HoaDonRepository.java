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
    
    @Query(value = "SELECT DATE_PART('year', hd.ngay_tao)::int as year, DATE_PART('month', hd.ngay_tao)::int as month, " +
           "COALESCE(SUM(CASE WHEN hd.trang_thai = 'da_thanh_toan' THEN hd.so_tien ELSE 0 END), 0) as tongThu, " +
           "COALESCE(SUM(CASE WHEN hd.trang_thai = 'chua_thanh_toan' THEN hd.so_tien ELSE 0 END), 0) as tongNo, " +
           "COUNT(hd.ma_hoa_don) as soLuong " +
           "FROM hoa_don hd " +
           "WHERE DATE_PART('year', hd.ngay_tao) = :year " +
           "GROUP BY DATE_PART('year', hd.ngay_tao), DATE_PART('month', hd.ngay_tao) " +
           "ORDER BY month", nativeQuery = true)
    List<Object[]> thongKeTheoThang(@Param("year") int year);
    
    @Query(value = "SELECT DATE_PART('year', hd.ngay_tao)::int as year, " +
           "COALESCE(SUM(CASE WHEN hd.trang_thai = 'da_thanh_toan' THEN hd.so_tien ELSE 0 END), 0) as tongThu, " +
           "COALESCE(SUM(CASE WHEN hd.trang_thai = 'chua_thanh_toan' THEN hd.so_tien ELSE 0 END), 0) as tongNo, " +
           "COUNT(hd.ma_hoa_don) as soLuong " +
           "FROM hoa_don hd " +
           "GROUP BY DATE_PART('year', hd.ngay_tao) " +
           "ORDER BY year", nativeQuery = true)
    List<Object[]> thongKeTheoNam();
    
    @Query(value = "SELECT hd.loai_hoa_don, " +
           "COALESCE(SUM(CASE WHEN hd.trang_thai = 'da_thanh_toan' THEN hd.so_tien ELSE 0 END), 0) as tongThu, " +
           "COALESCE(SUM(CASE WHEN hd.trang_thai = 'chua_thanh_toan' THEN hd.so_tien ELSE 0 END), 0) as tongNo, " +
           "COUNT(hd.ma_hoa_don) as soLuong " +
           "FROM hoa_don hd " +
           "WHERE DATE_PART('year', hd.ngay_tao) = :year " +
           "AND (:month IS NULL OR DATE_PART('month', hd.ngay_tao) = :month) " +
           "GROUP BY hd.loai_hoa_don", nativeQuery = true)
    List<Object[]> thongKeTheoLoai(@Param("year") int year, @Param("month") Integer month);
    
    @Query("SELECT hd FROM HoaDon hd WHERE " +
           "hd.ngayTao >= :tuNgay AND hd.ngayTao <= :denNgay")
    List<HoaDon> findByDateRange(LocalDate tuNgay, LocalDate denNgay);
    
    Page<HoaDon> findAll(Pageable pageable);
    
    @Query("SELECT hd FROM HoaDon hd WHERE " +
           "LOWER(hd.maHo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(hd.loaiHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<HoaDon> searchByKeyword(String keyword, Pageable pageable);
    
    @Query(value = "SELECT * FROM hoa_don hd WHERE " +
           "(:loaiHoaDon IS NULL OR hd.loai_hoa_don = :loaiHoaDon) AND " +
           "(:trangThai IS NULL OR hd.trang_thai = :trangThai) AND " +
           "(:tuNgay IS NULL OR hd.han_thanh_toan >= :tuNgay) AND " +
           "(:denNgay IS NULL OR hd.han_thanh_toan <= :denNgay)",
           nativeQuery = true)
    Page<HoaDon> filter(@Param("loaiHoaDon") String loaiHoaDon,
                        @Param("trangThai") String trangThai,
                        @Param("tuNgay") java.sql.Date tuNgay,
                        @Param("denNgay") java.sql.Date denNgay,
                        Pageable pageable);
    
    @Query(value = "SELECT * FROM hoa_don hd WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(hd.ma_ho) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(hd.loai_hoa_don) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:loaiHoaDon IS NULL OR hd.loai_hoa_don = :loaiHoaDon) AND " +
           "(:trangThai IS NULL OR hd.trang_thai = :trangThai) AND " +
           "(:tuNgay IS NULL OR hd.han_thanh_toan >= :tuNgay) AND " +
           "(:denNgay IS NULL OR hd.han_thanh_toan <= :denNgay)",
           nativeQuery = true)
    Page<HoaDon> searchAndFilter(@Param("keyword") String keyword,
                                 @Param("loaiHoaDon") String loaiHoaDon,
                                 @Param("trangThai") String trangThai,
                                 @Param("tuNgay") java.sql.Date tuNgay,
                                 @Param("denNgay") java.sql.Date denNgay,
                                 Pageable pageable);
}


