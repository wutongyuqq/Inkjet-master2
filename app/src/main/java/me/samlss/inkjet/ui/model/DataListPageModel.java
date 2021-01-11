package me.samlss.inkjet.ui.model;

import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.samlss.framework.utils.StringUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.PrintBean;
import me.samlss.inkjet.db.Project;
import me.samlss.inkjet.managers.UserManager;
import me.samlss.inkjet.ui.fragments.FetchExcelFragment;
import me.samlss.inkjet.utils.ExcelUtils;

//import me.samlss.inkjet.tasks.TransformExcelTask;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 数据列表逻辑类
 */
public class DataListPageModel {
    private FetchExcelFragment mFetchExcelFragment;
    private ThreadUtils.Task mFetchProjectTask;
    private ThreadUtils.Task mGenerateExcelDataTask;
    private SparseArray<Integer> mSelectedColArray = new SparseArray<>(); //选中的所有列数组
    private Set<String> mSet = new HashSet<>();
    private Set<Integer> cloumSet = new HashSet<>();
    List<SparseArray> selectedColList = new ArrayList<>();
    private int mWzbmIndex;
    private List<String> mTitleList = new ArrayList<>();
    private Map<Integer,String> mTitleMap = new HashMap<>();
    private Map<Integer,Boolean> mIsMap = new HashMap<>();


    public DataListPageModel(FetchExcelFragment fetchExcelFragment){
        mFetchExcelFragment = fetchExcelFragment;
        mSet.add("物资编码");
        
        mSet.add("物资简称");
        mSet.add("物资类型");
        mSet.add("规格");
        mSet.add("项目");

    }
    /**
     * 开始获取本地excel数据
     * */
    public void fetchExcelData(){
        stopFetchingProject();
        mFetchProjectTask = new ThreadUtils.Task<ProjectPageInfo>() {
            @Nullable
            @Override
            public ProjectPageInfo doInBackground() throws Throwable {
                return ExcelUtils.getProjectPageInfo();
            }

            @Override
            public void onSuccess(@Nullable ProjectPageInfo result) {
                if (mFetchExcelFragment != null){
                    mFetchExcelFragment.onFetchExcelFinished(result);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if (mFetchExcelFragment != null){
                    mFetchExcelFragment.onFetchExcelFinished(null);
                }
            }
        };

        ThreadUtils.executeByIo(mFetchProjectTask);
    }

    private void stopFetchingProject(){
        if (mFetchProjectTask != null
                && !mFetchProjectTask.isCanceled()){
            mFetchProjectTask.cancel();
        }
    }

    private void stopTransformingData(){
    }

    private void stopGeneratingData(){
        if (mGenerateExcelDataTask != null
                && !mGenerateExcelDataTask.isCanceled()){
            mGenerateExcelDataTask.cancel();
        }
    }



    /**
     * 开始生成所有喷码数据
     * @param beginRowNum 开始行数
     * @param endRowNum 结束行数
     * @param projectInfo excel表数据
     * */
    public void generateData(int beginRowNum, int endRowNum,
                             ProjectPageInfo projectInfo){
        stopGeneratingData();
        mGenerateExcelDataTask = new ThreadUtils.Task<Integer>() {
            @Nullable
            @Override
            public Integer doInBackground() throws Throwable {
                List<PrintBean> contents = new ArrayList<>();
                try {
                    Workbook workbook = WorkbookFactory.create(new File(InkConfig.getExcelDataPath()));
                    Sheet sheet = workbook.getSheetAt(InkConfig.getExcelSheetIndex()); //只取一个表

                    for (int rowIndex = beginRowNum - 1; rowIndex < 2; rowIndex++) {
                        Row row = sheet.getRow(rowIndex);
                        if (row == null) {
                            continue;
                        }
                        //取每一行数据出来
                        for (int cellIndex = row.getFirstCellNum(); cellIndex < projectInfo.columns.size(); cellIndex++) {
                            Cell cell = row.getCell(cellIndex);
                            if (cell == null) {
                                continue;
                            }
                            String title = ExcelUtils.getCellValue(cell);
                            if (title != null && mSet.contains(title)) {
                                cloumSet.add(cellIndex);
                                mTitleList.add(title);
                                mTitleMap.put(cellIndex, title);
                            }
                            if (StringUtils.equals(title, "物资编码")) {
                                mWzbmIndex = cellIndex;
                            }
                        }
                    }
                    int rowTotalNum = sheet.getPhysicalNumberOfRows();//获得总行数


                    for (int rowIndex = beginRowNum - 1; rowIndex < rowTotalNum; rowIndex++) {
                        Row row = sheet.getRow(rowIndex);
                        if (row == null) {
                            continue;
                        }
                        boolean isS = false;
                        //取每一行数据出来
                        for (int cellIndex = row.getFirstCellNum(); cellIndex < projectInfo.columns.size(); cellIndex++) {
                            Cell cell = row.getCell(cellIndex);
                            if (cell == null) {
                                continue;
                            }
                            String cellValue = ExcelUtils.getCellValue(cell);
                            if (mTitleMap.get(cellIndex) != null && StringUtils.equals("物资类型", mTitleMap.get(cellIndex))) {
                                if (cellValue != null && (StringUtils.equals("S", cellValue) || StringUtils.equals("S", cellValue.trim().toUpperCase()))) {
                                    isS = true;
                                }
                            }
                        }
                        mIsMap.put(rowIndex, isS);
                    }





                    for (int rowIndex = beginRowNum-1; rowIndex < rowTotalNum; rowIndex++) {
                        Row row = sheet.getRow(rowIndex);
                        if (row == null) {
                            continue;
                        }
                        String content = "";
                        List<String> splits = new ArrayList<>();
                        List<ItemBean> splitBeanList = new ArrayList<>();
                        Map<String, ItemBean> splitBeanMap = new HashMap<>();
                        splits.clear();
                        splitBeanList.clear();
                        splitBeanMap.clear();

                        //取每一行数据出来
                        for (int cellIndex = row.getFirstCellNum(); cellIndex < projectInfo.columns.size(); cellIndex++) {
                            Cell cell = row.getCell(cellIndex);
                            if (cell == null) {
                                continue;
                            }
                            if (cloumSet.contains(cellIndex)) {
                                String cellValue = ExcelUtils.getCellValue(cell);
                                if (mWzbmIndex == cellIndex) {
                                    content = cellValue;
                                }
                                ItemBean itemBean = new ItemBean(cellValue, mTitleMap.get(cellIndex), mIsMap.get(rowIndex));
                                //splits.add(cellValue);
                                splitBeanList.add(itemBean);
                                splitBeanMap.put(itemBean.getTitle(), itemBean);
                            }
                        }
                        ItemBean firstBean = splitBeanMap.get("项目");
                        String oneStr = "";
                        if (!firstBean.isS()) {
                            oneStr = firstBean.getContent();
                        }
                        splits.add(oneStr);
                        splits.add(getSelfContent(splitBeanMap, "物资编码"));
                        splits.add(getSelfContent(splitBeanMap, "物资编码"));
                        splits.add(getSelfContent(splitBeanMap, "物资简称"));
                        splits.add(getSelfContent(splitBeanMap, "规格"));


                        if (!TextUtils.isEmpty(content)) {
                            PrintBean printBean = new PrintBean();
                            printBean.setPrint_count(0);
                            printBean.setContent(content);
                            printBean.setProject(projectInfo.projectName);
                            printBean.setState(InkConstant.PRINT_STATE_NONE);
                            printBean.setLine_number(rowIndex + 1);
                            printBean.setUser_id(UserManager.getInstance().getCompanyUserId());
                            printBean.setSplits(JSON.toJSONString(splits));
                            contents.add(printBean);
                        }
                    }
                    DbManager.get().insertPrintList(contents);
                    Project project = new Project();
                    project.setGenerate_time(System.currentTimeMillis());
                    project.setProject_name(projectInfo.projectName);
                    project.setUser_id(UserManager.getInstance().getCompanyUserId());
                    DbManager.get().insertProject(project);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return contents.size();
            }

            @Override
            public void onSuccess(@Nullable Integer result) {
                if (result != null && result > 0){
                    mFetchExcelFragment.onGenerateDataSuccess(result);
                }else{
                    mFetchExcelFragment.onGenerateDataFailure();
                }
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onFail(Throwable t) {
                t.printStackTrace();
                mFetchExcelFragment.onGenerateDataFailure();
            }
        };
        ThreadUtils.executeByIo(mGenerateExcelDataTask);
    }

    private String getSelfContent(Map<String,ItemBean> splitBeanMap,String key){
        if(splitBeanMap==null){
            return "";
        }
        ItemBean fiveBean =  splitBeanMap.get(key);
        if(fiveBean==null){
            return "";
        }
        return fiveBean.getContent();
    }

    public void destroy(){
        stopFetchingProject();
        stopTransformingData();
        stopGeneratingData();

        mFetchExcelFragment = null;
    }

    public static class ProjectPageInfo{
        public int rowNumber;
        public String projectName;
        public List<String> columns;
    }

    class ItemBean{
        String content;
        String title;
        boolean isS;

        public ItemBean(String content, String title,boolean isS) {
            this.content = content;
            this.title = title;
            this.isS = isS;
        }

        public String getContent() {
            return content==null?"":content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isS() {
            return isS;
        }

        public void setS(boolean s) {
            isS = s;
        }
    }
}
