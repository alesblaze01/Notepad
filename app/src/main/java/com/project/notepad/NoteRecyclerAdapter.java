package com.project.notepad;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.project.notepad.Utility.NoteInfo;
import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.NoteViewHolder> {
    final public Context mContext;
    private List<NoteInfo> mNoteInfo;

    public NoteRecyclerAdapter(Context context,List<NoteInfo> noteInfos) {
        mContext = context;
        mNoteInfo = noteInfos;
    }
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_note_list,parent,false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteInfo note = mNoteInfo.get(position);
        holder.mNoteTitle.setText(note.getTitle());
        holder.mCourseTitle.setText(note.getCourse().getTitle());
        holder.mPosition = position;
    }

    @Override
    public int getItemCount() {
        return mNoteInfo.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        public final TextView mCourseTitle;
        public final TextView mNoteTitle;
        int mPosition;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            mCourseTitle = itemView.findViewById(R.id.text_view_course_title);
            mNoteTitle = itemView.findViewById(R.id.text_view_note_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,MainActivity.class);
                    intent.putExtra(MainActivity.NOTE_POSITION , mPosition);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
