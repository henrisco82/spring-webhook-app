package com.example.webhook.controller;

import com.example.webhook.model.Booking;
import com.google.cloud.firestore.Firestore;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

import java.util.Date;
import java.util.Map;
import java.time.Instant;

@RestController
@RequestMapping("/api")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Autowired(required = false)
    private Firestore firestore;

    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request,
            @RequestHeader("stripe-signature") String sigHeader) {
        System.out.println("🔔 Stripe webhook received!");

        String payload;
        try {
            payload = request.getReader().lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to read request body");
        }

        if (endpointSecret == null) {
            System.err.println("❌ STRIPE_WEBHOOK_SECRET not configured");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook secret not configured");
        }

        Event event;

        try {
            event = Webhook.constructEvent(
                    payload, sigHeader, endpointSecret);
            System.out.println("✅ Webhook verified, event type: " + event.getType());
        } catch (SignatureVerificationException e) {
            System.err.println("❌ Webhook signature verification failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Webhook error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook Error: " + e.getMessage());
        }

        if ("checkout.session.completed".equals(event.getType())) {
            // Deserialize the object to a Session
            // Note: event.getDataObjectDeserializer().getObject() returns an Optional
            // We need to cast it properly.
            // Using the Stripe Java object accessors is safer.

            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            if (session == null) {
                System.err.println("❌ Failed to deserialize session object");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to deserialize session");
            }

            System.out.println("📦 Processing checkout.session.completed: " + session.getId());

            Map<String, String> metadata = session.getMetadata();

            if (metadata == null || metadata.isEmpty()) {
                System.err.println("❌ No metadata found in session");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No metadata in session");
            }

            String bookerId = metadata.get("bookerId");
            String providerId = metadata.get("providerId");
            String startUTCStr = metadata.get("startUTC");
            String endUTCStr = metadata.get("endUTC");

            if (bookerId == null || providerId == null || startUTCStr == null || endUTCStr == null) {
                System.err.println("❌ Missing required metadata fields");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required metadata fields");
            }

            try {
                if (firestore == null) {
                    System.err.println("❌ Firestore client not initialized (check credentials)");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database connection failed");
                }

                Date now = new Date();
                Date startUTC = Date.from(Instant.parse(startUTCStr));
                Date endUTC = Date.from(Instant.parse(endUTCStr));

                Booking booking = new Booking(
                        providerId,
                        metadata.getOrDefault("providerName", ""),
                        bookerId,
                        metadata.getOrDefault("bookerName", ""),
                        metadata.getOrDefault("bookerEmail", ""),
                        startUTC,
                        endUTC,
                        "confirmed",
                        Integer.parseInt(metadata.getOrDefault("sessionMinutes", "60")),
                        metadata.get("notes"),
                        Double.parseDouble(metadata.getOrDefault("price", "0")),
                        session.getPaymentIntent(),
                        session.getId(),
                        now,
                        now);

                Map<String, Object> bookingData = booking.toMap();
                System.out.println("📝 Creating booking for provider: " + bookingData.get("providerId"));

                var future = firestore.collection("bookings").add(bookingData);
                String bookingId = future.get().getId();

                System.out.println("✅ Booking created successfully with ID: " + bookingId);

                return ResponseEntity.ok("Received");

            } catch (Exception e) {
                System.err.println("❌ Failed to create booking in Firestore: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Database Error: " + e.getMessage());
            }

        }

        System.out.println("ℹ️ Unhandled event type: " + event.getType());
        return ResponseEntity.ok("Received");
    }
}
