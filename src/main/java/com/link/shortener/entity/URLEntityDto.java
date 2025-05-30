package com.link.shortener.entity;

import java.util.Date;

public record URLEntityDto(
        String longURL,
        String shortCode,
        Date createdAt,
        Date updatedAt)
        {}
