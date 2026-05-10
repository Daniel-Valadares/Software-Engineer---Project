package com.ecocycle.repository;

import com.ecocycle.model.Asset;
import com.ecocycle.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio CRUD para a entidade Asset (RF01).
 */
public class AssetRepository {

    private final Connection connection;

    public AssetRepository() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    public void save(Asset asset) {
        String sql = "INSERT INTO assets (id, category, model, acquired_at, lifespan_years, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, asset.getId());
            ps.setString(2, asset.getCategory());
            ps.setString(3, asset.getModel());
            ps.setDate(4, Date.valueOf(asset.getAcquiredAt()));
            ps.setInt(5, asset.getLifespanYears());
            ps.setString(6, asset.getStatus().getValue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar ativo: " + e.getMessage(), e);
        }
    }

    public void update(Asset asset) {
        String sql = "UPDATE assets SET category = ?, model = ?, acquired_at = ?, "
                   + "lifespan_years = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, asset.getCategory());
            ps.setString(2, asset.getModel());
            ps.setDate(3, Date.valueOf(asset.getAcquiredAt()));
            ps.setInt(4, asset.getLifespanYears());
            ps.setString(5, asset.getStatus().getValue());
            ps.setString(6, asset.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar ativo: " + e.getMessage(), e);
        }
    }

    public void deleteById(String id) {
        String sql = "DELETE FROM assets WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover ativo: " + e.getMessage(), e);
        }
    }

    public Optional<Asset> findById(String id) {
        String sql = "SELECT * FROM assets WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar ativo: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Asset> findAll() {
        List<Asset> assets = new ArrayList<>();
        String sql = "SELECT * FROM assets ORDER BY acquired_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                assets.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar ativos: " + e.getMessage(), e);
        }
        return assets;
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    private Asset mapRow(ResultSet rs) throws SQLException {
        return new Asset(
                rs.getString("id"),
                rs.getString("category"),
                rs.getString("model"),
                rs.getDate("acquired_at").toLocalDate(),
                rs.getInt("lifespan_years"),
                Asset.Status.fromValue(rs.getString("status"))
        );
    }
}
