package com.intouch.IntouchApps.liveroom;

public enum ParticipantStatus {
    INVITED,   // slot created, not yet claimed
    JOINED,    // user claimed slot
    ACTIVE,    // room started and user participating
    LEFT,
    REMOVED
}
