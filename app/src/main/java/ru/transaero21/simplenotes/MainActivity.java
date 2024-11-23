package ru.transaero21.simplenotes;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> noteLauncher;
    private ArrayList<String> noteList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) { // Проверяем, был ли результат успешным
                        Intent data = result.getData(); // Получаем Intent с данными из возвращенной активности
                        if (data != null) { // Проверяем, что данные есть
                            String fileName = data.getStringExtra(Constants.EXTRA_FILE_NAME); // Получаем имя файла из Intent

                            // Проверяем, содержится ли имя файла в списке заметок
                            if (!noteList.contains(fileName)) {
                                noteList.add(fileName); // Если нет, добавляем его в список
                                adapter.notifyDataSetChanged(); // Обновляем адаптер для отображения изменений в UI
                            }
                        }
                    }
                }
        );

        noteList = loadNotes();

        ListView listView = findViewById(R.id.note_list);

        // Создаем адаптер для ListView
        adapter = new ArrayAdapter<>(this, R.layout.note_list_item, noteList);
        listView.setAdapter(adapter); // Устанавливаем адаптер для ListView

        // Устанавливаем действие нажатий на элемент списка
        listView.setOnItemClickListener((parent, view, position, id) -> {
            openNoteActivity(noteList.get(position));
        });

        // Устанавливаем действие долгого нажатия на элемент списка
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            deleteNode(position); // Удаляем заметку по позиции
            adapter.notifyDataSetChanged(); // Уведомляем адаптер об изменении данных, чтобы обновить отображение
            Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
            return true;
        });

        Button addNoteButton = findViewById(R.id.add_note_button);
        addNoteButton.setOnClickListener(v -> showFilenameInputDialog());
    }

    private void showFilenameInputDialog() {
        // Создаём объект для построения диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.filename_input_dialog_title); // Устанавливаем заголовок диалогового окна

        EditText input = new EditText(this); // Создаём EditText для ввода имени файла
        builder.setView(input); // Устанавливаем EditText как содержимое диалогового окна

        // Устанавливаем положительную кнопку для подтверждения ввода
        builder.setPositiveButton(R.string.filename_input_dialog_positive_button, (dialog, which) -> {
            String fileName = input.getText().toString().trim();

            if (fileName.isEmpty()) { // Проверяем, что имя файла не пустое
                Toast.makeText(this, R.string.filename_input_dialog_empty_file, Toast.LENGTH_SHORT).show();
                return;
            }

            if (noteList.contains(fileName)) { // Проверяем, существует ли уже файл с таким именем в списке заметок
                Toast.makeText(this, R.string.filename_input_dialog_file_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            openNoteActivity(fileName);
        });

        // Устанавливаем отрицательную кнопку для отмены ввода
        builder.setNegativeButton(
                R.string.filename_input_dialog_negative_button,
                (dialog, which) -> dialog.cancel()
        );

        // Показываем диалоговое окно пользователю
        builder.show();
    }

    private ArrayList<String> loadNotes() {
        ArrayList<String> noteList = new ArrayList<>();
        File[] files = getFilesDir().listFiles();
        if (files != null) {
            for (File file : files) {
                noteList.add(file.getName());
            }
        }
        return noteList;
    }

    private void deleteNode(int position) {
        String fileName = noteList.get(position);
        File file = new File(getFilesDir(), fileName);
        if (file.exists()) {
            file.delete();
        }
        noteList.remove(position);
    }

    private void openNoteActivity(String fileName) {
        // Создаем новый Intent для перехода к NoteActivity
        Intent intent = new Intent(this, NoteActivity.class);
        // Добавляем имя файла в качестве дополнительной информации к Intent
        intent.putExtra(Constants.EXTRA_FILE_NAME, fileName);
        noteLauncher.launch(intent); // Запускаем NoteActivity
    }
}