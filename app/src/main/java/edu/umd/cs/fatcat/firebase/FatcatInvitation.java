package edu.umd.cs.fatcat.firebase;

import android.support.annotation.NonNull;

public class FatcatInvitation implements Comparable<FatcatInvitation>{
    private int mStatus = PENDING;
    private FatcatEvent mEvent;

    public final static int PENDING = 1;
    public final static int ACCEPTED = 2;
    public final static int DECLINED = 3;

    public FatcatInvitation(FatcatEvent evt, int status) {
        mEvent = evt;
        mStatus = status;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public FatcatEvent getEvent() {
        return mEvent;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FatcatInvitation) {
            FatcatInvitation invite = (FatcatInvitation) other;
            return invite.getEvent().getEventID().equals(mEvent.getEventID());
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(@NonNull FatcatInvitation fatcatInvitation) {
        return getEvent().compareTo(fatcatInvitation.getEvent());
    }
}
