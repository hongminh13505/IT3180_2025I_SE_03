package com.apartment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apartment.entity.LichSuChinhSua;

@Repository
public interface LichSuChinhSuaRepository extends JpaRepository<LichSuChinhSua, Integer> {
    
    List<LichSuChinhSua> findByLoaiDoiTuongAndMaDoiTuongOrderByThoiGianDesc(String loaiDoiTuong, String maDoiTuong);
    
    Page<LichSuChinhSua> findByLoaiDoiTuongAndMaDoiTuongOrderByThoiGianDesc(String loaiDoiTuong, String maDoiTuong, Pageable pageable);
    
    List<LichSuChinhSua> findByCccdNguoiChinhSuaOrderByThoiGianDesc(String cccdNguoiChinhSua);
    
    Page<LichSuChinhSua> findByCccdNguoiChinhSuaOrderByThoiGianDesc(String cccdNguoiChinhSua, Pageable pageable);
    
    List<LichSuChinhSua> findByNguonChinhSuaOrderByThoiGianDesc(String nguonChinhSua);
    
    Page<LichSuChinhSua> findByNguonChinhSuaOrderByThoiGianDesc(String nguonChinhSua, Pageable pageable);
    
    @Query("SELECT l FROM LichSuChinhSua l WHERE l.loaiDoiTuong = :loaiDoiTuong ORDER BY l.thoiGian DESC")
    List<LichSuChinhSua> findByLoaiDoiTuongOrderByThoiGianDesc(String loaiDoiTuong);
    
    @Query("SELECT l FROM LichSuChinhSua l WHERE l.loaiDoiTuong = :loaiDoiTuong ORDER BY l.thoiGian DESC")
    Page<LichSuChinhSua> findByLoaiDoiTuongOrderByThoiGianDesc(String loaiDoiTuong, Pageable pageable);
    
    @Query("SELECT l FROM LichSuChinhSua l ORDER BY l.thoiGian DESC")
    List<LichSuChinhSua> findAllOrderByThoiGianDesc();
    
    @Query("SELECT l FROM LichSuChinhSua l ORDER BY l.thoiGian DESC")
    Page<LichSuChinhSua> findAllOrderByThoiGianDesc(Pageable pageable);
    
    @Query("SELECT l FROM LichSuChinhSua l WHERE l.maDoiTuong = :maDoiTuong ORDER BY l.thoiGian DESC")
    List<LichSuChinhSua> findByMaDoiTuongOrderByThoiGianDesc(String maDoiTuong);
}

