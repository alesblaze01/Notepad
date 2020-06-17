package com.project.notepad.Adpater;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.notepad.Contract.NotesDatabaseContract;
import com.project.notepad.Contract.NotesDatabaseContract.NotesInfoEntry;
import com.project.notepad.MainActivity;
import com.project.notepad.R;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.NoteViewHolder> {
    final public Context mContext;
    private Cursor mCursor;
    public void changeCursor(Cursor newCursor){
        if(mCursor!=null){
            mCursor.close();
        }
        mCursor = newCursor;
        notifyDataSetChanged();
    }
    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_note_list,parent,false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        int noteTitlePos = mCursor.getColumnIndex(NotesInfoEntry.COLUMN_NOTE_TITLE);
        int courseTitlePos = mCursor.getColumnIndex(NotesDatabaseContract.CourseInfoEntry.COLUMN_COURSE_TITLE);
        int idPos = mCursor.getColumnIndex(NotesInfoEntry._ID);

        String noteTitle = mCursor.getString(noteTitlePos);
        String courseTitle = mCursor.getString(courseTitlePos);
        int id = mCursor.getInt(idPos);

        holder.mNoteTitle.setText(noteTitle);
        holder.mCourseTitle.setText(courseTitle);
        holder.mId = id;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        public final TextView mCourseTitle;
        public final TextView mNoteTitle;
        int mId;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            mCourseTitle = itemView.findViewById(R.id.text_view_course_title);
            mNoteTitle = itemView.findViewById(R.id.text_view_note_title);
            itemView.setOnClickListener(v -> mContext.startActivity(MainActivity.getIntent(mContext,mId)));
        }
    }
}
