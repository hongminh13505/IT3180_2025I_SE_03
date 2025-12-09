package com.apartment.controller;

import com.apartment.entity.YeuCauGuiXe;
import com.apartment.service.DoiTuongService;
import com.apartment.service.YeuCauGuiXeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/gui-xe")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class AdminGuiXeController {

    @Autowired
    private YeuCauGuiXeService yeuCauGuiXeService;

    @Autowired
    private DoiTuongService doiTuongService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String trangThai,
                       Model model,
                       Authentication authentication) {
        try {
            List<YeuCauGuiXe> list;
            if (trangThai != null && !trangThai.isEmpty()) {
                list = yeuCauGuiXeService.findByTrangThaiWithNguoi(trangThai);
            } else {
                list = yeuCauGuiXeService.findAllWithNguoi();
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                list = list.stream()
                        .filter(x ->
                                (x.getBienSo() != null && x.getBienSo().toLowerCase().contains(searchLower)) ||
                                (x.getCccdNguoiGui() != null && x.getCccdNguoiGui().toLowerCase().contains(searchLower)) ||
                                (x.getLoaiXe() != null && x.getLoaiXe().toLowerCase().contains(searchLower)))
                        .collect(Collectors.toList());
            }

            model.addAttribute("yeuCauList", list);
            model.addAttribute("search", search);
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("username", authentication.getName());
            return "admin/gui-xe/list";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách gửi xe: " + e.getMessage());
            model.addAttribute("yeuCauList", new java.util.ArrayList<>());
            return "admin/gui-xe/list";
        }
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Integer id,
                         Model model,
                         Authentication authentication) {
        try {
            YeuCauGuiXe yeuCau = yeuCauGuiXeService.findWithNguoiById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu gửi xe"));

            model.addAttribute("yeuCau", yeuCau);
            model.addAttribute("username", authentication.getName());
            model.addAttribute("cuDanList", doiTuongService.findAllActiveCuDan());
            return "admin/gui-xe/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải chi tiết yêu cầu: " + e.getMessage());
            return "redirect:/admin/gui-xe";
        }
    }

    @PostMapping("/xu-ly/{id}")
    public String xuLy(@PathVariable Integer id,
                       @RequestParam("action") String action,
                       @RequestParam(required = false) String ghiChuXuLy,
                       RedirectAttributes redirectAttributes,
                       Authentication authentication) {
        try {
            YeuCauGuiXe yeuCau = yeuCauGuiXeService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu gửi xe"));

            if ("approve".equals(action)) {
                yeuCau.setTrangThai("da_duyet");
            } else if ("reject".equals(action)) {
                yeuCau.setTrangThai("tu_choi");
            } else {
                throw new IllegalArgumentException("Hành động không hợp lệ");
            }

            yeuCau.setGhiChuXuLy(ghiChuXuLy);
            yeuCau.setCccdNguoiXuLy(authentication.getName());
            yeuCau.setNgayXuLy(LocalDateTime.now());

            yeuCauGuiXeService.save(yeuCau);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái yêu cầu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi xử lý yêu cầu: " + e.getMessage());
        }
        return "redirect:/admin/gui-xe/detail/" + id;
    }
}

