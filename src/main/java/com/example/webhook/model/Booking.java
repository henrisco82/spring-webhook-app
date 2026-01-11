package com.example.webhook.model;

import java.util.Date;
import java.util.Map;

public class Booking {
    private String providerId;
    private String providerName;
    private String bookerId;
    private String bookerName;
    private String bookerEmail;
    private Date startUTC;
    private Date endUTC;
    private String status;
    private Integer sessionMinutes;
    private String notes;
    private Double priceAtBooking;
    private String paymentIntentId;
    private String stripeSessionId;
    private Date createdAt;
    private Date updatedAt;

    // Constructors, Getters, Setters

    public Booking() {
    }

    public Booking(String providerId, String providerName, String bookerId, String bookerName, String bookerEmail,
            Date startUTC, Date endUTC, String status, Integer sessionMinutes, String notes,
            Double priceAtBooking, String paymentIntentId, String stripeSessionId, Date createdAt, Date updatedAt) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.bookerId = bookerId;
        this.bookerName = bookerName;
        this.bookerEmail = bookerEmail;
        this.startUTC = startUTC;
        this.endUTC = endUTC;
        this.status = status;
        this.sessionMinutes = sessionMinutes;
        this.notes = notes;
        this.priceAtBooking = priceAtBooking;
        this.paymentIntentId = paymentIntentId;
        this.stripeSessionId = stripeSessionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Map<String, Object> toMap() {
        return Map.ofEntries(
                Map.entry("providerId", providerId != null ? providerId : ""),
                Map.entry("providerName", providerName != null ? providerName : ""),
                Map.entry("bookerId", bookerId != null ? bookerId : ""),
                Map.entry("bookerName", bookerName != null ? bookerName : ""),
                Map.entry("bookerEmail", bookerEmail != null ? bookerEmail : ""),
                Map.entry("startUTC", startUTC),
                Map.entry("endUTC", endUTC),
                Map.entry("status", status),
                Map.entry("sessionMinutes", sessionMinutes),
                Map.entry("notes", notes != null ? notes : ""),
                Map.entry("priceAtBooking", priceAtBooking),
                Map.entry("paymentIntentId", paymentIntentId != null ? paymentIntentId : ""),
                Map.entry("stripeSessionId", stripeSessionId),
                Map.entry("createdAt", createdAt),
                Map.entry("updatedAt", updatedAt));
    }
}
