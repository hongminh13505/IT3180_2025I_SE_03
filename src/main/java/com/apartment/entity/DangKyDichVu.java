package com.apartment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dang_ky_dich_vu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DangKyDichVu {
    
    @Id
    @Column(name = "ma_dang_ky")
    private Integer maDangKy;
    
    @Column(name = "cccd_nguoi_dung", nullable = false, length = 12)
    private String cccdNguoiDung;
    
    @Column(name = "ma_dich_vu", nullable = false)
    private Integer maDichVu;
    
    @Column(name = "mo_ta_yeu_cau", columnDefinition = "TEXT")
    private String moTaYeuCau;
    
    @Column(name = "ngay_dang_ky")
    private LocalDateTime ngayDangKy;
    
    @Column(name = "ngay_bat_dau_su_dung")
    private LocalDate ngayBatDauSuDung;
    
    @Column(name = "ngay_ket_thuc_su_dung")
    private LocalDate ngayKetThucSuDung;
    
    @Column(name = "trang_thai", length = 20)
    private String trangThai = "cho_duyet";
    
    @Column(name = "cccd_nguoi_duyet", length = 12)
    private String cccdNguoiDuyet;
    
    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet;
    
    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;
    
    @ManyToOne
    @JoinColumn(name = "cccd_nguoi_dung", referencedColumnName = "cccd", insertable = false, updatable = false)
    private DoiTuong nguoiDung;
    
    @ManyToOne
    @JoinColumn(name = "cccd_nguoi_duyet", referencedColumnName = "cccd", insertable = false, updatable = false)
    private DoiTuong nguoiDuyet;
    
    @PrePersist
    protected void onCreate() {
        ngayDangKy = LocalDateTime.now();
    }
}


