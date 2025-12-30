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
@RequestMapping("/co-quan")
@PreAuthorize("hasAnyRole('BAN_QUAN_TRI', 'CO_QUAN_CHUC_NANG')")
public class CoQuanController {
    
    @Autowired
    private DoiTuongService doiTuongService;
    
    @Autowired
    private HoGiaDinhService hoGiaDinhService;
    
    @Autowired
    private BaoCaoSuCoService baoCaoSuCoService;
    
    @Autowired
    private ThongBaoService thongBaoService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Thống kê tổng quan cho cơ quan chức năng
        model.addAttribute("tongCuDan", doiTuongService.countCuDan());
        model.addAttribute("tongHoGiaDinh", hoGiaDinhService.countActiveHo());
        model.addAttribute("baoCaoMoi", baoCaoSuCoService.countNewReports());
        
        // Báo cáo sự cố gần đây (giới hạn 7 dòng mới nhất)
        java.util.List<com.apartment.entity.BaoCaoSuCo> allReports = baoCaoSuCoService.findPendingReports();
        java.util.List<com.apartment.entity.BaoCaoSuCo> recentReports = allReports.stream()
            .limit(7)
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("baoCaoSuCoList", recentReports);
        
        // Thông báo gần đây (giới hạn 7 dòng mới nhất)
        java.util.List<com.apartment.entity.ThongBao> allNotifications = thongBaoService.findAll();
        java.util.List<com.apartment.entity.ThongBao> recentNotifications = allNotifications.stream()
            .limit(7)
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("thongBaoList", recentNotifications);
        
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", "Cơ quan chức năng");
        return "co-quan/dashboard";
    }
    
    @GetMapping("/doi-tuong")
    public String danhSachCuDan(@org.springframework.web.bind.annotation.RequestParam(required = false) String search, 
                                Model model, 
                                Authentication authentication) {
        try {
            java.util.List<com.apartment.entity.DoiTuong> doiTuongList = doiTuongService.findAll();
            
            // Tìm kiếm nếu có
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                doiTuongList = doiTuongList.stream()
                    .filter(dt -> 
                        (dt.getHoVaTen() != null && dt.getHoVaTen().toLowerCase().contains(searchLower)) ||
                        (dt.getCccd() != null && dt.getCccd().toLowerCase().contains(searchLower)) ||
                        (dt.getSoDienThoai() != null && dt.getSoDienThoai().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("doiTuongList", doiTuongList);
            model.addAttribute("search", search);
            model.addAttribute("username", authentication.getName());
            return "co-quan/doi-tuong";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách cư dân");
            model.addAttribute("doiTuongList", new java.util.ArrayList<>());
            return "co-quan/doi-tuong";
        }
    }
    
    @GetMapping("/ho-gia-dinh")
    public String danhSachHoGiaDinh(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                                    Model model,
                                    Authentication authentication) {
        try {
            java.util.List<com.apartment.entity.HoGiaDinh> hoGiaDinhList = hoGiaDinhService.findAll();
            
            // Tìm kiếm nếu có
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                hoGiaDinhList = hoGiaDinhList.stream()
                    .filter(ho -> 
                        (ho.getMaHo() != null && ho.getMaHo().toLowerCase().contains(searchLower)) ||
                        (ho.getTenHo() != null && ho.getTenHo().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("hoGiaDinhList", hoGiaDinhList);
            model.addAttribute("search", search);
            model.addAttribute("username", authentication.getName());
            return "co-quan/ho-gia-dinh";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách hộ gia đình");
            model.addAttribute("hoGiaDinhList", new java.util.ArrayList<>());
            return "co-quan/ho-gia-dinh";
        }
    }
    
    @Autowired
    private com.apartment.repository.ThanhVienHoRepository thanhVienHoRepository;
    
    @GetMapping("/ho-gia-dinh/detail/{maHo}")
    public String chiTietHoGiaDinh(@org.springframework.web.bind.annotation.PathVariable String maHo,
                                   Model model,
                                   Authentication authentication) {
        try {
            // Lấy thông tin hộ gia đình
            com.apartment.entity.HoGiaDinh hoGiaDinh = hoGiaDinhService.findByMaHo(maHo)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hộ gia đình"));
            
            // Lấy danh sách thành viên
            java.util.List<com.apartment.entity.ThanhVienHo> thanhVienList = 
                thanhVienHoRepository.findActiveByMaHo(maHo);
            
            model.addAttribute("hoGiaDinh", hoGiaDinh);
            model.addAttribute("thanhVienList", thanhVienList);
            model.addAttribute("username", authentication.getName());
            return "co-quan/ho-gia-dinh-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải thông tin hộ gia đình: " + e.getMessage());
            return "redirect:/co-quan/ho-gia-dinh";
        }
    }
    
    @GetMapping("/bao-cao-su-co")
    public String baoCaoSuCo(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                             Model model,
                             Authentication authentication) {
        try {
            java.util.List<com.apartment.entity.BaoCaoSuCo> baoCaoList = baoCaoSuCoService.findAll();
            
            // Tìm kiếm nếu có
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                baoCaoList = baoCaoList.stream()
                    .filter(bc -> 
                        (bc.getTieuDe() != null && bc.getTieuDe().toLowerCase().contains(searchLower)) ||
                        (bc.getTrangThai() != null && bc.getTrangThai().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("baoCaoList", baoCaoList);
            model.addAttribute("search", search);
            model.addAttribute("username", authentication.getName());
            return "co-quan/bao-cao-su-co";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách báo cáo");
            model.addAttribute("baoCaoList", new java.util.ArrayList<>());
            return "co-quan/bao-cao-su-co";
        }
    }
    
    @GetMapping("/thong-bao")
    public String thongBao(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                           Model model,
                           Authentication authentication) {
        try {
            java.util.List<com.apartment.entity.ThongBao> thongBaoList = thongBaoService.findAll();
            
            // Tìm kiếm nếu có
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                thongBaoList = thongBaoList.stream()
                    .filter(tb -> 
                        (tb.getTieuDe() != null && tb.getTieuDe().toLowerCase().contains(searchLower)) ||
                        (tb.getNoiDungThongBao() != null && tb.getNoiDungThongBao().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("thongBaoList", thongBaoList);
            model.addAttribute("search", search);
            model.addAttribute("username", authentication.getName());
            return "co-quan/thong-bao";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách thông báo");
            model.addAttribute("thongBaoList", new java.util.ArrayList<>());
            return "co-quan/thong-bao";
        }
    }
}

