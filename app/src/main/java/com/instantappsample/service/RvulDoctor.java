package com.instantappsample.service;

import java.util.Date;
import java.util.HashMap;

public class RvulDoctor {
    public enum WorkStatus {
        Nonworking ("Nonworking"),
        Available("Available"),
        Busy("Busy"),
        Holiday("Holiday");

        private String mName;

        WorkStatus(String s) {
            mName = s;
        }

        public boolean equalsName(String otherName) {
            return mName.equals(otherName);
        }

        public String toString() {
            return this.mName;
        }
    }

    private int mId;
    private String mFirstName;
    private String mLastName;
    private boolean mActive;
    private HashMap<Date, WorkStatus> mSchedule;

    public RvulDoctor(int id, String firstName, String lastName, boolean active, HashMap<Date, WorkStatus> schedule)
    {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mActive = active;
        mSchedule = schedule;
    }

    public int getId() { return mId; }
    public String getFirstName() { return mFirstName; }
    public String getLastName() { return mLastName; }
    public boolean getActive() { return mActive; }
    public HashMap<Date, WorkStatus> getSchedule() { return mSchedule; }
}
