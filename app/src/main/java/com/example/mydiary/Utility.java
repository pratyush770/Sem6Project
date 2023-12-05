package com.example.mydiary;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class Utility
{
    static void showToast(Context context,String message)
    {
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show();
    }
    static CollectionReference getCollectionReferenceForNotes(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();  // get the current user
        return FirebaseFirestore.getInstance().collection("diary").document(currentUser.getUid()).
                collection("mydiary");
        // creates a collection named notes which contains unique id of each user and then creates another
        // collection named mynotes which has the notes data
    }
    static String timestampToString(Timestamp timestamp)
    {
        return new SimpleDateFormat("MM/dd/yyyy").format(timestamp.toDate());
    }

}