package com.apartment.repository;

import com.apartment.entity.PhanAnh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhanAnhRepository extends JpaRepository<PhanAnh, Integer> {
    
    List<PhanAnh> findByCccdNguoiPhanAnhOrderByNgayTaoDesc(String cccd);
    
    List<PhanAnh> findByTrangThaiOrderByNgayTaoDesc(String trangThai);
    
    Page<PhanAnh> findByTrangThaiOrderByNgayTaoDesc(String trangThai, Pageable pageable);
    
    @Query("SELECT p FROM PhanAnh p ORDER BY p.ngayTao DESC")
    List<PhanAnh> findAllOrderByNgayTaoDesc();
    
    @Query("SELECT p FROM PhanAnh p ORDER BY p.ngayTao DESC")
    Page<PhanAnh> findAllOrderByNgayTaoDesc(Pageable pageable);
    
    @Query("SELECT p FROM PhanAnh p WHERE " +
           "(LOWER(p.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "p.cccdNguoiPhanAnh LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.ngayTao DESC")
    Page<PhanAnh> searchByKeyword(String keyword, Pageable pageable);
    
    @Query("SELECT p FROM PhanAnh p WHERE p.trangThai = :trangThai AND " +
           "(LOWER(p.tieuDe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "p.cccdNguoiPhanAnh LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.ngayTao DESC")
    Page<PhanAnh> searchByKeywordAndTrangThai(String keyword, String trangThai, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM PhanAnh p WHERE p.trangThai = 'moi'")
    Long countMoi();
}

