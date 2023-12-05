package com.example.mydiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.Timestamp;

import java.util.Date;

public class DiaryDetailsActivity extends AppCompatActivity {
    EditText title,content;
    ImageView saveBtn;
    TextView pageTitle; // used to change the title when user edits the note
    String getTitle,getContent,docId;
    boolean isEditMode = false;
    TextView delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_details);

        title = findViewById(R.id.editTextText4);
        content = findViewById(R.id.editTextText5);
        saveBtn = findViewById(R.id.imageView2);
        saveBtn.setOnClickListener((v)->saveDiary());
        pageTitle = findViewById(R.id.textView3);
        delete = findViewById(R.id.textView6);

        // receive data of specific note
        getTitle = getIntent().getStringExtra("title");
        getContent = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        if(docId!=null && !docId.isEmpty())
        {
            isEditMode = true;
        }
        // gets the title and content of the note
        title.setText(getTitle);
        content.setText(getContent);
        if(isEditMode)
        {
            pageTitle.setText("Edit your entry");
            delete.setVisibility(View.VISIBLE);  // delete note text becomes visible in edit mode
        }
        delete.setOnClickListener((v)-> deleteDiaryFromFireBase());
    }
    public void saveDiary(){
        String ntitle = title.getText().toString();
        String ncontent = content.getText().toString();
        if(ntitle==null || ntitle.isEmpty()){
            title.setError("Title is required");
        }
        if(ncontent==null || ncontent.isEmpty()){
            content.setError("Content is required");
        }
        Diary diary = new Diary();
        diary.setTitle(ntitle);
        diary.setContent(ncontent);
        // Set the timestamp to the current time
        diary.setTimestamp(new Timestamp(new Date()));
        saveDiaryToFirebase(diary);
    }
    public void saveDiaryToFirebase(Diary diary)
    {
        DocumentReference documentReference;
        if(isEditMode)
        {
            // updates the note
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        }
        else
        {
            // creates the note
            documentReference = Utility.getCollectionReferenceForNotes().document();
        }
        documentReference.set(diary).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    // note is added in the database
                    // Utility.showToast(NoteDetailsActivity.this,"Note added successfully");
                    finish();
                }
                else
                {
                    // note is not added in the database
                    Utility.showToast(DiaryDetailsActivity.this,"Failed to add note");
                }
            }
        });
    }
    public void deleteDiaryFromFireBase()
    {
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    // note is added in the database
                    // Utility.showToast(NoteDetailsActivity.this,"Note deleted successfully");
                    finish();
                }
                else
                {
                    // note is not added in the database
                    Utility.showToast(DiaryDetailsActivity.this,"Failed to delete note");
                }
            }
        });
    }
}

