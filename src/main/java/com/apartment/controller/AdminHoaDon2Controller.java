package com.apartment.controller;

import com.apartment.entity.HoaDon;
import com.apartment.entity.ChiSoDienNuoc;
import com.apartment.service.HoaDonService;
import com.apartment.service.HoGiaDinhService;
import com.apartment.service.ChiSoDienNuocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don-2")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class AdminHoaDon2Controller {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoGiaDinhService hoGiaDinhService;

    @Autowired
    private ChiSoDienNuocService chiSoService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "20") int size,
                      Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HoaDon> hoaDonPage;
        
        if (search != null && !search.trim().isEmpty()) {
            hoaDonPage = hoaDonService.searchByKeyword(search, pageable);
        } else {
            hoaDonPage = hoaDonService.findAll(pageable);
        }
        
        model.addAttribute("hoaDonList", hoaDonPage.getContent());
        model.addAttribute("hoaDonPage", hoaDonPage);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", hoaDonPage.getTotalPages());
        model.addAttribute("totalElements", hoaDonPage.getTotalElements());
        return "admin/hoa-don-2/list";
    }

    @GetMapping("/tao-hoa-don")
    public String taoHoaDon(@RequestParam String loai, Model model) {
        model.addAttribute("hoGiaDinhList", hoGiaDinhService.findAll());
        model.addAttribute("loaiHoaDon", loai);
        return "admin/hoa-don-2/tao-hoa-don";
    }

    @PostMapping("/luu")
    public String luuHoaDon(@RequestParam String maHo,
                            @RequestParam String loaiHoaDon,
                            @RequestParam(required = false) BigDecimal soTien,
                            @RequestParam(required = false) BigDecimal soTienDienNuoc,
                            @RequestParam LocalDate hanThanhToan,
                            @RequestParam(required = false) String ghiChu,
                            @RequestParam(required = false) String moTa,
                            @RequestParam(required = false) String kyThanhToan,
                            @RequestParam(required = false) Integer dienCu,
                            @RequestParam(required = false) Integer dienMoi,
                            @RequestParam(required = false) Integer nuocCu,
                            @RequestParam(required = false) Integer nuocMoi,
                            RedirectAttributes ra) {
        try {
            if (maHo == null || maHo.trim().isEmpty()) {
                ra.addFlashAttribute("error", "Vui lòng chọn mã hộ gia đình!");
                return "redirect:/admin/hoa-don-2";
            }

            BigDecimal finalSoTien;
            if ("dien_nuoc".equals(loaiHoaDon)) {
                finalSoTien = soTienDienNuoc != null ? soTienDienNuoc : BigDecimal.ZERO;
            } else {
                finalSoTien = soTien != null ? soTien : BigDecimal.ZERO;
            }

            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHo(maHo);
            hoaDon.setSoTien(finalSoTien);
            hoaDon.setHanThanhToan(hanThanhToan);
            hoaDon.setTrangThai("chua_thanh_toan");

            String finalGhiChu = ghiChu;
            if ("dien_nuoc".equals(loaiHoaDon)) {
                hoaDon.setLoaiHoaDon("dien_nuoc");
                finalGhiChu = "Hóa đơn điện nước - Kỳ: " + kyThanhToan;
                if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                    finalGhiChu += " - " + ghiChu;
                }
            } else {
                hoaDon.setLoaiHoaDon(loaiHoaDon != null ? loaiHoaDon : "khac");
                if (moTa != null && !moTa.trim().isEmpty()) {
                    finalGhiChu = moTa;
                    if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                        finalGhiChu += " - " + ghiChu;
                    }
                }
            }
            hoaDon.setGhiChu(finalGhiChu);

            hoaDonService.save(hoaDon);

            if ("dien_nuoc".equals(loaiHoaDon) && kyThanhToan != null &&
                dienCu != null && dienMoi != null && nuocCu != null && nuocMoi != null) {
                ChiSoDienNuoc chiSo = new ChiSoDienNuoc();
                chiSo.setMaHo(maHo);
                chiSo.setMaHoaDon(hoaDon.getMaHoaDon());
                chiSo.setKyThanhToan(kyThanhToan);
                chiSo.setDienCu(dienCu);
                chiSo.setDienMoi(dienMoi);
                chiSo.setNuocCu(nuocCu);
                chiSo.setNuocMoi(nuocMoi);
                chiSo.setTienDichVu(finalSoTien);
                chiSoService.save(chiSo);
            }

            ra.addFlashAttribute("success", "Tạo hóa đơn thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi khi tạo hóa đơn: " + e.getMessage());
        }
        return "redirect:/admin/hoa-don-2";
    }

    @PostMapping("/xoa/{id}")
    public String xoaHoaDon(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            hoaDonService.delete(id);
            ra.addFlashAttribute("success", "Xóa hóa đơn thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi: " + e.getMessage());
        }
        return "redirect:/admin/hoa-don-2";
    }
}


