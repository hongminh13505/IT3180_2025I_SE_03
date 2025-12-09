package com.apartment.service;

import com.apartment.entity.DoiTuong;
import com.apartment.entity.LichSuChinhSua;
import com.apartment.repository.LichSuChinhSuaRepository;
import com.apartment.repository.DoiTuongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LichSuChinhSuaService {
    
    @Autowired
    private LichSuChinhSuaRepository lichSuChinhSuaRepository;
    
    @Autowired
    private DoiTuongRepository doiTuongRepository;
    
    /**
     * Ghi lại lịch sử chỉnh sửa
     */
    @Transactional
    public void ghiLichSuChinhSua(String loaiDoiTuong, String maDoiTuong, String tenDoiTuong,
                                   String cccdNguoiChinhSua, String thaoTac,
                                   Map<String, ChangeInfo> thayDoi, String moTa) {
        try {
            DoiTuong nguoiChinhSua = doiTuongRepository.findById(cccdNguoiChinhSua).orElse(null);
            
            String tenNguoiChinhSua = nguoiChinhSua != null ? nguoiChinhSua.getHoVaTen() : "Không xác định";
            String vaiTroNguoiChinhSua = nguoiChinhSua != null ? nguoiChinhSua.getVaiTro() : "";
            
            // Xác định nguồn chỉnh sửa
            String nguonChinhSua = "ban_quan_tri".equals(vaiTroNguoiChinhSua) ? "admin" : "nguoi_dung";
            
            // Tạo danh sách các trường thay đổi
            List<String> truongThayDoiList = new ArrayList<>();
            List<String> giaTriCuList = new ArrayList<>();
            List<String> giaTriMoiList = new ArrayList<>();
            
            for (Map.Entry<String, ChangeInfo> entry : thayDoi.entrySet()) {
                truongThayDoiList.add(entry.getKey());
                giaTriCuList.add(entry.getValue().getGiaTriCu() != null ? entry.getValue().getGiaTriCu() : "");
                giaTriMoiList.add(entry.getValue().getGiaTriMoi() != null ? entry.getValue().getGiaTriMoi() : "");
            }
            
            String truongThayDoi = String.join(", ", truongThayDoiList);
            String giaTriCu = String.join(" | ", giaTriCuList);
            String giaTriMoi = String.join(" | ", giaTriMoiList);
            
            LichSuChinhSua lichSu = new LichSuChinhSua();
            lichSu.setLoaiDoiTuong(loaiDoiTuong);
            lichSu.setMaDoiTuong(maDoiTuong);
            lichSu.setTenDoiTuong(tenDoiTuong);
            lichSu.setCccdNguoiChinhSua(cccdNguoiChinhSua);
            lichSu.setTenNguoiChinhSua(tenNguoiChinhSua);
            lichSu.setVaiTroNguoiChinhSua(vaiTroNguoiChinhSua);
            lichSu.setNguonChinhSua(nguonChinhSua);
            lichSu.setThaoTac(thaoTac);
            lichSu.setTruongThayDoi(truongThayDoi);
            lichSu.setGiaTriCu(giaTriCu);
            lichSu.setGiaTriMoi(giaTriMoi);
            lichSu.setMoTa(moTa);
            lichSu.setThoiGian(LocalDateTime.now());
            
            lichSuChinhSuaRepository.save(lichSu);
        } catch (Exception e) {
            // Log lỗi nhưng không throw để không ảnh hưởng đến luồng chính
            System.err.println("Lỗi khi ghi lịch sử chỉnh sửa: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lấy lịch sử chỉnh sửa theo loại đối tượng và mã đối tượng
     */
    public List<LichSuChinhSua> findByLoaiDoiTuongAndMaDoiTuong(String loaiDoiTuong, String maDoiTuong) {
        return lichSuChinhSuaRepository.findByLoaiDoiTuongAndMaDoiTuongOrderByThoiGianDesc(loaiDoiTuong, maDoiTuong);
    }
    
    /**
     * Lấy tất cả lịch sử chỉnh sửa
     */
    public List<LichSuChinhSua> findAll() {
        return lichSuChinhSuaRepository.findAllOrderByThoiGianDesc();
    }
    
    /**
     * Lấy lịch sử chỉnh sửa theo người chỉnh sửa
     */
    public List<LichSuChinhSua> findByCccdNguoiChinhSua(String cccd) {
        return lichSuChinhSuaRepository.findByCccdNguoiChinhSuaOrderByThoiGianDesc(cccd);
    }
    
    /**
     * Lấy lịch sử chỉnh sửa theo nguồn (admin hoặc người dùng)
     */
    public List<LichSuChinhSua> findByNguonChinhSua(String nguon) {
        return lichSuChinhSuaRepository.findByNguonChinhSuaOrderByThoiGianDesc(nguon);
    }
    
    /**
     * Lấy lịch sử chỉnh sửa theo loại đối tượng
     */
    public List<LichSuChinhSua> findByLoaiDoiTuong(String loaiDoiTuong) {
        return lichSuChinhSuaRepository.findByLoaiDoiTuongOrderByThoiGianDesc(loaiDoiTuong);
    }
    
    /**
     * Lấy lịch sử chỉnh sửa theo ID
     */
    public java.util.Optional<LichSuChinhSua> findById(Integer id) {
        return lichSuChinhSuaRepository.findById(id);
    }
    
    /**
     * Class để lưu thông tin thay đổi
     */
    public static class ChangeInfo {
        private String giaTriCu;
        private String giaTriMoi;
        
        public ChangeInfo(String giaTriCu, String giaTriMoi) {
            this.giaTriCu = giaTriCu;
            this.giaTriMoi = giaTriMoi;
        }
        
        public String getGiaTriCu() {
            return giaTriCu;
        }
        
        public String getGiaTriMoi() {
            return giaTriMoi;
        }
    }
}

