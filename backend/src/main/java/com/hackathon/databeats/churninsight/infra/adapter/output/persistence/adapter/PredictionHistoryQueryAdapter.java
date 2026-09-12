package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.adapter;

import com.hackathon.databeats.churninsight.application.dto.PaginatedResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionHistoryResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionSearchFilter;
import com.hackathon.databeats.churninsight.application.port.output.PredictionHistoryQueryPort;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistoryEntity;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryRepository;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.specification.PredictionHistorySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter de consulta do histórico de predições.
 *
 * <p>Implementa {@link PredictionHistoryQueryPort} encapsulando toda a lógica JPA
 * (repositório, Specification, mapeamento de entidade para DTO) na camada de infraestrutura.
 * A camada de aplicação nunca importa classes JPA diretamente.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PredictionHistoryQueryAdapter implements PredictionHistoryQueryPort {

    private final PredictionHistoryRepository repository;

    private static final List<String> VALID_SORT_FIELDS = List.of(
            "createdAt", "probability", "age", "gender",
            "country", "subscriptionType", "churnStatus"
    );

    private static final Map<String, String> RECOMMENDED_ACTIONS = createActionsMap();

    private static Map<String, String> createActionsMap() {
        Map<String, String> actions = new HashMap<>();
        actions.put("FREE_HIGH_ADS", "Oferecer teste Premium para aliviar interrupções de áudio.");
        actions.put("HIGH_SKIP_RATE", "Recalibrar algoritmo de recomendação para reduzir pulos.");
        actions.put("LOW_ENGAGEMENT", "Enviar recomendações personalizadas para aumentar engajamento.");
        actions.put("PREMIUM_NO_OFFLINE", "Sugerir uso de downloads para experiência completa.");
        actions.put("HIGH_FRUSTRATION", "Enviar pesquisa de satisfação com cupom de desconto.");
        actions.put("STUDENT_GRADUATING", "Oferecer desconto no plano Premium pós-formatura.");
        actions.put("DEFAULT", "Monitorar comportamento e manter contato proativo.");
        return Map.copyOf(actions);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public long countByChurnStatus(ChurnStatus status) {
        return repository.countByChurnStatus(status);
    }

    @Override
    public Long countTop25AtRisk() {
        return repository.countTop25AtRisk();
    }

    @Override
    public Object[] getRiskFactorCounts() {
        return repository.getRiskFactorCounts();
    }

    @Override
    public Object[] getProbabilityBuckets() {
        return repository.getProbabilityBuckets();
    }

    @Override
    public Object[] getGlobalStats() {
        return repository.getGlobalStats();
    }

    @Override
    public List<Object[]> getTop25SubscriptionCounts() {
        return repository.getTop25SubscriptionCounts();
    }

    @Override
    public List<Object[]> countByGender() {
        return repository.countByGender();
    }

    @Override
    public List<Object[]> countBySubscriptionType() {
        return repository.countBySubscriptionType();
    }

    @Override
    public List<String> findUserIdsByPrefix(String prefix, int limit) {
        return repository.findUserIdsByPrefix(prefix, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<PredictionHistoryResponse> search(
            PredictionSearchFilter filter, int page, int size, String sortBy, String sortDir) {

        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, Objects.requireNonNull(sort, "Sort must not be null"));

        Specification<PredictionHistoryEntity> spec = PredictionHistorySpecification.withFilters(filter);
        Page<PredictionHistoryEntity> resultPage = repository.findAll(spec, pageable);

        List<PredictionHistoryResponse> content = resultPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        PaginatedResponse.PageStats stats = calculatePageStats(resultPage.getContent());

        log.debug("Busca paginada: page={}, size={}, totalElements={}, filters={}",
                page, size, resultPage.getTotalElements(), filter);

        return PaginatedResponse.<PredictionHistoryResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .first(resultPage.isFirst())
                .last(resultPage.isLast())
                .hasNext(resultPage.hasNext())
                .hasPrevious(resultPage.hasPrevious())
                .stats(stats)
                .build();
    }

    // === MÉTODOS PRIVADOS DE MAPEAMENTO ===

    private Sort createSort(String sortBy, String sortDir) {
        if (sortBy == null || !VALID_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, sortBy);
    }

    private PredictionHistoryResponse toResponse(PredictionHistoryEntity entity) {
        return PredictionHistoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .gender(entity.getGender())
                .age(entity.getAge())
                .country(entity.getCountry())
                .subscriptionType(entity.getSubscriptionType())
                .deviceType(entity.getDeviceType())
                .churnStatus(entity.getChurnStatus())
                .probability(entity.getProbability())
                .predictionLabel(entity.getChurnStatus() == ChurnStatus.WILL_CHURN
                        ? "Vai Cancelar" : "Vai Continuar")
                .frustrationIndex(entity.getFrustrationIndex())
                .isHeavyUser(entity.getIsHeavyUser())
                .recommendedAction(determineRecommendedAction(entity))
                .primaryRiskFactor(identifyPrimaryRiskFactor(entity))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String identifyPrimaryRiskFactor(PredictionHistoryEntity entity) {
        if (entity.getChurnStatus() == ChurnStatus.WILL_STAY) {
            return "Nenhum (Stay)";
        }
        if ("Free".equalsIgnoreCase(entity.getSubscriptionType()) && entity.getAdsListenedPerWeek() > 15) {
            return "Anúncios por Semana";
        }
        if (entity.getSkipRate() > 0.4) return "Taxa de Pulos Elevada";
        if (entity.getFrustrationIndex() != null && entity.getFrustrationIndex() > 3.0) return "Índice de Frustração";
        if (Boolean.TRUE.equals(entity.getPremiumNoOffline())) return "Subutilização Premium";
        if (entity.getListeningTime() < 100) return "Baixo Engajamento";
        return "Perfil de Risco Moderado";
    }

    private String determineRecommendedAction(PredictionHistoryEntity entity) {
        if (entity.getChurnStatus() == ChurnStatus.WILL_STAY) {
            return "Manter relacionamento atual.";
        }
        if ("Free".equalsIgnoreCase(entity.getSubscriptionType()) && entity.getAdsListenedPerWeek() > 15) {
            return RECOMMENDED_ACTIONS.get("FREE_HIGH_ADS");
        }
        if (entity.getSkipRate() > 0.4) {
            return RECOMMENDED_ACTIONS.get("HIGH_SKIP_RATE");
        }
        if (entity.getFrustrationIndex() != null && entity.getFrustrationIndex() > 3.0) {
            return RECOMMENDED_ACTIONS.get("HIGH_FRUSTRATION");
        }
        if (Boolean.TRUE.equals(entity.getPremiumNoOffline())) {
            return RECOMMENDED_ACTIONS.get("PREMIUM_NO_OFFLINE");
        }
        if (entity.getListeningTime() < 100) {
            return RECOMMENDED_ACTIONS.get("LOW_ENGAGEMENT");
        }
        return RECOMMENDED_ACTIONS.get("DEFAULT");
    }

    private PaginatedResponse.PageStats calculatePageStats(List<PredictionHistoryEntity> content) {
        if (content.isEmpty()) {
            return PaginatedResponse.PageStats.builder()
                    .willChurnCount(0)
                    .willStayCount(0)
                    .avgProbability(0.0)
                    .build();
        }

        long willChurn = content.stream()
                .filter(e -> e.getChurnStatus() == ChurnStatus.WILL_CHURN)
                .count();
        long willStay = content.size() - willChurn;

        double avgProb = content.stream()
                .mapToDouble(PredictionHistoryEntity::getProbability)
                .average()
                .orElse(0.0);

        return PaginatedResponse.PageStats.builder()
                .willChurnCount(willChurn)
                .willStayCount(willStay)
                .avgProbability(Math.round(avgProb * 1000.0) / 1000.0)
                .build();
    }
}
