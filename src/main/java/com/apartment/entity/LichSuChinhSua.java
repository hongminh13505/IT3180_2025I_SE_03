package com.apartment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lich_su_chinh_sua")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LichSuChinhSua {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "loai_doi_tuong", nullable = false, length = 50)
    private String loaiDoiTuong; // "doi_tuong", "ho_gia_dinh", "thanh_vien_ho"
    
    @Column(name = "ma_doi_tuong", nullable = false, length = 50)
    private String maDoiTuong; // CCCD hoặc mã hộ
    
    @Column(name = "ten_doi_tuong", length = 200)
    private String tenDoiTuong; // Tên để hiển thị
    
    @Column(name = "cccd_nguoi_chinh_sua", nullable = false, length = 12)
    private String cccdNguoiChinhSua;
    
    @Column(name = "ten_nguoi_chinh_sua", length = 100)
    private String tenNguoiChinhSua;
    
    @Column(name = "vai_tro_nguoi_chinh_sua", length = 50)
    private String vaiTroNguoiChinhSua; // "ban_quan_tri", "nguoi_dung_thuong", etc.
    
    @Column(name = "nguon_chinh_sua", length = 20)
    private String nguonChinhSua; // "admin", "nguoi_dung"
    
    @Column(name = "thao_tac", length = 50)
    private String thaoTac; // "create", "update", "delete"
    
    @Column(name = "truong_thay_doi", columnDefinition = "TEXT")
    private String truongThayDoi; // JSON hoặc danh sách các trường đã thay đổi
    
    @Column(name = "gia_tri_cu", columnDefinition = "TEXT")
    private String giaTriCu;
    
    @Column(name = "gia_tri_moi", columnDefinition = "TEXT")
    private String giaTriMoi;
    
    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;
    
    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;
    
    @PrePersist
    protected void onCreate() {
        if (thoiGian == null) {
            thoiGian = LocalDateTime.now();
        }
    }
    
    @ManyToOne
    @JoinColumn(name = "cccd_nguoi_chinh_sua", referencedColumnName = "cccd", insertable = false, updatable = false)
    private DoiTuong nguoiChinhSua;
}

