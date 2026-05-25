package com.ecocycle.service;

import com.ecocycle.model.Asset;
import com.ecocycle.model.Disposal;
import com.ecocycle.repository.DisposalRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servico de relatorios de impacto ambiental (RF04).
 * Gera estatisticas e texto formatado para exibicao ou exportacao.
 */
public class ReportService {

    // Estimativas de impacto por ativo descartado corretamente (valores de referencia ABNT/WEEE)
    private static final double KG_CO2_SAVED_PER_DEVICE   = 12.5;  // kg CO2 evitados por descarte correto
    private static final double KG_EWASTE_PER_DEVICE      = 1.8;   // kg de e-lixo evitado por reciclagem
    private static final double LITERS_WATER_SAVED        = 340.0; // litros de agua economizados

    private final AssetService     assetService;
    private final DisposalService  disposalService;
    private final DisposalRepository disposalRepo;

    public ReportService() {
        this.assetService    = new AssetService();
        this.disposalService = new DisposalService();
        this.disposalRepo    = new DisposalRepository();
    }

    public ReportData generate() {
        List<Asset>   assets   = assetService.listAll();
        List<Disposal> disposals = disposalService.listAll();

        long total     = assets.size();
        long active    = assets.stream().filter(a -> a.getStatus() == Asset.Status.ACTIVE).count();
        long expiring  = assets.stream().filter(a -> a.getStatus() == Asset.Status.EXPIRING).count();
        long overdue   = assets.stream().filter(a -> a.getStatus() == Asset.Status.OVERDUE).count();
        long disposed  = assets.stream().filter(a -> a.getStatus() == Asset.Status.DISPOSED).count();

        long diskSanitized  = disposalRepo.countDiskSanitized();
        long batteryRemoved = disposalRepo.countBatteryRemoved();

        // Periculosidade (RF05)
        Map<Asset.HazardLevel, Long> byHazard = assets.stream()
                .collect(Collectors.groupingBy(Asset::getHazardLevel, Collectors.counting()));

        // Impacto ambiental estimado
        double co2Saved     = disposals.size() * KG_CO2_SAVED_PER_DEVICE;
        double eWasteAvoided = disposals.size() * KG_EWASTE_PER_DEVICE;
        double waterSaved   = diskSanitized * LITERS_WATER_SAVED;

        return new ReportData(total, active, expiring, overdue, disposed,
                              disposals.size(), diskSanitized, batteryRemoved,
                              byHazard, co2Saved, eWasteAvoided, waterSaved);
    }

    public String generateText() {
        ReportData d   = generate();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();

        sb.append("=".repeat(60)).append("\n");
        sb.append("  RELATÓRIO DE IMPACTO AMBIENTAL — EcoCycle Manager\n");
        sb.append("  Gerado em: ").append(LocalDate.now().format(fmt)).append("\n");
        sb.append("=".repeat(60)).append("\n\n");

        sb.append(">>> INVENTÁRIO DE ATIVOS\n");
        sb.append(String.format("  Total de ativos cadastrados : %d%n", d.total));
        sb.append(String.format("  Ativos em uso (ativo)       : %d%n", d.active));
        sb.append(String.format("  Próximos ao vencimento      : %d%n", d.expiring));
        sb.append(String.format("  Prazo vencido (pendente)    : %d%n", d.overdue));
        sb.append(String.format("  Descartados                 : %d%n", d.disposed));
        sb.append("\n");

        sb.append(">>> DESCARTES REALIZADOS\n");
        sb.append(String.format("  Total de descartes          : %d%n", d.totalDisposals));
        sb.append(String.format("  Com disco sanitizado        : %d%n", d.diskSanitized));
        sb.append(String.format("  Com bateria removida        : %d%n", d.batteryRemoved));
        sb.append("\n");

        sb.append(">>> CLASSIFICAÇÃO POR PERICULOSIDADE (RF05)\n");
        sb.append(String.format("  Periculosidade ALTA         : %d ativos%n",
                d.byHazard.getOrDefault(Asset.HazardLevel.HIGH, 0L)));
        sb.append(String.format("  Periculosidade MÉDIA        : %d ativos%n",
                d.byHazard.getOrDefault(Asset.HazardLevel.MEDIUM, 0L)));
        sb.append(String.format("  Periculosidade BAIXA        : %d ativos%n",
                d.byHazard.getOrDefault(Asset.HazardLevel.LOW, 0L)));
        sb.append("\n");

        sb.append(">>> IMPACTO AMBIENTAL ESTIMADO\n");
        sb.append(String.format("  CO₂ evitado (estimativa)    : %.1f kg%n", d.co2Saved));
        sb.append(String.format("  E-lixo evitado              : %.1f kg%n", d.eWasteAvoided));
        sb.append(String.format("  Água economizada            : %.0f litros%n", d.waterSaved));
        sb.append("\n");
        sb.append("  * Estimativas baseadas em referencias ABNT/WEEE Directive.\n");
        sb.append("=".repeat(60)).append("\n");

        return sb.toString();
    }

    // DTO interno de dados do relatorio
    public static class ReportData {
        public final long total, active, expiring, overdue, disposed;
        public final long totalDisposals, diskSanitized, batteryRemoved;
        public final Map<Asset.HazardLevel, Long> byHazard;
        public final double co2Saved, eWasteAvoided, waterSaved;

        ReportData(long total, long active, long expiring, long overdue, long disposed,
                   long totalDisposals, long diskSanitized, long batteryRemoved,
                   Map<Asset.HazardLevel, Long> byHazard,
                   double co2Saved, double eWasteAvoided, double waterSaved) {
            this.total          = total;
            this.active         = active;
            this.expiring       = expiring;
            this.overdue        = overdue;
            this.disposed       = disposed;
            this.totalDisposals = totalDisposals;
            this.diskSanitized  = diskSanitized;
            this.batteryRemoved = batteryRemoved;
            this.byHazard       = byHazard;
            this.co2Saved       = co2Saved;
            this.eWasteAvoided  = eWasteAvoided;
            this.waterSaved     = waterSaved;
        }
    }
}
