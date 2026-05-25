package com.ecocycle.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Modelo de Ativo (RF01 / RF02 / RF05).
 * Representa um item de hardware sob gestao do EcoCycle Manager.
 */
public class Asset {

    /** Nivel de periculosidade ambiental do ativo (RF05). */
    public enum HazardLevel {
        HIGH("Alta", "Contém baterias, metais pesados ou substâncias tóxicas"),
        MEDIUM("Média", "Contém componentes eletrônicos recicláveis"),
        LOW("Baixa", "Materiais de baixo impacto ambiental");

        private final String label;
        private final String description;

        HazardLevel(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }

        @Override
        public String toString() { return label; }
    }

    public enum Status {
        ACTIVE("active"),
        EXPIRING("expiring"),
        OVERDUE("overdue"),
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

    // -----------------------------------------------------------------------
    // Keywords para classificacao de periculosidade (RF05)
    // -----------------------------------------------------------------------
    private static final String[] HIGH_KEYWORDS = {
        "notebook", "laptop", "celular", "smartphone", "tablet", "bateria",
        "ups", "nobreak", "impressora laser", "monitor lcd", "monitor oled"
    };
    private static final String[] MEDIUM_KEYWORDS = {
        "computador", "desktop", "monitor", "impressora", "scanner",
        "servidor", "switch", "roteador", "projetor"
    };

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

    /**
     * Percentual de depreciacao linear (0-100) com base na vida util (RF02).
     * 100% significa que o ativo ultrapassou o fim da vida util.
     */
    public int getDepreciationPercent() {
        if (acquiredAt == null || lifespanYears <= 0) return 0;
        long totalDays = ChronoUnit.DAYS.between(acquiredAt, acquiredAt.plusYears(lifespanYears));
        long elapsed   = ChronoUnit.DAYS.between(acquiredAt, LocalDate.now());
        if (totalDays <= 0) return 100;
        int pct = (int) Math.round((double) elapsed / totalDays * 100);
        return Math.max(0, Math.min(100, pct));
    }

    /**
     * Dias restantes ate o fim da vida util (negativo = ja vencido) (RF02).
     */
    public long getDaysUntilExpiration() {
        LocalDate exp = getExpirationDate();
        if (exp == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), exp);
    }

    /**
     * Nivel de periculosidade ambiental derivado da categoria (RF05).
     */
    public HazardLevel getHazardLevel() {
        if (category == null) return HazardLevel.LOW;
        String lower = category.toLowerCase();
        for (String kw : HIGH_KEYWORDS) {
            if (lower.contains(kw)) return HazardLevel.HIGH;
        }
        for (String kw : MEDIUM_KEYWORDS) {
            if (lower.contains(kw)) return HazardLevel.MEDIUM;
        }
        return HazardLevel.LOW;
    }

    @Override
    public String toString() {
        return id + " - " + category + " (" + status.getValue() + ")";
    }
}
