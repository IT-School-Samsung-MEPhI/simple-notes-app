package ru.transaero21.simplenotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class NoteActivity extends AppCompatActivity {
    private EditText noteEditText;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        fileName = getIntent().getStringExtra(Constants.EXTRA_FILE_NAME);

        noteEditText = findViewById(R.id.note_edit_text);
        noteEditText.setText(loadNoteContent(fileName));

        Button saveNoteButton = findViewById(R.id.save_note_button);
        saveNoteButton.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String noteContent = noteEditText.getText().toString().trim();

        if (noteContent.isEmpty()) {
            Toast.makeText(this, R.string.note_edit_text_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        saveNoteContent(fileName, noteContent);

        Intent resIntent = new Intent(); // Создаём intent ля возврата результата в предыдущую активность
        resIntent.putExtra(Constants.EXTRA_FILE_NAME, fileName); // Передаём имя файла
        setResult(RESULT_OK, resIntent); // Устанавливаем код и intent как результат активности

        finish(); // Завершаем текущую активность
    }

    private String loadNoteContent(String fileName) {
        /* StringBuilder нужен для эффективного конкатенирования (склеивания) строк, чтение
         * файла будет побитово, поэтому будет неэффективно пересоздавать строку на каждый
         * символ, этот класс создан как раз для подобных случаев
         *
         * https://metanit.com/sharp/tutorial/7.3.php
         */
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = openFileInput(fileName)) { // Открываем файл для чтения
            int c; // Метод read возвращает биты
            while ((c = fis.read()) != -1) {
                /* Приводим бит к символу, у StringBuilder есть метод для добавления чисел,
                 * поэтому необходимо приводить бит к символу
                 */
                sb.append((char) c);
            }
        } catch (IOException e) {
            return null; // Возвращаем null если произошла ошибка
        }
        return sb.toString();
    }

    private void saveNoteContent(String fileName, String content) {
        try (FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
        } catch (IOException ignored) { }
    }
}