package com.sunrisedental.domain;

/**
 * Specialises Receptionist (matches the actor/class generalisation in Task A):
 * an Administrator can do everything a Receptionist can, plus manage staff
 * accounts and generate reports.
 */
public class Administrator extends Receptionist {

    public Administrator(int userId, String username, String passwordHash, String passwordSalt, String staffName) {
        super(userId, username, passwordHash, passwordSalt, staffName);
        this.role = "ADMINISTRATOR";
    }

    @Override
    public String[] getMenuOptions() {
        String[] base = super.getMenuOptions();
        String[] extended = new String[base.length + 2];
        System.arraycopy(base, 0, extended, 0, base.length - 1); // keep "Exit" last
        extended[base.length - 1] = "Manage Staff Accounts";
        extended[base.length] = "Generate Reports";
        extended[base.length + 1] = "Exit";
        return extended;
    }
}
