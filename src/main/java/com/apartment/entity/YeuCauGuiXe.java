package com.apartment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "yeu_cau_gui_xe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YeuCauGuiXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_yeu_cau")
    private Integer maYeuCau;

    @Column(name = "cccd_nguoi_gui", length = 12, nullable = false)
    private String cccdNguoiGui;

    @Column(name = "ma_ho", length = 20)
    private String maHo;

    @Column(name = "bien_so", length = 20, nullable = false)
    private String bienSo;

    @Column(name = "loai_xe", length = 20)
    private String loaiXe; // oto, xe_may, khac

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "trang_thai", length = 20)
    private String trangThai = "cho_duyet"; // cho_duyet, da_duyet, tu_choi

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_xu_ly")
    private LocalDateTime ngayXuLy;

    @Column(name = "cccd_nguoi_xu_ly", length = 12)
    private String cccdNguoiXuLy;

    @Column(name = "ghi_chu_xu_ly", columnDefinition = "TEXT")
    private String ghiChuXuLy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd_nguoi_gui", referencedColumnName = "cccd", insertable = false, updatable = false)
    private DoiTuong nguoiGui;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cccd_nguoi_xu_ly", referencedColumnName = "cccd", insertable = false, updatable = false)
    private DoiTuong nguoiXuLy;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
        if (trangThai == null) {
            trangThai = "cho_duyet";
        }
    }
}

