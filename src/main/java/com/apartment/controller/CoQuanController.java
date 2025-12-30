package com.apartment.controller;

import com.apartment.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    public String danhSachCuDan(@RequestParam(required = false) String search,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size,
                                Model model, 
                                Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<com.apartment.entity.DoiTuong> doiTuongPage;
            
            if (search != null && !search.trim().isEmpty()) {
                doiTuongPage = doiTuongService.searchByKeyword(search, pageable);
            } else {
                doiTuongPage = doiTuongService.findAll(pageable);
            }
            
            model.addAttribute("doiTuongList", doiTuongPage.getContent());
            model.addAttribute("doiTuongPage", doiTuongPage);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", doiTuongPage.getTotalPages());
            model.addAttribute("totalElements", doiTuongPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            return "co-quan/doi-tuong";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách cư dân");
            model.addAttribute("doiTuongList", new java.util.ArrayList<>());
            return "co-quan/doi-tuong";
        }
    }
    
    @GetMapping("/ho-gia-dinh")
    public String danhSachHoGiaDinh(@RequestParam(required = false) String search,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    Model model,
                                    Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<com.apartment.entity.HoGiaDinh> hoGiaDinhPage;
            
            if (search != null && !search.trim().isEmpty()) {
                hoGiaDinhPage = hoGiaDinhService.searchByName(search, pageable);
            } else {
                hoGiaDinhPage = hoGiaDinhService.findAll(pageable);
            }
            
            model.addAttribute("hoGiaDinhList", hoGiaDinhPage.getContent());
            model.addAttribute("hoGiaDinhPage", hoGiaDinhPage);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", hoGiaDinhPage.getTotalPages());
            model.addAttribute("totalElements", hoGiaDinhPage.getTotalElements());
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
    public String baoCaoSuCo(@RequestParam(required = false) String search,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             Model model,
                             Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<com.apartment.entity.BaoCaoSuCo> baoCaoPage;
            
            if (search != null && !search.trim().isEmpty()) {
                java.util.List<com.apartment.entity.BaoCaoSuCo> allBaoCao = baoCaoSuCoService.findAll();
                String searchLower = search.trim().toLowerCase();
                java.util.List<com.apartment.entity.BaoCaoSuCo> filteredList = allBaoCao.stream()
                    .filter(bc -> 
                        (bc.getTieuDe() != null && bc.getTieuDe().toLowerCase().contains(searchLower)) ||
                        (bc.getTrangThai() != null && bc.getTrangThai().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
                
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), filteredList.size());
                java.util.List<com.apartment.entity.BaoCaoSuCo> pageContent = start < filteredList.size() 
                    ? filteredList.subList(start, end) 
                    : java.util.Collections.emptyList();
                
                baoCaoPage = new PageImpl<>(pageContent, pageable, filteredList.size());
            } else {
                baoCaoPage = baoCaoSuCoService.findAll(pageable);
            }
            
            model.addAttribute("baoCaoList", baoCaoPage.getContent());
            model.addAttribute("baoCaoPage", baoCaoPage);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", baoCaoPage.getTotalPages());
            model.addAttribute("totalElements", baoCaoPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            return "co-quan/bao-cao-su-co";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách báo cáo");
            model.addAttribute("baoCaoList", new java.util.ArrayList<>());
            return "co-quan/bao-cao-su-co";
        }
    }
    
    @GetMapping("/thong-bao")
    public String thongBao(@RequestParam(required = false) String search,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           Model model,
                           Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<com.apartment.entity.ThongBao> thongBaoPage;
            
            if (search != null && !search.trim().isEmpty()) {
                thongBaoPage = thongBaoService.searchByKeyword(search, pageable);
            } else {
                thongBaoPage = thongBaoService.findAll(pageable);
            }
            
            model.addAttribute("thongBaoList", thongBaoPage.getContent());
            model.addAttribute("thongBaoPage", thongBaoPage);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", thongBaoPage.getTotalPages());
            model.addAttribute("totalElements", thongBaoPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            return "co-quan/thong-bao";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách thông báo");
            model.addAttribute("thongBaoList", new java.util.ArrayList<>());
            return "co-quan/thong-bao";
        }
    }
}

