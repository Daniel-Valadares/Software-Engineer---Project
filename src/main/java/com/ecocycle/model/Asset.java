package com.ecocycle.model;

import java.time.LocalDate;

/**
 * Modelo de Ativo (RF01).
 * Representa um item de hardware sob gestao do EcoCycle Manager.
 */
public class Asset {

    public enum Status {
        ACTIVE("active"),
        EXPIRING("expiring"),
        DISPOSED("disposed");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Status fromValue(String value) {
            for (Status s : values()) {
                if (s.value.equalsIgnoreCase(value)) {
                    return s;
                }
            }
            return ACTIVE;
        }
    }

    private String id;
    private String category;
    private String model;
    private LocalDate acquiredAt;
    private int lifespanYears;
    private Status status;

    public Asset() {
        this.status = Status.ACTIVE;
    }

    public Asset(String id, String category, String model, LocalDate acquiredAt, int lifespanYears, Status status) {
        this.id = id;
        this.category = category;
        this.model = model;
        this.acquiredAt = acquiredAt;
        this.lifespanYears = lifespanYears;
        this.status = status != null ? status : Status.ACTIVE;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public LocalDate getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDate acquiredAt) { this.acquiredAt = acquiredAt; }

    public int getLifespanYears() { return lifespanYears; }
    public void setLifespanYears(int lifespanYears) { this.lifespanYears = lifespanYears; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    /** Data prevista de fim de vida util. */
    public LocalDate getExpirationDate() {
        if (acquiredAt == null) return null;
        return acquiredAt.plusYears(lifespanYears);
    }

    @Override
    public String toString() {
        return id + " - " + category + " (" + status.getValue() + ")";
    }
}
