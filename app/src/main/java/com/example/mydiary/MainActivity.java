package com.example.mydiary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.EditText;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton floatingActionButton;
    RecyclerView recyclerView;
    ImageView imgBtn;
    DiaryAdapter diaryAdapter;
    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatingActionButton = findViewById(R.id.floatingActionButton);
        recyclerView = findViewById(R.id.recyclerView);
        imgBtn = findViewById(R.id.menuBtn);
        floatingActionButton.setOnClickListener((v)->startActivity(new Intent(MainActivity.this, DiaryDetailsActivity.class)));
        imgBtn.setOnClickListener((v)->showMenu());
        FirebaseApp.initializeApp(this);
        setUpRecyclerView();
        searchEditText = findViewById(R.id.editTextText2);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed for this example
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Update the query based on the search text
                updateSearchQuery(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed for this example
            }
        });


    }
    public void showMenu()
    {
        // Display logout button on menu
        PopupMenu popupMenu = new PopupMenu(MainActivity.this,imgBtn);
        popupMenu.getMenu().add("User Agreement");
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getTitle()=="Logout")
                {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    finish();
                    return true;
                }
                if(item.getTitle()=="User Agreement")
                {
                    startActivity(new Intent(MainActivity.this, UserAgreementActivity.class));
                    return true;
                }
                return false;
            }
        });
    }
    public void setUpRecyclerView()
    {
        Query query = Utility.getCollectionReferenceForNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Diary> options = new FirestoreRecyclerOptions.Builder<Diary>()
                .setQuery(query, Diary.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        diaryAdapter = new DiaryAdapter(options,this);
        recyclerView.setAdapter(diaryAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        diaryAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        diaryAdapter.stopListening();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        diaryAdapter.notifyDataSetChanged();
    }

    private void updateSearchQuery(String searchText) {
        // Modify the query based on the search text
        Query query;
        if (TextUtils.isEmpty(searchText)) {
            // If search text is empty, show all notes
            query = Utility.getCollectionReferenceForNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            query = Utility.getCollectionReferenceForNotes()
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .whereGreaterThanOrEqualTo("title", searchText)
                    .whereLessThanOrEqualTo("title", searchText + "\uf8ff");
        }

        FirestoreRecyclerOptions<Diary> options = new FirestoreRecyclerOptions.Builder<Diary>()
                .setQuery(query, Diary.class).build();
        diaryAdapter.updateOptions(options); // Add this method to your DiaryAdapter class
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("MyDiary");
        builder.setIcon(R.drawable.diary1);
        builder.setMessage("Do you really want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.show();
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
    };
}