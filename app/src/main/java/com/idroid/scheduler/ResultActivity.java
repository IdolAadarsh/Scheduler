package com.idroid.scheduler;

import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    TextView txtDescription, txtGantt, txtStats;
    GanttChartView ganttChartView;
    Button btnPlay, btnPause, btnStep;

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
        ganttChartView = findViewById(R.id.ganttChartView);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStep = findViewById(R.id.btnStep);

        // Retrieve data
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String gantt = getIntent().getStringExtra("gantt");
        String stats = getIntent().getStringExtra("stats");
        ArrayList<GanttChartView.GanttBlock> blocks =
                (ArrayList<GanttChartView.GanttBlock>) getIntent().getSerializableExtra("blocks");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        txtDescription.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT));

        txtGantt.setText("Gantt Chart:\n" + gantt);
        txtStats.setText("Statistics:\n" + stats);

        if (blocks != null) {
            ganttChartView.setBlocks(blocks);
        }

        btnPlay.setOnClickListener(v -> ganttChartView.playAnimation());
        btnPause.setOnClickListener(v -> ganttChartView.pauseAnimation());
        btnStep.setOnClickListener(v -> ganttChartView.stepForward());
    }
}
