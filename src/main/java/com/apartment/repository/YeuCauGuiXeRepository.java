package com.apartment.repository;

import com.apartment.entity.YeuCauGuiXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
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
}

