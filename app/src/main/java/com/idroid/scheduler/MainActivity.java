package com.idroid.scheduler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity {
    private RadioGroup algorithmGroup;
    private EditText editName, editArrival, editBurst, editPriority;
    private TextView txtResults;
    private ProcessAdapter adapter;
    private final ArrayList<ProcessModel> processList = new ArrayList<>();

    @SuppressLint({"MissingInflatedId", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        algorithmGroup = findViewById(R.id.algorithmGroup);
        editName = findViewById(R.id.editProcessName);
        editArrival = findViewById(R.id.editArrival);
        editBurst = findViewById(R.id.editBurst);
        editPriority = findViewById(R.id.editPriority);
        Button addButton = findViewById(R.id.btnAddProcess);
        Button runButton = findViewById(R.id.btnRunSimulation);
        Button clearButton = findViewById(R.id.btnClearProcesses);
        RecyclerView recyclerView = findViewById(R.id.processRecyclerView);
        txtResults = findViewById(R.id.txtResults);

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

        // Prepare intent
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", getAlgorithmDescription(title));
        intent.putExtra("gantt", ganttText.toString());
        intent.putExtra("stats", stats);
        intent.putParcelableArrayListExtra("blocks", (ArrayList<? extends Parcelable>) blocks);
        startActivity(intent);
    }

    private String getAlgorithmDescription(String title) {
        switch (title) {
            case "FCFS Gantt Chart":
                return "<b>First-Come, First-Served (FCFS)</b><br><br>" +
                        "🔹 <b>Type:</b> Non-preemptive<br>" +
                        "🔹 <b>Strategy:</b> Processes are executed in the order they arrive.<br>" +
                        "🔹 <b>Advantage:</b> Simple to implement<br>" +
                        "🔹 <b>Disadvantage:</b> May cause long wait times for short processes<br><br>" +
                        "<i>Use case:</i> Suitable when all jobs arrive nearly at the same time.";

            case "SJF Gantt Chart":
                return "<b>Shortest Job First (SJF)</b><br><br>" +
                        "🔹 <b>Type:</b> Non-preemptive<br>" +
                        "🔹 <b>Strategy:</b> Executes the process with the shortest burst time.<br>" +
                        "🔹 <b>Advantage:</b> Minimizes average waiting time<br>" +
                        "🔹 <b>Disadvantage:</b> Starvation for longer processes<br><br>" +
                        "<i>Use case:</i> Best when burst times are known in advance.";

            case "Priority Gantt Chart":
                return "<b>Priority Scheduling</b><br><br>" +
                        "🔹 <b>Type:</b> Non-preemptive<br>" +
                        "🔹 <b>Strategy:</b> Executes the highest-priority process first (lower number = higher priority).<br>" +
                        "🔹 <b>Advantage:</b> Handles important tasks first<br>" +
                        "🔹 <b>Disadvantage:</b> Starvation of low-priority tasks<br><br>" +
                        "<i>Use case:</i> Systems where processes have different importance levels.";

            case "Round Robin Gantt Chart":
                return "<b>Round Robin (RR)</b><br><br>" +
                        "🔹 <b>Type:</b> Preemptive<br>" +
                        "🔹 <b>Strategy:</b> Each process gets a fixed time quantum in a cyclic order.<br>" +
                        "🔹 <b>Advantage:</b> Fair to all processes<br>" +
                        "🔹 <b>Disadvantage:</b> Performance depends on time quantum<br><br>" +
                        "<i>Use case:</i> Ideal for time-sharing systems and multitasking environments.";

            default:
                return "<b>CPU Scheduling Algorithm</b><br><br>" +
                        "Schedules processes based on specific criteria like arrival time, burst time, or priority.<br>" +
                        "Choose an algorithm to see how the CPU time is allocated among processes.";
        }
    }


    private void runFCFS() {
        ArrayList<ProcessModel> list = new ArrayList<>(processList);
        list.sort(Comparator.comparingInt(p -> p.arrival));
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
