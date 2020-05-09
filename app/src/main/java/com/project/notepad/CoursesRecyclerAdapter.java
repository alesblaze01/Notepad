package com.project.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.project.notepad.Utility.CourseInfo;

import java.util.List;

public class CoursesRecyclerAdapter extends RecyclerView.Adapter<CoursesRecyclerAdapter.NoteViewHolder> {
    final public Context mContext;
    private List<CourseInfo> mCourses;

    public CoursesRecyclerAdapter(Context context, List<CourseInfo> courseInfos) {
        mContext = context;
        mCourses = courseInfos;
    }
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_course_list,parent,false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        CourseInfo courseInfo = mCourses.get(position);
        holder.mCourseTitle.setText(courseInfo.getTitle());
        holder.mPosition = position;
    }

    @Override
    public int getItemCount() {
        return mCourses.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        public final TextView mCourseTitle;

        int mPosition;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            mCourseTitle = itemView.findViewById(R.id.text_view_course_list_item);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, mCourseTitle.getText() , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
