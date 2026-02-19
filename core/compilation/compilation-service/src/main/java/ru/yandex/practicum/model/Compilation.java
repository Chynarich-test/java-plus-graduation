package ru.yandex.practicum.model;

import lombok.*;

import jakarta.persistence.*;
import ru.yandex.practicum.event.dto.EventFullDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 50, unique = true)
    private String title;

    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

    @ElementCollection
    @CollectionTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id")
    )
    @Column(name = "event_id")
    @Builder.Default
    private Set<Long> eventIds = new HashSet<>();
}