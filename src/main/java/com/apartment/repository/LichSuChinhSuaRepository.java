package com.apartment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apartment.entity.LichSuChinhSua;

@Repository
public interface LichSuChinhSuaRepository extends JpaRepository<LichSuChinhSua, Integer> {
    
    List<LichSuChinhSua> findByLoaiDoiTuongAndMaDoiTuongOrderByThoiGianDesc(String loaiDoiTuong, String maDoiTuong);
    
    List<LichSuChinhSua> findByCccdNguoiChinhSuaOrderByThoiGianDesc(String cccdNguoiChinhSua);
    
    List<LichSuChinhSua> findByNguonChinhSuaOrderByThoiGianDesc(String nguonChinhSua);
    
    @Query("SELECT l FROM LichSuChinhSua l WHERE l.loaiDoiTuong = :loaiDoiTuong ORDER BY l.thoiGian DESC")
    List<LichSuChinhSua> findByLoaiDoiTuongOrderByThoiGianDesc(String loaiDoiTuong);
    
    @Query("SELECT l FROM LichSuChinhSua l ORDER BY l.thoiGian DESC")
    List<LichSuChinhSua> findAllOrderByThoiGianDesc();
    
    @Query("SELECT l FROM LichSuChinhSua l WHERE l.maDoiTuong = :maDoiTuong ORDER BY l.thoiGian DESC")
    List<LichSuChinhSua> findByMaDoiTuongOrderByThoiGianDesc(String maDoiTuong);
}

