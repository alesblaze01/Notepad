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

import com.project.notepad.Contract.NoteContentContract.Courses;
import com.project.notepad.CourseActivity;
import com.project.notepad.R;

public class CoursesRecyclerAdapter extends RecyclerView.Adapter<CoursesRecyclerAdapter.NoteViewHolder> {
    private final Context mContext;
    private Cursor mCoursesCursor;

    public CoursesRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCoursesCursor = cursor;
    }

//    public void changeCursor(Cursor newCursor){
//        if (mCoursesCursor!=null){
//            newCursor.close();
//        }
//        mCoursesCursor=newCursor;
//        notifyDataSetChanged();
//    }
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_course_list,parent,false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        int courseTitlePos = mCoursesCursor.getColumnIndex(Courses.COURSE_TITLE);
        int courseIdPos = mCoursesCursor.getColumnIndex(Courses.COURSE_ID);
        int idPos = mCoursesCursor.getColumnIndex(Courses._ID);

        mCoursesCursor.moveToPosition(position);

        String courseTitle = mCoursesCursor.getString(courseTitlePos);
        holder.mCourseTitle.setText(courseTitle);

        holder.mPosition = position;
        holder.mCourseId = mCoursesCursor.getString(courseIdPos);
        holder.dbId = mCoursesCursor.getInt(idPos);
    }

    @Override
    public int getItemCount() {
        return mCoursesCursor.getCount();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder{
        final TextView mCourseTitle;
        String mCourseId;
        int dbId;
        int mPosition;
        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            mCourseTitle = itemView.findViewById(R.id.text_view_course_list_item);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, CourseActivity.class);
                    intent.putExtra(CourseActivity.COURSE_DB_ID , dbId);
                    intent.putExtra(CourseActivity.COURSE_ID,mCourseId);
                    intent.putExtra(CourseActivity.COURSE_TITLE,mCourseTitle.getText().toString());
                    mContext.startActivity(intent);
//                    Toast.makeText(mContext, mCourseTitle.getText() , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
