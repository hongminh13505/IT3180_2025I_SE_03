package com.apartment.controller;

import com.apartment.entity.PhanAnh;
import com.apartment.service.PhanAnhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/phan-anh")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class AdminPhanAnhController {
    
    @Autowired
    private PhanAnhService phanAnhService;
    
    @GetMapping
    public String list(@RequestParam(required = false) String search,
                      @RequestParam(required = false) String trangThai,
                      Model model) {
        try {
            java.util.List<PhanAnh> phanAnhList;
            
            if (trangThai != null && !trangThai.isEmpty()) {
                phanAnhList = phanAnhService.findByTrangThai(trangThai);
            } else {
                phanAnhList = phanAnhService.findAll();
            }
            
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                phanAnhList = phanAnhList.stream()
                    .filter(pa -> 
                        (pa.getTieuDe() != null && pa.getTieuDe().toLowerCase().contains(searchLower)) ||
                        (pa.getNoiDung() != null && pa.getNoiDung().toLowerCase().contains(searchLower)) ||
                        (pa.getCccdNguoiPhanAnh() != null && pa.getCccdNguoiPhanAnh().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("phanAnhList", phanAnhList);
            model.addAttribute("search", search);
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("phanAnhMoi", phanAnhService.countMoi());
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách phản ánh: " + e.getMessage());
            model.addAttribute("phanAnhList", java.util.Collections.emptyList());
        }
        
        return "admin/phan-anh/list";
    }
    
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model, Authentication authentication) {
        try {
            PhanAnh phanAnh = phanAnhService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phản ánh"));
            
            // Đánh dấu đã xem nếu chưa xem
            if ("moi".equals(phanAnh.getTrangThai())) {
                phanAnhService.danhDauDaXem(id);
            }
            
            model.addAttribute("phanAnh", phanAnh);
            return "admin/phan-anh/detail";
        } catch (Exception e) {
            return "redirect:/admin/phan-anh?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/{id}/phan-hoi")
    public String phanHoi(@PathVariable Integer id,
                          @RequestParam String noiDungPhanHoi,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        try {
            String cccd = authentication.getName();
            phanAnhService.phanHoi(id, noiDungPhanHoi, cccd);
            redirectAttributes.addFlashAttribute("success", "Phản hồi phản ánh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi phản hồi: " + e.getMessage());
        }
        return "redirect:/admin/phan-anh/" + id;
    }
    
    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            phanAnhService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa phản ánh thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/phan-anh";
    }
}

