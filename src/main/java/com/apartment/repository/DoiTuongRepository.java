package com.apartment.repository;

import com.apartment.entity.DoiTuong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoiTuongRepository extends JpaRepository<DoiTuong, String> {
    
    Optional<DoiTuong> findByCccd(String cccd);
    
    @Query("SELECT d FROM DoiTuong d WHERE d.vaiTro = :vaiTro AND d.trangThaiTaiKhoan = 'hoat_dong'")
    List<DoiTuong> findByVaiTroAndActive(String vaiTro);
    
    @Query("SELECT d FROM DoiTuong d WHERE d.laCuDan = true AND d.trangThaiTaiKhoan = 'hoat_dong'")
    List<DoiTuong> findAllActiveCuDan();
    
    List<DoiTuong> findByHoVaTenContainingIgnoreCase(String hoVaTen);
    
    Optional<DoiTuong> findBySoDienThoai(String soDienThoai);
    
    Optional<DoiTuong> findByEmail(String email);
    
    @Query("SELECT COUNT(d) FROM DoiTuong d WHERE d.laCuDan = true")
    Long countCuDan();
    
    @Query("SELECT d FROM DoiTuong d WHERE d.laCuDan = true AND " +
           "(LOWER(d.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "d.cccd LIKE CONCAT('%', :keyword, '%') OR " +
           "d.soDienThoai LIKE CONCAT('%', :keyword, '%'))")
    List<DoiTuong> searchCuDanByKeyword(String keyword);

     @Query("SELECT d FROM DoiTuong d WHERE d.trangThaiTaiKhoan = 'hoat_dong' AND " +
           "(LOWER(d.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "d.cccd LIKE CONCAT('%', :keyword, '%') OR " +
           "d.soDienThoai LIKE CONCAT('%', :keyword, '%'))")
    List<DoiTuong> searchAllByKeyword(String keyword);
    
    @Query("SELECT d FROM DoiTuong d WHERE d.trangThaiTaiKhoan = 'hoat_dong' AND " +
           "(LOWER(d.hoVaTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "d.cccd LIKE CONCAT('%', :keyword, '%') OR " +
           "d.soDienThoai LIKE CONCAT('%', :keyword, '%'))")
    Page<DoiTuong> searchAllByKeyword(String keyword, Pageable pageable);
    
    Page<DoiTuong> findAll(Pageable pageable);
}


