package com.instantappsample.service;

public class RvulService {
    private int mId;
    private String mName;
    private Integer[] mDoctorIds;
    private boolean mActive;

    public RvulService(int id, String name, Integer[] doctorIds, boolean acctive)
    {
        mId = id;
        mName = name;
        mDoctorIds = doctorIds;
        mActive = acctive;
    }

    public int getId() { return mId; }
    public String getName() { return mName; }
    public Integer[] getDoctorIds() { return mDoctorIds; }
    public boolean getActive() { return mActive; }
}
