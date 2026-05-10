package com.ecocycle.service;

import com.ecocycle.model.Asset;
import com.ecocycle.repository.AssetRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Servico de regras de negocio para ativos (RF01 / RF02).
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
     * - disposed: nao recalcula (estado terminal)
     * - expiring: a menos de 6 meses do fim
     * - active: caso contrario
     */
    private Asset.Status calculateStatus(Asset asset) {
        if (asset.getStatus() == Asset.Status.DISPOSED) {
            return Asset.Status.DISPOSED;
        }
        LocalDate expiration = asset.getExpirationDate();
        if (expiration == null) {
            return Asset.Status.ACTIVE;
        }
        LocalDate sixMonthsAhead = LocalDate.now().plusMonths(6);
        if (expiration.isBefore(sixMonthsAhead)) {
            return Asset.Status.EXPIRING;
        }
        return Asset.Status.ACTIVE;
    }
}
