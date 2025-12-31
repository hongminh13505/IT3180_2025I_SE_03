package com.apartment.repository;

import com.apartment.entity.BaoCaoSuCo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BaoCaoSuCoRepository extends JpaRepository<BaoCaoSuCo, Integer> {
    
    List<BaoCaoSuCo> findByCccdNguoiBaoCao(String cccdNguoiBaoCao);
    
    List<BaoCaoSuCo> findByTrangThai(String trangThai);
    
    Page<BaoCaoSuCo> findByTrangThai(String trangThai, Pageable pageable);
    
    Page<BaoCaoSuCo> findAll(Pageable pageable);
    
    List<BaoCaoSuCo> findByMucDoUuTien(String mucDoUuTien);
    
    @Query("SELECT bc FROM BaoCaoSuCo bc WHERE bc.trangThai IN ('moi_tiep_nhan', 'dang_xu_ly') ORDER BY bc.mucDoUuTien DESC, bc.ngayBaoCao ASC")
    List<BaoCaoSuCo> findPendingReports();
    
    @Query("SELECT COUNT(bc) FROM BaoCaoSuCo bc WHERE bc.trangThai = 'moi_tiep_nhan'")
    Long countNewReports();
}


