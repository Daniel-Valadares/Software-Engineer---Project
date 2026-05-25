package com.ecocycle.service;

import com.ecocycle.model.Asset;
import com.ecocycle.model.Disposal;
import com.ecocycle.repository.AssetRepository;
import com.ecocycle.repository.DisposalRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Servico de descarte responsavel de ativos (RF03).
 */
public class DisposalService {

    private final DisposalRepository disposalRepo;
    private final AssetRepository    assetRepo;

    public DisposalService() {
        this.disposalRepo = new DisposalRepository();
        this.assetRepo    = new AssetRepository();
    }

    /**
     * Registra o descarte de um ativo, validando o checklist e marcando o ativo como DISPOSED.
     */
    public void register(Disposal disposal) {
        validate(disposal);

        if (disposal.getId() == null || disposal.getId().isBlank()) {
            disposal.setId("DSP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        disposalRepo.save(disposal);

        // Atualiza o status do ativo para DISPOSED
        Asset asset = assetRepo.findById(disposal.getAssetId()).orElse(null);
        if (asset != null) {
            asset.setStatus(Asset.Status.DISPOSED);
            assetRepo.update(asset);
        }
    }

    public List<Disposal> listAll() {
        return disposalRepo.findAll();
    }

    public List<Disposal> listByAsset(String assetId) {
        return disposalRepo.findByAssetId(assetId);
    }

    private void validate(Disposal d) {
        if (d == null) throw new IllegalArgumentException("Descarte nao pode ser nulo");
        if (d.getAssetId() == null || d.getAssetId().isBlank())
            throw new IllegalArgumentException("Ativo de origem e obrigatorio");
        if (d.getDisposedAt() == null)
            throw new IllegalArgumentException("Data de descarte e obrigatoria");
        if (d.getDisposedAt().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data de descarte nao pode ser futura");
        if (d.getDestination() == null || d.getDestination().isBlank())
            throw new IllegalArgumentException("Destino e obrigatorio");
    }
}
