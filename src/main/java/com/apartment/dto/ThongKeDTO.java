package com.apartment.dto;

import java.math.BigDecimal;

public class ThongKeDTO {
    private Integer year;
    private Integer month;
    private String loaiHoaDon;
    private BigDecimal tongThu;
    private BigDecimal tongNo;
    private Long soLuong;
    
    public ThongKeDTO() {
    }
    
    public ThongKeDTO(Integer year, Integer month, String loaiHoaDon, BigDecimal tongThu, BigDecimal tongNo, Long soLuong) {
        this.year = year;
        this.month = month;
        this.loaiHoaDon = loaiHoaDon;
        this.tongThu = tongThu != null ? tongThu : BigDecimal.ZERO;
        this.tongNo = tongNo != null ? tongNo : BigDecimal.ZERO;
        this.soLuong = soLuong != null ? soLuong : 0L;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    public String getLoaiHoaDon() {
        return loaiHoaDon;
    }
    
    public void setLoaiHoaDon(String loaiHoaDon) {
        this.loaiHoaDon = loaiHoaDon;
    }
    
    public BigDecimal getTongThu() {
        return tongThu;
    }
    
    public void setTongThu(BigDecimal tongThu) {
        this.tongThu = tongThu;
    }
    
    public BigDecimal getTongNo() {
        return tongNo;
    }
    
    public void setTongNo(BigDecimal tongNo) {
        this.tongNo = tongNo;
    }
    
    public Long getSoLuong() {
        return soLuong;
    }
    
    public void setSoLuong(Long soLuong) {
        this.soLuong = soLuong;
    }
}

