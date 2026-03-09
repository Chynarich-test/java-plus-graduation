package practicum.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPredictionsRequest {
    private Long userId;
    private Long maxResults;
}
