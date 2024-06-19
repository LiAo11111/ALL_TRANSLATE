package com.example.simpleocr.DataBase;


import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.simpleocr.Model.OcrItem;

import java.util.List;

/**
 * 对于数据库表的操作
 */
@Dao
public interface DAO {

    //数据库操作：增加记录
    @Insert(onConflict = REPLACE)
    long insert(OcrItem ocrItem);

    //数据库操作：查询记录
    @Query("SELECT * FROM items ORDER BY ID DESC")
    List<OcrItem> getAll();

    //数据库操作：修改记录
    @Update
    void update(OcrItem ocrItem);

    //数据库操作：删除记录
    @Delete
    void delete(OcrItem ocrItem);
}