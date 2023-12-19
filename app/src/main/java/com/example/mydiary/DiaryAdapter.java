package com.example.mydiary;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class DiaryAdapter extends FirestoreRecyclerAdapter<Diary, DiaryAdapter.NoteViewHolder>
{
    Context context;
    public DiaryAdapter(@NonNull FirestoreRecyclerOptions<Diary> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Diary diary) {
        holder.title.setText(diary.title);
        holder.content.setText(diary.content);
        holder.timestamp.setText(Utility.timestampToString(diary.timestamp));

        //edit the notes
        holder.itemView.setOnClickListener((v)->{
            Intent intent = new Intent(context, DiaryDetailsActivity.class);
            intent.putExtra("title", diary.title);
            intent.putExtra("content", diary.content);
            String docId = this.getSnapshots().getSnapshot(position).getId(); // get the id of the specific note
            intent.putExtra("docId",docId);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_diary_item,parent,false);
        return new NoteViewHolder(view);
    }

    class NoteViewHolder extends RecyclerView.ViewHolder
    {
        TextView title,content,timestamp;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
    @Override
    public void startListening() {
        super.startListening();
        notifyDataSetChanged();
    }

    @Override
    public void stopListening() {
        super.stopListening();
        notifyDataSetChanged();
    }
}