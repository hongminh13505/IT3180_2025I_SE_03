package com.apartment.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.apartment.entity.DoiTuong;
import com.apartment.entity.HoGiaDinh;
import com.apartment.entity.ThanhVienHo;
import com.apartment.repository.DoiTuongRepository;
import com.apartment.repository.ThanhVienHoRepository;
import com.apartment.service.DoiTuongService;
import com.apartment.service.HoGiaDinhService;
import com.apartment.service.LichSuChinhSuaService;
import com.apartment.service.TaiSanChungCuService;

@Controller
@RequestMapping("/admin/ho-gia-dinh")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class HoGiaDinhController {
    
    @Autowired
    private HoGiaDinhService hoGiaDinhService;
    
    @Autowired
    private DoiTuongService doiTuongService;
    
    @Autowired
    private TaiSanChungCuService taiSanChungCuService;
    
    @Autowired
    private DoiTuongRepository doiTuongRepository;
    
    @Autowired
    private ThanhVienHoRepository thanhVienHoRepository;
    
    @Autowired
    private LichSuChinhSuaService lichSuChinhSuaService;
    
    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        List<HoGiaDinh> hoGiaDinhList;
        if (search != null && !search.isEmpty()) {
            hoGiaDinhList = hoGiaDinhService.searchByName(search);
        } else {
            hoGiaDinhList = hoGiaDinhService.findAll();
        }
        model.addAttribute("hoGiaDinhList", hoGiaDinhList);
        model.addAttribute("search", search);
        
     
        model.addAttribute("allCanHo", taiSanChungCuService.findAll());
        
        return "admin/ho-gia-dinh/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model, @RequestParam(required = false) String error) {
        model.addAttribute("hoGiaDinh", new HoGiaDinh());
        model.addAttribute("isEdit", false);
        
        model.addAttribute("canHoList", taiSanChungCuService.findAllCanHo());
     
        model.addAttribute("thanhVienList", java.util.Collections.emptyList());
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/ho-gia-dinh/form";
    }
    
    @GetMapping("/edit/{maHo}")
    public String editForm(@PathVariable String maHo, Model model) {
        HoGiaDinh hoGiaDinh = hoGiaDinhService.findByMaHo(maHo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hộ gia đình"));
        model.addAttribute("hoGiaDinh", hoGiaDinh);
        model.addAttribute("isEdit", true);
      
        model.addAttribute("canHoList", taiSanChungCuService.findAllCanHo());
        return "admin/ho-gia-dinh/form";
    }
    
    @GetMapping("/detail/{maHo}")
    public String detail(@PathVariable String maHo, Model model) {
        HoGiaDinh hoGiaDinh = hoGiaDinhService.findByMaHo(maHo)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hộ gia đình"));
        model.addAttribute("hoGiaDinh", hoGiaDinh);
        
     
        if (hoGiaDinh.getMaCanHo() != null) {
            taiSanChungCuService.findById(hoGiaDinh.getMaCanHo())
                    .ifPresent(canHo -> model.addAttribute("canHo", canHo));
        }
        
       
        List<ThanhVienHo> thanhVienList = thanhVienHoRepository.findActiveByMaHo(maHo);
        model.addAttribute("thanhVienList", thanhVienList);
        
        return "admin/ho-gia-dinh/detail";
    }
    
 
    @GetMapping("/api/search-cudan")
    @ResponseBody
    public ResponseEntity<List<DoiTuong>> searchCuDan(@RequestParam String keyword) {
        List<DoiTuong> cuDanList = doiTuongRepository.searchCuDanByKeyword(keyword);
        return ResponseEntity.ok(cuDanList);
    }
    
   
    @PostMapping("/api/add-member")
    @ResponseBody
    public ResponseEntity<String> addMember(
            @RequestParam String maHo,
            @RequestParam String cccd,
            @RequestParam String quanHe,
            @RequestParam(defaultValue = "false") boolean laChuHo) {
        try {
            if (laChuHo) {
                List<ThanhVienHo> chuHoHienTai = thanhVienHoRepository.findChuHoByMaHo(maHo);
                if (!chuHoHienTai.isEmpty()) {
                    return ResponseEntity.badRequest().body("Hộ này đã có chủ hộ đang hoạt động. Vui lòng kết thúc/chuyển chủ hộ trước khi thêm mới.");
                }
            }

            ThanhVienHo thanhVien = new ThanhVienHo();
            thanhVien.setMaHo(maHo);
            thanhVien.setCccd(cccd);
            thanhVien.setQuanHeVoiChuHo(quanHe);
            thanhVien.setLaChuHo(laChuHo);
            thanhVien.setNgayBatDau(LocalDate.now());
            
            thanhVienHoRepository.save(thanhVien);
            return ResponseEntity.ok("Thêm thành viên thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    
    @PostMapping("/api/remove-member")
    @ResponseBody
    public ResponseEntity<String> removeMember(@RequestParam String maHo, @RequestParam String cccd, @RequestParam String ngayBatDau) {
        try {
            ThanhVienHo.ThanhVienHoId id = new ThanhVienHo.ThanhVienHoId();
            id.setCccd(cccd);
            id.setNgayBatDau(LocalDate.parse(ngayBatDau));
            thanhVienHoRepository.deleteById(id);
            return ResponseEntity.ok("Xóa thành viên thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    
    @PostMapping("/save")
    public String save(@ModelAttribute HoGiaDinh hoGiaDinh, 
                      @RequestParam(required = false) String cccdChuHo,
                      @RequestParam(required = false) String isEdit,
                      RedirectAttributes redirectAttributes,
                      Authentication authentication) {
        try {
          
            boolean isNew = hoGiaDinhService.findByMaHo(hoGiaDinh.getMaHo()).isEmpty();
            HoGiaDinh existingHoGiaDinh = isNew ? null : hoGiaDinhService.findByMaHo(hoGiaDinh.getMaHo()).orElse(null);
            boolean isEditFlag = isEdit != null && isEdit.equalsIgnoreCase("true");

    
            if (!isNew && !isEditFlag) {
                redirectAttributes.addFlashAttribute("error", "Mã hộ đã tồn tại. Vui lòng nhập mã khác hoặc chỉnh sửa hộ gia đình hiện có.");
                return "redirect:/admin/ho-gia-dinh/create";
            }
          
            if (hoGiaDinh.getMaCanHo() != null) {
                HoGiaDinh existingHo = hoGiaDinhService.findByMaCanHo(hoGiaDinh.getMaCanHo()).orElse(null);
                
               
                if (existingHo != null && !existingHo.getMaHo().equals(hoGiaDinh.getMaHo())) {
                    String errorMsg = "⚠️ Căn hộ này đã được gán cho hộ gia đình " + existingHo.getMaHo() + " (Tên: " + existingHo.getTenHo() + ")! Vui lòng chọn căn hộ khác.";
                    redirectAttributes.addFlashAttribute("error", errorMsg);
                    System.out.println("=== DEBUG: " + errorMsg + " ===");
                    if (isNew) {
                        return "redirect:/admin/ho-gia-dinh";
                    } else {
                        return "redirect:/admin/ho-gia-dinh/edit/" + hoGiaDinh.getMaHo();
                    }
                }
            }
            
         
            hoGiaDinhService.save(hoGiaDinh);
            
        
            if (!isNew && existingHoGiaDinh != null) {
                Map<String, LichSuChinhSuaService.ChangeInfo> thayDoi = new HashMap<>();
                
                if (!equalsValue(existingHoGiaDinh.getTenHo(), hoGiaDinh.getTenHo())) {
                    thayDoi.put("Tên hộ", new LichSuChinhSuaService.ChangeInfo(
                        existingHoGiaDinh.getTenHo(), hoGiaDinh.getTenHo()));
                }
                if (!equalsValue(existingHoGiaDinh.getMaCanHo(), hoGiaDinh.getMaCanHo())) {
                    thayDoi.put("Mã căn hộ", new LichSuChinhSuaService.ChangeInfo(
                        String.valueOf(existingHoGiaDinh.getMaCanHo()), 
                        String.valueOf(hoGiaDinh.getMaCanHo())));
                }
                if (!equalsValue(existingHoGiaDinh.getTrangThai(), hoGiaDinh.getTrangThai())) {
                    thayDoi.put("Trạng thái", new LichSuChinhSuaService.ChangeInfo(
                        existingHoGiaDinh.getTrangThai(), hoGiaDinh.getTrangThai()));
                }
                if (!equalsValue(existingHoGiaDinh.getGhiChu(), hoGiaDinh.getGhiChu())) {
                    thayDoi.put("Ghi chú", new LichSuChinhSuaService.ChangeInfo(
                        existingHoGiaDinh.getGhiChu(), hoGiaDinh.getGhiChu()));
                }
                
                if (!thayDoi.isEmpty()) {
                    String moTa = "Admin chỉnh sửa thông tin hộ gia đình: " + hoGiaDinh.getTenHo() + " (Mã hộ: " + hoGiaDinh.getMaHo() + ")";
                    
                    lichSuChinhSuaService.ghiLichSuChinhSua(
                        "ho_gia_dinh",
                        hoGiaDinh.getMaHo(),
                        hoGiaDinh.getTenHo(),
                        authentication.getName(),
                        "update",
                        thayDoi,
                        moTa
                    );
                }
            } else if (isNew) {
           
                Map<String, LichSuChinhSuaService.ChangeInfo> thayDoi = new HashMap<>();
                thayDoi.put("Tạo mới", new LichSuChinhSuaService.ChangeInfo("", "Tạo hộ gia đình mới"));
                
                String moTa = "Admin tạo hộ gia đình mới: " + hoGiaDinh.getTenHo() + " (Mã hộ: " + hoGiaDinh.getMaHo() + ")";
                
                lichSuChinhSuaService.ghiLichSuChinhSua(
                    "ho_gia_dinh",
                    hoGiaDinh.getMaHo(),
                    hoGiaDinh.getTenHo(),
                    authentication.getName(),
                    "create",
                    thayDoi,
                    moTa
                );
            }
           
            if (cccdChuHo != null && !cccdChuHo.trim().isEmpty()) {
                List<ThanhVienHo> chuHoHienTai = thanhVienHoRepository.findChuHoByMaHo(hoGiaDinh.getMaHo());
                if (!chuHoHienTai.isEmpty() && !cccdChuHo.equals(chuHoHienTai.get(0).getCccd())) {
                    redirectAttributes.addFlashAttribute("error", "Hộ này đã có chủ hộ: " + chuHoHienTai.get(0).getCccd() + ". Vui lòng kết thúc/chuyển chủ hộ trước.");
                    return isNew ? "redirect:/admin/ho-gia-dinh" : "redirect:/admin/ho-gia-dinh/edit/" + hoGiaDinh.getMaHo();
                }

                List<ThanhVienHo> existingMembers = thanhVienHoRepository.findActiveByCccd(cccdChuHo);
                boolean alreadyExists = existingMembers.stream()
                    .anyMatch(m -> m.getMaHo().equals(hoGiaDinh.getMaHo()) && m.getLaChuHo());
                
                if (!alreadyExists) {
                    ThanhVienHo chuHo = new ThanhVienHo();
                    chuHo.setCccd(cccdChuHo);
                    chuHo.setMaHo(hoGiaDinh.getMaHo());
                    chuHo.setLaChuHo(true);
                    chuHo.setQuanHeVoiChuHo("Chủ hộ");
                    chuHo.setNgayBatDau(LocalDate.now());
                    
                    thanhVienHoRepository.save(chuHo);
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Lưu thành công!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", 
                "Căn hộ này đã được gán cho hộ gia đình khác! Vui lòng chọn căn hộ khác.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/ho-gia-dinh";
    }
    
    private boolean equalsValue(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) return true;
        if (oldValue == null || newValue == null) return false;
        return oldValue.equals(newValue);
    }
    
    @GetMapping("/delete/{maHo}")
    public String delete(@PathVariable String maHo, RedirectAttributes redirectAttributes) {
        try {
            hoGiaDinhService.delete(maHo);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/ho-gia-dinh";
    }
}


