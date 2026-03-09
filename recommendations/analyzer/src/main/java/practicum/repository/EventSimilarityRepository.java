package practicum.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import practicum.model.EventSimilarity;
import practicum.model.RecommendedEvent;
import practicum.model.SimilarEventRequest;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class EventSimilarityRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final RowMapper<EventSimilarity> EVENT_SIMILARITY_ROW_MAPPER = (rs, rowNum) ->
            EventSimilarity.builder()
                    .eventA(rs.getLong("event_a_id"))
                    .eventB(rs.getLong("event_b_id"))
                    .score(rs.getDouble("score"))
                    .timestamp(rs.getTimestamp("created").toInstant())
                    .build();


    public void save(EventSimilarity eventSimilarity){
        String sql = """
                INSERT INTO events_similarity (event_a_id, event_b_id, score, created)
                VALUES (:eventA, :eventB, :score, :timestamp)
                ON CONFLICT (event_a_id, event_b_id)
                DO UPDATE SET
                    score = EXCLUDED.score,
                    created = GREATEST(events_similarity.created, EXCLUDED.created);
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("eventA", eventSimilarity.getEventA())
                .addValue("eventB", eventSimilarity.getEventB())
                .addValue("score", eventSimilarity.getScore())
                .addValue("timestamp", eventSimilarity.getTimestamp());

        namedParameterJdbcTemplate.update(sql, params);
    }

    public List<EventSimilarity> getSimilarEvents(SimilarEventRequest similarEventRequest){
        String sql = """
                SELECT es.event_a_id, es.event_b_id, es.score, es.created
                FROM events_similarity as es
                LEFT JOIN user_actions_history AS uah_a
                ON es.event_a_id = uah_a.event_id AND uah_a.user_id = :userId
                LEFT JOIN user_actions_history AS uah_b
                ON es.event_b_id = uah_b.event_id AND uah_b.user_id = :userId
                WHERE (es.event_a_id = :event OR es.event_b_id = :event)
                AND NOT (uah_a.event_id IS NOT NULL AND uah_b.event_id IS NOT NULL)
                ORDER BY es.score DESC
                LIMIT :maxResults
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("event", similarEventRequest.getEventId())
                .addValue("userId", similarEventRequest.getUserId())
                .addValue("maxResults", similarEventRequest.getMaxResults());

        return namedParameterJdbcTemplate.query(sql, params, EVENT_SIMILARITY_ROW_MAPPER);
    }

    public List<RecommendedEvent> getRecommendationsForUser(Long userId, Long maxResults){
        String sql = """
            WITH recent_views AS (
                SELECT event_id
                FROM user_actions_history
                WHERE user_id = :userId
                ORDER BY created DESC
                LIMIT :n
            ),
            
            top_candidates AS (
                SELECT 
                    CASE WHEN es.event_a_id = rv.event_id THEN es.event_b_id ELSE es.event_a_id END AS candidate_id,
                    MAX(es.score) as max_sim
                FROM events_similarity es
                JOIN recent_views rv ON (es.event_a_id = rv.event_id OR es.event_b_id = rv.event_id)
                WHERE NOT EXISTS (
                    SELECT 1 FROM user_actions_history uah 
                    WHERE uah.user_id = :userId 
                    AND uah.event_id = (CASE WHEN es.event_a_id = rv.event_id THEN es.event_b_id ELSE es.event_a_id END)
                )
                GROUP BY candidate_id
                ORDER BY max_sim DESC
                LIMIT :n
            ),

            scoring_neighbors AS (
                SELECT 
                    tc.candidate_id,
                    uah.action_score,
                    es.score AS sim,
                    ROW_NUMBER() OVER (PARTITION BY tc.candidate_id ORDER BY es.score DESC) as rank
                FROM top_candidates tc
                JOIN events_similarity es ON (tc.candidate_id = es.event_a_id OR tc.candidate_id = es.event_b_id)
                JOIN user_actions_history uah ON (uah.user_id = :userId AND (uah.event_id = es.event_a_id OR uah.event_id = es.event_b_id))
                WHERE uah.event_id != tc.candidate_id
            )
            
            SELECT 
                candidate_id,
                SUM(action_score * sim) / SUM(sim) AS final_score
            FROM scoring_neighbors
            WHERE rank <= 5
            GROUP BY candidate_id
            ORDER BY final_score DESC
            LIMIT :n
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("n", maxResults);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) ->
                RecommendedEvent.builder()
                        .eventId(rs.getLong("candidate_id"))
                        .score(rs.getDouble("final_score"))
                        .build()
        );
    }

}
