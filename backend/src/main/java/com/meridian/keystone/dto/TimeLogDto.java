package com.meridian.keystone.dto;

import com.meridian.keystone.domain.TimeLog;

import java.time.Instant;

public record TimeLogDto(
        Long id,
        int minutes,
        String note,
        Long loggedById,
        String loggedByName,
        Instant loggedAt) {

    public static TimeLogDto from(TimeLog tl) {
        String name = tl.getLoggedBy() != null ? tl.getLoggedBy().getFullName() : null;
        Long uid = tl.getLoggedBy() != null ? tl.getLoggedBy().getId() : null;
        return new TimeLogDto(tl.getId(), tl.getMinutes(), tl.getNote(), uid, name, tl.getLoggedAt());
    }
}