package com.apartment.service;

import com.apartment.entity.YeuCauGuiXe;
import com.apartment.repository.YeuCauGuiXeRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}

