package me.samlss.inkjet.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.samlss.framework.utils.FileUtils;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.InkAnnal;
import me.samlss.inkjet.db.PrintBean;
import me.samlss.inkjet.db.Project;
import me.samlss.inkjet.ui.model.DataListModel;
import me.samlss.inkjet.ui.model.DataListPageModel;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description excel处理工具
 */
public class ExcelUtils {
    public static final int FILE_NOT_EXISTS = -2; //失败
    public static final int FAILED = -1; //失败
    public static final int SUCCESS = 1; //成功
    public static final int NO_SHEETS = 2; //表为空
    public static final int NO_ROWS = 3; //表没有行数，或者只有一行
    public static final int EXISTS_PROJECT = 4; //该excel文件已存在数据

    private ExcelUtils(){}

    public static boolean isExcel(String filePath){
        return !TextUtils.isEmpty(filePath)
                 && (filePath.endsWith(".xls") || filePath.endsWith(".xlsx"));
    }

    public static List<String> getSheets(File excelFile){
        try {
            Workbook workbook = WorkbookFactory.create(excelFile);
            if (workbook.getNumberOfSheets() == 0) {
                return null;
            }

            List<String> sheets = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++){
                sheets.add(workbook.getSheetAt(i).getSheetName());
            }

            return sheets;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 开始解析Excel文件，获取即将要选择打印行列的数据，需异步处理，否则会阻塞主线程
     *
     * @param sheetAt excel表索引
     * @param deleteOld 是否删除旧数据
     * @param excelFile Excel文件本地路径
     * */
    public static int parseExcel(int sheetAt, boolean deleteOld, final File excelFile){
        try {
            if (!excelFile.exists()){
                return FILE_NOT_EXISTS;
            }

            Workbook workbook = WorkbookFactory.create(excelFile);
            if (workbook.getNumberOfSheets() == 0){
                return NO_SHEETS;
            }


            Sheet sheet = workbook.getSheetAt(sheetAt); //只取一个表

            int rowNumber = sheet.getLastRowNum() - sheet.getFirstRowNum() + 1;

            if (rowNumber == 0 || rowNumber == 1){
                return NO_ROWS;
            }

            String projectName = FileUtils.getFileNameNoExtension(excelFile) + "-" + sheet.getSheetName();
            Project project = DbManager.get().getProject(projectName);
            if (project != null){ //存在
                if (!deleteOld){
                    return EXISTS_PROJECT;
                }else{
                    DbManager.get().deleteProject(project);
                    DbManager.get().deleteProjects(projectName);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return FAILED;
        }

        return SUCCESS;
    }

    public static String getCellValue(Cell cell) {
        String cellValue;
        try {
            switch (cell.getCellTypeEnum()) {
                case NUMERIC: // 数字
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    if(cellValue != null && cellValue.endsWith(".0")){
                        cellValue = cellValue.substring(0, cellValue.indexOf(".0"));
                    }
                    break;
                case STRING: // 字符串
                    cellValue = cell.getStringCellValue();
                    break;
                case BOOLEAN: // Boolean
                    cellValue = cell.getBooleanCellValue() + "";
                    break;
                case FORMULA: // 公式
                    cellValue = "";
                    break;

                case BLANK: // 空值
                case ERROR: // 故障
                default:
                    cellValue = "";
                    break;
            }

            return cellValue;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public static String getPrintContent(PrintBean printBean){
        String content = "";
        try {
            List<String> contents = JSON.parseArray(printBean.getSplits(), String.class);
            int lineNumber = 1;

            for (String line : contents){
                content += new StringBuilder()
                        .append("第")
                        .append(lineNumber++)
                        .append("行：")
                        .append(line)
                        .append(lineNumber == (contents.size() + 1) ? "" : "\n\n")
                        .toString();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        if (content == null){
            content = "";
        }

        return content;
    }

    public static String getInkAnnalContent(InkAnnal inkAnnal){
        String content = "";
        try {
            if (!TextUtils.isEmpty(inkAnnal.getContent())
                    && !inkAnnal.getContent().contains("[")){
                return inkAnnal.getContent();
            }

            List<String> contents = JSON.parseArray(inkAnnal.getContent(), String.class);
            int lineNumber = 1;

            for (String line : contents){
                content += new StringBuilder()
                        .append("第")
                        .append(lineNumber++)
                        .append("行：")
                        .append(line)
                        .append(lineNumber == (contents.size() + 1) ? "" : "\n\n")
                        .toString();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        if (content == null){
            content = "";
        }

        return content;
    }

    public static DataListModel.ProjectInfo getProjectInfo(){
        try {
            DataListModel.ProjectInfo projectInfo = new DataListModel.ProjectInfo();

            Workbook workbook = WorkbookFactory.create(new File(InkConfig.getExcelDataPath()));
            if (workbook.getNumberOfSheets() == 0){
                return null;
            }

            Sheet sheet = workbook.getSheetAt(InkConfig.getExcelSheetIndex()); //只取一个表
            projectInfo.projectName = FileUtils.getFileNameNoExtension(InkConfig.getExcelDataPath()) + "-" + sheet.getSheetName();

            projectInfo.rowNumber = sheet.getLastRowNum() - sheet.getFirstRowNum() + 1;
            if (projectInfo.rowNumber == 0 || projectInfo.rowNumber == 1){
                return null;
            }

            List<String> columns = new ArrayList<>();
            Row row = sheet.getRow(sheet.getFirstRowNum()); //获取表头列数据
            for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) { //列数从1开始到n
                Cell cell = row.getCell(cellIndex);

                if (cell == null) {
                    columns.add("");
                    continue;
                }

                columns.add(getCellValue(cell));
            }

            projectInfo.columns = columns;
            return projectInfo;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }




    public static DataListPageModel.ProjectPageInfo getProjectPageInfo(){
        try {
            DataListPageModel.ProjectPageInfo projectInfo = new DataListPageModel.ProjectPageInfo();

            Workbook workbook = WorkbookFactory.create(new File(InkConfig.getExcelDataPath()));
            if (workbook.getNumberOfSheets() == 0){
                return null;
            }

            Sheet sheet = workbook.getSheetAt(InkConfig.getExcelSheetIndex()); //只取一个表
            projectInfo.projectName = FileUtils.getFileNameNoExtension(InkConfig.getExcelDataPath()) + "-" + sheet.getSheetName();

            projectInfo.rowNumber = sheet.getLastRowNum() - sheet.getFirstRowNum() + 1;
            if (projectInfo.rowNumber == 0 || projectInfo.rowNumber == 1){
                return null;
            }

            List<String> columns = new ArrayList<>();
            Row row = sheet.getRow(sheet.getFirstRowNum()); //获取表头列数据
            for (int cellIndex = row.getFirstCellNum(); cellIndex < row.getLastCellNum(); cellIndex++) { //列数从1开始到n
                Cell cell = row.getCell(cellIndex);

                if (cell == null) {
                    columns.add("");
                    continue;
                }

                columns.add(getCellValue(cell));
            }

            projectInfo.columns = columns;
            return projectInfo;
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
