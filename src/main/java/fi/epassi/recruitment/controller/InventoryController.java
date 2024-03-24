package fi.epassi.recruitment.controller;

import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.dto.InventoryDto;
import fi.epassi.recruitment.dto.InventoryGlobalDto;
import fi.epassi.recruitment.services.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping(value = "/isbn/{isbn}/copies", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<InventoryDto>> getInventoryCopiesByIsbn(
            @PathVariable("isbn") @Validated UUID isbn) {
        //return ApiResponse.ok(inventoryService.getCopiesByIsbn(isbn));
        return inventoryService.getCopiesByIsbn(isbn)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN",isbn.toString())));
    }

    @GetMapping(value = "/author/{author}/copies", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<InventoryDto>> getInventoryCopiesByAuthor(
            @PathVariable("author") String author) {
        return inventoryService.getCopiesByAuthor(author)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("Author", author)));
    }

    @GetMapping(value = "/title/{title}/copies", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<InventoryDto>> getInventoryCopiesByTitle(
            @PathVariable("title") String title) {
        return inventoryService.getCopiesByTitle(title)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("Title", title)));
    }

    @PutMapping(value = "/isbn/{isbn}/copies", consumes = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<UUID>> updateInventoriesCopiesByIsbn(@RequestBody @Validated InventoryDto inventoryDto) {
        return inventoryService.updateInventory(inventoryDto)
                .map(ApiResponse::ok);
    }

    @GetMapping(value = "/copies", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<InventoryGlobalDto>> getInventoryTotalCopies() {
        return inventoryService.getTotalCopies()
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException()));
    }
}
