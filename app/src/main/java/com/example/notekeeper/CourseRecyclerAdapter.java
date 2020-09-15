package com.example.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static com.example.notekeeper.NoteKeeperDatabaseContract.*;

class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<CourseInfo> mCourses;
    private final LayoutInflater mLayoutInflater;

    private Cursor mCursor;
    private int mCourseTitlePos;
    private int mId;

    public CourseRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    private void populateColumnPosition() {
        if(mCursor == null)
            return;

        //get column indexes from mCursor
        mCourseTitlePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mId = mCursor.getColumnIndex(CourseInfoEntry._ID);
    }

    public void changeCusor(Cursor newCusor){
        /*the mean of this function is compare the new cursor with the old cursor
        if the new cursor has new value (different than the old cursor), if true assign the
        old cursor with the new one and then delete the old one*/
        Cursor oldCusor = mCursor;
        if(mCursor == newCusor)
            return;
        mCursor = newCusor;
        if(oldCusor !=null)
            oldCusor.close();
        populateColumnPosition();
        //thông báo dữ liệu trong recycler view đã thay đổi
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_course_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String courseTitle = mCursor.getString(mCourseTitlePos);
        int id = mCursor.getInt(mId);

        holder.mTextCourse.setText(courseTitle);
        holder._id = id;
    }

    @Override
    public int getItemCount() {
        return mCursor == null? 0 : mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public int _id;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextCourse = (TextView) itemView.findViewById(R.id.text_course);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, mCourses.get(_id).getTitle(),
                            Snackbar.LENGTH_LONG).show();

                }
            });
        }
    }
}