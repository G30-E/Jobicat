package com.jobicat.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.jobicat.app.model.Task;
import com.jobicat.app.repository.TaskRepository;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final MutableLiveData<String> difficultyFilter = new MutableLiveData<>("Todas");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    private final LiveData<List<Task>> tasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        tasks = Transformations.switchMap(
                Transformations.map(difficultyFilter, d -> d),
                d -> repository.getFiltered(getDifficulty(), getSearch())
        );
    }

    public LiveData<List<Task>> getTasks() {
        return repository.getFiltered(getDifficulty(), getSearch());
    }

    public void setDifficulty(String diff) {
        difficultyFilter.setValue(diff);
    }

    public void setSearch(String query) {
        searchQuery.setValue(query);
    }

    public String getDifficulty() {
        return difficultyFilter.getValue() == null ? "Todas" : difficultyFilter.getValue();
    }

    public String getSearch() {
        return searchQuery.getValue() == null ? "" : searchQuery.getValue();
    }

    public void insert(String title, String desc, String difficulty, String timeHHmm, TaskRepository.ResultCallback<Long> cb) {
        repository.insert(title, desc, difficulty, timeHHmm, cb);
    }

    public void update(long id, String title, String desc, String difficulty, String timeHHmm, TaskRepository.ResultCallback<Integer> cb) {
        repository.update(id, title, desc, difficulty, timeHHmm, cb);
    }

    public void delete(Task task, TaskRepository.ResultCallback<Integer> cb) {
        repository.delete(task, cb);
    }
}