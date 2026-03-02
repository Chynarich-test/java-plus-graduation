package practicum.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InteractionsCountRequest {
    private Long event_id;
}
