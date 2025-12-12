package com.apartment.repository;

import com.apartment.entity.TaiSanChungCu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaiSanChungCuRepository extends JpaRepository<TaiSanChungCu, Integer> {
    
    List<TaiSanChungCu> findByLoaiTaiSan(String loaiTaiSan);
    
    List<TaiSanChungCu> findByMaHo(String maHo);
    
    List<TaiSanChungCu> findByTrangThai(String trangThai);
    
    @Query("SELECT ts FROM TaiSanChungCu ts WHERE ts.loaiTaiSan = 'can_ho'")
    List<TaiSanChungCu> findAllCanHo();
    
    @Query("SELECT COUNT(ts) FROM TaiSanChungCu ts WHERE ts.loaiTaiSan = 'can_ho'")
    Long countCanHo();

    Optional<TaiSanChungCu> findByLoaiTaiSanAndTenTaiSanIgnoreCase(String loaiTaiSan, String tenTaiSan);
}


