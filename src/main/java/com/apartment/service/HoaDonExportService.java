package com.apartment.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apartment.entity.HoaDon;
import com.apartment.repository.HoaDonRepository;
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
public class HoaDonExportService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export danh sách hóa đơn ra file Excel
     */
    public byte[] exportToExcel(List<HoaDon> hoaDonList) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách hóa đơn");

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

            // Create number style
            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.cloneStyleFrom(dataStyle);
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0"));

            // Create date style
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.cloneStyleFrom(dataStyle);
            dateStyle.setDataFormat(format.getFormat("dd/MM/yyyy"));

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã hóa đơn", "Mã hộ", "Loại hóa đơn", "Số tiền (VNĐ)", "Hạn thanh toán", 
                               "Trạng thái", "Ngày tạo", "Ngày thanh toán", "Phương thức thanh toán", "Ghi chú"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (HoaDon hoaDon : hoaDonList) {
                Row row = sheet.createRow(rowNum++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(hoaDon.getMaHoaDon() != null ? hoaDon.getMaHoaDon() : 0);
                cell0.setCellStyle(dataStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(hoaDon.getMaHo() != null ? hoaDon.getMaHo() : "");
                cell1.setCellStyle(dataStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(getLoaiHoaDonText(hoaDon.getLoaiHoaDon()));
                cell2.setCellStyle(dataStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(hoaDon.getSoTien() != null ? hoaDon.getSoTien().doubleValue() : 0);
                cell3.setCellStyle(numberStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(hoaDon.getHanThanhToan() != null ? hoaDon.getHanThanhToan().format(DATE_FORMATTER) : "");
                cell4.setCellStyle(dataStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(getTrangThaiText(hoaDon.getTrangThai()));
                cell5.setCellStyle(dataStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(hoaDon.getNgayTao() != null ? hoaDon.getNgayTao().format(DATETIME_FORMATTER) : "");
                cell6.setCellStyle(dataStyle);

                Cell cell7 = row.createCell(7);
                cell7.setCellValue(hoaDon.getNgayThanhToan() != null ? hoaDon.getNgayThanhToan().format(DATETIME_FORMATTER) : "");
                cell7.setCellStyle(dataStyle);

                Cell cell8 = row.createCell(8);
                cell8.setCellValue(hoaDon.getPhuongThucThanhToan() != null ? hoaDon.getPhuongThucThanhToan() : "");
                cell8.setCellStyle(dataStyle);

                Cell cell9 = row.createCell(9);
                cell9.setCellValue(hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "");
                cell9.setCellStyle(dataStyle);
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
     * Export danh sách hóa đơn ra file PDF
     */
    public byte[] exportToPdf(List<HoaDon> hoaDonList) throws DocumentException, IOException {
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
        Font headerFont = new Font(baseFont, 10, Font.BOLD, Color.WHITE);
        Font dataFont = new Font(baseFont, 9, Font.NORMAL, Color.BLACK);

        // Title
        Paragraph title = new Paragraph("DANH SÁCH HÓA ĐƠN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Export date
        Paragraph exportDate = new Paragraph("Ngày xuất: " + LocalDateTime.now().format(DATETIME_FORMATTER), dataFont);
        exportDate.setAlignment(Element.ALIGN_RIGHT);
        exportDate.setSpacingAfter(15);
        document.add(exportDate);

        // Create table
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        float[] columnWidths = {0.8f, 1f, 1.2f, 1.5f, 1.2f, 1.3f, 1.2f, 2f};
        table.setWidths(columnWidths);

        // Header cells
        String[] headers = {"Mã HĐ", "Mã hộ", "Loại", "Số tiền", "Hạn TT", "Trạng thái", "Ngày tạo", "Ghi chú"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(41, 128, 185));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8);
            table.addCell(cell);
        }

        // Data cells
        for (HoaDon hoaDon : hoaDonList) {
            addTableCell(table, hoaDon.getMaHoaDon() != null ? hoaDon.getMaHoaDon().toString() : "", dataFont);
            addTableCell(table, hoaDon.getMaHo() != null ? hoaDon.getMaHo() : "", dataFont);
            addTableCell(table, getLoaiHoaDonText(hoaDon.getLoaiHoaDon()), dataFont);
            addTableCell(table, formatMoney(hoaDon.getSoTien()), dataFont);
            addTableCell(table, hoaDon.getHanThanhToan() != null ? hoaDon.getHanThanhToan().format(DATE_FORMATTER) : "", dataFont);
            addTableCell(table, getTrangThaiText(hoaDon.getTrangThai()), dataFont);
            addTableCell(table, hoaDon.getNgayTao() != null ? hoaDon.getNgayTao().format(DATE_FORMATTER) : "", dataFont);
            addTableCell(table, hoaDon.getGhiChu() != null ? hoaDon.getGhiChu() : "", dataFont);
        }

        document.add(table);

        // Summary
        BigDecimal totalAmount = hoaDonList.stream()
                .map(HoaDon::getSoTien)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidCount = hoaDonList.stream()
                .filter(hd -> "da_thanh_toan".equals(hd.getTrangThai()))
                .count();

        long unpaidCount = hoaDonList.stream()
                .filter(hd -> "chua_thanh_toan".equals(hd.getTrangThai()))
                .count();

        Paragraph summary = new Paragraph();
        summary.setSpacingBefore(20);
        summary.add(new Chunk("Tổng số hóa đơn: " + hoaDonList.size() + " | ", dataFont));
        summary.add(new Chunk("Đã thanh toán: " + paidCount + " | ", dataFont));
        summary.add(new Chunk("Chưa thanh toán: " + unpaidCount + " | ", dataFont));
        summary.add(new Chunk("Tổng tiền: " + formatMoney(totalAmount) + " VNĐ", new Font(baseFont, 10, Font.BOLD)));
        document.add(summary);

        document.close();
        return outputStream.toByteArray();
    }

    /**
     * Import hóa đơn từ file Excel
     */
    public ImportResult importFromExcel(MultipartFile file) throws IOException {
        List<HoaDon> importedList = new ArrayList<>();
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
                    HoaDon hoaDon = parseRowToHoaDon(row, rowNum);
                    if (hoaDon != null) {
                        hoaDonRepository.save(hoaDon);
                        importedList.add(hoaDon);
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
            Sheet sheet = workbook.createSheet("Template Import Hóa Đơn");

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

            // Create instruction style
            CellStyle instructionStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font instructionFont = workbook.createFont();
            instructionFont.setItalic(true);
            instructionFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            instructionStyle.setFont(instructionFont);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã hộ (*)", "Loại hóa đơn (*)", "Số tiền (*)", "Hạn thanh toán (*)", 
                               "Trạng thái", "Ghi chú"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample data row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("H001");
            sampleRow.createCell(1).setCellValue("dien_nuoc");
            sampleRow.createCell(2).setCellValue(500000);
            sampleRow.createCell(3).setCellValue("31/12/2024");
            sampleRow.createCell(4).setCellValue("chua_thanh_toan");
            sampleRow.createCell(5).setCellValue("Hóa đơn tháng 12");

            // Instructions sheet
            Sheet instructionSheet = workbook.createSheet("Hướng dẫn");
            Row r1 = instructionSheet.createRow(0);
            r1.createCell(0).setCellValue("HƯỚNG DẪN IMPORT HÓA ĐƠN");
            
            Row r3 = instructionSheet.createRow(2);
            r3.createCell(0).setCellValue("Loại hóa đơn (bắt buộc):");
            r3.createCell(1).setCellValue("dien_nuoc, dich_vu, sua_chua, phat, khac");

            Row r4 = instructionSheet.createRow(3);
            r4.createCell(0).setCellValue("Trạng thái:");
            r4.createCell(1).setCellValue("chua_thanh_toan, da_thanh_toan, qua_han");

            Row r5 = instructionSheet.createRow(4);
            r5.createCell(0).setCellValue("Hạn thanh toán:");
            r5.createCell(1).setCellValue("Định dạng dd/MM/yyyy (ví dụ: 31/12/2024)");

            Row r6 = instructionSheet.createRow(5);
            r6.createCell(0).setCellValue("(*) Các trường bắt buộc");

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

    private HoaDon parseRowToHoaDon(Row row, int rowNum) {
        String maHo = getCellStringValue(row.getCell(0));
        if (maHo == null || maHo.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hộ không được để trống");
        }

        String loaiHoaDon = getCellStringValue(row.getCell(1));
        if (loaiHoaDon == null || loaiHoaDon.trim().isEmpty()) {
            loaiHoaDon = "khac";
        }

        BigDecimal soTien = getCellNumericValue(row.getCell(2));
        if (soTien == null) {
            throw new IllegalArgumentException("Số tiền không hợp lệ");
        }

        LocalDate hanThanhToan = getCellDateValue(row.getCell(3));
        if (hanThanhToan == null) {
            throw new IllegalArgumentException("Hạn thanh toán không hợp lệ");
        }

        String trangThai = getCellStringValue(row.getCell(4));
        if (trangThai == null || trangThai.trim().isEmpty()) {
            trangThai = "chua_thanh_toan";
        }

        String ghiChu = getCellStringValue(row.getCell(5));

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaHo(maHo.trim());
        hoaDon.setLoaiHoaDon(loaiHoaDon.trim().toLowerCase());
        hoaDon.setSoTien(soTien);
        hoaDon.setHanThanhToan(hanThanhToan);
        hoaDon.setTrangThai(trangThai.trim().toLowerCase());
        hoaDon.setGhiChu(ghiChu);

        return hoaDon;
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

    private BigDecimal getCellNumericValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING:
                try {
                    String value = cell.getStringCellValue().replaceAll("[^0-9.]", "");
                    return new BigDecimal(value);
                } catch (NumberFormatException e) {
                    return null;
                }
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

    private String getLoaiHoaDonText(String loaiHoaDon) {
        if (loaiHoaDon == null) return "Khác";
        switch (loaiHoaDon) {
            case "dien_nuoc": return "Điện nước";
            case "dich_vu": return "Dịch vụ";
            case "sua_chua": return "Sửa chữa";
            case "phat": return "Phạt";
            case "khac": return "Khác";
            default: return loaiHoaDon;
        }
    }

    private String getTrangThaiText(String trangThai) {
        if (trangThai == null) return "Chưa thanh toán";
        switch (trangThai) {
            case "da_thanh_toan": return "Đã thanh toán";
            case "chua_thanh_toan": return "Chưa thanh toán";
            case "qua_han": return "Quá hạn";
            default: return trangThai;
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount);
    }

    /**
     * Class để lưu kết quả import
     */
    public static class ImportResult {
        private final int successCount;
        private final int errorCount;
        private final List<String> errors;
        private final List<HoaDon> importedList;

        public ImportResult(int successCount, int errorCount, List<String> errors, List<HoaDon> importedList) {
            this.successCount = successCount;
            this.errorCount = errorCount;
            this.errors = errors;
            this.importedList = importedList;
        }

        public int getSuccessCount() { return successCount; }
        public int getErrorCount() { return errorCount; }
        public List<String> getErrors() { return errors; }
        public List<HoaDon> getImportedList() { return importedList; }
    }
}

