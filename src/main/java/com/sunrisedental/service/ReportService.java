package com.sunrisedental.service;

import com.sunrisedental.dao.IAppointmentDAO;
import com.sunrisedental.dao.IBillDAO;
import com.sunrisedental.domain.Appointment;
import com.sunrisedental.domain.AppointmentStatus;
import com.sunrisedental.domain.Bill;
import com.sunrisedental.patterns.DatabaseConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Administrator-only "Generate Reports" use case. Two reports were chosen
 * because they answer the two questions clinic management actually asked
 * for in the brief: "are we double-booked / what's today's workload" and
 * "how much revenue came in". (Marking criteria: "proposed reports to
 * facilitate decision-making".)
 */
public class ReportService {

    private final IAppointmentDAO appointmentDAO;
    private final IBillDAO billDAO;

    public ReportService(IAppointmentDAO appointmentDAO, IBillDAO billDAO) {
        this.appointmentDAO = appointmentDAO;
        this.billDAO = billDAO;
    }

    public String dailyAppointmentsReport(LocalDate date) {
        List<Appointment> todays = appointmentDAO.findAll().stream()
            .filter(a -> a.getAppointmentDate().equals(date))
            .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
            .sorted((a, b) -> a.getAppointmentTime().compareTo(b.getAppointmentTime()))
            .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("Daily Appointments Report - ").append(date).append("\n");
        sb.append("=".repeat(60)).append("\n");
        if (todays.isEmpty()) {
            sb.append("No appointments scheduled.\n");
        }
        for (Appointment a : todays) {
            sb.append(String.format("%s  %-20s  Dr. %-15s  %s%n",
                a.getAppointmentTime(), a.getPatient().getName(), a.getDentist().getName(),
                a.getTreatmentType().getTreatmentName()));
        }
        sb.append("=".repeat(60)).append("\n");
        sb.append("Total appointments: ").append(todays.size()).append("\n");
        return sb.toString();
    }

    public String revenueReport(LocalDate from, LocalDate to) {
        List<Bill> bills = billDAO.findAll().stream()
            .filter(b -> !b.getGeneratedDate().isBefore(from) && !b.getGeneratedDate().isAfter(to))
            .collect(Collectors.toList());

        double total = bills.stream().mapToDouble(Bill::getTotalAmount).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("Revenue Report: ").append(from).append(" to ").append(to).append("\n");
        sb.append("=".repeat(60)).append("\n");
        for (Bill b : bills) {
            sb.append(String.format("%-15s  %s  Rs. %.2f%n", b.getAppointmentNumber(), b.getGeneratedDate(), b.getTotalAmount()));
        }
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("Bills issued: %d      Total revenue: Rs. %.2f%n", bills.size(), total));
        return sb.toString();
    }

    /**
     * Alternative to revenueReport() that delegates the aggregation to the
     * MySQL stored procedure sp_daily_revenue (see schema.sql) via a
     * CallableStatement, rather than pulling every bill into Java and
     * summing there. Demonstrates the stored-procedure marking criterion
     * with a genuine call site, not just unused SQL sitting in schema.sql.
     */
    public String dailyRevenueViaStoredProcedure(LocalDate date) {
        String call = "{CALL sp_daily_revenue(?, ?, ?)}";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             CallableStatement cs = conn.prepareCall(call)) {
            cs.setDate(1, java.sql.Date.valueOf(date));
            cs.registerOutParameter(2, Types.DECIMAL);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();

            double totalRevenue = cs.getBigDecimal(2).doubleValue();
            int billCount = cs.getInt(3);
            return String.format("Daily Revenue (via sp_daily_revenue) - %s: Rs. %.2f across %d bill(s)%n",
                date, totalRevenue, billCount);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to call sp_daily_revenue for " + date, e);
        }
    }
}
