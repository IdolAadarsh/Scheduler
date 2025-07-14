package com.idroid.scheduler;

public class ProcessModel {
    public String name;
    public int arrival, burst, priority;


    public ProcessModel(String name, int arrival, int burst, int priority) {
        this.name = name;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
    }
}