package com.jobicat.app.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.jobicat.app.R;
import com.jobicat.app.data.AppDatabase;
import com.jobicat.app.model.Task;
import com.jobicat.app.viewmodel.TaskViewModel;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditTaskActivity extends AppCompatActivity {

    private TaskViewModel vm;
    private long editingId = -1;
    private EditText etTitle, etDesc;
    private Spinner spinner;
    private TextView tvTime;
    private String timeValue = "--:--";
    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        vm = new ViewModelProvider(this).get(TaskViewModel.class);

        etTitle = findViewById(R.id.etTitle);
        etDesc = findViewById(R.id.etDesc);
        spinner = findViewById(R.id.spinnerDifficulty);
        tvTime = findViewById(R.id.tvTime);
        Button btnPick = findViewById(R.id.btnPickTime);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        String[] diffs = new String[] {"Fácil", "Medio", "Difícil"};
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, diffs));

        btnPick.setOnClickListener(v -> showTimePicker());

        btnCancel.setOnClickListener(v -> finish());

        editingId = getIntent().getLongExtra("id", -1);
        if (editingId > 0) {
            // Load existing
            exec.execute(() -> {
                Task t = AppDatabase.getInstance(this).taskDao().getByIdSync(editingId);
                runOnUiThread(() -> {
                    if (t != null) {
                        etTitle.setText(t.title);
                        etDesc.setText(t.description);
                        if (t.difficulty != null) {
                            int pos = t.difficulty.equals("Fácil") ? 0 : t.difficulty.equals("Medio") ? 1 : 2;
                            spinner.setSelection(pos);
                        }
                        timeValue = t.timeHHmm == null ? "--:--" : t.timeHHmm;
                        tvTime.setText("Horario: " + timeValue);
                    }
                });
            });
        }

        btnSave.setOnClickListener(v -> save());
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        TimePickerDialog dlg = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            timeValue = String.format("%02d:%02d", hourOfDay, minute1);
            tvTime.setText("Horario: " + timeValue);
        }, hour, minute, true);
        dlg.show();
    }

    private void save() {
        String title = etTitle.getText() == null ? "" : etTitle.getText().toString().trim();
        String desc = etDesc.getText() == null ? "" : etDesc.getText().toString().trim();
        String dif = spinner.getSelectedItem() == null ? "Fácil" : spinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingId > 0) {
            vm.update(editingId, title, desc, dif, timeValue, rows -> {
                if (rows == -1) {
                    Toast.makeText(this, "Duplicado: ya existe un hobby con ese título", Toast.LENGTH_LONG).show();
                } else if (rows > 0) {
                    Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "No se pudo actualizar", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            vm.insert(title, desc, dif, timeValue, id -> {
                if (id != null && id > 0) {
                    Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Duplicado: ya existe un hobby con ese título", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}