package com.link.shortener.entity;

import java.time.LocalDateTime;
import java.util.Date;

public record URLEntity(
		String longURL,
		String shortCode,
		int accessCount,
		Date createdAt,
		Date updatedAt)
{}
