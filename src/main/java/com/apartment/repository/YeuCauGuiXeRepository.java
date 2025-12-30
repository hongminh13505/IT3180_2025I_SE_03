package com.apartment.repository;

import com.apartment.entity.YeuCauGuiXe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YeuCauGuiXeRepository extends JpaRepository<YeuCauGuiXe, Integer> {
    List<YeuCauGuiXe> findByCccdNguoiGui(String cccdNguoiGui);
    List<YeuCauGuiXe> findByTrangThai(String trangThai);

    @EntityGraph(attributePaths = {"nguoiGui", "nguoiXuLy"})
    List<YeuCauGuiXe> findAll();

    @EntityGraph(attributePaths = {"nguoiGui", "nguoiXuLy"})
    List<YeuCauGuiXe> findByTrangThaiOrderByNgayTaoDesc(String trangThai);

    @EntityGraph(attributePaths = {"nguoiGui", "nguoiXuLy"})
    Optional<YeuCauGuiXe> findByMaYeuCau(Integer id);
    
    @EntityGraph(attributePaths = {"nguoiGui", "nguoiXuLy"})
    Page<YeuCauGuiXe> findAll(Pageable pageable);
    
    @EntityGraph(attributePaths = {"nguoiGui", "nguoiXuLy"})
    Page<YeuCauGuiXe> findByTrangThai(String trangThai, Pageable pageable);
    
    @EntityGraph(attributePaths = {"nguoiGui", "nguoiXuLy"})
    @Query("SELECT y FROM YeuCauGuiXe y WHERE " +
           "LOWER(y.bienSo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(y.cccdNguoiGui) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(y.loaiXe) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<YeuCauGuiXe> searchByKeyword(String keyword, Pageable pageable);
    
    @EntityGraph(attributePaths = {"nguoiGui", "nguoiXuLy"})
    @Query("SELECT y FROM YeuCauGuiXe y WHERE y.trangThai = :trangThai AND " +
           "(LOWER(y.bienSo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(y.cccdNguoiGui) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(y.loaiXe) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<YeuCauGuiXe> searchByKeywordAndTrangThai(String keyword, String trangThai, Pageable pageable);
}

