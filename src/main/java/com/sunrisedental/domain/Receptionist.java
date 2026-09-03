package com.sunrisedental.domain;

/**
 * Front-desk staff member: the primary actor in the Task A use case diagram.
 * Can log in, register/search/display/reschedule/cancel appointments,
 * calculate and print bills, and view help.
 */
public class Receptionist extends User {

    private String staffName;

    public Receptionist(int userId, String username, String passwordHash, String passwordSalt, String staffName) {
        super(userId, username, passwordHash, passwordSalt, "RECEPTIONIST");
        this.staffName = staffName;
    }

    public String getStaffName() {
        return staffName;
    }

    @Override
    public String[] getMenuOptions() {
        return new String[] {
            "Register New Appointment",
            "Search / Display Appointment",
            "Update / Reschedule Appointment",
            "Cancel Appointment",
            "Calculate & Print Bill",
            "Help",
            "Exit"
        };
    }
}
