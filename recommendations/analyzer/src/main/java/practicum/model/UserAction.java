package practicum.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
@Builder
public class UserAction {
    private Long userId;
    private Long eventId;
    private Double actionType;
    private Instant timestamp;
}
