package com.apartment.repository;

import com.apartment.entity.ThongBaoHo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ThongBaoHoRepository extends JpaRepository<ThongBaoHo, Integer> {
    
    List<ThongBaoHo> findByMaHo(String maHo);
    
    List<ThongBaoHo> findByMaThongBao(Integer maThongBao);
    
    @Query("SELECT tbh FROM ThongBaoHo tbh WHERE tbh.maHo = :maHo AND tbh.daXem = false")
    List<ThongBaoHo> findUnreadByMaHo(String maHo);
    
    void deleteByMaThongBao(Integer maThongBao);
}


