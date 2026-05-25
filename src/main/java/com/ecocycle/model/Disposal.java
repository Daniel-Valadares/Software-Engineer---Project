package com.ecocycle.model;

import java.time.LocalDate;

/**
 * Modelo de Descarte (RF03).
 * Registra o checklist de descarte responsavel de um ativo de hardware.
 */
public class Disposal {

    private String id;
    private String assetId;
    private LocalDate disposedAt;
    private boolean diskSanitized;
    private boolean batteryRemoved;
    private String destination;   // nome/tipo do destino (cooperativa, fabricante, reciclador)

    public Disposal() {}

    public Disposal(String id, String assetId, LocalDate disposedAt,
                    boolean diskSanitized, boolean batteryRemoved, String destination) {
        this.id             = id;
        this.assetId        = assetId;
        this.disposedAt     = disposedAt;
        this.diskSanitized  = diskSanitized;
        this.batteryRemoved = batteryRemoved;
        this.destination    = destination;
    }

    public String getId()                  { return id; }
    public void   setId(String id)         { this.id = id; }

    public String getAssetId()             { return assetId; }
    public void   setAssetId(String v)     { this.assetId = v; }

    public LocalDate getDisposedAt()       { return disposedAt; }
    public void   setDisposedAt(LocalDate v) { this.disposedAt = v; }

    public boolean isDiskSanitized()       { return diskSanitized; }
    public void   setDiskSanitized(boolean v) { this.diskSanitized = v; }

    public boolean isBatteryRemoved()      { return batteryRemoved; }
    public void   setBatteryRemoved(boolean v) { this.batteryRemoved = v; }

    public String getDestination()         { return destination; }
    public void   setDestination(String v) { this.destination = v; }

    @Override
    public String toString() {
        return "Disposal{id='" + id + "', assetId='" + assetId + "'}";
    }
}
