package com.apartment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.apartment.entity.LichSuChinhSua;
import com.apartment.service.LichSuChinhSuaService;

@Controller
@RequestMapping("/admin/lich-su-chinh-sua")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class LichSuChinhSuaController {
    
    @Autowired
    private LichSuChinhSuaService lichSuChinhSuaService;
    
    /**
     * Xem tất cả lịch sử chỉnh sửa
     */
    @GetMapping
    public String list(@RequestParam(required = false) String loaiDoiTuong,
                      @RequestParam(required = false) String maDoiTuong,
                      @RequestParam(required = false) String nguon,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "20") int size,
                      Model model,
                      Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LichSuChinhSua> lichSuPage;
        
        if (maDoiTuong != null && !maDoiTuong.trim().isEmpty() && 
            loaiDoiTuong != null && !loaiDoiTuong.trim().isEmpty()) {
            lichSuPage = lichSuChinhSuaService.findByLoaiDoiTuongAndMaDoiTuong(loaiDoiTuong, maDoiTuong, pageable);
        } else if (nguon != null && !nguon.trim().isEmpty()) {
            lichSuPage = lichSuChinhSuaService.findByNguonChinhSua(nguon, pageable);
        } else if (loaiDoiTuong != null && !loaiDoiTuong.trim().isEmpty()) {
            lichSuPage = lichSuChinhSuaService.findByLoaiDoiTuong(loaiDoiTuong, pageable);
        } else {
            lichSuPage = lichSuChinhSuaService.findAll(pageable);
        }
        
        model.addAttribute("lichSuList", lichSuPage.getContent());
        model.addAttribute("lichSuPage", lichSuPage);
        model.addAttribute("loaiDoiTuong", loaiDoiTuong);
        model.addAttribute("maDoiTuong", maDoiTuong);
        model.addAttribute("nguon", nguon);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", lichSuPage.getTotalPages());
        model.addAttribute("totalElements", lichSuPage.getTotalElements());
        model.addAttribute("username", authentication.getName());
        
        return "admin/lich-su-chinh-sua/list";
    }
    
    /**
     * Xem lịch sử chỉnh sửa của một người dùng cụ thể
     */
    @GetMapping("/nguoi-dung/{cccd}")
    public String lichSuNguoiDung(@PathVariable String cccd, Model model, Authentication authentication) {
        List<LichSuChinhSua> lichSuList = lichSuChinhSuaService.findByCccdNguoiChinhSua(cccd);
        model.addAttribute("lichSuList", lichSuList);
        model.addAttribute("cccd", cccd);
        model.addAttribute("username", authentication.getName());
        return "admin/lich-su-chinh-sua/list";
    }
    
    /**
     * Xem chi tiết một bản ghi lịch sử
     */
    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(@PathVariable Integer id, Model model, Authentication authentication) {
        LichSuChinhSua lichSu = lichSuChinhSuaService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch sử"));
        
        model.addAttribute("lichSu", lichSu);
        model.addAttribute("username", authentication.getName());
        return "admin/lich-su-chinh-sua/detail";
    }
}

