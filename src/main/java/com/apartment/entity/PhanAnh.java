package com.apartment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "phan_anh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhanAnh {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_phan_anh")
    private Integer maPhanAnh;
    
    @Column(name = "cccd_nguoi_phan_anh", nullable = false, length = 12)
    private String cccdNguoiPhanAnh;
    
    @Column(name = "tieu_de", nullable = false, length = 200)
    private String tieuDe;
    
    @Column(name = "noi_dung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;
    
    @Column(name = "loai_phan_anh", length = 20)
    private String loaiPhanAnh = "gop_y";
    
    @Column(name = "trang_thai", length = 20)
    private String trangThai = "moi";
    
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
    
    @Column(name = "ngay_phan_hoi")
    private LocalDateTime ngayPhanHoi;
    
    @Column(name = "noi_dung_phan_hoi", columnDefinition = "TEXT")
    private String noiDungPhanHoi;
    
    @Column(name = "cccd_nguoi_phan_hoi", length = 12)
    private String cccdNguoiPhanHoi;
    
    @ManyToOne
    @JoinColumn(name = "cccd_nguoi_phan_anh", referencedColumnName = "cccd", insertable = false, updatable = false)
    private DoiTuong nguoiPhanAnh;
    
    @ManyToOne
    @JoinColumn(name = "cccd_nguoi_phan_hoi", referencedColumnName = "cccd", insertable = false, updatable = false)
    private DoiTuong nguoiPhanHoi;
    
    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}

