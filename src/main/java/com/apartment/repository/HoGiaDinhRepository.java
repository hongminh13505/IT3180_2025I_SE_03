package com.apartment.repository;

import com.apartment.entity.HoGiaDinh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoGiaDinhRepository extends JpaRepository<HoGiaDinh, String> {
    
    Optional<HoGiaDinh> findByMaHo(String maHo);
    
    Optional<HoGiaDinh> findByMaCanHo(Integer maCanHo);
    
    List<HoGiaDinh> findByTrangThai(String trangThai);
    
    List<HoGiaDinh> findByTenHoContainingIgnoreCase(String tenHo);
    
    Page<HoGiaDinh> findByTenHoContainingIgnoreCase(String tenHo, Pageable pageable);
    
    Page<HoGiaDinh> findAll(Pageable pageable);
    
    @Query("SELECT COUNT(h) FROM HoGiaDinh h WHERE h.trangThai = 'hoat_dong'")
    Long countActiveHo();
}


