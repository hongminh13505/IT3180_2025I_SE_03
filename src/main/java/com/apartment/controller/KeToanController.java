package com.apartment.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.apartment.entity.ChiSoDienNuoc;
import com.apartment.entity.HoaDon;
import com.apartment.service.ChiSoDienNuocService;
import com.apartment.service.HoGiaDinhService;
import com.apartment.service.HoaDonExportService;
import com.apartment.service.HoaDonExportService.ImportResult;
import com.apartment.service.HoaDonService;
import com.apartment.service.DangKyDichVuService;
import com.apartment.service.DichVuService;
import com.apartment.service.DoiTuongService;
import com.apartment.entity.DangKyDichVu;
import com.apartment.entity.DichVu;
import com.apartment.entity.DoiTuong;
import com.apartment.repository.ThanhVienHoRepository;
import com.apartment.entity.ThanhVienHo;

@Controller
@RequestMapping("/ke-toan")
@PreAuthorize("hasAnyRole('BAN_QUAN_TRI', 'KE_TOAN')")
public class KeToanController {
    
    @Autowired
    private HoaDonService hoaDonService;
    
    @Autowired
    private HoGiaDinhService hoGiaDinhService;
    
    @Autowired
    private ChiSoDienNuocService chiSoService;
    
    @Autowired
    private HoaDonExportService hoaDonExportService;
    
    @Autowired
    private DangKyDichVuService dangKyDichVuService;
    
    @Autowired
    private DichVuService dichVuService;
    
    @Autowired
    private ThanhVienHoRepository thanhVienHoRepository;
    
    @Autowired
    private DoiTuongService doiTuongService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("tongThuNhap", hoaDonService.sumPaidAmount());
        model.addAttribute("congNoConLai", hoaDonService.sumUnpaidAmount());
        model.addAttribute("tongHoGiaDinh", hoGiaDinhService.countActiveHo());

        java.util.List<com.apartment.entity.HoaDon> recent = hoaDonService.findRecentInvoices();
        if (recent.size() > 7) {
            recent = recent.subList(0, 7);
        }
        model.addAttribute("recentInvoices", recent);
        model.addAttribute("hoaDonDaThanhToan", hoaDonService.countPaidInvoices());
        model.addAttribute("hoaDonChuaThanhToan", hoaDonService.countUnpaidInvoices());

        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", "Kế toán");
        return "ke-toan/dashboard";
    }
    
    @GetMapping("/chi-so")
    public String chiSoPage(@RequestParam(required = false) String maHo,
                            @RequestParam(required = false) String ky,
                            Model model) {
        model.addAttribute("hoList", hoGiaDinhService.findAll());
        model.addAttribute("maHo", maHo);
        model.addAttribute("ky", ky);
        model.addAttribute("records", chiSoService.search(maHo, ky));
        return "ke-toan/chi-so/list";
    }
    
    @PostMapping("/chi-so/save")
    public String saveChiSo(@RequestParam String maHo,
                            @RequestParam String ky,
                            @RequestParam Integer dienCu,
                            @RequestParam Integer dienMoi,
                            @RequestParam Integer nuocCu,
                            @RequestParam Integer nuocMoi,
                            @RequestParam(required = false, defaultValue = "0") java.math.BigDecimal tienDichVu,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        try {
            com.apartment.entity.ChiSoDienNuoc cs = new com.apartment.entity.ChiSoDienNuoc();
            cs.setMaHo(maHo);
            cs.setKyThanhToan(ky);
            cs.setDienCu(dienCu);
            cs.setDienMoi(dienMoi);
            cs.setNuocCu(nuocCu);
            cs.setNuocMoi(nuocMoi);
            cs.setTienDichVu(tienDichVu);
            chiSoService.save(cs);
            ra.addFlashAttribute("success", "Lưu chỉ số thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi khi lưu: " + e.getMessage());
        }
        return "redirect:/ke-toan/chi-so?maHo=" + maHo + "&ky=" + ky;
    }
    
    @GetMapping("/quan-ly-hoa-don")
    public String quanLyHoaDon(@RequestParam(required = false) String search,
                               @RequestParam(required = false) String loaiHoaDon,
                               @RequestParam(required = false) String trangThai,
                               @RequestParam(required = false) String tuNgay,
                               @RequestParam(required = false) String denNgay,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               Model model,
                               Authentication authentication) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<HoaDon> hoaDonPage;
            
            java.time.LocalDate tuNgayDate = null;
            java.time.LocalDate denNgayDate = null;
            
            try {
                if (tuNgay != null && !tuNgay.trim().isEmpty()) {
                    tuNgayDate = java.time.LocalDate.parse(tuNgay);
                }
                if (denNgay != null && !denNgay.trim().isEmpty()) {
                    denNgayDate = java.time.LocalDate.parse(denNgay);
                }
            } catch (Exception e) {
            }

            boolean hasSearch = search != null && !search.trim().isEmpty();
            boolean hasFilter = (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) ||
                               (trangThai != null && !trangThai.trim().isEmpty()) ||
                               tuNgayDate != null || denNgayDate != null;

            if (hasSearch || hasFilter) {
                hoaDonPage = hoaDonService.searchAndFilter(search, loaiHoaDon, trangThai, tuNgayDate, denNgayDate, pageable);
            } else {
                hoaDonPage = hoaDonService.findAll(pageable);
            }
            
            model.addAttribute("hoaDonList", hoaDonPage.getContent());
            model.addAttribute("hoaDonPage", hoaDonPage);
            model.addAttribute("search", search);
            model.addAttribute("loaiHoaDon", loaiHoaDon);
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("tuNgay", tuNgay);
            model.addAttribute("denNgay", denNgay);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", hoaDonPage.getTotalPages());
            model.addAttribute("totalElements", hoaDonPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            
            return "ke-toan/quan-ly-hoa-don";
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("hoaDonList", new java.util.ArrayList<>());
            model.addAttribute("error", "Lỗi khi tải danh sách hóa đơn");
            return "ke-toan/quan-ly-hoa-don";
        }
    }
    
    @GetMapping("/quan-ly-hoa-don/tao-moi")
    public String taoHoaDonMoi(Model model) {
        try {
            HoaDon hoaDon = new HoaDon();
            hoaDon.setTrangThai("chua_thanh_toan");
            model.addAttribute("hoaDon", hoaDon);
            model.addAttribute("hoGiaDinhList", hoGiaDinhService.findAll());
            model.addAttribute("isEdit", false);
            return "ke-toan/tao-hoa-don";
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return "redirect:/ke-toan/quan-ly-hoa-don";
        }
    }
    
    @GetMapping("/quan-ly-hoa-don/sua/{id}")
    public String suaHoaDon(@PathVariable Integer id, Model model) {
        try {
            HoaDon hoaDon = hoaDonService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            model.addAttribute("hoaDon", hoaDon);
            model.addAttribute("hoGiaDinhList", hoGiaDinhService.findAll());
            model.addAttribute("isEdit", true);
            return "ke-toan/tao-hoa-don";
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return "redirect:/ke-toan/quan-ly-hoa-don";
        }
    }
    
    @PostMapping("/quan-ly-hoa-don/luu")
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
            System.out.println("=== DEBUG: Bắt đầu lưu hóa đơn ===");
            System.out.println("maHo: " + maHo);
            System.out.println("loaiHoaDon: " + loaiHoaDon);
            System.out.println("soTien: " + soTien);
            System.out.println("soTienDienNuoc: " + soTienDienNuoc);
            System.out.println("hanThanhToan: " + hanThanhToan);
            System.out.println("ghiChu: " + ghiChu);
            System.out.println("moTa: " + moTa);
            System.out.println("kyThanhToan: " + kyThanhToan);
            
            if (maHo == null || maHo.trim().isEmpty()) {
                ra.addFlashAttribute("error", "Vui lòng chọn mã hộ gia đình!");
                return "redirect:/ke-toan/quan-ly-hoa-don";
            }
        
            BigDecimal finalSoTien;
            if ("dien_nuoc".equals(loaiHoaDon)) {
                finalSoTien = soTienDienNuoc != null ? soTienDienNuoc : BigDecimal.ZERO;
            } else {
                finalSoTien = soTien != null ? soTien : BigDecimal.ZERO;
            }
            
            System.out.println("=== DEBUG: Số tiền cuối cùng: " + finalSoTien);
            
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
     
            System.out.println("=== DEBUG: Lưu hóa đơn ===");
            System.out.println("loaiHoaDon: " + loaiHoaDon);
            System.out.println("hoaDon.loaiHoaDon: " + hoaDon.getLoaiHoaDon());
            System.out.println("Hóa đơn: " + hoaDon.toString());
            hoaDonService.save(hoaDon);
            System.out.println("=== DEBUG: Đã lưu hóa đơn thành công, maHoaDon: " + hoaDon.getMaHoaDon() + " ===");
           
     
            if ("dien_nuoc".equals(loaiHoaDon) && kyThanhToan != null && 
                dienCu != null && dienMoi != null && nuocCu != null && nuocMoi != null) {
                
                System.out.println("=== DEBUG: Lưu chỉ số điện nước ===");
                ChiSoDienNuoc chiSo = new ChiSoDienNuoc();
                chiSo.setMaHo(maHo);
                chiSo.setMaHoaDon(hoaDon.getMaHoaDon());
                chiSo.setKyThanhToan(kyThanhToan);
                chiSo.setDienCu(dienCu);
                chiSo.setDienMoi(dienMoi);
                chiSo.setNuocCu(nuocCu);
                chiSo.setNuocMoi(nuocMoi);
                chiSo.setTienDichVu(finalSoTien);
                
                System.out.println("Chỉ số: " + chiSo.toString());
                chiSoService.save(chiSo);
                System.out.println("=== DEBUG: Đã lưu chỉ số điện nước thành công ===");
            }

            
            String loaiHoaDonName = getLoaiHoaDonName(loaiHoaDon);
            ra.addFlashAttribute("success", "Tạo hóa đơn " + loaiHoaDonName + " thành công! Tổng tiền: " + 
                               finalSoTien.toPlainString() + " VNĐ");
        } catch (Exception e) {
            System.err.println("=== DEBUG: Lỗi khi lưu hóa đơn ===");
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Có lỗi khi tạo hóa đơn: " + e.getMessage());
        }
        return "redirect:/ke-toan/quan-ly-hoa-don";
    }
    
    private String getLoaiHoaDonName(String loaiHoaDon) {
        switch (loaiHoaDon) {
            case "dien_nuoc": return "điện nước";
            case "dich_vu": return "dịch vụ";
            case "sua_chua": return "sửa chữa";
            case "phat": return "phạt";
            case "khac": return "khác";
            default: return loaiHoaDon;
        }
    }
    
    @PostMapping("/quan-ly-hoa-don/xoa/{id}")
    public String xoaHoaDon(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            hoaDonService.delete(id);
            ra.addFlashAttribute("success", "Xóa hóa đơn thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi: " + e.getMessage());
        }
        return "redirect:/ke-toan/quan-ly-hoa-don";
    }
    
    @GetMapping("/quan-ly-hoa-don/tao-hoa-don")
    public String taoHoaDon(@RequestParam String loai, Model model, Authentication authentication) {
        try {
            model.addAttribute("hoGiaDinhList", hoGiaDinhService.findAll());
            model.addAttribute("username", authentication.getName());
            model.addAttribute("loaiHoaDon", loai);
            return "ke-toan/tao-hoa-don";
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            return "redirect:/ke-toan/quan-ly-hoa-don";
        }
    }
    
    /**
     * Export danh sách hóa đơn ra file Excel
     */
    @GetMapping("/quan-ly-hoa-don/export/excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestParam(required = false) String search,
                                                @RequestParam(required = false) String loaiHoaDon,
                                                @RequestParam(required = false) String trangThai,
                                                @RequestParam(required = false) String tuNgay,
                                                @RequestParam(required = false) String denNgay) {
        try {
            java.util.List<HoaDon> hoaDonList;
            
            java.time.LocalDate tuNgayDate = null;
            java.time.LocalDate denNgayDate = null;
            
            try {
                if (tuNgay != null && !tuNgay.trim().isEmpty()) {
                    tuNgayDate = java.time.LocalDate.parse(tuNgay);
                }
                if (denNgay != null && !denNgay.trim().isEmpty()) {
                    denNgayDate = java.time.LocalDate.parse(denNgay);
                }
            } catch (Exception e) {
            }

            boolean hasSearch = search != null && !search.trim().isEmpty();
            boolean hasFilter = (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) ||
                               (trangThai != null && !trangThai.trim().isEmpty()) ||
                               tuNgayDate != null || denNgayDate != null;

            if (hasSearch || hasFilter) {
                hoaDonList = hoaDonService.searchAndFilter(search, loaiHoaDon, trangThai, tuNgayDate, denNgayDate, org.springframework.data.domain.Pageable.unpaged()).getContent();
            } else {
                hoaDonList = hoaDonService.findAll(org.springframework.data.domain.Pageable.unpaged()).getContent();
            }
            
            byte[] excelBytes = hoaDonExportService.exportToExcel(hoaDonList);
            
            String filename = "hoa_don_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        } catch (Exception e) {
            System.err.println("Lỗi khi export Excel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Export danh sách hóa đơn ra file PDF
     */
    @GetMapping("/quan-ly-hoa-don/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(@RequestParam(required = false) String search,
                                              @RequestParam(required = false) String loaiHoaDon,
                                              @RequestParam(required = false) String trangThai,
                                              @RequestParam(required = false) String tuNgay,
                                              @RequestParam(required = false) String denNgay) {
        try {
            java.util.List<HoaDon> hoaDonList;
            
            java.time.LocalDate tuNgayDate = null;
            java.time.LocalDate denNgayDate = null;
            
            try {
                if (tuNgay != null && !tuNgay.trim().isEmpty()) {
                    tuNgayDate = java.time.LocalDate.parse(tuNgay);
                }
                if (denNgay != null && !denNgay.trim().isEmpty()) {
                    denNgayDate = java.time.LocalDate.parse(denNgay);
                }
            } catch (Exception e) {
            }

            boolean hasSearch = search != null && !search.trim().isEmpty();
            boolean hasFilter = (loaiHoaDon != null && !loaiHoaDon.trim().isEmpty()) ||
                               (trangThai != null && !trangThai.trim().isEmpty()) ||
                               tuNgayDate != null || denNgayDate != null;

            if (hasSearch || hasFilter) {
                hoaDonList = hoaDonService.searchAndFilter(search, loaiHoaDon, trangThai, tuNgayDate, denNgayDate, org.springframework.data.domain.Pageable.unpaged()).getContent();
            } else {
                hoaDonList = hoaDonService.findAll(org.springframework.data.domain.Pageable.unpaged()).getContent();
            }
            
            byte[] pdfBytes = hoaDonExportService.exportToPdf(hoaDonList);
            
            String filename = "hoa_don_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            System.err.println("Lỗi khi export PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Download template Excel để import
     */
    @GetMapping("/quan-ly-hoa-don/import/template")
    public ResponseEntity<byte[]> downloadImportTemplate() {
        try {
            byte[] templateBytes = hoaDonExportService.createTemplateExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "template_import_hoa_don.xlsx");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo template: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Import hóa đơn từ file Excel
     */
    @PostMapping("/quan-ly-hoa-don/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        try {
            if (file.isEmpty()) {
                ra.addFlashAttribute("error", "Vui lòng chọn file để import!");
                return "redirect:/ke-toan/quan-ly-hoa-don";
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                ra.addFlashAttribute("error", "Vui lòng chọn file Excel (.xlsx hoặc .xls)!");
                return "redirect:/ke-toan/quan-ly-hoa-don";
            }
            
            ImportResult result = hoaDonExportService.importFromExcel(file);
            
            StringBuilder message = new StringBuilder();
            message.append("Import thành công ").append(result.getSuccessCount()).append(" hóa đơn.");
            
            if (result.getErrorCount() > 0) {
                message.append(" Có ").append(result.getErrorCount()).append(" lỗi.");
                ra.addFlashAttribute("importErrors", result.getErrors());
            }
            
            ra.addFlashAttribute("success", message.toString());
            
        } catch (Exception e) {
            System.err.println("Lỗi khi import: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi khi import file: " + e.getMessage());
        }
        
        return "redirect:/ke-toan/quan-ly-hoa-don";
    }
    
    @GetMapping("/dang-ky-dich-vu")
    public String danhSachDangKyDichVu(@RequestParam(required = false) String trangThai,
                                       @RequestParam(required = false) String search,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       Model model,
                                       Authentication authentication) {
        try {
            List<DangKyDichVu> allDangKy;
            
            if (trangThai != null && !trangThai.trim().isEmpty()) {
                allDangKy = dangKyDichVuService.findByTrangThai(trangThai);
            } else {
                allDangKy = dangKyDichVuService.findAll();
            }
            
            // Load thông tin dịch vụ và cư dân trước khi filter
            Map<Integer, DichVu> dichVuMap = new HashMap<>();
            Map<String, DoiTuong> nguoiDungMap = new HashMap<>();
            for (DangKyDichVu dk : allDangKy) {
                if (dk.getMaDichVu() != null && !dichVuMap.containsKey(dk.getMaDichVu())) {
                    dichVuService.findById(dk.getMaDichVu()).ifPresent(dv -> dichVuMap.put(dk.getMaDichVu(), dv));
                }
                if (dk.getCccdNguoiDung() != null && !nguoiDungMap.containsKey(dk.getCccdNguoiDung())) {
                    doiTuongService.findByCccd(dk.getCccdNguoiDung()).ifPresent(dt -> nguoiDungMap.put(dk.getCccdNguoiDung(), dt));
                }
            }
            
            // Filter theo search nếu có
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                allDangKy = allDangKy.stream()
                    .filter(dk -> {
                        // Tìm theo CCCD
                        if (dk.getCccdNguoiDung() != null && dk.getCccdNguoiDung().toLowerCase().contains(searchLower)) {
                            return true;
                        }
                        // Tìm theo tên cư dân
                        if (nguoiDungMap.containsKey(dk.getCccdNguoiDung())) {
                            DoiTuong dt = nguoiDungMap.get(dk.getCccdNguoiDung());
                            if (dt.getHoVaTen() != null && dt.getHoVaTen().toLowerCase().contains(searchLower)) {
                                return true;
                            }
                        }
                        // Tìm theo tên dịch vụ
                        if (dichVuMap.containsKey(dk.getMaDichVu())) {
                            DichVu dv = dichVuMap.get(dk.getMaDichVu());
                            if (dv.getTenDichVu() != null && dv.getTenDichVu().toLowerCase().contains(searchLower)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("dichVuMap", dichVuMap);
            model.addAttribute("nguoiDungMap", nguoiDungMap);
            
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allDangKy.size());
            List<DangKyDichVu> pageContent = start < allDangKy.size() 
                ? allDangKy.subList(start, end) 
                : java.util.Collections.emptyList();
            
            Page<DangKyDichVu> dangKyPage = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, allDangKy.size());
            
            model.addAttribute("dangKyList", dangKyPage.getContent());
            model.addAttribute("dangKyPage", dangKyPage);
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", dangKyPage.getTotalPages());
            model.addAttribute("totalElements", dangKyPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            return "ke-toan/dang-ky-dich-vu";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách đăng ký dịch vụ");
            model.addAttribute("dangKyList", new java.util.ArrayList<>());
            return "ke-toan/dang-ky-dich-vu";
        }
    }
    
    @PostMapping("/dang-ky-dich-vu/duyet/{id}")
    public String duyetDangKyDichVu(@PathVariable Integer id,
                                    @RequestParam(required = false) String ghiChu,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {
        try {
            DangKyDichVu dangKy = dangKyDichVuService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đăng ký dịch vụ"));
            
            if (!"cho_duyet".equals(dangKy.getTrangThai())) {
                redirectAttributes.addFlashAttribute("error", "Đăng ký này đã được xử lý");
                return "redirect:/ke-toan/dang-ky-dich-vu";
            }
            
            // Duyệt đăng ký
            dangKy.setTrangThai("da_duyet");
            dangKy.setCccdNguoiDuyet(authentication.getName());
            dangKy.setNgayDuyet(LocalDateTime.now());
            if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                dangKy.setGhiChu(ghiChu);
            }
            dangKyDichVuService.save(dangKy);
            
            // Tạo hóa đơn dịch vụ
            try {
                DichVu dichVu = dichVuService.findById(dangKy.getMaDichVu())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));
                
                // Lấy mã hộ từ cư dân
                String maHo = getMaHoByCccd(dangKy.getCccdNguoiDung());
                if (maHo == null) {
                    redirectAttributes.addFlashAttribute("error", "Cư dân chưa được gán vào hộ gia đình");
                    return "redirect:/ke-toan/dang-ky-dich-vu";
                }
                
                HoaDon hoaDon = new HoaDon();
                hoaDon.setMaHo(maHo);
                hoaDon.setLoaiHoaDon("dich_vu"); // Đảm bảo loại hóa đơn là "dịch vụ"
                hoaDon.setMaDichVu(dangKy.getMaDichVu()); // Liên kết với dịch vụ
                hoaDon.setSoTien(dichVu.getGiaThanh());
                hoaDon.setHanThanhToan(LocalDate.now().plusDays(30));
                hoaDon.setTrangThai("chua_thanh_toan");
                hoaDon.setGhiChu("Hóa đơn dịch vụ: " + dichVu.getTenDichVu() + 
                    (dangKy.getMoTaYeuCau() != null && !dangKy.getMoTaYeuCau().equals("1") ? " - " + dangKy.getMoTaYeuCau() : ""));
                hoaDonService.save(hoaDon);
                
                redirectAttributes.addFlashAttribute("success", 
                    "Duyệt đăng ký thành công và đã tạo hóa đơn dịch vụ!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("warning", 
                    "Đã duyệt đăng ký nhưng có lỗi khi tạo hóa đơn: " + e.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi duyệt đăng ký: " + e.getMessage());
        }
        return "redirect:/ke-toan/dang-ky-dich-vu";
    }
    
    @PostMapping("/dang-ky-dich-vu/tu-choi/{id}")
    public String tuChoiDangKyDichVu(@PathVariable Integer id,
                                     @RequestParam(required = false) String ghiChu,
                                     RedirectAttributes redirectAttributes,
                                     Authentication authentication) {
        try {
            DangKyDichVu dangKy = dangKyDichVuService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đăng ký dịch vụ"));
            
            dangKy.setTrangThai("da_tu_choi");
            dangKy.setCccdNguoiDuyet(authentication.getName());
            dangKy.setNgayDuyet(LocalDateTime.now());
            if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                dangKy.setGhiChu(ghiChu);
            }
            dangKyDichVuService.save(dangKy);
            
            redirectAttributes.addFlashAttribute("success", "Từ chối đăng ký thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi từ chối đăng ký: " + e.getMessage());
        }
        return "redirect:/ke-toan/dang-ky-dich-vu";
    }
    
    private String getMaHoByCccd(String cccd) {
        List<ThanhVienHo> thanhVienList = thanhVienHoRepository.findActiveByCccd(cccd);
        if (thanhVienList.isEmpty()) {
            return null;
        }
        return thanhVienList.get(0).getMaHo();
    }
    
}

