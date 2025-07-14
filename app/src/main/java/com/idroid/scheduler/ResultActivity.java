package com.idroid.scheduler;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    TextView txtDescription, txtGantt, txtStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        txtDescription = findViewById(R.id.txtDescription);
        txtGantt = findViewById(R.id.txtGanttChart);
        txtStats = findViewById(R.id.txtStats);

        // Retrieve data from Intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String gantt = getIntent().getStringExtra("gantt");
        String stats = getIntent().getStringExtra("stats");

        //added
        com.idroid.scheduler.GanttChartView ganttChartView = findViewById(R.id.ganttChartView);

        ArrayList<com.idroid.scheduler.GanttChartView.GanttBlock> blocks =
                getIntent().getParcelableArrayListExtra("blocks");

        if (blocks != null) {
            ganttChartView.setBlocks(blocks);
        }


        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        txtDescription.setText("Description:\n" + description);
        txtGantt.setText("Gantt Chart:\n" + gantt);
        txtStats.setText("Statistics:\n" + stats);
    }
}
