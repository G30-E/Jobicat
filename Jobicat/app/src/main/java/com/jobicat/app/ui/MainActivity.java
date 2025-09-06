package com.jobicat.app.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jobicat.app.R;
import com.jobicat.app.adapter.TaskAdapter;
import com.jobicat.app.model.Task;
import com.jobicat.app.util.CsvUtils;
import com.jobicat.app.viewmodel.TaskViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.Listener {

    private TaskViewModel vm;
    private TaskAdapter adapter;
    private Spinner spn;
    private EditText etSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vm = new ViewModelProvider(this).get(TaskViewModel.class);

        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this);
        rv.setAdapter(adapter);

        etSearch = findViewById(R.id.etSearch);
        spn = findViewById(R.id.spinnerFilterDifficulty);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnExport = findViewById(R.id.btnExportCsv);
        Button btnImport = findViewById(R.id.btnImportCsv);

        String[] diffs = new String[] {"Todas", "Fácil", "Medio", "Difícil"};
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, diffs);
        spn.setAdapter(spnAdapter);

        spn.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                refresh();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        vm.getTasks().observe(this, tasks -> adapter.submitList(tasks));

        btnAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditTaskActivity.class));
        });

        btnExport.setOnClickListener(v -> exportCsv());
        btnImport.setOnClickListener(v -> importCsv());

        refresh();
    }

    private void refresh() {
        String diff = spn.getSelectedItem() == null ? "Todas" : spn.getSelectedItem().toString();
        String q = etSearch.getText() == null ? "" : etSearch.getText().toString().trim();
        vm.setDifficulty(diff);
        vm.setSearch(q);
        vm.getTasks().observe(this, tasks -> adapter.submitList(tasks));
    }

    private void exportCsv() {
        vm.getTasks().observe(this, tasks -> {
            List<Task> list = tasks == null ? new ArrayList<>() : tasks;
            boolean ok = CsvUtils.exportToCsv(this, list);
            File f = CsvUtils.getCsvFile(this);
            if (ok) Toast.makeText(this, "Exportado: " + f.getAbsolutePath(), Toast.LENGTH_LONG).show();
            else Toast.makeText(this, "Error al exportar", Toast.LENGTH_SHORT).show();
        });
    }

    private void importCsv() {
        new Thread(() -> {
            CsvUtils.ImportResult pr = CsvUtils.parseCsv(this);
            int inserted = 0;
            int duplicates = 0;
            int errors = pr.errors;

            for (Task t : pr.parsed) {
                if (t.title == null || t.title.trim().isEmpty()) { errors++; continue; }
                final int[] resHolder = new int[1];
                final long[] idHolder = new long[1];
                Object lock = new Object();
                vm.insert(t.title, t.description, t.difficulty, t.timeHHmm, id -> {
                    synchronized (lock) {
                        idHolder[0] = id;
                        lock.notify();
                    }
                });
                synchronized (lock) {
                    try { lock.wait(1000); } catch (InterruptedException e) {}
                }
                if (idHolder[0] > 0) inserted++;
                else duplicates++;
            }

            int finInserted = inserted;
            int finDup = duplicates;
            int finErr = errors;
            runOnUiThread(() -> {
                new AlertDialog.Builder(this)
                        .setTitle("Importación CSV")
                        .setMessage("Nuevos: " + finInserted + "\nDuplicados: " + finDup + "\nErrores: " + finErr)
                        .setPositiveButton("OK", null)
                        .show();
                refresh();
            });
        }).start();
    }

    @Override
    public void onClick(Task task) {
        Intent i = new Intent(this, AddEditTaskActivity.class);
        i.putExtra("id", task.id);
        startActivity(i);
    }

    @Override
    public void onLongClick(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar \"" + task.title + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    vm.delete(task, rows -> {
                        Toast.makeText(this, rows > 0 ? "Eliminado" : "No se pudo eliminar", Toast.LENGTH_SHORT).show();
                        refresh();
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}