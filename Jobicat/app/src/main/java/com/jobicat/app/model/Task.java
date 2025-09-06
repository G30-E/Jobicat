package com.jobicat.app.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks",
        indices = {@Index(value = {"normalized_title"}, unique = true)})
public class Task {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "difficulty")
    public String difficulty; // "Fácil", "Medio", "Difícil"

    @ColumnInfo(name = "time_hhmm")
    public String timeHHmm; // "HH:mm"

    @ColumnInfo(name = "normalized_title")
    public String normalizedTitle;

    public Task(String title, String description, String difficulty, String timeHHmm, String normalizedTitle) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.timeHHmm = timeHHmm;
        this.normalizedTitle = normalizedTitle;
    }
}