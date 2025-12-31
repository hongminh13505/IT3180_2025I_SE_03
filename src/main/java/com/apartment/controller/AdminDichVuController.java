package com.apartment.controller;

import com.apartment.entity.DichVu;
import com.apartment.service.DichVuService;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/dich-vu")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class AdminDichVuController {

    @Autowired
    private DichVuService dichVuService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "20") int size,
                      Model model) {
        try {
            List<DichVu> allDichVu;
            
            if (search != null && !search.trim().isEmpty()) {
                allDichVu = dichVuService.searchByName(search.trim());
            } else {
                allDichVu = dichVuService.findAll();
            }
            
            // Manual pagination
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allDichVu.size());
            List<DichVu> pageContent = start < allDichVu.size() 
                ? allDichVu.subList(start, end) 
                : Collections.emptyList();
            
            Page<DichVu> dichVuPage = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, allDichVu.size());
            
            model.addAttribute("dichVuList", dichVuPage.getContent());
            model.addAttribute("dichVuPage", dichVuPage);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", dichVuPage.getTotalPages());
            model.addAttribute("totalElements", dichVuPage.getTotalElements());
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách dịch vụ: " + e.getMessage());
            model.addAttribute("dichVuList", Collections.emptyList());
        }
        return "admin/dich-vu/list";
    }

    @GetMapping("/create")
    public String createForm(Model model, Authentication authentication) {
        DichVu dichVu = new DichVu();
        dichVu.setCccdBanQuanTri(authentication.getName());
        dichVu.setTrangThai("hoat_dong");
        model.addAttribute("dichVu", dichVu);
        model.addAttribute("isEdit", false);
        return "admin/dich-vu/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            DichVu dichVu = dichVuService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));
            model.addAttribute("dichVu", dichVu);
            model.addAttribute("isEdit", true);
            return "admin/dich-vu/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không tìm thấy dịch vụ: " + e.getMessage());
            return "redirect:/admin/dich-vu";
        }
    }

    @PostMapping("/save")
    public String save(@ModelAttribute DichVu dichVu,
                      RedirectAttributes redirectAttributes,
                      Authentication authentication) {
        try {
            if (dichVu.getMaDichVu() == null) {
                // Tạo mới
                dichVu.setCccdBanQuanTri(authentication.getName());
            } else {
                // Cập nhật - giữ nguyên cccdBanQuanTri
                DichVu existing = dichVuService.findById(dichVu.getMaDichVu())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));
                dichVu.setCccdBanQuanTri(existing.getCccdBanQuanTri());
            }
            
            // Set mặc định đơn vị là "1" nếu null hoặc rỗng
            if (dichVu.getDonVi() == null || dichVu.getDonVi().trim().isEmpty()) {
                dichVu.setDonVi("1");
            }
            
            dichVuService.save(dichVu);
            redirectAttributes.addFlashAttribute("success", 
                dichVu.getMaDichVu() == null ? "Tạo dịch vụ thành công!" : "Cập nhật dịch vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi lưu dịch vụ: " + e.getMessage());
        }
        return "redirect:/admin/dich-vu";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            dichVuService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa dịch vụ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi xóa dịch vụ: " + e.getMessage());
        }
        return "redirect:/admin/dich-vu";
    }
}

