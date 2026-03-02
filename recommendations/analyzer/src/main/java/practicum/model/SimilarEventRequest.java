package practicum.model;

import lombok.Data;

@Data
public class SimilarEventRequest {
    private Long eventId;
    private Long userId;
    private Long maxResults;
}
