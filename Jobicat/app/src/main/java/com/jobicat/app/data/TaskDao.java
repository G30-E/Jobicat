package com.jobicat.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jobicat.app.model.Task;

import java.util.List;

@Dao
public interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY title ASC")
    LiveData<List<Task>> getAll();

    @Query("SELECT * FROM tasks WHERE (:difficulty = 'Todas' OR difficulty = :difficulty) " +
            "AND (:search IS NULL OR :search = '' OR title LIKE '%' || :search || '%') ORDER BY title ASC")
    LiveData<List<Task>> getFiltered(String difficulty, String search);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Task task);

    @Update
    int update(Task task);

    @Delete
    int delete(Task task);

    @Query("SELECT COUNT(*) FROM tasks WHERE normalized_title = :normalized")
    int countByNormalized(String normalized);

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    Task getByIdSync(long id);
}