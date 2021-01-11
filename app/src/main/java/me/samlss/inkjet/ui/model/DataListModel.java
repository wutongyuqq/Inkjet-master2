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
import java.util.List;

import me.samlss.framework.utils.ResourceUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.R;
import me.samlss.inkjet.config.InkConfig;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.db.DbManager;
import me.samlss.inkjet.db.PrintBean;
import me.samlss.inkjet.db.Project;
import me.samlss.inkjet.managers.UserManager;
import me.samlss.inkjet.ui.fragments.DataListFragment;
import me.samlss.inkjet.utils.ExcelUtils;

//import me.samlss.inkjet.tasks.TransformExcelTask;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description 数据列表逻辑类
 */
public class DataListModel {
    private DataListFragment mDataListFragment;
    private ThreadUtils.Task mFetchProjectTask;
    private ThreadUtils.Task mGenerateExcelDataTask;

    public DataListModel(DataListFragment dataListFragment){
        mDataListFragment = dataListFragment;
    }

    /**
     * 开始获取本地excel数据
     * */
    public void fetchExcelData(){
        stopFetchingProject();
        mFetchProjectTask = new ThreadUtils.Task<ProjectInfo>() {
            @Nullable
            @Override
            public ProjectInfo doInBackground() throws Throwable {
                return ExcelUtils.getProjectInfo();
            }

            @Override
            public void onSuccess(@Nullable ProjectInfo result) {
                if (mDataListFragment != null){
                    mDataListFragment.onFetchExcelFinished(result);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {
                if (mDataListFragment != null){
                    mDataListFragment.onFetchExcelFinished(null);
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
     * @param selectedColArray 选中的所有列数组
     * @param selectedColList 根据选中列再选中的列组合列表
     * */
    public void generateData(int beginRowNum, int endRowNum,
                             ProjectInfo projectInfo,
                             SparseArray<Integer> selectedColArray, List<SparseArray> selectedColList){
        stopGeneratingData();
        mGenerateExcelDataTask = new ThreadUtils.Task<Integer>() {
            @Nullable
            @Override
            public Integer doInBackground() throws Throwable {
                Workbook workbook = WorkbookFactory.create(new File(InkConfig.getExcelDataPath()));
                Sheet sheet = workbook.getSheetAt(InkConfig.getExcelSheetIndex()); //只取一个表

                List<PrintBean> contents = new ArrayList<>();
                List<String> splits = new ArrayList<>();
                List<String> rowStrings = new ArrayList<>();
//                //件数列下标
//                int piecesIndex = projectInfo.columns.indexOf(ResourceUtils.getString(R.string.pieces_number));
//                if (piecesIndex < 0){
//                    return 0;
//                }

                for (int rowIndex = beginRowNum - 1; rowIndex < endRowNum; rowIndex++){
                    Row row = sheet.getRow(rowIndex);

                    if (row == null){
                        continue;
                    }

                    rowStrings.clear();
                    //取每一行数据出来
                    for (int cellIndex = row.getFirstCellNum(); cellIndex < projectInfo.columns.size(); cellIndex++){
                        Cell cell = row.getCell(cellIndex);
                        if (cell == null) {
                            rowStrings.add("");
                            continue;
                        }
                        rowStrings.add(ExcelUtils.getCellValue(cell));
                    }

                    splits.clear();
                    String content = null;
                    for (int splitIndex = 0; splitIndex < selectedColList.size(); splitIndex++){
                        String split = "";

                        for (int colIndex = 0; colIndex < selectedColList.get(splitIndex).size(); colIndex++){
                            int colAt = selectedColList.get(splitIndex).keyAt(colIndex);
                            colAt = selectedColArray.keyAt(colAt);

                            if (colAt < 0 || colAt >= rowStrings.size()){
                                continue;
                            }

                            split += rowStrings.get(colAt);
                        }

                        if (TextUtils.isEmpty(content)){
                            content = split;
                        }

                        splits.add(split);
                    }

                    PrintBean printBean = new PrintBean();
                    printBean.setPrint_count(0);
                    printBean.setContent(content);
                    printBean.setProject(projectInfo.projectName);
                    printBean.setState(InkConstant.PRINT_STATE_NONE);
                    printBean.setLine_number(rowIndex + 1);
                    printBean.setUser_id(UserManager.getInstance().getCompanyUserId());
//                    //防止rowStrings.get(piecesIndex)为null或者不是数字
//                    try {
//                        printBean.setPieces_number(TextUtils.isEmpty(rowStrings.get(piecesIndex)) ?
//                                0 : Integer.valueOf(rowStrings.get(piecesIndex)));
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }

                    printBean.setSplits(JSON.toJSONString(splits));
                    contents.add(printBean);
                }

                DbManager.get().insertPrintList(contents);

                Project project = new Project();
                project.setGenerate_time(System.currentTimeMillis());
                project.setProject_name(projectInfo.projectName);
                project.setUser_id(UserManager.getInstance().getCompanyUserId());
                DbManager.get().insertProject(project);
                return contents.size();
            }

            @Override
            public void onSuccess(@Nullable Integer result) {
                if (result != null && result > 0){
                    mDataListFragment.onGenerateDataSuccess(result);
                }else{
                    mDataListFragment.onGenerateDataFailure();
                }
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onFail(Throwable t) {
                t.printStackTrace();
                mDataListFragment.onGenerateDataFailure();
            }
        };
        ThreadUtils.executeByIo(mGenerateExcelDataTask);
    }

    public void destroy(){
        stopFetchingProject();
        stopTransformingData();
        stopGeneratingData();

        mDataListFragment = null;
    }

    public static class ProjectInfo{
        public int rowNumber;
        public String projectName;
        public List<String> columns;
    }
}
