package me.samlss.inkjet.db;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import me.samlss.framework.utils.AppUtils;
import me.samlss.framework.utils.ListUtils;
import me.samlss.framework.utils.ThreadUtils;
import me.samlss.inkjet.constant.InkConstant;
import me.samlss.inkjet.managers.UserManager;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description
 */
public class DbManager {
    private final static String DB_NAME = "ink";
    private  static DbManager sINSTANCE;
    private DaoMaster.DevOpenHelper mDevOpenHelper;
    private Database mDatabase;
    private DaoMaster daoMaster;
    private DaoSession mDaoSession;

    private DbManager(){
        mDevOpenHelper = new DaoMaster.DevOpenHelper(AppUtils.getApp(), DB_NAME);

        mDatabase = mDevOpenHelper.getWritableDb();
        daoMaster = new DaoMaster(mDatabase);
        mDaoSession = daoMaster.newSession();
    }

    public static DbManager get() {
        if (sINSTANCE == null){
            synchronized (DbManager.class){
                if (sINSTANCE == null) {
                    sINSTANCE = new DbManager();
                }
            }
        }
        return sINSTANCE;
    }

    public synchronized void insertProject(Project project){
        if (project == null){
            return;
        }

        mDaoSession.getProjectDao().insert(project);
    }

    //判断数据库中是否已包含该project，project = excel文件名
    public Project getProject(String project){
        List<Project> projects = mDaoSession.getProjectDao().queryBuilder()
                .where(ProjectDao.Properties.User_id.eq(UserManager.getInstance().getCompanyUserId()))
                .where(ProjectDao.Properties.Project_name.eq(project))
                .list();

        if (!ListUtils.isEmpty(projects)){
            return projects.get(0);
        }

        return null;
    }

    public List<Project> getProjects(){
        return mDaoSession.getProjectDao().queryBuilder()
                .where(ProjectDao.Properties.User_id.eq(UserManager.getInstance().getCompanyUserId()))
                .orderDesc(ProjectDao.Properties.Id)
                .list();
    }

    public List<Project> getProjects(int page, int count){
        QueryBuilder queryBuilder = mDaoSession.getProjectDao()
                .queryBuilder()
                .where(ProjectDao.Properties.User_id.eq(UserManager.getInstance().getCompanyUserId()))
                .orderDesc(ProjectDao.Properties.Id);

        return queryBuilder.limit(count)
                .offset(page * count)
                .list();
    }

    public synchronized void deleteProject(Project project){
        if (project == null){
            return;
        }

        mDaoSession.getProjectDao().delete(project);
    }

    public synchronized void deleteProjects(List<Project> projects){
        if (ListUtils.isEmpty(projects)){
            return;
        }


        mDaoSession.getProjectDao().deleteInTx(projects);
    }

    public synchronized void deleteProjects(String project){
        mDaoSession.getPrintBeanDao().deleteInTx(getPrintList(project));
    }

    public List<PrintBean> getPrintList(String project){
        QueryBuilder queryBuilder = mDaoSession.getPrintBeanDao()
                .queryBuilder()
                .where(PrintBeanDao.Properties.User_id.eq(UserManager.getInstance().getCompanyUserId()))
                .where(PrintBeanDao.Properties.Project.eq(project));

        return queryBuilder.list();
    }

    public List<PrintBean> getPrintList(String project, String search, int type){
        QueryBuilder queryBuilder = mDaoSession.getPrintBeanDao().queryBuilder()
                .where(PrintBeanDao.Properties.Project.eq(project))
                .where(PrintBeanDao.Properties.User_id.eq(UserManager.getInstance().getCompanyUserId()));

        if (!TextUtils.isEmpty(search)){
            queryBuilder.where(PrintBeanDao.Properties.Content.like("%" + search + "%"));
        }

        if (type == InkConstant.ANNAL_TYPE_NOT_SPRAYED){
            queryBuilder.where(PrintBeanDao.Properties.State.notEq(InkConstant.PRINT_STATE_FINISH));
        }else if (type == InkConstant.ANNAL_TYPE_SPRAYED){
            queryBuilder.where(PrintBeanDao.Properties.State.eq(InkConstant.PRINT_STATE_FINISH));
        }

        return queryBuilder.list();
    }


    public List<PrintBean> getPrintSkipPageList(String project, String search, int type){
        QueryBuilder queryBuilder = mDaoSession.getPrintBeanDao().queryBuilder()
                .where(PrintBeanDao.Properties.Project.eq(project))
                .where(PrintBeanDao.Properties.User_id.eq(UserManager.getInstance().getCompanyUserId()));

        if (!TextUtils.isEmpty(search)){
            queryBuilder.where(PrintBeanDao.Properties.Content.like("%" + search + "%"));
        }

        /*if (type == InkConstant.ANNAL_TYPE_NOT_SPRAYED){
            queryBuilder.where(PrintBeanDao.Properties.State.notEq(InkConstant.PRINT_STATE_FINISH));
        }else if (type == InkConstant.ANNAL_TYPE_SPRAYED){
            queryBuilder.where(PrintBeanDao.Properties.State.eq(InkConstant.PRINT_STATE_FINISH));
        }*/

        return queryBuilder.list();
    }

    public void updatePrintBeanAsync(PrintBean printBean){
        if (printBean == null){
            return;
        }

        ThreadUtils.executeByIo(new ThreadUtils.Task<Void>() {
            @Nullable
            @Override
            public Void doInBackground() throws Throwable {
                try {
                    mDaoSession.getPrintBeanDao().save(printBean);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void onSuccess(@Nullable Void result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
    }

    /**
     * 插入喷码列表
     * */
    public synchronized  void insertPrintList(List<PrintBean> printBeans){
        if (ListUtils.isEmpty(printBeans)){
            return;
        }
        mDaoSession.getPrintBeanDao().insertInTx(printBeans);
    }

    /**
     * 插入喷码记录
     * */
    public synchronized void insertInkAnnal(InkAnnal annal){
        if (annal == null){
            return;
        }

        mDaoSession.insert(annal);
    }

    /**
     * 插入喷码记录
     * */
    public synchronized void insertInkAnnals(List<InkAnnal> annals){
        if (ListUtils.isEmpty(annals)){
            return;
        }
        mDaoSession.getInkAnnalDao().insertInTx(annals);
    }

    public List<InkAnnal> getAnnals(long begin, long end) {
        QueryBuilder queryBuilder = mDaoSession.getInkAnnalDao().queryBuilder()
                .where(InkAnnalDao.Properties.User_id.eq(UserManager.getInstance().getCompanyUserId()))
                .where(InkAnnalDao.Properties.Print_time.between(begin, end))
                .orderDesc(InkAnnalDao.Properties.Id);

        return queryBuilder.list();
    }

    public List<InkAnnal> getAnnals(int page, int count) {
        QueryBuilder queryBuilder = mDaoSession.getInkAnnalDao().queryBuilder()
                .where(InkAnnalDao.Properties.User_id.eq(UserManager.getInstance().getCompanyUserId()))
                .orderDesc(InkAnnalDao.Properties.Id);

        return queryBuilder.limit(count)
                .offset(page * count)
                .list();
    }

    public synchronized void close(){
        try {
            mDevOpenHelper.close();
            mDaoSession.clear();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
