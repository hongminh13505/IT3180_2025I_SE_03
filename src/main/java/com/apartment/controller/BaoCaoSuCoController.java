package com.apartment.controller;

import com.apartment.entity.BaoCaoSuCo;
import com.apartment.service.BaoCaoSuCoService;
import com.apartment.service.DoiTuongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/bao-cao-su-co")
@PreAuthorize("hasAnyRole('BAN_QUAN_TRI', 'CO_QUAN_CHUC_NANG')")
public class BaoCaoSuCoController {
    
    @Autowired
    private BaoCaoSuCoService baoCaoSuCoService;
    
    @Autowired
    private DoiTuongService doiTuongService;
    
    @GetMapping
    public String list(@RequestParam(required = false) String trangThai,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "20") int size,
                      Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BaoCaoSuCo> baoCaoPage;
        
        if (trangThai != null && !trangThai.isEmpty()) {
            baoCaoPage = baoCaoSuCoService.findByTrangThai(trangThai, pageable);
        } else {
            baoCaoPage = baoCaoSuCoService.findAll(pageable);
        }
        
        model.addAttribute("baoCaoList", baoCaoPage.getContent());
        model.addAttribute("baoCaoPage", baoCaoPage);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", baoCaoPage.getTotalPages());
        model.addAttribute("totalElements", baoCaoPage.getTotalElements());
        return "admin/bao-cao-su-co/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model, Authentication authentication) {
        BaoCaoSuCo baoCaoSuCo = new BaoCaoSuCo();
        baoCaoSuCo.setCccdNguoiNhap(authentication.getName());
        model.addAttribute("baoCaoSuCo", baoCaoSuCo);
        model.addAttribute("cuDanList", doiTuongService.findAllActiveCuDan());
        model.addAttribute("isEdit", false);
        return "admin/bao-cao-su-co/form";
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        BaoCaoSuCo baoCaoSuCo = baoCaoSuCoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));
        model.addAttribute("baoCaoSuCo", baoCaoSuCo);
        model.addAttribute("cuDanList", doiTuongService.findAllActiveCuDan());
        model.addAttribute("isEdit", true);
        return "admin/bao-cao-su-co/form";
    }
    
    @PostMapping("/save")
    public String save(@ModelAttribute BaoCaoSuCo baoCaoSuCo, RedirectAttributes redirectAttributes) {
        try {
            // If maBaoCao exists, it's an update; otherwise it's a new record
            if (baoCaoSuCo.getMaBaoCao() != null) {
                // Preserve the original data by fetching existing record first
                BaoCaoSuCo existingBaoCao = baoCaoSuCoService.findById(baoCaoSuCo.getMaBaoCao())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));
                
                // Update only the editable fields
                existingBaoCao.setTieuDe(baoCaoSuCo.getTieuDe());
                existingBaoCao.setMoTaSuCo(baoCaoSuCo.getMoTaSuCo());
                existingBaoCao.setCccdNguoiBaoCao(baoCaoSuCo.getCccdNguoiBaoCao());
                existingBaoCao.setMucDoUuTien(baoCaoSuCo.getMucDoUuTien());
                existingBaoCao.setTrangThai(baoCaoSuCo.getTrangThai());
                
                baoCaoSuCoService.save(existingBaoCao);
                redirectAttributes.addFlashAttribute("success", "Cập nhật báo cáo thành công!");
            } else {
                baoCaoSuCoService.save(baoCaoSuCo);
                redirectAttributes.addFlashAttribute("success", "Tạo báo cáo thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/bao-cao-su-co";
    }
    
    @PostMapping("/xu-ly/{id}")
    public String xuLy(@PathVariable Integer id, 
                      @RequestParam(required = false) String ketQuaXuLy,
                      Authentication authentication,
                      RedirectAttributes redirectAttributes) {
        try {
            BaoCaoSuCo baoCao = baoCaoSuCoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));
            
            baoCao.setCccdNguoiXuLy(authentication.getName());
            baoCao.setTrangThai("da_hoan_thanh");
            baoCao.setNgayHoanThanh(LocalDateTime.now());
            if (ketQuaXuLy != null) {
                baoCao.setKetQuaXuLy(ketQuaXuLy);
            }
            
            baoCaoSuCoService.save(baoCao);
            redirectAttributes.addFlashAttribute("success", "Xử lý báo cáo thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/bao-cao-su-co?trangThai=da_hoan_thanh";
    }
}


