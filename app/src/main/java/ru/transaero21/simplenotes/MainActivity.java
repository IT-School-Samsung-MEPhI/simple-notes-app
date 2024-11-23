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
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String fileName = data.getStringExtra(Constants.EXTRA_FILE_NAME);
                            if (!noteList.contains(fileName)) {
                                noteList.add(fileName);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
        );

        noteList = loadNotes();

        ListView listView = findViewById(R.id.note_list);
        adapter = new ArrayAdapter<>(this, R.layout.note_list_item, noteList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            openNoteActivity(noteList.get(position));
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            deleteNode(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
            return true;
        });

        Button addNoteButton = findViewById(R.id.add_note_button);
        addNoteButton.setOnClickListener(v -> showFilenameInputDialog());
    }

    private void showFilenameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.filename_input_dialog_title);

        EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton(R.string.filename_input_dialog_positive_button, (dialog, which) -> {
            String fileName = input.getText().toString().trim();

            if (fileName.isEmpty()) {
                Toast.makeText(this, R.string.filename_input_dialog_empty_file, Toast.LENGTH_SHORT).show();
                return;
            }

            if (noteList.contains(fileName)) {
                Toast.makeText(this, R.string.filename_input_dialog_file_exists, Toast.LENGTH_SHORT).show();
                return;
            }

            openNoteActivity(fileName);
        });

        builder.setNegativeButton(
                R.string.filename_input_dialog_negative_button,
                (dialog, which) -> dialog.cancel()
        );

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
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(Constants.EXTRA_FILE_NAME, fileName);
        noteLauncher.launch(intent);
    }
}