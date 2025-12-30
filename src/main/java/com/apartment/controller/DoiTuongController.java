package com.apartment.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.apartment.entity.DoiTuong;
import com.apartment.service.DoiTuongExportService;
import com.apartment.service.DoiTuongService;
import com.apartment.service.LichSuChinhSuaService;

@Controller
@RequestMapping("/admin/doi-tuong")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class DoiTuongController {
    
    @Autowired
    private DoiTuongService doiTuongService;
    
    @Autowired
    private LichSuChinhSuaService lichSuChinhSuaService;
    
    @Autowired
    private DoiTuongExportService doiTuongExportService;
    
    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("doiTuongList", doiTuongService.searchByKeyword(search));
        } else {
            model.addAttribute("doiTuongList", doiTuongService.findAll());
        }
        model.addAttribute("search", search);
        return "admin/doi-tuong/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("doiTuong", new DoiTuong());
        model.addAttribute("isEdit", false);
        return "admin/doi-tuong/form";
    }
    
    @GetMapping("/edit/{cccd}")
    public String editForm(@PathVariable String cccd, Model model) {
        DoiTuong doiTuong = doiTuongService.findByCccd(cccd)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tượng"));
        model.addAttribute("doiTuong", doiTuong);
        model.addAttribute("isEdit", true);
        return "admin/doi-tuong/form";
    }
    
    @PostMapping("/save")
    public String save(@ModelAttribute DoiTuong doiTuong, 
                      RedirectAttributes redirectAttributes,
                      Authentication authentication) {
        try {
           
            DoiTuong existingDoiTuong = doiTuongService.findByCccd(doiTuong.getCccd()).orElse(null);
            boolean isNew = existingDoiTuong == null;

            // Validate giới tính để tránh lỗi constraint
            if (doiTuong.getGioiTinh() == null || doiTuong.getGioiTinh().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn giới tính.");
                return isNew ? "redirect:/admin/doi-tuong/create" : "redirect:/admin/doi-tuong/edit/" + doiTuong.getCccd();
            }
            
          
            doiTuongService.save(doiTuong);
            
          
            if (!isNew) {
                Map<String, LichSuChinhSuaService.ChangeInfo> thayDoi = new HashMap<>();
                
        
                if (!equalsValue(existingDoiTuong.getHoVaTen(), doiTuong.getHoVaTen())) {
                    thayDoi.put("Họ và tên", new LichSuChinhSuaService.ChangeInfo(
                        existingDoiTuong.getHoVaTen(), doiTuong.getHoVaTen()));
                }
                if (!equalsValue(existingDoiTuong.getNgaySinh(), doiTuong.getNgaySinh())) {
                    thayDoi.put("Ngày sinh", new LichSuChinhSuaService.ChangeInfo(
                        String.valueOf(existingDoiTuong.getNgaySinh()), 
                        String.valueOf(doiTuong.getNgaySinh())));
                }
                if (!equalsValue(existingDoiTuong.getGioiTinh(), doiTuong.getGioiTinh())) {
                    thayDoi.put("Giới tính", new LichSuChinhSuaService.ChangeInfo(
                        existingDoiTuong.getGioiTinh(), doiTuong.getGioiTinh()));
                }
                if (!equalsValue(existingDoiTuong.getSoDienThoai(), doiTuong.getSoDienThoai())) {
                    thayDoi.put("Số điện thoại", new LichSuChinhSuaService.ChangeInfo(
                        existingDoiTuong.getSoDienThoai(), doiTuong.getSoDienThoai()));
                }
                if (!equalsValue(existingDoiTuong.getEmail(), doiTuong.getEmail())) {
                    thayDoi.put("Email", new LichSuChinhSuaService.ChangeInfo(
                        existingDoiTuong.getEmail(), doiTuong.getEmail()));
                }
                if (!equalsValue(existingDoiTuong.getQueQuan(), doiTuong.getQueQuan())) {
                    thayDoi.put("Quê quán", new LichSuChinhSuaService.ChangeInfo(
                        existingDoiTuong.getQueQuan(), doiTuong.getQueQuan()));
                }
                if (!equalsValue(existingDoiTuong.getNgheNghiep(), doiTuong.getNgheNghiep())) {
                    thayDoi.put("Nghề nghiệp", new LichSuChinhSuaService.ChangeInfo(
                        existingDoiTuong.getNgheNghiep(), doiTuong.getNgheNghiep()));
                }
                if (!equalsValue(existingDoiTuong.getVaiTro(), doiTuong.getVaiTro())) {
                    thayDoi.put("Vai trò", new LichSuChinhSuaService.ChangeInfo(
                        existingDoiTuong.getVaiTro(), doiTuong.getVaiTro()));
                }
                if (!equalsValue(existingDoiTuong.getTrangThaiTaiKhoan(), doiTuong.getTrangThaiTaiKhoan())) {
                    thayDoi.put("Trạng thái tài khoản", new LichSuChinhSuaService.ChangeInfo(
                        existingDoiTuong.getTrangThaiTaiKhoan(), doiTuong.getTrangThaiTaiKhoan()));
                }
                
                if (!thayDoi.isEmpty()) {
                    String moTa = "Admin chỉnh sửa thông tin người dùng: " + doiTuong.getHoVaTen() + " (CCCD: " + doiTuong.getCccd() + ")";
                    
                    lichSuChinhSuaService.ghiLichSuChinhSua(
                        "doi_tuong",
                        doiTuong.getCccd(),
                        doiTuong.getHoVaTen(),
                        authentication.getName(),
                        "update",
                        thayDoi,
                        moTa
                    );
                }
            } else {
               
                Map<String, LichSuChinhSuaService.ChangeInfo> thayDoi = new HashMap<>();
                thayDoi.put("Tạo mới", new LichSuChinhSuaService.ChangeInfo("", "Tạo tài khoản mới"));
                
                String moTa = "Admin tạo tài khoản mới: " + doiTuong.getHoVaTen() + " (CCCD: " + doiTuong.getCccd() + ")";
                
                lichSuChinhSuaService.ghiLichSuChinhSua(
                    "doi_tuong",
                    doiTuong.getCccd(),
                    doiTuong.getHoVaTen(),
                    authentication.getName(),
                    "create",
                    thayDoi,
                    moTa
                );
            }
            
            redirectAttributes.addFlashAttribute("success", "Lưu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/doi-tuong";
    }
    
    private boolean equalsValue(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) return true;
        if (oldValue == null || newValue == null) return false;
        return oldValue.equals(newValue);
    }
    
    @GetMapping("/delete/{cccd}")
    public String delete(@PathVariable String cccd, RedirectAttributes redirectAttributes) {
        try {
            doiTuongService.delete(cccd);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/doi-tuong";
    }
    
    
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(@RequestParam(required = false) String search) {
        try {
            java.util.List<DoiTuong> doiTuongList;
            if (search != null && !search.isEmpty()) {
                doiTuongList = doiTuongService.searchByKeyword(search);
            } else {
                doiTuongList = doiTuongService.findAll();
            }
            
            byte[] excelBytes = doiTuongExportService.exportToExcel(doiTuongList);
            
            String filename = "cu_dan_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
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
    
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(@RequestParam(required = false) String search) {
        try {
            java.util.List<DoiTuong> doiTuongList;
            if (search != null && !search.isEmpty()) {
                doiTuongList = doiTuongService.searchByKeyword(search);
            } else {
                doiTuongList = doiTuongService.findAll();
            }
            
            byte[] pdfBytes = doiTuongExportService.exportToPdf(doiTuongList);
            
            String filename = "cu_dan_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
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
    
  
    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadImportTemplate() {
        try {
            byte[] templateBytes = doiTuongExportService.createTemplateExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "template_import_cu_dan.xlsx");
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
    
   
    @PostMapping("/import")
    public String importFromExcel(@RequestParam("file") MultipartFile file, RedirectAttributes ra) {
        try {
            if (file.isEmpty()) {
                ra.addFlashAttribute("error", "Vui lòng chọn file để import!");
                return "redirect:/admin/doi-tuong";
            }
            
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                ra.addFlashAttribute("error", "Vui lòng chọn file Excel (.xlsx hoặc .xls)!");
                return "redirect:/admin/doi-tuong";
            }
            
            DoiTuongExportService.ImportResult result = doiTuongExportService.importFromExcel(file);
            
            StringBuilder message = new StringBuilder();
            message.append("Import thành công ").append(result.getSuccessCount()).append(" cư dân.");
            
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
        
        return "redirect:/admin/doi-tuong";
    }
}


