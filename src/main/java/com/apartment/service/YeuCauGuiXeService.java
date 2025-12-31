package com.apartment.service;

import com.apartment.entity.YeuCauGuiXe;
import com.apartment.repository.YeuCauGuiXeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class YeuCauGuiXeService {

    @Autowired
    private YeuCauGuiXeRepository yeuCauGuiXeRepository;

    public List<YeuCauGuiXe> findAll() {
        return yeuCauGuiXeRepository.findAll();
    }

    public List<YeuCauGuiXe> findAllWithNguoi() {
        return yeuCauGuiXeRepository.findAll();
    }

    public Optional<YeuCauGuiXe> findById(Integer id) {
        return yeuCauGuiXeRepository.findById(id);
    }

    public Optional<YeuCauGuiXe> findWithNguoiById(Integer id) {
        return yeuCauGuiXeRepository.findByMaYeuCau(id);
    }

    public YeuCauGuiXe save(YeuCauGuiXe yeuCauGuiXe) {
        return yeuCauGuiXeRepository.save(yeuCauGuiXe);
    }

    public void deleteById(Integer id) {
        yeuCauGuiXeRepository.deleteById(id);
    }

    public List<YeuCauGuiXe> findByCccdNguoiGui(String cccd) {
        return yeuCauGuiXeRepository.findByCccdNguoiGui(cccd);
    }

    public List<YeuCauGuiXe> findByTrangThai(String trangThai) {
        return yeuCauGuiXeRepository.findByTrangThai(trangThai);
    }

    public List<YeuCauGuiXe> findByTrangThaiWithNguoi(String trangThai) {
        return yeuCauGuiXeRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai);
    }
    
    public Page<YeuCauGuiXe> findAllWithNguoi(Pageable pageable) {
        return yeuCauGuiXeRepository.findAll(pageable);
    }
    
    public Page<YeuCauGuiXe> findByTrangThaiWithNguoi(String trangThai, Pageable pageable) {
        return yeuCauGuiXeRepository.findByTrangThai(trangThai, pageable);
    }
    
    public Page<YeuCauGuiXe> searchByKeyword(String keyword, String trangThai, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            if (trangThai != null && !trangThai.isEmpty()) {
                return findByTrangThaiWithNguoi(trangThai, pageable);
            }
            return findAllWithNguoi(pageable);
        }
        if (trangThai != null && !trangThai.isEmpty()) {
            return yeuCauGuiXeRepository.searchByKeywordAndTrangThai(keyword.trim(), trangThai, pageable);
        }
        return yeuCauGuiXeRepository.searchByKeyword(keyword.trim(), pageable);
    }
}

