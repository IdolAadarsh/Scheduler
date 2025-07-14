package com.idroid.scheduler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {
    private RadioGroup algorithmGroup;
    private EditText editName, editArrival, editBurst, editPriority;
    private Button addButton, runButton;
    private RecyclerView recyclerView;
    private TextView txtResults, performanceStats;
    private GanttChartView ganttChart;
    private ProcessAdapter adapter;
    private final ArrayList<ProcessModel> processList = new ArrayList<>();

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar (App bar)
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Initialize views
        algorithmGroup = findViewById(R.id.algorithmGroup);
        editName = findViewById(R.id.editProcessName);
        editArrival = findViewById(R.id.editArrival);
        editBurst = findViewById(R.id.editBurst);
        editPriority = findViewById(R.id.editPriority);
        addButton = findViewById(R.id.btnAddProcess);
        runButton = findViewById(R.id.btnRunSimulation);
        Button clearButton = findViewById(R.id.btnClearProcesses);
        recyclerView = findViewById(R.id.processRecyclerView);
        txtResults = findViewById(R.id.txtResults);
        //performanceStats = findViewById(R.id.performanceStats);
        //ganttChart = findViewById(R.id.ganttChart);

        adapter = new ProcessAdapter(processList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Add process
        addButton.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            if (name.isEmpty()) name = "P" + (processList.size() + 1);
            int arrival = parseIntOrZero(editArrival.getText().toString());
            int burst = parseIntOrZero(editBurst.getText().toString());
            int priority = editPriority.getText().toString().isEmpty() ? -1 :
                    Integer.parseInt(editPriority.getText().toString());
            processList.add(new ProcessModel(name, arrival, burst, priority));
            adapter.notifyDataSetChanged();
            editName.setText("");
            editArrival.setText("");
            editBurst.setText("");
            editPriority.setText("");
        });

        // Run simulation
        runButton.setOnClickListener(v -> {
            int selectedId = algorithmGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.radioFcfs) runFCFS();
            else if (selectedId == R.id.radioSjf) runSJF();
            else if (selectedId == R.id.radioRoundRobin) runRoundRobin(2);
            else if (selectedId == R.id.radioPriority) runPriority();
        });

        // Clear processes
        clearButton.setOnClickListener(v -> {
            processList.clear();
            adapter.notifyDataSetChanged();
            txtResults.setText("");
            performanceStats.setText("");
            ganttChart.setBlocks(new ArrayList<>());
        });
    }

    private int parseIntOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void displayResult(String title, ArrayList<GanttChartView.GanttBlock> blocks,
                               HashMap<String, Integer> waitingTimes,
                               HashMap<String, Integer> turnaroundTimes) {

        StringBuilder ganttText = new StringBuilder();
        int totalWT = 0, totalTAT = 0;

        for (GanttChartView.GanttBlock block : blocks) {
            ganttText.append(block.name).append(" [")
                    .append(block.start).append("-")
                    .append(block.end).append("]  ");
        }

        int n = waitingTimes.size();
        for (String name : waitingTimes.keySet()) {
            totalWT += waitingTimes.get(name);
            totalTAT += turnaroundTimes.get(name);
        }

        double avgWT = n > 0 ? (double) totalWT / n : 0;
        double avgTAT = n > 0 ? (double) totalTAT / n : 0;
        double throughput = n > 0 && !blocks.isEmpty() ?
                (double) n / (blocks.get(blocks.size() - 1).end) : 0;

        String stats = "Avg Waiting Time: " + String.format("%.2f", avgWT) +
                "\nAvg Turnaround Time: " + String.format("%.2f", avgTAT) +
                "\nThroughput: " + String.format("%.2f", throughput) + " processes/unit time";

        // Fetch the explanation
        String description = getAlgorithmDescription(title);

        // Start result activity
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("gantt", ganttText.toString());
        intent.putExtra("stats", stats);
        intent.putExtra("blocks", blocks);
        startActivity(intent);
    }

    private String getAlgorithmDescription(String title) {
        switch (title) {
            case "FCFS Gantt Chart":
                return "First-Come, First-Served (FCFS) is a simple scheduling algorithm where the first process to arrive is the first to be executed.";
            case "SJF Gantt Chart":
                return "Shortest Job First (SJF) selects the process with the smallest burst time. Non-preemptive version is used here.";
            case "Priority Gantt Chart":
                return "Priority Scheduling executes processes based on priority value. Lower number means higher priority.";
            case "Round Robin Gantt Chart":
                return "Round Robin scheduling cycles through all ready processes using a fixed time quantum, ensuring fairness.";
            default:
                return "This algorithm schedules processes based on defined criteria.";
        }
    }



    private void runFCFS() {
        ArrayList<ProcessModel> list = new ArrayList<>(processList);
        Collections.sort(list, Comparator.comparingInt(p -> p.arrival));
        ArrayList<GanttChartView.GanttBlock> blocks = new ArrayList<>();
        HashMap<String, Integer> waiting = new HashMap<>();
        HashMap<String, Integer> turnaround = new HashMap<>();

        int time = 0;
        for (ProcessModel p : list) {
            int start = Math.max(time, p.arrival);
            int wt = start - p.arrival;
            int tat = wt + p.burst;
            waiting.put(p.name, wt);
            turnaround.put(p.name, tat);
            blocks.add(new GanttChartView.GanttBlock(p.name, start, start + p.burst));
            time = start + p.burst;
        }
        displayResult("FCFS Gantt Chart", blocks, waiting, turnaround);
    }

    private void runSJF() {
        ArrayList<ProcessModel> list = new ArrayList<>(processList);
        Collections.sort(list, Comparator.comparingInt(p -> p.arrival));
        ArrayList<ProcessModel> readyQueue = new ArrayList<>();
        ArrayList<GanttChartView.GanttBlock> blocks = new ArrayList<>();
        HashMap<String, Integer> waiting = new HashMap<>();
        HashMap<String, Integer> turnaround = new HashMap<>();

        int time = 0;
        while (!list.isEmpty() || !readyQueue.isEmpty()) {
            for (Iterator<ProcessModel> it = list.iterator(); it.hasNext();) {
                ProcessModel p = it.next();
                if (p.arrival <= time) {
                    readyQueue.add(p);
                    it.remove();
                }
            }
            if (!readyQueue.isEmpty()) {
                readyQueue.sort(Comparator.comparingInt(p -> p.burst));
                ProcessModel shortest = readyQueue.remove(0);
                waiting.put(shortest.name, time - shortest.arrival);
                turnaround.put(shortest.name, time - shortest.arrival + shortest.burst);
                blocks.add(new GanttChartView.GanttBlock(shortest.name, time, time + shortest.burst));
                time += shortest.burst;
            } else {
                time++;
            }
        }
        displayResult("SJF Gantt Chart", blocks, waiting, turnaround);
    }

    private void runRoundRobin(int quantum) {
        ArrayList<ProcessModel> list = new ArrayList<>(processList);
        list.sort(Comparator.comparingInt(p -> p.arrival));
        Queue<ProcessModel> queue = new LinkedList<>();
        HashMap<String, Integer> remaining = new HashMap<>();
        HashMap<String, Integer> waiting = new HashMap<>();
        HashMap<String, Integer> turnaround = new HashMap<>();
        ArrayList<GanttChartView.GanttBlock> blocks = new ArrayList<>();

        int time = 0, index = 0;
        for (ProcessModel p : list) remaining.put(p.name, p.burst);

        while (!queue.isEmpty() || index < list.size()) {
            while (index < list.size() && list.get(index).arrival <= time) {
                queue.add(list.get(index));
                index++;
            }
            if (!queue.isEmpty()) {
                ProcessModel current = queue.poll();
                int runTime = Math.min(quantum, remaining.get(current.name));
                blocks.add(new GanttChartView.GanttBlock(current.name, time, time + runTime));
                time += runTime;
                remaining.put(current.name, remaining.get(current.name) - runTime);
                if (remaining.get(current.name) > 0) {
                    while (index < list.size() && list.get(index).arrival <= time) {
                        queue.add(list.get(index));
                        index++;
                    }
                    queue.add(current);
                } else {
                    int wt = time - current.arrival - current.burst;
                    int tat = time - current.arrival;
                    waiting.put(current.name, wt);
                    turnaround.put(current.name, tat);
                }
            } else {
                time++;
            }
        }
        displayResult("Round Robin Gantt Chart", blocks, waiting, turnaround);
    }

    private void runPriority() {
        ArrayList<ProcessModel> list = new ArrayList<>(processList);
        list.removeIf(p -> p.priority < 0);
        list.sort(Comparator.comparingInt(p -> p.arrival));
        ArrayList<ProcessModel> readyQueue = new ArrayList<>();
        ArrayList<GanttChartView.GanttBlock> blocks = new ArrayList<>();
        HashMap<String, Integer> waiting = new HashMap<>();
        HashMap<String, Integer> turnaround = new HashMap<>();

        int time = 0;
        while (!list.isEmpty() || !readyQueue.isEmpty()) {
            for (Iterator<ProcessModel> it = list.iterator(); it.hasNext();) {
                ProcessModel p = it.next();
                if (p.arrival <= time) {
                    readyQueue.add(p);
                    it.remove();
                }
            }
            if (!readyQueue.isEmpty()) {
                readyQueue.sort(Comparator.comparingInt(p -> p.priority));
                ProcessModel highest = readyQueue.remove(0);
                waiting.put(highest.name, time - highest.arrival);
                turnaround.put(highest.name, time - highest.arrival + highest.burst);
                blocks.add(new GanttChartView.GanttBlock(highest.name, time, time + highest.burst));
                time += highest.burst;
            } else {
                time++;
            }
        }
        displayResult("Priority Gantt Chart", blocks, waiting, turnaround);
    }
}
