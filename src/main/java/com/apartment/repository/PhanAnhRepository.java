package com.apartment.repository;

import com.apartment.entity.PhanAnh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PhanAnhRepository extends JpaRepository<PhanAnh, Integer> {
    
    List<PhanAnh> findByCccdNguoiPhanAnhOrderByNgayTaoDesc(String cccd);
    
    List<PhanAnh> findByTrangThaiOrderByNgayTaoDesc(String trangThai);
    
    @Query("SELECT COUNT(p) FROM PhanAnh p WHERE p.trangThai = 'moi'")
    Long countMoi();
    
    @Query("SELECT p FROM PhanAnh p ORDER BY p.ngayTao DESC")
    List<PhanAnh> findAllOrderByNgayTaoDesc();
}

