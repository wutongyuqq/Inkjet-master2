package me.samlss.inkjet.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "PRINT_BEAN".
*/
public class PrintBeanDao extends AbstractDao<PrintBean, Long> {

    public static final String TABLENAME = "PRINT_BEAN";

    /**
     * Properties of entity PrintBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Content = new Property(1, String.class, "content", false, "CONTENT");
        public final static Property User_id = new Property(2, String.class, "user_id", false, "USER_ID");
        public final static Property Project = new Property(3, String.class, "project", false, "PROJECT");
        public final static Property State = new Property(4, Integer.class, "state", false, "STATE");
        public final static Property Splits = new Property(5, String.class, "splits", false, "SPLITS");
        public final static Property Pieces_number = new Property(6, Integer.class, "pieces_number", false, "PIECES_NUMBER");
        public final static Property Line_number = new Property(7, Integer.class, "line_number", false, "LINE_NUMBER");
        public final static Property Print_count = new Property(8, Integer.class, "print_count", false, "PRINT_COUNT");
    }


    public PrintBeanDao(DaoConfig config) {
        super(config);
    }
    
    public PrintBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PRINT_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"CONTENT\" TEXT," + // 1: content
                "\"USER_ID\" TEXT," + // 2: user_id
                "\"PROJECT\" TEXT," + // 3: project
                "\"STATE\" INTEGER," + // 4: state
                "\"SPLITS\" TEXT," + // 5: splits
                "\"PIECES_NUMBER\" INTEGER," + // 6: pieces_number
                "\"LINE_NUMBER\" INTEGER," + // 7: line_number
                "\"PRINT_COUNT\" INTEGER);"); // 8: print_count
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_PRINT_BEAN_PROJECT ON \"PRINT_BEAN\"" +
                " (\"PROJECT\");");
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PRINT_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, PrintBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(2, content);
        }
 
        String user_id = entity.getUser_id();
        if (user_id != null) {
            stmt.bindString(3, user_id);
        }
 
        String project = entity.getProject();
        if (project != null) {
            stmt.bindString(4, project);
        }
 
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(5, state);
        }
 
        String splits = entity.getSplits();
        if (splits != null) {
            stmt.bindString(6, splits);
        }
 
        Integer pieces_number = entity.getPieces_number();
        if (pieces_number != null) {
            stmt.bindLong(7, pieces_number);
        }
 
        Integer line_number = entity.getLine_number();
        if (line_number != null) {
            stmt.bindLong(8, line_number);
        }
 
        Integer print_count = entity.getPrint_count();
        if (print_count != null) {
            stmt.bindLong(9, print_count);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, PrintBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String content = entity.getContent();
        if (content != null) {
            stmt.bindString(2, content);
        }
 
        String user_id = entity.getUser_id();
        if (user_id != null) {
            stmt.bindString(3, user_id);
        }
 
        String project = entity.getProject();
        if (project != null) {
            stmt.bindString(4, project);
        }
 
        Integer state = entity.getState();
        if (state != null) {
            stmt.bindLong(5, state);
        }
 
        String splits = entity.getSplits();
        if (splits != null) {
            stmt.bindString(6, splits);
        }
 
        Integer pieces_number = entity.getPieces_number();
        if (pieces_number != null) {
            stmt.bindLong(7, pieces_number);
        }
 
        Integer line_number = entity.getLine_number();
        if (line_number != null) {
            stmt.bindLong(8, line_number);
        }
 
        Integer print_count = entity.getPrint_count();
        if (print_count != null) {
            stmt.bindLong(9, print_count);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public PrintBean readEntity(Cursor cursor, int offset) {
        PrintBean entity = new PrintBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // content
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // user_id
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // project
            cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // state
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // splits
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // pieces_number
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // line_number
            cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8) // print_count
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, PrintBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setContent(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setUser_id(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setProject(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setState(cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4));
        entity.setSplits(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setPieces_number(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
        entity.setLine_number(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setPrint_count(cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(PrintBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(PrintBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(PrintBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}