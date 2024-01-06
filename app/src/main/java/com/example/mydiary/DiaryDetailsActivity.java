package com.example.mydiary;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class DiaryDetailsActivity extends AppCompatActivity {
    EditText title,content;
    ImageView saveBtn,changingImg;
    TextView pageTitle; // used to change the title when user edits the note
    String getTitle,getContent,docId;
    Button btn,clearImageButton;
    boolean isEditMode = false;
    TextView delete;
    private final int GALLERY_REQ_CODE = 1000;
    private Uri selectedImageUri; // Variable to store the selected image URI
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
        changingImg = findViewById(R.id.changingImg);
        btn = findViewById(R.id.button);
        clearImageButton = findViewById(R.id.clearImageButton); // Initialize the clearImageButton

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iGallery,GALLERY_REQ_CODE);
            }
        });
        // Set the onClickListener for clearing the selected image
        clearImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the selected image
                selectedImageUri = null;
                changingImg.setImageDrawable(null); // Clear the ImageView
                // Delete the image from Firebase Storage if in edit mode
                if (isEditMode && docId != null && !docId.isEmpty()) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                            .child("diary_images/" + docId + ".jpg");

                    storageReference.delete().addOnSuccessListener(aVoid -> {
                        // Image deleted successfully from Firebase Storage
                    }).addOnFailureListener(exception -> {
                        // Handle errors during image deletion
//                        Utility.showToast(DiaryDetailsActivity.this, "Failed to delete image");
                    });
                }
            }
        });

        // receive data of specific note
        getTitle = getIntent().getStringExtra("title");
        getContent = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        if(docId!=null && !docId.isEmpty())
        {
            isEditMode = true;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("diary_images/" + docId + ".jpg");

            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                // Load the image using the retrieved URL into your ImageView (changingImg)
                Glide.with(this)
                        .load(uri)
                        .into(changingImg);
            }).addOnFailureListener(exception -> {
                // Handle errors during image retrieval
//                Utility.showToast(DiaryDetailsActivity.this, "Failed to retrieve image");
            });
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
    public void saveDiary() {
        String ntitle = title.getText().toString();
        String ncontent = content.getText().toString();

        if (ntitle == null || ntitle.isEmpty()) {
            title.setError("Title is required");
            return;  // Stop execution if title is empty
        }

        if (ncontent == null || ncontent.isEmpty()) {
            content.setError("Content is required");
            return;  // Stop execution if content is empty
        }

        Diary diary = new Diary();
        diary.setTitle(ntitle);
        diary.setContent(ncontent);

        // Set the timestamp to the current time
        diary.setTimestamp(new Timestamp(new Date()));

        if (selectedImageUri != null) {
            // If an image is selected, save it to Firebase
            saveDiaryToFirebase(diary, selectedImageUri);
        } else {
            // If no image is selected, save the diary without an image
            saveDiaryToFirebase(diary, null);
        }
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
    public void saveDiaryToFirebase(Diary diary, Uri imageUri) {
        DocumentReference documentReference;

        if (isEditMode) {
            // updates the note
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        } else {
            // creates the note
            documentReference = Utility.getCollectionReferenceForNotes().document();
        }

        if (imageUri != null) {
            // Upload the image to Firebase Storage
            try {
                // Get the reference to the Firebase Storage
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("diary_images/" + documentReference.getId() + ".jpg");

                // Upload the image to Firebase Storage
                storageReference.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Get the download URL of the uploaded image
                            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Set the image URL in the diary object
                                diary.setImageUrl(uri.toString());

                                // Set other diary fields
                                documentReference.set(diary).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Diary is added or updated in the database
                                        finish();
                                    } else {
                                        // Diary is not added or updated in the database
                                        Utility.showToast(DiaryDetailsActivity.this, "Failed to add or update diary");
                                    }
                                });
                            });
                        })
                        .addOnFailureListener(exception -> {
                            // Handle errors during image upload
                            Utility.showToast(DiaryDetailsActivity.this, "Failed to upload image");
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            // No image selected, set other diary fields and update the database
            documentReference.set(diary).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Diary is added or updated in the database
                    finish();
                } else {
                    // Diary is not added or updated in the database
                    Utility.showToast(DiaryDetailsActivity.this, "Failed to add or update diary");
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if(requestCode == GALLERY_REQ_CODE)
            {
                // for gallery
                selectedImageUri = data.getData();
                changingImg.setImageURI(data.getData());
            }
        }
    }
}