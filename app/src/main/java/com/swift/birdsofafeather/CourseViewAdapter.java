package com.swift.birdsofafeather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swift.birdsofafeather.model.db.AppDatabase;
import com.swift.birdsofafeather.model.db.Class;
import com.swift.birdsofafeather.model.db.Session;
import com.swift.birdsofafeather.model.db.Student;
import com.swift.birdsofafeather.model.db.StudentWithClasses;
import com.swift.birdsofafeather.model.db.UUIDConverter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CourseViewAdapter extends RecyclerView.Adapter<CourseViewAdapter.ViewHolder> {
    private final List<Session> courses;

    public CourseViewAdapter(List<Session> courses) {
        super();
        this.courses = courses;
    }

    @NonNull
    @Override
    public CourseViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.display_course_with_student, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewAdapter.ViewHolder holder, int position) {
        holder.setCourse(courses.get(position));
    }

    @Override
    public int getItemCount() {
        return this.courses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView courseNameView;
        private Session course;

        ViewHolder(View itemView) {
            super(itemView);
            this.courseNameView = itemView.findViewById(R.id.student_course);
            itemView.setOnClickListener(this);
        }


        public void setCourse(Session course) {
            this.course = course;
            this.courseNameView.setText(course.getName());
        }

        @Override
        public void onClick(View view) {
            // set current_session_id in preferences to the new session_id
            // finish back to search student with similar classes

            Context context = view.getContext();
            SharedPreferences preferences = Utils.getSharedPreferences(context.getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("current_session_id", UUIDConverter.fromUUID(course.getId()));
            editor.putBoolean("new_class", false);
            editor.apply();
            ((Activity)context).finish();
        }
    }
}