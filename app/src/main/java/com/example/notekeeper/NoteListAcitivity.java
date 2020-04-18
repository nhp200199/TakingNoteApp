package com.example.notekeeper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class NoteListAcitivity extends AppCompatActivity {


    private NoteRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NoteListAcitivity.this, NoteActivity.class));
            }
        });

        connectViews();
    }

    @Override
    protected  void onResume(){
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }
    private void connectViews() {
        final RecyclerView recyclerNotes = findViewById(R.id.list_notes);
        final LinearLayoutManager linearLayout = new LinearLayoutManager(this);
        recyclerNotes.setLayoutManager(linearLayout);


        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mAdapter = new NoteRecyclerAdapter(this, notes);
        recyclerNotes.setAdapter(mAdapter);

//        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(NoteListAcitivity.this, NoteActivity.class);
//                intent.putExtra(NoteActivity.NOTE_POSITION, position);
//
//                startActivity(intent);
//            }
//        });
    }

}
