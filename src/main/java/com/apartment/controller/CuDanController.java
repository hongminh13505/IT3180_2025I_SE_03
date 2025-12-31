package com.apartment.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; 

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.apartment.entity.BaoCaoSuCo;
import com.apartment.entity.DoiTuong;
import com.apartment.entity.HoaDon;
import com.apartment.entity.ThanhVienHo;
import com.apartment.entity.ThongBao; 
import com.apartment.repository.ThanhVienHoRepository;
import com.apartment.entity.PhanAnh;
import com.apartment.entity.PhanAnh;
import com.apartment.service.BaoCaoSuCoService;
import com.apartment.service.DoiTuongService;
import com.apartment.service.HoaDonService;
import com.apartment.service.LichSuChinhSuaService;
import com.apartment.service.PhanAnhService;
import com.apartment.service.PhanAnhService;
import com.apartment.service.ThongBaoService;
import com.apartment.service.YeuCauGuiXeService;
import com.apartment.entity.YeuCauGuiXe;
import com.apartment.service.DichVuService;
import com.apartment.service.DangKyDichVuService;
import com.apartment.entity.DichVu;
import com.apartment.entity.DangKyDichVu;
import java.math.BigDecimal;

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
    
    @Autowired
    private PhanAnhService phanAnhService;

    @Autowired
    private YeuCauGuiXeService yeuCauGuiXeService;
    
    @Autowired
    private DichVuService dichVuService;
    
    @Autowired
    private DangKyDichVuService dangKyDichVuService;
    
   
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
        // Thông báo mới nhất (giới hạn 7 dòng)
        java.util.List<com.apartment.entity.ThongBao> allThongBao = thongBaoService.findAllVisible();
        java.util.List<com.apartment.entity.ThongBao> recentThongBao = allThongBao.stream()
            .limit(7)
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("thongBaoList", recentThongBao);
        
        // Báo cáo sự cố mới nhất (giới hạn 7 dòng)
        java.util.List<com.apartment.entity.BaoCaoSuCo> allBaoCao = baoCaoSuCoService.findAll();
        java.util.List<com.apartment.entity.BaoCaoSuCo> recentBaoCao = allBaoCao.stream()
            .limit(7)
            .collect(java.util.stream.Collectors.toList());
        model.addAttribute("baoCaoSuCoList", recentBaoCao);
        
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
            
            if (!thayDoi.isEmpty()) {
                lichSuChinhSuaService.ghiLichSuChinhSua("doi_tuong", cccd, existingDoiTuong.getHoVaTen(), cccd, "update", thayDoi, "Người dùng tự chỉnh sửa thông tin cá nhân");
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
            return "cu-dan/thong-bao";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách thông báo");
            model.addAttribute("thongBaoList", new java.util.ArrayList<>());
            return "cu-dan/thong-bao";
        }
    }
    
    @GetMapping("/thong-bao/{id}")
    public String chiTietThongBao(@PathVariable Integer id,
                                  Model model,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            ThongBao thongBao = thongBaoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
            
            // Kiểm tra thông báo có hiển thị không
            if (thongBao.getTrangThai() == null || !"hien".equals(thongBao.getTrangThai())) {
                redirectAttributes.addFlashAttribute("error", "Thông báo không tồn tại hoặc đã bị ẩn");
                return "redirect:/cu-dan/thong-bao";
            }
            
            // Tránh lazy loading exception - không truy cập banQuanTri trong template
            // Chỉ cần thông tin cơ bản của thông báo
            
            model.addAttribute("thongBao", thongBao);
            model.addAttribute("username", authentication.getName());
            return "cu-dan/thong-bao/detail";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông báo");
            return "redirect:/cu-dan/thong-bao";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi tải chi tiết thông báo: " + e.getMessage());
            return "redirect:/cu-dan/thong-bao";
        }
    }
    

    @GetMapping("/bao-cao-su-co")
    public String baoCaoSuCo(@RequestParam(required = false) String search,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size,
                             Model model,
                             Authentication authentication) {
        try {
            String cccd = authentication.getName();
            Pageable pageable = PageRequest.of(page, size);
            
            java.util.List<com.apartment.entity.BaoCaoSuCo> allBaoCao = baoCaoSuCoService.findByCccdNguoiBaoCao(cccd);
            
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                allBaoCao = allBaoCao.stream()
                    .filter(bc -> 
                        (bc.getTieuDe() != null && bc.getTieuDe().toLowerCase().contains(searchLower)) ||
                        (bc.getTrangThai() != null && bc.getTrangThai().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allBaoCao.size());
            java.util.List<com.apartment.entity.BaoCaoSuCo> pageContent = start < allBaoCao.size() 
                ? allBaoCao.subList(start, end) 
                : java.util.Collections.emptyList();
            
            Page<com.apartment.entity.BaoCaoSuCo> baoCaoPage = 
                new PageImpl<>(pageContent, pageable, allBaoCao.size());
            
            model.addAttribute("baoCaoList", baoCaoPage.getContent());
            model.addAttribute("baoCaoPage", baoCaoPage);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", baoCaoPage.getTotalPages());
            model.addAttribute("totalElements", baoCaoPage.getTotalElements());
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
    public String hoaDon(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        Model model, 
                        Authentication authentication) {
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
            java.util.List<HoaDon> allHoaDon = hoaDonService.findByMaHo(maHo);
            
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allHoaDon.size());
            java.util.List<HoaDon> pageContent = start < allHoaDon.size() 
                ? allHoaDon.subList(start, end) 
                : java.util.Collections.emptyList();
            
            Page<HoaDon> hoaDonPage = 
                new PageImpl<>(pageContent, pageable, allHoaDon.size());
            
            model.addAttribute("hoaDonList", hoaDonPage.getContent());
            model.addAttribute("hoaDonPage", hoaDonPage);
            model.addAttribute("isChuHo", isChuHo);
            model.addAttribute("maHo", maHo);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", hoaDonPage.getTotalPages());
            model.addAttribute("totalElements", hoaDonPage.getTotalElements());
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
            
          
            ThongBao tb = new ThongBao();
            tb.setTieuDe("Xác nhận thanh toán hóa đơn #" + id);
            String noiDung = String.format("Hộ gia đình %s đã thanh toán thành công hóa đơn %s. Số tiền: %,.0f VNĐ. Thời gian: %s", 
                                           maHo, hoaDon.getLoaiHoaDon(), hoaDon.getSoTien(), 
                                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            tb.setNoiDungThongBao(noiDung);
            tb.setNgayTaoThongBao(LocalDateTime.now());
            tb.setLoaiThongBao("binh_thuong");
            tb.setDoiTuongNhan("tat_ca");
            tb.setCccdBanQuanTri("001234567891"); 
            
            thongBaoService.save(tb);
            
            redirectAttributes.addFlashAttribute("success", "Thanh toán thành công! Đã gửi thông báo xác nhận.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi thanh toán: " + e.getMessage());
        }
        
        return "redirect:/cu-dan/hoa-don";
    }
    
    
    @GetMapping("/gui-xe")
    public String danhSachGuiXe(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String trangThai,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size,
                                Model model,
                                Authentication authentication) {
        try {
            String cccd = authentication.getName();
            java.util.List<YeuCauGuiXe> allList = yeuCauGuiXeService.findByCccdNguoiGui(cccd);
            
            if (trangThai != null && !trangThai.isEmpty()) {
                String tt = trangThai.trim();
                allList = allList.stream().filter(x -> x.getTrangThai() != null && x.getTrangThai().equals(tt)).collect(Collectors.toList());
            }
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                allList = allList.stream().filter(x ->
                        (x.getBienSo() != null && x.getBienSo().toLowerCase().contains(searchLower)) ||
                        (x.getLoaiXe() != null && x.getLoaiXe().toLowerCase().contains(searchLower))
                ).collect(Collectors.toList());
            }
            
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allList.size());
            java.util.List<YeuCauGuiXe> pageContent = start < allList.size() 
                ? allList.subList(start, end) 
                : java.util.Collections.emptyList();
            
            Page<YeuCauGuiXe> yeuCauPage = 
                new PageImpl<>(pageContent, pageable, allList.size());
            
            model.addAttribute("yeuCauList", yeuCauPage.getContent());
            model.addAttribute("yeuCauPage", yeuCauPage);
            model.addAttribute("search", search);
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", yeuCauPage.getTotalPages());
            model.addAttribute("totalElements", yeuCauPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            return "cu-dan/gui-xe";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách gửi xe");
            model.addAttribute("yeuCauList", new java.util.ArrayList<>());
            return "cu-dan/gui-xe";
        }
    }

    @GetMapping("/gui-xe/tao-moi")
    public String taoGuiXe(Model model, Authentication authentication) {
        try {
            YeuCauGuiXe yeuCau = new YeuCauGuiXe();
            model.addAttribute("yeuCau", yeuCau);
            model.addAttribute("username", authentication.getName());
            return "cu-dan/tao-gui-xe";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải form đăng ký gửi xe");
            return "redirect:/cu-dan/gui-xe";
        }
    }

    @PostMapping("/gui-xe/luu")
    public String luuGuiXe(@ModelAttribute YeuCauGuiXe yeuCauGuiXe,
                           RedirectAttributes redirectAttributes,
                           Authentication authentication) {
        try {
            String cccd = authentication.getName();
            String maHo = getMaHoByCccd(cccd);

            yeuCauGuiXe.setCccdNguoiGui(cccd);
            yeuCauGuiXe.setMaHo(maHo);
            yeuCauGuiXe.setTrangThai("cho_duyet");
            yeuCauGuiXe.setNgayTao(LocalDateTime.now());

            yeuCauGuiXeService.save(yeuCauGuiXe);
            redirectAttributes.addFlashAttribute("success", "Gửi yêu cầu đăng ký gửi xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi gửi yêu cầu: " + e.getMessage());
        }
        return "redirect:/cu-dan/gui-xe";
    }

    @GetMapping("/gui-xe/chi-tiet/{id}")
    public String chiTietGuiXe(@PathVariable Integer id,
                               Model model,
                               Authentication authentication) {
        try {
            String cccd = authentication.getName();
            YeuCauGuiXe yeuCau = yeuCauGuiXeService.findWithNguoiById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu gửi xe"));

            if (!cccd.equals(yeuCau.getCccdNguoiGui())) {
                model.addAttribute("error", "Bạn không có quyền xem yêu cầu này");
                return "redirect:/cu-dan/gui-xe";
            }

            model.addAttribute("yeuCau", yeuCau);
            model.addAttribute("username", authentication.getName());
            return "cu-dan/gui-xe/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải chi tiết yêu cầu: " + e.getMessage());
            return "redirect:/cu-dan/gui-xe";
        }
    }
    
    @GetMapping("/phan-anh")
    public String phanAnh(@RequestParam(required = false) String search,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model,
                          Authentication authentication) {
        try {
            String cccd = authentication.getName();
            Pageable pageable = PageRequest.of(page, size);
            
            java.util.List<PhanAnh> allPhanAnh = phanAnhService.findByCccdNguoiPhanAnh(cccd);
            
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                allPhanAnh = allPhanAnh.stream()
                    .filter(pa -> 
                        (pa.getTieuDe() != null && pa.getTieuDe().toLowerCase().contains(searchLower)) ||
                        (pa.getNoiDung() != null && pa.getNoiDung().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allPhanAnh.size());
            java.util.List<PhanAnh> pageContent = start < allPhanAnh.size() 
                ? allPhanAnh.subList(start, end) 
                : java.util.Collections.emptyList();
            
            Page<PhanAnh> phanAnhPage = 
                new PageImpl<>(pageContent, pageable, allPhanAnh.size());
            
            model.addAttribute("phanAnhList", phanAnhPage.getContent());
            model.addAttribute("phanAnhPage", phanAnhPage);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", phanAnhPage.getTotalPages());
            model.addAttribute("totalElements", phanAnhPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            return "cu-dan/phan-anh";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách phản ánh");
            model.addAttribute("phanAnhList", new java.util.ArrayList<>());
            return "cu-dan/phan-anh";
        }
    }
    
    @GetMapping("/phan-anh/tao-moi")
    public String taoPhanAnh(Model model, Authentication authentication) {
        try {
            PhanAnh phanAnh = new PhanAnh();
            model.addAttribute("phanAnh", phanAnh);
            model.addAttribute("username", authentication.getName());
            return "cu-dan/tao-phan-anh";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải form tạo phản ánh");
            return "redirect:/cu-dan/phan-anh";
        }
    }
    
    @PostMapping("/phan-anh/luu")
    public String luuPhanAnh(@ModelAttribute PhanAnh phanAnh,
                             RedirectAttributes redirectAttributes,
                             Authentication authentication) {
        try {
            String cccd = authentication.getName();
            phanAnh.setCccdNguoiPhanAnh(cccd);
            phanAnh.setTrangThai("moi");
            
            phanAnhService.save(phanAnh);
            redirectAttributes.addFlashAttribute("success", "Gửi phản ánh thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi gửi phản ánh: " + e.getMessage());
        }
        return "redirect:/cu-dan/phan-anh";
    }
    
    @GetMapping("/phan-anh/{id}")
    public String chiTietPhanAnh(@PathVariable Integer id, Model model, Authentication authentication) {
        try {
            String cccd = authentication.getName();
            PhanAnh phanAnh = phanAnhService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phản ánh"));
            
            if (!phanAnh.getCccdNguoiPhanAnh().equals(cccd)) {
                model.addAttribute("error", "Bạn không có quyền xem phản ánh này");
                return "redirect:/cu-dan/phan-anh";
            }
            
            model.addAttribute("phanAnh", phanAnh);
            model.addAttribute("username", authentication.getName());
            return "cu-dan/phan-anh/detail";
        } catch (Exception e) {
            return "redirect:/cu-dan/phan-anh?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/dich-vu")
    public String dichVu(@RequestParam(required = false) String search,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "20") int size,
                         Model model,
                         Authentication authentication) {
        try {
            List<DichVu> allDichVu = dichVuService.findAllActive();
            
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                allDichVu = allDichVu.stream()
                    .filter(dv -> 
                        (dv.getTenDichVu() != null && dv.getTenDichVu().toLowerCase().contains(searchLower)) ||
                        (dv.getLoaiDichVu() != null && dv.getLoaiDichVu().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allDichVu.size());
            List<DichVu> pageContent = start < allDichVu.size() 
                ? allDichVu.subList(start, end) 
                : java.util.Collections.emptyList();
            
            Page<DichVu> dichVuPage = new PageImpl<>(pageContent, pageable, allDichVu.size());
            
            model.addAttribute("dichVuList", dichVuPage.getContent());
            model.addAttribute("dichVuPage", dichVuPage);
            model.addAttribute("search", search);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", dichVuPage.getTotalPages());
            model.addAttribute("totalElements", dichVuPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            return "cu-dan/dich-vu";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách dịch vụ");
            model.addAttribute("dichVuList", new java.util.ArrayList<>());
            return "cu-dan/dich-vu";
        }
    }
    
    @GetMapping("/dich-vu/dang-ky/{id}")
    public String dangKyDichVuForm(@PathVariable Integer id,
                                  Model model,
                                  Authentication authentication) {
        try {
            DichVu dichVu = dichVuService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));
            
            if (!"hoat_dong".equals(dichVu.getTrangThai())) {
                model.addAttribute("error", "Dịch vụ này hiện không hoạt động");
                return "redirect:/cu-dan/dich-vu";
            }
            
            DangKyDichVu dangKy = new DangKyDichVu();
            dangKy.setMaDichVu(id);
            model.addAttribute("dangKy", dangKy);
            model.addAttribute("dichVu", dichVu);
            model.addAttribute("username", authentication.getName());
            return "cu-dan/dang-ky-dich-vu";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải form đăng ký: " + e.getMessage());
            return "redirect:/cu-dan/dich-vu";
        }
    }
    
    @PostMapping("/dich-vu/dang-ky")
    public String luuDangKyDichVu(@ModelAttribute DangKyDichVu dangKy,
                                  RedirectAttributes redirectAttributes,
                                  Authentication authentication) {
        try {
            String cccd = authentication.getName();
            dangKy.setCccdNguoiDung(cccd);
            dangKy.setTrangThai("cho_duyet");
            
            // Tự động điền mô tả yêu cầu nếu trống
            if (dangKy.getMoTaYeuCau() == null || dangKy.getMoTaYeuCau().trim().isEmpty()) {
                dangKy.setMoTaYeuCau("1");
            }
            
            // Tạo mã đăng ký tự động
            List<DangKyDichVu> allDangKy = dangKyDichVuService.findAll();
            int maxId = allDangKy.stream()
                .mapToInt(dk -> dk.getMaDangKy() != null ? dk.getMaDangKy() : 0)
                .max()
                .orElse(0);
            dangKy.setMaDangKy(maxId + 1);
            
            dangKyDichVuService.save(dangKy);
            redirectAttributes.addFlashAttribute("success", "Đăng ký dịch vụ thành công! Đang chờ duyệt.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi đăng ký dịch vụ: " + e.getMessage());
        }
        return "redirect:/cu-dan/dich-vu";
    }
    
    @GetMapping("/dich-vu/dang-ky-cua-toi")
    public String danhSachDangKyDichVu(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      Model model,
                                      Authentication authentication) {
        try {
            String cccd = authentication.getName();
            List<DangKyDichVu> allDangKy = dangKyDichVuService.findByCccdNguoiDung(cccd);
            
            // Load thông tin dịch vụ cho mỗi đăng ký
            Map<Integer, DichVu> dichVuMap = new HashMap<>();
            for (DangKyDichVu dk : allDangKy) {
                if (dk.getMaDichVu() != null && !dichVuMap.containsKey(dk.getMaDichVu())) {
                    dichVuService.findById(dk.getMaDichVu()).ifPresent(dv -> dichVuMap.put(dk.getMaDichVu(), dv));
                }
            }
            model.addAttribute("dichVuMap", dichVuMap);
            
            Pageable pageable = PageRequest.of(page, size);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allDangKy.size());
            List<DangKyDichVu> pageContent = start < allDangKy.size() 
                ? allDangKy.subList(start, end) 
                : java.util.Collections.emptyList();
            
            Page<DangKyDichVu> dangKyPage = new PageImpl<>(pageContent, pageable, allDangKy.size());
            
            model.addAttribute("dangKyList", dangKyPage.getContent());
            model.addAttribute("dangKyPage", dangKyPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", dangKyPage.getTotalPages());
            model.addAttribute("totalElements", dangKyPage.getTotalElements());
            model.addAttribute("username", authentication.getName());
            return "cu-dan/dang-ky-dich-vu-list";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách đăng ký");
            model.addAttribute("dangKyList", new java.util.ArrayList<>());
            return "cu-dan/dang-ky-dich-vu-list";
        }
    }
}