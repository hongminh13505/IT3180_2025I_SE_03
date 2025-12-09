package com.apartment.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.apartment.entity.BaoCaoSuCo;
import com.apartment.entity.DoiTuong;
import com.apartment.entity.HoaDon;
import com.apartment.entity.ThanhVienHo;
import com.apartment.repository.ThanhVienHoRepository;
import com.apartment.service.BaoCaoSuCoService;
import com.apartment.service.DoiTuongService;
import com.apartment.service.HoaDonService;
import com.apartment.service.LichSuChinhSuaService;
import com.apartment.service.ThongBaoService;

@Controller
@RequestMapping("/cu-dan")
@PreAuthorize("hasAnyRole('BAN_QUAN_TRI', 'NGUOI_DUNG_THUONG')")
public class CuDanController {
    
    @Autowired
    private ThongBaoService thongBaoService;
    
    @Autowired
    private BaoCaoSuCoService baoCaoSuCoService;
    
    @Autowired
    private DoiTuongService doiTuongService;
    
    @Autowired
    private HoaDonService hoaDonService;
    
    @Autowired
    private ThanhVienHoRepository thanhVienHoRepository;
    
    @Autowired
    private LichSuChinhSuaService lichSuChinhSuaService;
    
   
    private String getMaHoByCccd(String cccd) {
        java.util.List<ThanhVienHo> thanhVienList = thanhVienHoRepository.findActiveByCccd(cccd);
        if (thanhVienList.isEmpty()) {
            return null;
        }
        return thanhVienList.get(0).getMaHo();
    }
  
    private boolean isChuHo(String cccd, String maHo) {
        java.util.List<ThanhVienHo> chuHoList = thanhVienHoRepository.findChuHoByMaHo(maHo);
        return chuHoList.stream()
                .anyMatch(tv -> tv.getCccd().equals(cccd) && tv.getLaChuHo());
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
   
        model.addAttribute("thongBaoList", thongBaoService.findAllVisible());
        model.addAttribute("baoCaoSuCoList", baoCaoSuCoService.findAll());
        
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", "Cư dân");
        return "cu-dan/dashboard";
    }
    
    @GetMapping("/thong-tin-ca-nhan")
    public String thongTinCaNhan(Model model, Authentication authentication) {
        try {
            String cccd = authentication.getName();
            DoiTuong doiTuong = doiTuongService.findByCccd(cccd)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin cá nhân"));
            
            model.addAttribute("doiTuong", doiTuong);
            return "cu-dan/thong-tin-ca-nhan";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải thông tin cá nhân: " + e.getMessage());
            return "cu-dan/thong-tin-ca-nhan";
        }
    }
    
    @GetMapping("/thong-tin-ca-nhan/edit")
    public String editThongTinCaNhan(Model model, Authentication authentication) {
        try {
            String cccd = authentication.getName();
            DoiTuong doiTuong = doiTuongService.findByCccd(cccd)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin cá nhân"));
            
            model.addAttribute("doiTuong", doiTuong);
            return "cu-dan/thong-tin-ca-nhan-edit";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải thông tin cá nhân: " + e.getMessage());
            return "redirect:/cu-dan/thong-tin-ca-nhan";
        }
    }
    
    @PostMapping("/thong-tin-ca-nhan/save")
    public String saveThongTinCaNhan(@ModelAttribute DoiTuong doiTuong, 
                                     @RequestParam(required = false) String matKhau,
                                     RedirectAttributes redirectAttributes,
                                     Authentication authentication) {
        try {
            String cccd = authentication.getName();
            DoiTuong existingDoiTuong = doiTuongService.findByCccd(cccd)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin cá nhân"));
            
            // So sánh và ghi lại các thay đổi
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
            if (matKhau != null && !matKhau.trim().isEmpty()) {
                thayDoi.put("Mật khẩu", new LichSuChinhSuaService.ChangeInfo("***", "***"));
            }
        
            existingDoiTuong.setHoVaTen(doiTuong.getHoVaTen());
            existingDoiTuong.setNgaySinh(doiTuong.getNgaySinh());
            existingDoiTuong.setGioiTinh(doiTuong.getGioiTinh());
            existingDoiTuong.setSoDienThoai(doiTuong.getSoDienThoai());
            existingDoiTuong.setEmail(doiTuong.getEmail());
            existingDoiTuong.setQueQuan(doiTuong.getQueQuan());
            existingDoiTuong.setNgheNghiep(doiTuong.getNgheNghiep());
          
            if (matKhau != null && !matKhau.trim().isEmpty()) {
                existingDoiTuong.setMatKhau(matKhau);
            }
            
            doiTuongService.save(existingDoiTuong);
            
            // Ghi lại lịch sử chỉnh sửa
            if (!thayDoi.isEmpty()) {
                String moTa = "Người dùng tự chỉnh sửa thông tin cá nhân";
                
                lichSuChinhSuaService.ghiLichSuChinhSua(
                    "doi_tuong",
                    cccd,
                    existingDoiTuong.getHoVaTen(),
                    cccd,
                    "update",
                    thayDoi,
                    moTa
                );
            }
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi cập nhật thông tin: " + e.getMessage());
        }
        return "redirect:/cu-dan/thong-tin-ca-nhan";
    }
    
    private boolean equalsValue(Object oldValue, Object newValue) {
        if (oldValue == null && newValue == null) return true;
        if (oldValue == null || newValue == null) return false;
        return oldValue.equals(newValue);
    }
    
    @GetMapping("/thong-bao")
    public String thongBao(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                           Model model,
                           Authentication authentication) {
        try {
            java.util.List<com.apartment.entity.ThongBao> thongBaoList = thongBaoService.findAll();
            
           
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
            return "cu-dan/thong-bao";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách thông báo");
            model.addAttribute("thongBaoList", new java.util.ArrayList<>());
            return "cu-dan/thong-bao";
        }
    }
    
    @GetMapping("/bao-cao-su-co")
    public String baoCaoSuCo(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                             Model model,
                             Authentication authentication) {
        try {
            String cccd = authentication.getName();
            java.util.List<com.apartment.entity.BaoCaoSuCo> baoCaoList = baoCaoSuCoService.findByCccdNguoiBaoCao(cccd);
          
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
            return "cu-dan/bao-cao-su-co";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách báo cáo");
            model.addAttribute("baoCaoList", new java.util.ArrayList<>());
            return "cu-dan/bao-cao-su-co";
        }
    }
    
    @GetMapping("/bao-cao-su-co/tao-moi")
    public String taoBaoCaoSuCo(Model model, Authentication authentication) {
        try {
            BaoCaoSuCo baoCaoSuCo = new BaoCaoSuCo();
            model.addAttribute("baoCaoSuCo", baoCaoSuCo);
            model.addAttribute("username", authentication.getName());
            return "cu-dan/tao-bao-cao-su-co";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải form tạo báo cáo");
            return "redirect:/cu-dan/bao-cao-su-co";
        }
    }
    
    @PostMapping("/bao-cao-su-co/luu")
    public String luuBaoCaoSuCo(@ModelAttribute BaoCaoSuCo baoCaoSuCo,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        try {
            String cccd = authentication.getName();
            
        
            baoCaoSuCo.setCccdNguoiBaoCao(cccd);
            baoCaoSuCo.setCccdNguoiNhap(cccd);
            baoCaoSuCo.setPhuongThucBaoCao("truc_tuyen");
            baoCaoSuCo.setTrangThai("moi_tiep_nhan");
            
           
            baoCaoSuCo.setNgayBaoCao(java.time.LocalDateTime.now());
            
            baoCaoSuCoService.save(baoCaoSuCo);
            redirectAttributes.addFlashAttribute("success", "Tạo báo cáo sự cố thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi tạo báo cáo: " + e.getMessage());
        }
        
        return "redirect:/cu-dan/bao-cao-su-co";
    }
    
    @GetMapping("/hoa-don")
    public String hoaDon(Model model, Authentication authentication) {
        try {
            String cccd = authentication.getName();
            String maHo = getMaHoByCccd(cccd);
            
            if (maHo == null) {
                model.addAttribute("error", "Bạn chưa được gán vào hộ gia đình nào!");
                model.addAttribute("hoaDonList", new java.util.ArrayList<>());
                model.addAttribute("isChuHo", false);
                return "cu-dan/hoa-don";
            }
            
            boolean isChuHo = isChuHo(cccd, maHo);
            java.util.List<HoaDon> hoaDonList = hoaDonService.findByMaHo(maHo);
            
            model.addAttribute("hoaDonList", hoaDonList);
            model.addAttribute("isChuHo", isChuHo);
            model.addAttribute("maHo", maHo);
            model.addAttribute("username", authentication.getName());
            
            return "cu-dan/hoa-don";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách hóa đơn: " + e.getMessage());
            model.addAttribute("hoaDonList", new java.util.ArrayList<>());
            model.addAttribute("isChuHo", false);
            return "cu-dan/hoa-don";
        }
    }
    
    @PostMapping("/hoa-don/thanh-toan/{id}")
    public String thanhToan(@PathVariable Integer id,
                           @RequestParam(required = false, defaultValue = "chuyen_khoan") String phuongThucThanhToan,
                           RedirectAttributes redirectAttributes,
                           Authentication authentication) {
        try {
            String cccd = authentication.getName();
            String maHo = getMaHoByCccd(cccd);
            
            if (maHo == null || !isChuHo(cccd, maHo)) {
                redirectAttributes.addFlashAttribute("error", "Chỉ chủ hộ mới được thanh toán hóa đơn!");
                return "redirect:/cu-dan/hoa-don";
            }
            
            HoaDon hoaDon = hoaDonService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            
           
            if (!hoaDon.getMaHo().equals(maHo)) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thanh toán hóa đơn này!");
                return "redirect:/cu-dan/hoa-don";
            }
            
            hoaDon.setTrangThai("da_thanh_toan");
            hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
            hoaDon.setNgayThanhToan(LocalDateTime.now());
            
            hoaDonService.save(hoaDon);
            redirectAttributes.addFlashAttribute("success", "Thanh toán hóa đơn thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi thanh toán: " + e.getMessage());
        }
        
        return "redirect:/cu-dan/hoa-don";
    }
}

