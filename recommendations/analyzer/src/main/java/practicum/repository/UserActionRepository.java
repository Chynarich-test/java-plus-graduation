package practicum.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import practicum.model.EventSimilarity;
import practicum.model.RecommendedEvent;
import practicum.model.UserAction;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class UserActionRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private static final RowMapper<UserAction> USER_ACTION_ROW_MAPPER = (rs, rowNum) ->
            UserAction.builder()
                    .userId(rs.getLong("user_id"))
                    .eventId(rs.getLong("event_id"))
                    .actionType(rs.getDouble("action_score"))
                    .timestamp(rs.getTimestamp("created").toInstant())
                    .build();

    public void save(UserAction userAction){
        String sql = """
                INSERT INTO user_actions_history (user_id, event_id, action_score, created)
                VALUES (:userId, :eventId, :actionScore, :timestamp)
                ON CONFLICT (user_id, event_id)
                DO UPDATE SET
                    action_score = GREATEST(user_actions_history.action_score, EXCLUDED.action_score),
                    created = GREATEST(user_actions_history.created, EXCLUDED.created);
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userAction.getUserId())
                .addValue("eventId", userAction.getEventId())
                .addValue("actionScore", userAction.getActionType())
                .addValue("timestamp", userAction.getTimestamp() != null
                        ? java.sql.Timestamp.from(userAction.getTimestamp())
                        : null);

        namedParameterJdbcTemplate.update(sql, params);
    }

    public List<RecommendedEvent> getInteractionsCount(List<Long> eventIds) {
        String sql = """
            SELECT event_id, SUM(action_score) AS score
            FROM user_actions_history
            WHERE event_id IN (:eventIds)
            GROUP BY event_id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("eventIds", eventIds);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) ->
                RecommendedEvent.builder()
                        .eventId(rs.getLong("event_id"))
                        .score(rs.getDouble("score"))
                        .build()
        );
    }
}
