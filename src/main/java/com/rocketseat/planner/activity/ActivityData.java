package com.rocketseat.planner.activity;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityData(
        UUID id,
        LocalDateTime accursAt,
        String title
) {}
