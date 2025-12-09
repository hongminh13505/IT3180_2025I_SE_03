-- =====================================================
-- HỆ THỐNG QUẢN LÝ CHUNG CƯ - POSTGRESQL SCHEMA V2
-- Hỗ trợ lưu lịch sử đầy đủ
-- =====================================================

-- =====================================================
-- PHẦN 1: QUẢN LÝ NGƯỜI DÙNG & CƯ DÂN
-- =====================================================

-- Bảng người dùng (tài khoản đăng nhập hệ thống)
CREATE TABLE doi_tuong (
    cccd VARCHAR(12) PRIMARY KEY,
    mat_khau VARCHAR(255) NOT NULL,
    vai_tro VARCHAR(20) NOT NULL CHECK (vai_tro IN ('ban_quan_tri', 'co_quan_chuc_nang', 'ke_toan', 'nguoi_dung_thuong', 'khong_dung_he_thong')),
    la_cu_dan BOOLEAN NOT NULL,
    ho_va_ten VARCHAR(100) NOT NULL,
    ngay_sinh DATE NOT NULL,
    gioi_tinh VARCHAR(10) CHECK (gioi_tinh IN ('nam', 'nu')),
    que_quan TEXT,
    so_dien_thoai VARCHAR(15),
    email VARCHAR(100),
    nghe_nghiep VARCHAR(100),
    trang_thai_tai_khoan VARCHAR(20) DEFAULT 'hoat_dong' CHECK (trang_thai_tai_khoan IN ('hoat_dong', 'khoa', 'tam_ngung')),
    trang_thai_dan_cu VARCHAR(20) CHECK (trang_thai_dan_cu IN ('roi_di', 'o_chung_cu', 'da_chet')),
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lan_dang_nhap_cuoi TIMESTAMP,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_ngay_sinh_nguoidung CHECK (ngay_sinh <= CURRENT_DATE)
);

-- Bảng hộ gia đình
CREATE TABLE ho_gia_dinh (
    ma_ho VARCHAR(20) PRIMARY KEY,
    ten_ho VARCHAR(100) NOT NULL,
    ma_can_ho INTEGER UNIQUE, -- Liên kết với bảng tai_san_chung_cu
    ngay_thanh_lap DATE DEFAULT CURRENT_DATE,
    trang_thai VARCHAR(20) DEFAULT 'hoat_dong' CHECK (trang_thai IN ('hoat_dong', 'da_chuyen_di', 'giai_the')),
    ghi_chu TEXT,
    ngay_cap_nhat TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng thành viên hộ (lưu lịch sử quan hệ)
CREATE TABLE thanh_vien_ho (
    cccd VARCHAR(12),
    ma_ho VARCHAR(20) NOT NULL,
    la_chu_ho BOOLEAN DEFAULT FALSE,
    quan_he_voi_chu_ho VARCHAR(50),
    ngay_bat_dau DATE NOT NULL DEFAULT CURRENT_DATE,
    ngay_ket_thuc DATE, -- NULL = đang sinh sống
    ly_do_ket_thuc VARCHAR(100), -- 'chuyen_di', 'qua_doi', 'chuyen_chu_ho', 'tach_ho'
    ghi_chu TEXT,
    CONSTRAINT pk_thanh_vien_ho PRIMARY KEY (cccd, ngay_bat_dau),
    CONSTRAINT fk_ma_ho FOREIGN KEY (ma_ho) 
        REFERENCES ho_gia_dinh(ma_ho) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_cccd_thanh_vien FOREIGN KEY (cccd) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT check_ngay_thanh_vien CHECK (ngay_ket_thuc IS NULL OR ngay_ket_thuc >= ngay_bat_dau)
);

-- =====================================================
-- PHẦN 2: QUẢN LÝ TÀI SẢN
-- =====================================================

-- Bảng tài sản chung cư
CREATE TABLE tai_san_chung_cu (
    ma_tai_san SERIAL PRIMARY KEY,
    ten_tai_san VARCHAR(100) NOT NULL,
    loai_tai_san VARCHAR(30) NOT NULL CHECK (loai_tai_san IN ('can_ho', 'thiet_bi', 'tien_ich')),
    ma_ho VARCHAR(20), -- Hộ gia đình sở hữu/sử dụng
    trang_thai VARCHAR(20) DEFAULT 'hoat_dong' 
        CHECK (trang_thai IN ('hoat_dong', 'bao_tri', 'hong', 'ngung_hoat_dong')),
    dien_tich DECIMAL(10,2), -- m2 (cho căn hộ)
    vi_tri TEXT,
    gia_tri DECIMAL(15,2),
    ngay_them TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ma_ho_taisan FOREIGN KEY (ma_ho) 
        REFERENCES ho_gia_dinh(ma_ho) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE
);

-- Cập nhật liên kết ngược từ ho_gia_dinh đến tai_san_chung_cu
ALTER TABLE ho_gia_dinh
ADD CONSTRAINT fk_ma_can_ho FOREIGN KEY (ma_can_ho) 
    REFERENCES tai_san_chung_cu(ma_tai_san) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE;

-- =====================================================
-- PHẦN 3: QUẢN LÝ DỊCH VỤ
-- =====================================================

-- Bảng dịch vụ (các dịch vụ có phí)
CREATE TABLE dich_vu (
    ma_dich_vu SERIAL PRIMARY KEY,
    ten_dich_vu VARCHAR(100) NOT NULL,
    cccd_ban_quan_tri VARCHAR(12),
    mo_ta TEXT,
    gia_thanh DECIMAL(15,2) NOT NULL,
    don_vi VARCHAR(20), -- VD: VNĐ/tháng, VNĐ/lần
    loai_dich_vu VARCHAR(30) CHECK (loai_dich_vu IN ('dinh_ky', 'theo_yeu_cau')),
    trang_thai VARCHAR(20) DEFAULT 'hoat_dong' CHECK (trang_thai IN ('hoat_dong', 'tam_ngung')),
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cccd_bqt_dichvu FOREIGN KEY (cccd_ban_quan_tri) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT check_gia_thanh CHECK (gia_thanh >= 0)
);

-- Bảng đăng ký dịch vụ
CREATE TABLE dang_ky_dich_vu (
    ma_dang_ky INTEGER PRIMARY KEY,
    cccd_nguoi_dung VARCHAR(12) NOT NULL,
    ma_dich_vu INTEGER NOT NULL,
    mo_ta_yeu_cau TEXT,
    ngay_dang_ky TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ngay_bat_dau_su_dung DATE,
    ngay_ket_thuc_su_dung DATE, -- NULL = vô thời hạn (cho dịch vụ định kỳ)
    trang_thai VARCHAR(20) DEFAULT 'cho_duyet' 
        CHECK (trang_thai IN ('cho_duyet', 'da_duyet', 'dang_su_dung', 'da_huy', 'da_ket_thuc')),
    cccd_nguoi_duyet VARCHAR(12), -- BQL phê duyệt
    ngay_duyet TIMESTAMP,
    ghi_chu TEXT,
    CONSTRAINT fk_cccd_nguoidung_dkdv FOREIGN KEY (cccd_nguoi_dung) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ma_dichvu FOREIGN KEY (ma_dich_vu) 
        REFERENCES dich_vu(ma_dich_vu) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_cccd_nguoi_duyet FOREIGN KEY (cccd_nguoi_duyet) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE
);

-- =====================================================
-- PHẦN 4: BÁO CÁO SỰ CỐ (tách riêng khỏi dịch vụ)
-- =====================================================

-- Bảng báo cáo sự cố
CREATE TABLE bao_cao_su_co (
    ma_bao_cao SERIAL PRIMARY KEY,
    cccd_nguoi_bao_cao VARCHAR(12) NOT NULL, -- Cư dân báo sự cố
    cccd_nguoi_nhap VARCHAR(12), -- NULL nếu cư dân tự báo, có giá trị nếu BQL nhập thay
    tieu_de VARCHAR(200) NOT NULL,
    mo_ta_su_co TEXT NOT NULL,
    vi_tri_su_co TEXT, -- Vị trí xảy ra sự cố
    muc_do_uu_tien VARCHAR(20) DEFAULT 'binh_thuong' 
        CHECK (muc_do_uu_tien IN ('thap', 'binh_thuong', 'cao', 'khan_cap')),
    phuong_thuc_bao_cao VARCHAR(20) DEFAULT 'truc_tuyen' 
        CHECK (phuong_thuc_bao_cao IN ('truc_tuyen', 'truc_tiep', 'dien_thoai', 'email')),
    ngay_bao_cao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    trang_thai VARCHAR(20) DEFAULT 'moi_tiep_nhan' 
        CHECK (trang_thai IN ('moi_tiep_nhan', 'dang_xu_ly', 'cho_phe_duyet', 'da_hoan_thanh', 'da_huy')),
    cccd_nguoi_xu_ly VARCHAR(12), -- Nhân viên kỹ thuật xử lý
    ngay_bat_dau_xu_ly TIMESTAMP,
    ngay_hoan_thanh TIMESTAMP,
    ket_qua_xu_ly TEXT,
    chi_phi_xu_ly DECIMAL(15,2) DEFAULT 0,
    ghi_chu TEXT,
    CONSTRAINT fk_cccd_nguoi_bao_cao FOREIGN KEY (cccd_nguoi_bao_cao) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_cccd_nguoi_nhap FOREIGN KEY (cccd_nguoi_nhap) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT fk_cccd_nguoi_xu_ly FOREIGN KEY (cccd_nguoi_xu_ly) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT check_chi_phi CHECK (chi_phi_xu_ly >= 0)
);

-- Bảng ảnh đính kèm báo cáo sự cố
CREATE TABLE anh_bao_cao_su_co (
    ma_anh SERIAL PRIMARY KEY,
    ma_bao_cao INTEGER NOT NULL,
    duong_dan TEXT NOT NULL,
    mo_ta VARCHAR(200),
    ngay_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ma_bao_cao FOREIGN KEY (ma_bao_cao) 
        REFERENCES bao_cao_su_co(ma_bao_cao) 
        ON DELETE CASCADE
);

-- =====================================================
-- PHẦN 5: HÓA ĐƠN & THANH TOÁN
-- =====================================================

-- Bảng hóa đơn
CREATE TABLE hoa_don (
    ma_hoa_don SERIAL PRIMARY KEY,
    ma_ho VARCHAR(20) NOT NULL, -- Hóa đơn theo hộ gia đình
    so_tien DECIMAL(15,2) NOT NULL,
    ma_dich_vu INTEGER, -- Hóa đơn dịch vụ
    ma_bao_cao INTEGER, -- Hóa đơn chi phí sửa chữa (nếu có)
    ma_tai_san INTEGER, -- Căn hộ liên quan
    loai_hoa_don VARCHAR(30) CHECK (loai_hoa_don IN ('dich_vu', 'dien_nuoc', 'sua_chua', 'phat', 'khac')),
    trang_thai VARCHAR(20) DEFAULT 'chua_thanh_toan' 
        CHECK (trang_thai IN ('da_thanh_toan', 'chua_thanh_toan', 'qua_han', 'giam_tru')),
    ngay_tao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    han_thanh_toan DATE,
    ngay_thanh_toan TIMESTAMP,
    phuong_thuc_thanh_toan VARCHAR(30),
    ghi_chu TEXT,
    CONSTRAINT fk_ma_ho_hoadon FOREIGN KEY (ma_ho) 
        REFERENCES ho_gia_dinh(ma_ho) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ma_dichvu_hoadon FOREIGN KEY (ma_dich_vu) 
        REFERENCES dich_vu(ma_dich_vu) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ma_bao_cao_hoadon FOREIGN KEY (ma_bao_cao) 
        REFERENCES bao_cao_su_co(ma_bao_cao) 
        ON DELETE SET NULL,
    CONSTRAINT fk_ma_taisan_hoadon FOREIGN KEY (ma_tai_san) 
        REFERENCES tai_san_chung_cu(ma_tai_san) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT check_so_tien CHECK (so_tien >= 0),
    CONSTRAINT check_one_source_for_hoadon
    CHECK (
        (CASE WHEN ma_dich_vu IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN ma_bao_cao IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN ma_tai_san IS NOT NULL THEN 1 ELSE 0 END) <= 1)
);

-- =====================================================
-- PHẦN 6: CHỈ SỐ ĐIỆN NƯỚC
-- =====================================================

-- Bảng chỉ số điện nước
CREATE TABLE chi_so_dien_nuoc (
    ma_chi_so SERIAL PRIMARY KEY,
    ma_ho VARCHAR(20) NOT NULL,
    ma_hoa_don INTEGER, -- Foreign key đến bảng hoa_don
    ky_thanh_toan VARCHAR(20) NOT NULL, -- VD: "01/2024"
    dien_cu INTEGER NOT NULL DEFAULT 0,
    dien_moi INTEGER NOT NULL DEFAULT 0,
    nuoc_cu INTEGER NOT NULL DEFAULT 0,
    nuoc_moi INTEGER NOT NULL DEFAULT 0,
    don_gia_dien DECIMAL(15,2) DEFAULT 3500.00, -- VNĐ/số
    don_gia_nuoc DECIMAL(15,2) DEFAULT 10000.00, -- VNĐ/khối
    tien_dien DECIMAL(15,2) DEFAULT 0,
    tien_nuoc DECIMAL(15,2) DEFAULT 0,
    tien_dich_vu DECIMAL(15,2) DEFAULT 0, -- Tổng tiền dịch vụ khác
    tong_tien DECIMAL(15,2) DEFAULT 0,
    ngay_nhap TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ghi_chu TEXT,
    CONSTRAINT fk_ma_ho_chiso FOREIGN KEY (ma_ho) 
        REFERENCES ho_gia_dinh(ma_ho) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ma_hoa_don_chiso FOREIGN KEY (ma_hoa_don) 
        REFERENCES hoa_don(ma_hoa_don) 
        ON DELETE SET NULL 
        ON UPDATE CASCADE,
    CONSTRAINT check_dien_cu_moi CHECK (dien_moi >= dien_cu),
    CONSTRAINT check_nuoc_cu_moi CHECK (nuoc_moi >= nuoc_cu),
    CONSTRAINT check_chi_so_duong CHECK (dien_cu >= 0 AND dien_moi >= 0 AND nuoc_cu >= 0 AND nuoc_moi >= 0),
    CONSTRAINT check_tien_duong CHECK (tien_dien >= 0 AND tien_nuoc >= 0 AND tien_dich_vu >= 0 AND tong_tien >= 0),
    CONSTRAINT unique_ma_ho_ky UNIQUE (ma_ho, ky_thanh_toan)
);

-- =====================================================
-- PHẦN 7: THÔNG BÁO & PHẢN HỒI
-- =====================================================

-- Bảng thông báo
CREATE TABLE thong_bao (
    ma_thong_bao SERIAL PRIMARY KEY,
    cccd_ban_quan_tri VARCHAR(12) NOT NULL,
    tieu_de VARCHAR(200) NOT NULL,
    noi_dung_thong_bao TEXT NOT NULL,
    ngay_tao_thong_bao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    trang_thai VARCHAR(20) DEFAULT 'hien' CHECK (trang_thai IN ('an', 'hien')),
    loai_thong_bao VARCHAR(30) DEFAULT 'binh_thuong' 
        CHECK (loai_thong_bao IN ('quan_trong', 'binh_thuong', 'khan_cap')),
    doi_tuong_nhan VARCHAR(20) DEFAULT 'tat_ca' 
        CHECK (doi_tuong_nhan IN ('tat_ca', 'chu_ho', 'theo_ho')), -- Gửi cho ai
    CONSTRAINT fk_cccd_bqt_thongbao FOREIGN KEY (cccd_ban_quan_tri) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- Bảng thông báo gửi đến hộ cụ thể (nếu doi_tuong_nhan = 'theo_ho')
CREATE TABLE thong_bao_ho (
    id SERIAL PRIMARY KEY,
    ma_thong_bao INTEGER NOT NULL,
    ma_ho VARCHAR(20) NOT NULL,
    da_xem BOOLEAN DEFAULT FALSE,
    ngay_xem TIMESTAMP,
    CONSTRAINT fk_ma_thongbao_ho FOREIGN KEY (ma_thong_bao) 
        REFERENCES thong_bao(ma_thong_bao) 
        ON DELETE CASCADE,
    CONSTRAINT fk_ma_ho_thongbao FOREIGN KEY (ma_ho) 
        REFERENCES ho_gia_dinh(ma_ho) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE
);

-- Bảng phản hồi
CREATE TABLE phan_hoi (
    ma_phan_hoi SERIAL PRIMARY KEY,
    cccd_nguoi_dung VARCHAR(12) NOT NULL,
    ma_thong_bao INTEGER NOT NULL,
    noi_dung_phan_hoi TEXT NOT NULL,
    ngay_phan_hoi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cccd_nguoidung_phanhoi FOREIGN KEY (cccd_nguoi_dung) 
        REFERENCES doi_tuong(cccd) 
        ON DELETE CASCADE 
        ON UPDATE CASCADE,
    CONSTRAINT fk_ma_thongbao FOREIGN KEY (ma_thong_bao) 
        REFERENCES thong_bao(ma_thong_bao) 
        ON DELETE CASCADE
);

-- =====================================================
-- PHẦN 8: LỊCH SỬ CHỈNH SỬA
-- =====================================================

-- Bảng lịch sử chỉnh sửa
CREATE TABLE IF NOT EXISTS lich_su_chinh_sua (
    id SERIAL PRIMARY KEY,
    loai_doi_tuong VARCHAR(50) NOT NULL,
    ma_doi_tuong VARCHAR(50) NOT NULL,
    ten_doi_tuong VARCHAR(200),
    cccd_nguoi_chinh_sua VARCHAR(12) NOT NULL,
    ten_nguoi_chinh_sua VARCHAR(100),
    vai_tro_nguoi_chinh_sua VARCHAR(50),
    nguon_chinh_sua VARCHAR(20),
    thao_tac VARCHAR(50),
    truong_thay_doi TEXT,
    gia_tri_cu TEXT,
    gia_tri_moi TEXT,
    mo_ta TEXT,
    thoi_gian TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_nguoi_chinh_sua FOREIGN KEY (cccd_nguoi_chinh_sua) 
        REFERENCES doi_tuong(cccd) ON DELETE SET NULL
);

-- Tạo index để tăng tốc độ truy vấn
CREATE INDEX IF NOT EXISTS idx_lich_su_loai_ma ON lich_su_chinh_sua(loai_doi_tuong, ma_doi_tuong);
CREATE INDEX IF NOT EXISTS idx_lich_su_cccd ON lich_su_chinh_sua(cccd_nguoi_chinh_sua);
CREATE INDEX IF NOT EXISTS idx_lich_su_thoi_gian ON lich_su_chinh_sua(thoi_gian DESC);
CREATE INDEX IF NOT EXISTS idx_lich_su_nguon ON lich_su_chinh_sua(nguon_chinh_sua);

-- Comment cho bảng
COMMENT ON TABLE lich_su_chinh_sua IS 'Bảng lưu lịch sử chỉnh sửa thông tin của các đối tượng trong hệ thống';
COMMENT ON COLUMN lich_su_chinh_sua.loai_doi_tuong IS 'Loại đối tượng: doi_tuong, ho_gia_dinh, thanh_vien_ho';
COMMENT ON COLUMN lich_su_chinh_sua.ma_doi_tuong IS 'Mã đối tượng (CCCD hoặc mã hộ)';
COMMENT ON COLUMN lich_su_chinh_sua.nguon_chinh_sua IS 'Nguồn chỉnh sửa: admin hoặc nguoi_dung';
COMMENT ON COLUMN lich_su_chinh_sua.thao_tac IS 'Thao tác: create, update, delete';