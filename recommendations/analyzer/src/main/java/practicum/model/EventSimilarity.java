package practicum.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
@Data
public class EventSimilarity {
    private Long eventA;
    private Long eventB;
    private Double score;
    private Instant timestamp;
}
