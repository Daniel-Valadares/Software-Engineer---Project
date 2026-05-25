package com.ecocycle.service;

import com.ecocycle.model.Asset;
import com.ecocycle.repository.AssetRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servico de regras de negocio para ativos (RF01 / RF02 / RF05).
 */
public class AssetService {

    private final AssetRepository repository;

    public AssetService() {
        this.repository = new AssetRepository();
    }

    public AssetService(AssetRepository repository) {
        this.repository = repository;
    }

    public void register(Asset asset) {
        validate(asset);
        if (repository.existsById(asset.getId())) {
            throw new IllegalArgumentException("Ja existe um ativo com ID: " + asset.getId());
        }
        asset.setStatus(calculateStatus(asset));
        repository.save(asset);
    }

    public void update(Asset asset) {
        validate(asset);
        if (!repository.existsById(asset.getId())) {
            throw new IllegalArgumentException("Ativo nao encontrado: " + asset.getId());
        }
        asset.setStatus(calculateStatus(asset));
        repository.update(asset);
    }

    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID nao pode ser vazio");
        }
        repository.deleteById(id);
    }

    public List<Asset> listAll() {
        List<Asset> all = repository.findAll();
        all.forEach(a -> a.setStatus(calculateStatus(a)));
        return all;
    }

    /** Ativos proximos ao vencimento ou ja vencidos (nao descartados) — RF02. */
    public List<Asset> listNeedingAttention() {
        return listAll().stream()
                .filter(a -> a.getStatus() == Asset.Status.EXPIRING
                          || a.getStatus() == Asset.Status.OVERDUE)
                .collect(Collectors.toList());
    }

    /** Filtra ativos pelo nivel de periculosidade — RF05. */
    public List<Asset> listByHazardLevel(Asset.HazardLevel level) {
        return listAll().stream()
                .filter(a -> a.getHazardLevel() == level)
                .collect(Collectors.toList());
    }

    private void validate(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Ativo nao pode ser nulo");
        }
        if (asset.getId() == null || asset.getId().isBlank()) {
            throw new IllegalArgumentException("ID e obrigatorio");
        }
        if (asset.getCategory() == null || asset.getCategory().isBlank()) {
            throw new IllegalArgumentException("Categoria e obrigatoria");
        }
        if (asset.getAcquiredAt() == null) {
            throw new IllegalArgumentException("Data de aquisicao e obrigatoria");
        }
        if (asset.getAcquiredAt().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de aquisicao nao pode ser futura");
        }
        if (asset.getLifespanYears() <= 0) {
            throw new IllegalArgumentException("Vida util deve ser maior que zero");
        }
    }

    /**
     * Calcula o status atual com base no prazo de vida util (RF02).
     * - disposed : estado terminal — nao recalcula
     * - overdue  : prazo ja ultrapassado (nao descartado)
     * - expiring : a menos de 12 meses do fim de vida util
     * - active   : dentro do prazo
     */
    Asset.Status calculateStatus(Asset asset) {
        if (asset.getStatus() == Asset.Status.DISPOSED) {
            return Asset.Status.DISPOSED;
        }
        LocalDate expiration = asset.getExpirationDate();
        if (expiration == null) {
            return Asset.Status.ACTIVE;
        }
        LocalDate today = LocalDate.now();
        if (expiration.isBefore(today)) {
            return Asset.Status.OVERDUE;
        }
        LocalDate twelveMonthsAhead = today.plusMonths(12);
        if (expiration.isBefore(twelveMonthsAhead)) {
            return Asset.Status.EXPIRING;
        }
        return Asset.Status.ACTIVE;
    }
}
