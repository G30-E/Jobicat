package com.jobicat.app.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.jobicat.app.data.AppDatabase;
import com.jobicat.app.data.TaskDao;
import com.jobicat.app.model.Task;
import com.jobicat.app.util.TextNormalizer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {
    private final TaskDao taskDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        taskDao = db.taskDao();
    }

    public LiveData<List<Task>> getAll() {
        return taskDao.getAll();
    }

    public LiveData<List<Task>> getFiltered(String difficulty, String search) {
        return taskDao.getFiltered(difficulty, search);
    }

    public interface ResultCallback<T> { void onResult(T result); }

    public void insert(String title, String description, String difficulty, String timeHHmm, ResultCallback<Long> cb) {
        executor.execute(() -> {
            String norm = TextNormalizer.normalize(title);
            int exists = taskDao.countByNormalized(norm);
            if (exists > 0) {
                post(cb, -1L);
                return;
            }
            Task t = new Task(title, description, difficulty, timeHHmm, norm);
            long id;
            try {
                id = taskDao.insert(t);
            } catch (Exception e) {
                id = -1L;
            }
            post(cb, id);
        });
    }

    public void update(long id, String title, String description, String difficulty, String timeHHmm, ResultCallback<Integer> cb) {
        executor.execute(() -> {
            String norm = TextNormalizer.normalize(title);
            Task existing = taskDao.getByIdSync(id);
            if (existing == null) { post(cb, 0); return; }
            // If title changed, ensure uniqueness
            if (!existing.normalizedTitle.equals(norm)) {
                int exists = taskDao.countByNormalized(norm);
                if (exists > 0) {
                    post(cb, -1);
                    return;
                }
            }
            existing.title = title;
            existing.description = description;
            existing.difficulty = difficulty;
            existing.timeHHmm = timeHHmm;
            existing.normalizedTitle = norm;
            int rows = taskDao.update(existing);
            post(cb, rows);
        });
    }

    public void delete(Task task, ResultCallback<Integer> cb) {
        executor.execute(() -> {
            int rows = taskDao.delete(task);
            post(cb, rows);
        });
    }

    private <T> void post(ResultCallback<T> cb, T v) {
        if (cb == null) return;
        mainHandler.post(() -> cb.onResult(v));
    }
}