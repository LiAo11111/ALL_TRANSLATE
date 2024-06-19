package com.example.simpleocr.Model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * 该类为数据库实体类，定义了数据库的items表，每一行为一个历史记录的信息
 */
@Entity(tableName = "items")
public class OcrItem implements Serializable {
    // 主键ID，自动生成
    @PrimaryKey(autoGenerate = true)
    long id = 0;

    // 识别文本
    @ColumnInfo(name = "text")
    String text = "";

    // 任务日期
    @ColumnInfo(name = "date")
    String date = "";

    // 图像url
    @ColumnInfo(name = "image")
    String image = "";

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
