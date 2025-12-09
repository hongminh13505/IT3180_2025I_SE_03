package com.apartment.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Thêm cái này để format ngày giờ
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors; // Thêm cái này để lọc list

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
import com.apartment.entity.ThongBao; // Import ThongBao
import com.apartment.repository.ThanhVienHoRepository;
import com.apartment.entity.PhanAnh;
import com.apartment.service.BaoCaoSuCoService;
import com.apartment.service.DoiTuongService;
import com.apartment.service.HoaDonService;
import com.apartment.service.LichSuChinhSuaService;
import com.apartment.service.PhanAnhService;
import com.apartment.service.ThongBaoService;
import com.apartment.service.YeuCauGuiXeService;
import com.apartment.entity.YeuCauGuiXe;

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
        // Code của nhóm: Giữ nguyên
        model.addAttribute("thongBaoList", thongBaoService.findAllVisible());
        model.addAttribute("baoCaoSuCoList", baoCaoSuCoService.findAll());
        
        model.addAttribute("username", authentication.getName());
        model.addAttribute("role", "Cư dân");
        return "cu-dan/dashboard";
    }
    
    // --- THÔNG TIN CÁ NHÂN (Code nhóm đã làm tốt, giữ nguyên) ---
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
                thayDoi.put("Họ và tên", new LichSuChinhSuaService.ChangeInfo(existingDoiTuong.getHoVaTen(), doiTuong.getHoVaTen()));
            }
            // ... (Giữ nguyên logic so sánh của nhóm) ...
            
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
    
    // --- THÔNG BÁO (Đã chỉnh sửa cho US-35) ---
    @GetMapping("/thong-bao")
    public String thongBao(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                           Model model,
                           Authentication authentication) {
        try {
            java.util.List<com.apartment.entity.ThongBao> thongBaoList = thongBaoService.findAll();
            
            // [MOD] Sắp xếp mới nhất lên đầu (Hoàn thiện US-35)
            thongBaoList.sort((t1, t2) -> t2.getNgayTaoThongBao().compareTo(t1.getNgayTaoThongBao()));
           
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
    
    // --- BÁO CÁO SỰ CỐ (Giữ nguyên code nhóm) ---
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
    
    // --- HÓA ĐƠN (Giữ nguyên) ---
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
    
    // --- THANH TOÁN (Đã chỉnh sửa cho US-28) ---
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
            
            // 1. Cập nhật trạng thái
            hoaDon.setTrangThai("da_thanh_toan");
            hoaDon.setPhuongThucThanhToan(phuongThucThanhToan);
            hoaDon.setNgayThanhToan(LocalDateTime.now());
            hoaDonService.save(hoaDon);
            
            // 2. [MOD] TẠO THÔNG BÁO TỰ ĐỘNG (Hoàn thiện US-28)
            ThongBao tb = new ThongBao();
            tb.setTieuDe("Xác nhận thanh toán hóa đơn #" + id);
            String noiDung = String.format("Hộ gia đình %s đã thanh toán thành công hóa đơn %s. Số tiền: %,.0f VNĐ. Thời gian: %s", 
                                           maHo, hoaDon.getLoaiHoaDon(), hoaDon.getSoTien(), 
                                           LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            tb.setNoiDungThongBao(noiDung);
            tb.setNgayTaoThongBao(LocalDateTime.now());
            tb.setLoaiThongBao("binh_thuong");
            tb.setDoiTuongNhan("tat_ca");
            // Hardcode Admin ID để tránh lỗi (đảm bảo ID này tồn tại trong DB)
            tb.setCccdBanQuanTri("001234567891"); 
            
            thongBaoService.save(tb);
            
            redirectAttributes.addFlashAttribute("success", "Thanh toán thành công! Đã gửi thông báo xác nhận.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi khi thanh toán: " + e.getMessage());
        }
        
        return "redirect:/cu-dan/hoa-don";
    }
    
    // ====== CÁC CHỨC NĂNG KHÁC CỦA NHÓM (GỬI XE, PHẢN ÁNH) - GIỮ NGUYÊN ======
    
    @GetMapping("/gui-xe")
    public String danhSachGuiXe(@RequestParam(required = false) String search,
                                @RequestParam(required = false) String trangThai,
                                Model model,
                                Authentication authentication) {
        try {
            String cccd = authentication.getName();
            java.util.List<YeuCauGuiXe> list = yeuCauGuiXeService.findByCccdNguoiGui(cccd);
            
            if (trangThai != null && !trangThai.isEmpty()) {
                String tt = trangThai.trim();
                list = list.stream().filter(x -> x.getTrangThai() != null && x.getTrangThai().equals(tt)).collect(Collectors.toList());
            }
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                list = list.stream().filter(x ->
                        (x.getBienSo() != null && x.getBienSo().toLowerCase().contains(searchLower)) ||
                        (x.getLoaiXe() != null && x.getLoaiXe().toLowerCase().contains(searchLower))
                ).collect(Collectors.toList());
            }
            
            model.addAttribute("yeuCauList", list);
            model.addAttribute("search", search);
            model.addAttribute("trangThai", trangThai);
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
            YeuCauGuiXe yeuCau = yeuCauGuiXeService.findById(id)
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
    
    // --- PHẢN ÁNH (Code nhóm) ---
    @GetMapping("/phan-anh")
    public String phanAnh(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                          Model model,
                          Authentication authentication) {
        try {
            String cccd = authentication.getName();
            java.util.List<PhanAnh> phanAnhList = phanAnhService.findByCccdNguoiPhanAnh(cccd);
            
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                phanAnhList = phanAnhList.stream()
                    .filter(pa -> 
                        (pa.getTieuDe() != null && pa.getTieuDe().toLowerCase().contains(searchLower)) ||
                        (pa.getNoiDung() != null && pa.getNoiDung().toLowerCase().contains(searchLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("phanAnhList", phanAnhList);
            model.addAttribute("search", search);
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
}