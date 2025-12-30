package com.apartment.controller;

import com.apartment.entity.ThongBao;
import com.apartment.entity.ThongBaoHo;
import com.apartment.entity.HoGiaDinh;
import com.apartment.service.ThongBaoService;
import com.apartment.service.HoGiaDinhService;
import com.apartment.repository.ThongBaoHoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/thong-bao-2")
@PreAuthorize("hasRole('BAN_QUAN_TRI')")
public class AdminThongBao2Controller {

    @Autowired
    private ThongBaoService thongBaoService;
    
    @Autowired
    private HoGiaDinhService hoGiaDinhService;
    
    @Autowired
    private ThongBaoHoRepository thongBaoHoRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "20") int size,
                      Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ThongBao> thongBaoPage;
            
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
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi khi tải danh sách thông báo: " + e.getMessage());
            model.addAttribute("thongBaoList", Collections.emptyList());
        }
        return "admin/thong-bao-2/list";
    }

    @GetMapping("/create")
    public String createForm(Model model, Authentication authentication) {
        ThongBao thongBao = new ThongBao();
        thongBao.setCccdBanQuanTri(authentication.getName());
        model.addAttribute("thongBao", thongBao);
        model.addAttribute("isEdit", false);
        model.addAttribute("hoGiaDinhList", hoGiaDinhService.findAll());
        model.addAttribute("selectedHoList", Collections.emptyList());
        return "admin/thong-bao-2/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes ra) {
        try {
            ThongBao thongBao = thongBaoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
            model.addAttribute("thongBao", thongBao);
            model.addAttribute("isEdit", true);
            model.addAttribute("hoGiaDinhList", hoGiaDinhService.findAll());
            
            // Lấy danh sách hộ đã được gửi thông báo
            List<String> selectedHoList = thongBaoHoRepository.findByMaThongBao(id)
                    .stream()
                    .map(ThongBaoHo::getMaHo)
                    .collect(Collectors.toList());
            model.addAttribute("selectedHoList", selectedHoList);
            
            return "admin/thong-bao-2/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không tìm thấy thông báo: " + e.getMessage());
            return "redirect:/admin/thong-bao-2";
        }
    }

    @PostMapping("/save")
    @Transactional
    public String save(@ModelAttribute ThongBao thongBao, 
                       @RequestParam(value = "selectedHoGiaDinh", required = false) List<String> selectedHoGiaDinh,
                       RedirectAttributes ra) {
        try {
            ThongBao savedThongBao = thongBaoService.save(thongBao);
            
            // Nếu đối tượng nhận là hộ gia đình và có chọn các hộ
            if ("ho_gia_dinh".equals(thongBao.getDoiTuongNhan()) && selectedHoGiaDinh != null && !selectedHoGiaDinh.isEmpty()) {
                // Xóa các liên kết cũ nếu đang edit
                if (thongBao.getMaThongBao() != null) {
                    thongBaoHoRepository.deleteByMaThongBao(thongBao.getMaThongBao());
                }
                
                // Tạo liên kết mới cho các hộ được chọn
                for (String maHo : selectedHoGiaDinh) {
                    ThongBaoHo thongBaoHo = new ThongBaoHo();
                    thongBaoHo.setMaThongBao(savedThongBao.getMaThongBao());
                    thongBaoHo.setMaHo(maHo);
                    thongBaoHo.setDaXem(false);
                    thongBaoHoRepository.save(thongBaoHo);
                }
            }
            
            ra.addFlashAttribute("success", "Lưu thông báo thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/thong-bao-2";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            // Xóa các liên kết thông báo - hộ trước
            thongBaoHoRepository.deleteByMaThongBao(id);
            // Sau đó xóa thông báo
            thongBaoService.delete(id);
            ra.addFlashAttribute("success", "Xóa thông báo thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/thong-bao-2";
    }
}

