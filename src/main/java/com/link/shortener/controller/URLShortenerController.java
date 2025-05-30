package com.link.shortener.controller;

import com.link.shortener.entity.URLEntity;
import com.link.shortener.entity.URLEntityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.link.shortener.services.URLShortenerService;

import java.util.Optional;

@RestController()
@RequestMapping("/shorten")
public class URLShortenerController {
	
	@Autowired
	URLShortenerService linkShortenService;
	
	@PostMapping
	public ResponseEntity<URLEntityDto> create(@RequestBody String longUrl){
		if(linkShortenService.entityExists(longUrl))
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		URLEntity entity = linkShortenService.save(longUrl);
		return ResponseEntity.ok(toDto(entity));
	}


	@GetMapping("/{shortCode}/stats")
	public ResponseEntity<URLEntity> getStats(@PathVariable String shortCode) {
		if (!linkShortenService.findIfEntityExistsByShortCode(shortCode)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		Optional<URLEntity> entity = linkShortenService.getLongURl(shortCode);
		return ResponseEntity.ok(entity.get());
	}

	@GetMapping("/{shortCode}")
	public ResponseEntity<URLEntityDto> getLongURL(@PathVariable String shortCode) {
		Optional<URLEntity> entity = linkShortenService.getLongURlAndUpdateAccessCount(shortCode);
		if (entity.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		return ResponseEntity.ok(toDto(entity.get()));
	}

	@PutMapping("/{shortCode}")
	public ResponseEntity<URLEntityDto> update(@PathVariable String shortCode, @RequestBody String longUrl) {
		if (!linkShortenService.findIfEntityExistsByShortCode(shortCode)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		URLEntity updatedEntity = linkShortenService.update(shortCode, longUrl);
		return ResponseEntity.ok(toDto(updatedEntity));
	}


	@DeleteMapping("/{shortCode}")
	public ResponseEntity<?> delete(@PathVariable String shortCode) {
		if (!linkShortenService.findIfEntityExistsByShortCode(shortCode)) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		linkShortenService.delete(shortCode);
		return ResponseEntity.accepted().build();
	}



	private URLEntityDto toDto(URLEntity entity) {
		return new URLEntityDto(
				entity.longURL(),
				entity.shortCode(),
				entity.createdAt(),
				entity.updatedAt()
		);
	}

}
