package com.apartment.controller;

import com.apartment.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class AdminController {
    
    @Autowired
    private DoiTuongService doiTuongService;
    
    @Autowired
    private HoGiaDinhService hoGiaDinhService;
    
    @Autowired
    private BaoCaoSuCoService baoCaoSuCoService;
    
    @Autowired
    private HoaDonService hoaDonService;
    
    @Autowired
    private ThongBaoService thongBaoService;
    
    @Autowired
    private PhanAnhService phanAnhService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Thống kê tổng quan
        model.addAttribute("tongCuDan", doiTuongService.countCuDan());
        model.addAttribute("tongHoGiaDinh", hoGiaDinhService.countActiveHo());
        model.addAttribute("baoCaoMoi", baoCaoSuCoService.countNewReports());
        model.addAttribute("tongThuNhap", hoaDonService.sumPaidAmount());
        model.addAttribute("congNoConLai", hoaDonService.sumUnpaidAmount());
        
        // Báo cáo sự cố mới nhất
        model.addAttribute("baoCaoSuCoList", baoCaoSuCoService.findPendingReports());
        
        // Hóa đơn - chart data
        model.addAttribute("hoaDonDaThanhToan", hoaDonService.countPaidInvoices());
        model.addAttribute("hoaDonChuaThanhToan", hoaDonService.countUnpaidInvoices());
        
        // Thông báo mới nhất
        model.addAttribute("thongBaoList", thongBaoService.findAllVisible());
        
        // Phản ánh mới nhất
        model.addAttribute("phanAnhMoi", phanAnhService.countMoi());
        model.addAttribute("phanAnhList", phanAnhService.findMoi());
        
        model.addAttribute("username", authentication.getName());
        return "admin/dashboard";
    }
}


