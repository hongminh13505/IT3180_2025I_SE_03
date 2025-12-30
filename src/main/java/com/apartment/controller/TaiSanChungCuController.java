package com.apartment.controller;

import com.apartment.entity.TaiSanChungCu;
import com.apartment.service.TaiSanChungCuService;
import com.apartment.service.HoGiaDinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/admin/tai-san")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class TaiSanChungCuController {
    
    private static final Logger logger = LoggerFactory.getLogger(TaiSanChungCuController.class);
    
    @Autowired
    private TaiSanChungCuService taiSanService;
    
    @Autowired
    private HoGiaDinhService hoGiaDinhService;
    
    @GetMapping
    public String list(@RequestParam(required = false) String loai,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "20") int size,
                      Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TaiSanChungCu> taiSanPage;
        
        if (loai != null && !loai.isEmpty()) {
            taiSanPage = taiSanService.findByLoaiTaiSan(loai, pageable);
        } else {
            taiSanPage = taiSanService.findAll(pageable);
        }
        
        java.util.Map<Integer, String> hoGiaDinhMap = new java.util.HashMap<>();
        hoGiaDinhService.findAll().forEach(ho -> {
            if (ho.getMaCanHo() != null) {
                hoGiaDinhMap.put(ho.getMaCanHo(), ho.getMaHo());
            }
        });
        
        model.addAttribute("taiSanList", taiSanPage.getContent());
        model.addAttribute("taiSanPage", taiSanPage);
        model.addAttribute("hoGiaDinhMap", hoGiaDinhMap);
        model.addAttribute("loai", loai);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taiSanPage.getTotalPages());
        model.addAttribute("totalElements", taiSanPage.getTotalElements());
        return "admin/tai-san/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("taiSan", new TaiSanChungCu());
        model.addAttribute("isEdit", false);
        return "admin/tai-san/form";
    }
    
    @GetMapping("/edit/{maTaiSan}")
    public String editForm(@PathVariable Integer maTaiSan, Model model) {
        TaiSanChungCu taiSan = taiSanService.findById(maTaiSan)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài sản"));
        model.addAttribute("taiSan", taiSan);
        model.addAttribute("isEdit", true);
        return "admin/tai-san/form";
    }
    
    @PostMapping("/save")
    public String save(@ModelAttribute TaiSanChungCu taiSan, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Saving tai san: {}", taiSan);
            boolean isEdit = taiSan.getMaTaiSan() != null;
            
        
            if (taiSan.getMaHo() != null && taiSan.getMaHo().trim().isEmpty()) {
                taiSan.setMaHo(null);
            }

       
            if (taiSan.getTenTaiSan() != null) {
                taiSan.setTenTaiSan(taiSan.getTenTaiSan().trim());
            }

            if ("can_ho".equalsIgnoreCase(taiSan.getLoaiTaiSan()) && taiSan.getTenTaiSan() != null && !taiSan.getTenTaiSan().isEmpty()) {
                taiSanService.findCanHoByTen(taiSan.getTenTaiSan())
                        .ifPresent(existing -> {
                            if (taiSan.getMaTaiSan() == null || !existing.getMaTaiSan().equals(taiSan.getMaTaiSan())) {
                                throw new IllegalArgumentException("Căn hộ '" + taiSan.getTenTaiSan() + "' đã tồn tại. Vui lòng chọn tên khác.");
                            }
                        });
            }
            
            
            if (taiSan.getTrangThai() == null || taiSan.getTrangThai().trim().isEmpty()) {
                taiSan.setTrangThai("hoat_dong");
            }
            
            TaiSanChungCu saved = taiSanService.save(taiSan);
            logger.info("Tai san saved successfully with ID: {}", saved.getMaTaiSan());
            
            redirectAttributes.addFlashAttribute("success", 
                taiSan.getMaTaiSan() == null ? "Thêm tài sản mới thành công!" : "Cập nhật tài sản thành công!");
        } catch (Exception e) {
            logger.error("Error saving tai san", e);
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            if (taiSan.getMaTaiSan() != null) {
                return "redirect:/admin/tai-san/edit/" + taiSan.getMaTaiSan();
            }
            return "redirect:/admin/tai-san/create";
        }
        return "redirect:/admin/tai-san";
    }
    
    @GetMapping("/delete/{maTaiSan}")
    public String delete(@PathVariable Integer maTaiSan, RedirectAttributes redirectAttributes) {
        try {
            taiSanService.delete(maTaiSan);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/tai-san";
    }
}

