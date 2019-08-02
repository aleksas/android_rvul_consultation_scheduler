package com.instantappsample.service;

import java.util.Collection;

public class RvulData {
    private Collection<RvulService> mRvulServices;
    private Collection<RvulDoctor> mRvulDoctors;

    public RvulData(Collection<RvulService> rvulServices, Collection<RvulDoctor> rvulDoctors)
    {
        mRvulServices = rvulServices;
        mRvulDoctors = rvulDoctors;
    }

    Collection<RvulService> getRvulServices() {
        return mRvulServices;
    }

    Collection<RvulDoctor> getRvulDoctors() {
        return mRvulDoctors;
    }
}
