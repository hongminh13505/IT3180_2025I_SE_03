package com.apartment.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apartment.entity.DoiTuong;
import com.apartment.repository.DoiTuongRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class DoiTuongExportService {

    @Autowired
    private DoiTuongRepository doiTuongRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export danh sách cư dân ra file Excel
     */
    public byte[] exportToExcel(List<DoiTuong> doiTuongList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách cư dân");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Create date style
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            dateStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"CCCD", "Họ và tên", "Ngày sinh", "Giới tính", "Quê quán", 
                               "Số điện thoại", "Email", "Nghề nghiệp", "Vai trò", "Là cư dân", 
                               "Trạng thái tài khoản", "Trạng thái dân cư", "Ngày tạo"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (DoiTuong doiTuong : doiTuongList) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(doiTuong.getCccd() != null ? doiTuong.getCccd() : "");
                cell0.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(doiTuong.getHoVaTen() != null ? doiTuong.getHoVaTen() : "");
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(2);
                if (doiTuong.getNgaySinh() != null) {
                    cell2.setCellValue(doiTuong.getNgaySinh().format(DATE_FORMATTER));
                } else {
                    cell2.setCellValue("");
                }
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(getGioiTinhText(doiTuong.getGioiTinh()));
                cell3.setCellStyle(dataStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(doiTuong.getQueQuan() != null ? doiTuong.getQueQuan() : "");
                cell4.setCellStyle(dataStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(doiTuong.getSoDienThoai() != null ? doiTuong.getSoDienThoai() : "");
                cell5.setCellStyle(dataStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(doiTuong.getEmail() != null ? doiTuong.getEmail() : "");
                cell6.setCellStyle(dataStyle);

                Cell cell7 = row.createCell(7);
                cell7.setCellValue(doiTuong.getNgheNghiep() != null ? doiTuong.getNgheNghiep() : "");
                cell7.setCellStyle(dataStyle);

                Cell cell8 = row.createCell(8);
                cell8.setCellValue(getVaiTroText(doiTuong.getVaiTro()));
                cell8.setCellStyle(dataStyle);

                Cell cell9 = row.createCell(9);
                cell9.setCellValue(doiTuong.getLaCuDan() != null && doiTuong.getLaCuDan() ? "Có" : "Không");
                cell9.setCellStyle(dataStyle);

                Cell cell10 = row.createCell(10);
                cell10.setCellValue(getTrangThaiTaiKhoanText(doiTuong.getTrangThaiTaiKhoan()));
                cell10.setCellStyle(dataStyle);

                Cell cell11 = row.createCell(11);
                cell11.setCellValue(getTrangThaiDanCuText(doiTuong.getTrangThaiDanCu()));
                cell11.setCellStyle(dataStyle);

                Cell cell12 = row.createCell(12);
                if (doiTuong.getNgayTao() != null) {
                    cell12.setCellValue(doiTuong.getNgayTao().format(DATETIME_FORMATTER));
                } else {
                    cell12.setCellValue("");
                }
                cell12.setCellStyle(dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export danh sách cư dân ra file PDF
     */
    public byte[] exportToPdf(List<DoiTuong> doiTuongList) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Load Vietnamese font
        BaseFont baseFont;
        try {
            baseFont = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            // Fallback to Helvetica if Arial not available
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED);
        }

        Font titleFont = new Font(baseFont, 18, Font.BOLD, Color.DARK_GRAY);
        Font headerFont = new Font(baseFont, 9, Font.BOLD, Color.WHITE);
        Font dataFont = new Font(baseFont, 8, Font.NORMAL, Color.BLACK);

        // Title
        Paragraph title = new Paragraph("DANH SÁCH CƯ DÂN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Export date
        Paragraph exportDate = new Paragraph("Ngày xuất: " + LocalDateTime.now().format(DATETIME_FORMATTER), dataFont);
        exportDate.setAlignment(Element.ALIGN_RIGHT);
        exportDate.setSpacingAfter(15);
        document.add(exportDate);

        // Create table
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        float[] columnWidths = {1f, 1.5f, 1f, 0.8f, 1.2f, 1f, 1.2f, 1f, 1f, 1f};
        table.setWidths(columnWidths);

        // Header cells
        String[] headers = {"CCCD", "Họ và tên", "Ngày sinh", "GT", "SĐT", "Email", "Vai trò", "Cư dân", "TT TK", "TT DC"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(41, 128, 185));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            table.addCell(cell);
        }

        // Data cells
        for (DoiTuong doiTuong : doiTuongList) {
            addTableCell(table, doiTuong.getCccd() != null ? doiTuong.getCccd() : "", dataFont);
            addTableCell(table, doiTuong.getHoVaTen() != null ? doiTuong.getHoVaTen() : "", dataFont);
            addTableCell(table, doiTuong.getNgaySinh() != null ? doiTuong.getNgaySinh().format(DATE_FORMATTER) : "", dataFont);
            addTableCell(table, getGioiTinhText(doiTuong.getGioiTinh()), dataFont);
            addTableCell(table, doiTuong.getSoDienThoai() != null ? doiTuong.getSoDienThoai() : "", dataFont);
            addTableCell(table, doiTuong.getEmail() != null ? doiTuong.getEmail() : "", dataFont);
            addTableCell(table, getVaiTroText(doiTuong.getVaiTro()), dataFont);
            addTableCell(table, doiTuong.getLaCuDan() != null && doiTuong.getLaCuDan() ? "Có" : "Không", dataFont);
            addTableCell(table, getTrangThaiTaiKhoanText(doiTuong.getTrangThaiTaiKhoan()), dataFont);
            addTableCell(table, getTrangThaiDanCuText(doiTuong.getTrangThaiDanCu()), dataFont);
        }

        document.add(table);

        // Summary
        long totalCount = doiTuongList.size();
        long cuDanCount = doiTuongList.stream()
                .filter(dt -> dt.getLaCuDan() != null && dt.getLaCuDan())
                .count();
        long activeCount = doiTuongList.stream()
                .filter(dt -> "hoat_dong".equals(dt.getTrangThaiTaiKhoan()))
                .count();

        Paragraph summary = new Paragraph();
        summary.setSpacingBefore(20);
        summary.add(new Chunk("Tổng số: " + totalCount + " | ", dataFont));
        summary.add(new Chunk("Cư dân: " + cuDanCount + " | ", dataFont));
        summary.add(new Chunk("Hoạt động: " + activeCount, new Font(baseFont, 9, Font.BOLD)));
        document.add(summary);

        document.close();
        return outputStream.toByteArray();
    }

    /**
     * Import cư dân từ file Excel
     */
    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        List<DoiTuong> importedList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;
                
                try {
                    DoiTuong doiTuong = parseRowToDoiTuong(row, rowNum);
                    if (doiTuong != null) {
                        // Kiểm tra CCCD đã tồn tại chưa
                        if (doiTuongRepository.existsById(doiTuong.getCccd())) {
                            errorCount++;
                            errors.add("Dòng " + rowNum + ": CCCD '" + doiTuong.getCccd() + "' đã tồn tại trong hệ thống");
                            continue;
                        }
                        
                        doiTuongRepository.save(doiTuong);
                        importedList.add(doiTuong);
                        successCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    errors.add("Dòng " + rowNum + ": " + e.getMessage());
                }
            }
        }

        return new ImportResult(successCount, errorCount, errors, importedList);
    }

    /**
     * Tạo file Excel mẫu cho import
     */
    public byte[] createTemplateExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Template Import Cư Dân");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"CCCD (*)", "Họ và tên (*)", "Ngày sinh (*)", "Giới tính", "Quê quán", 
                               "Số điện thoại", "Email", "Nghề nghiệp", "Vai trò (*)", "Là cư dân (*)", 
                               "Mật khẩu (*)", "Trạng thái tài khoản", "Trạng thái dân cư"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample data row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("001234567890");
            sampleRow.createCell(1).setCellValue("Nguyễn Văn A");
            sampleRow.createCell(2).setCellValue("01/01/1990");
            sampleRow.createCell(3).setCellValue("nam");
            sampleRow.createCell(4).setCellValue("Hà Nội");
            sampleRow.createCell(5).setCellValue("0123456789");
            sampleRow.createCell(6).setCellValue("nguyenvana@email.com");
            sampleRow.createCell(7).setCellValue("Kỹ sư");
            sampleRow.createCell(8).setCellValue("nguoi_dung_thuong");
            sampleRow.createCell(9).setCellValue("true");
            sampleRow.createCell(10).setCellValue("123456");
            sampleRow.createCell(11).setCellValue("hoat_dong");
            sampleRow.createCell(12).setCellValue("o_chung_cu");

            // Instructions sheet
            Sheet instructionSheet = workbook.createSheet("Hướng dẫn");
            Row r1 = instructionSheet.createRow(0);
            r1.createCell(0).setCellValue("HƯỚNG DẪN IMPORT CƯ DÂN");
            
            Row r3 = instructionSheet.createRow(2);
            r3.createCell(0).setCellValue("Giới tính:");
            r3.createCell(1).setCellValue("nam, nu");

            Row r4 = instructionSheet.createRow(3);
            r4.createCell(0).setCellValue("Vai trò (bắt buộc):");
            r4.createCell(1).setCellValue("ban_quan_tri, co_quan_chuc_nang, ke_toan, nguoi_dung_thuong, khong_dung_he_thong");

            Row r5 = instructionSheet.createRow(4);
            r5.createCell(0).setCellValue("Là cư dân (bắt buộc):");
            r5.createCell(1).setCellValue("true, false");

            Row r6 = instructionSheet.createRow(5);
            r6.createCell(0).setCellValue("Trạng thái tài khoản:");
            r6.createCell(1).setCellValue("hoat_dong, khoa, tam_ngung");

            Row r7 = instructionSheet.createRow(6);
            r7.createCell(0).setCellValue("Trạng thái dân cư:");
            r7.createCell(1).setCellValue("roi_di, o_chung_cu, da_chet");

            Row r8 = instructionSheet.createRow(7);
            r8.createCell(0).setCellValue("Ngày sinh:");
            r8.createCell(1).setCellValue("Định dạng dd/MM/yyyy (ví dụ: 01/01/1990)");

            Row r9 = instructionSheet.createRow(8);
            r9.createCell(0).setCellValue("(*) Các trường bắt buộc");

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            instructionSheet.autoSizeColumn(0);
            instructionSheet.autoSizeColumn(1);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private DoiTuong parseRowToDoiTuong(Row row, int rowNum) {
        String cccd = getCellStringValue(row.getCell(0));
        if (cccd == null || cccd.trim().isEmpty()) {
            throw new IllegalArgumentException("CCCD không được để trống");
        }
        if (cccd.length() != 12) {
            throw new IllegalArgumentException("CCCD phải có đúng 12 ký tự");
        }

        String hoVaTen = getCellStringValue(row.getCell(1));
        if (hoVaTen == null || hoVaTen.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ và tên không được để trống");
        }

        LocalDate ngaySinh = getCellDateValue(row.getCell(2));
        if (ngaySinh == null) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ (định dạng: dd/MM/yyyy)");
        }

        String gioiTinh = getCellStringValue(row.getCell(3));
        if (gioiTinh != null && !gioiTinh.trim().isEmpty()) {
            gioiTinh = gioiTinh.trim().toLowerCase();
            if (!gioiTinh.equals("nam") && !gioiTinh.equals("nu")) {
                throw new IllegalArgumentException("Giới tính chỉ được phép: nam, nu");
            }
        }

        String queQuan = getCellStringValue(row.getCell(4));
        String soDienThoai = getCellStringValue(row.getCell(5));
        String email = getCellStringValue(row.getCell(6));
        String ngheNghiep = getCellStringValue(row.getCell(7));

        String vaiTro = getCellStringValue(row.getCell(8));
        if (vaiTro == null || vaiTro.trim().isEmpty()) {
            throw new IllegalArgumentException("Vai trò không được để trống");
        }
        vaiTro = vaiTro.trim().toLowerCase();
        List<String> validVaiTro = List.of("ban_quan_tri", "co_quan_chuc_nang", "ke_toan", "nguoi_dung_thuong", "khong_dung_he_thong");
        if (!validVaiTro.contains(vaiTro)) {
            throw new IllegalArgumentException("Vai trò không hợp lệ. Chỉ chấp nhận: " + String.join(", ", validVaiTro));
        }

        Boolean laCuDan = getCellBooleanValue(row.getCell(9));
        if (laCuDan == null) {
            throw new IllegalArgumentException("Là cư dân không được để trống (true/false)");
        }

        String matKhau = getCellStringValue(row.getCell(10));
        if (matKhau == null || matKhau.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }

        String trangThaiTaiKhoan = getCellStringValue(row.getCell(11));
        if (trangThaiTaiKhoan == null || trangThaiTaiKhoan.trim().isEmpty()) {
            trangThaiTaiKhoan = "hoat_dong";
        } else {
            trangThaiTaiKhoan = trangThaiTaiKhoan.trim().toLowerCase();
            List<String> validTrangThai = List.of("hoat_dong", "khoa", "tam_ngung");
            if (!validTrangThai.contains(trangThaiTaiKhoan)) {
                throw new IllegalArgumentException("Trạng thái tài khoản không hợp lệ. Chỉ chấp nhận: " + String.join(", ", validTrangThai));
            }
        }

        String trangThaiDanCu = getCellStringValue(row.getCell(12));
        if (trangThaiDanCu != null && !trangThaiDanCu.trim().isEmpty()) {
            trangThaiDanCu = trangThaiDanCu.trim().toLowerCase();
            List<String> validTrangThaiDanCu = List.of("roi_di", "o_chung_cu", "da_chet");
            if (!validTrangThaiDanCu.contains(trangThaiDanCu)) {
                throw new IllegalArgumentException("Trạng thái dân cư không hợp lệ. Chỉ chấp nhận: " + String.join(", ", validTrangThaiDanCu));
            }
        }

        DoiTuong doiTuong = new DoiTuong();
        doiTuong.setCccd(cccd.trim());
        doiTuong.setHoVaTen(hoVaTen.trim());
        doiTuong.setNgaySinh(ngaySinh);
        doiTuong.setGioiTinh(gioiTinh);
        doiTuong.setQueQuan(queQuan);
        doiTuong.setSoDienThoai(soDienThoai);
        doiTuong.setEmail(email);
        doiTuong.setNgheNghiep(ngheNghiep);
        doiTuong.setVaiTro(vaiTro);
        doiTuong.setLaCuDan(laCuDan);
        doiTuong.setMatKhau(passwordEncoder.encode(matKhau.trim()));
        doiTuong.setTrangThaiTaiKhoan(trangThaiTaiKhoan);
        doiTuong.setTrangThaiDanCu(trangThaiDanCu);

        return doiTuong;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FORMATTER);
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private Boolean getCellBooleanValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case STRING:
                String value = cell.getStringCellValue().trim().toLowerCase();
                if ("true".equals(value) || "1".equals(value) || "yes".equals(value) || "có".equals(value)) {
                    return true;
                } else if ("false".equals(value) || "0".equals(value) || "no".equals(value) || "không".equals(value)) {
                    return false;
                }
                return null;
            case NUMERIC:
                return cell.getNumericCellValue() != 0;
            default:
                return null;
        }
    }

    private LocalDate getCellDateValue(Cell cell) {
        if (cell == null) return null;
        
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getLocalDateTimeCellValue().toLocalDate();
                    }
                    break;
                case STRING:
                    String dateStr = cell.getStringCellValue().trim();
                    return LocalDate.parse(dateStr, DATE_FORMATTER);
            }
        } catch (Exception e) {
            // Try alternative format
            try {
                String dateStr = getCellStringValue(cell);
                if (dateStr != null) {
                    return LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String getGioiTinhText(String gioiTinh) {
        if (gioiTinh == null) return "";
        switch (gioiTinh.toLowerCase()) {
            case "nam": return "Nam";
            case "nu": return "Nữ";
            default: return gioiTinh;
        }
    }

    private String getVaiTroText(String vaiTro) {
        if (vaiTro == null) return "";
        switch (vaiTro) {
            case "ban_quan_tri": return "Ban quản trị";
            case "co_quan_chuc_nang": return "Cơ quan chức năng";
            case "ke_toan": return "Kế toán";
            case "nguoi_dung_thuong": return "Người dùng thường";
            case "khong_dung_he_thong": return "Không dùng hệ thống";
            default: return vaiTro;
        }
    }

    private String getTrangThaiTaiKhoanText(String trangThai) {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "hoat_dong": return "Hoạt động";
            case "khoa": return "Khóa";
            case "tam_ngung": return "Tạm ngưng";
            default: return trangThai;
        }
    }

    private String getTrangThaiDanCuText(String trangThai) {
        if (trangThai == null) return "";
        switch (trangThai) {
            case "roi_di": return "Rời đi";
            case "o_chung_cu": return "Ở chung cư";
            case "da_chet": return "Đã chết";
            default: return trangThai;
        }
    }

    /**
     * Class để lưu kết quả import
     */
    public static class ImportResult {
        private final int successCount;
        private final int errorCount;
        private final List<String> errors;
        private final List<DoiTuong> importedList;

        public ImportResult(int successCount, int errorCount, List<String> errors, List<DoiTuong> importedList) {
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.errors = errors;
            this.importedList = importedList;
        }

        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public List<String> getErrors() { return errors; }
        public List<DoiTuong> getImportedList() { return importedList; }
    }
}

