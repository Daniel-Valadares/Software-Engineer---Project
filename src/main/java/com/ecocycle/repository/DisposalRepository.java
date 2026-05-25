package com.ecocycle.repository;

import com.ecocycle.model.Disposal;
import com.ecocycle.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositorio JDBC para a entidade Disposal (RF03).
 */
public class DisposalRepository {

    private Connection conn() {
        return DBConnection.getInstance().getConnection();
    }

    public void save(Disposal d) {
        String sql = "INSERT INTO disposals (id, asset_id, disposed_at, disk_sanitized, battery_removed, destination_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, d.getId());
            ps.setString(2, d.getAssetId());
            ps.setString(3, d.getDisposedAt().toString());
            ps.setInt   (4, d.isDiskSanitized()  ? 1 : 0);
            ps.setInt   (5, d.isBatteryRemoved()  ? 1 : 0);
            ps.setString(6, d.getDestination());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar descarte: " + e.getMessage(), e);
        }
    }

    public List<Disposal> findAll() {
        List<Disposal> list = new ArrayList<>();
        String sql = "SELECT id, asset_id, disposed_at, disk_sanitized, battery_removed, destination_id FROM disposals";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar descartes: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Disposal> findByAssetId(String assetId) {
        List<Disposal> list = new ArrayList<>();
        String sql = "SELECT id, asset_id, disposed_at, disk_sanitized, battery_removed, destination_id "
                   + "FROM disposals WHERE asset_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar descartes por ativo: " + e.getMessage(), e);
        }
        return list;
    }

    public int countDiskSanitized() {
        return countBoolColumn("disk_sanitized");
    }

    public int countBatteryRemoved() {
        return countBoolColumn("battery_removed");
    }

    private int countBoolColumn(String col) {
        String sql = "SELECT COUNT(*) FROM disposals WHERE " + col + " = 1";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    private Disposal mapRow(ResultSet rs) throws SQLException {
        Disposal d = new Disposal();
        d.setId(rs.getString("id"));
        d.setAssetId(rs.getString("asset_id"));
        String dt = rs.getString("disposed_at");
        d.setDisposedAt(dt != null ? LocalDate.parse(dt) : LocalDate.now());
        d.setDiskSanitized(rs.getInt("disk_sanitized") == 1);
        d.setBatteryRemoved(rs.getInt("battery_removed") == 1);
        d.setDestination(rs.getString("destination_id"));
        return d;
    }
}
