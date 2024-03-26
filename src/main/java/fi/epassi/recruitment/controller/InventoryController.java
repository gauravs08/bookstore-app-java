package fi.epassi.recruitment.controller;

import fi.epassi.recruitment.api.ApiResponse;
import fi.epassi.recruitment.dto.InventoryDto;
import fi.epassi.recruitment.dto.InventoryGlobalDto;
import fi.epassi.recruitment.exception.BookstoreNotFoundException;
import fi.epassi.recruitment.exception.InventoryNotFoundException;
import fi.epassi.recruitment.services.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping(value = "/isbn/{isbn}/copies", produces = APPLICATION_JSON_VALUE)
    Flux<ApiResponse<InventoryDto>> getInventoryCopiesByIsbn(
            @PathVariable("isbn") @Validated UUID isbn) {
        //return ApiResponse.ok(inventoryService.getCopiesByIsbn(isbn));
        return inventoryService.getCopiesByIsbn(isbn)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("ISBN", isbn.toString())));
    }

    @GetMapping(value = "/author/{author}/copies", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Map<String, Integer>>> getInventoryCopiesByAuthor(
            @PathVariable("author") String author) {
        return inventoryService.getCopiesByAuthorBookstore(author)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("Author", author)));
    }

    @GetMapping(value = "/title/{title}/copies", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<Map<String, Integer>>> getInventoryCopiesByTitle(
            @PathVariable("title") String title) {
        return inventoryService.getCopiesByTitleBookstore(title)
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException("Title", title)));
    }

    @PutMapping(value = "/isbn/{isbn}/copies", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<UUID>> updateInventoriesCopiesByIsbn(
            @PathVariable("isbn") @Validated UUID isbn,
            @RequestParam(value = "copies") Integer copies,
            @RequestParam(value = "bookstore_id", required = false) Long bookstore_id) {
        return inventoryService.updateInventory(isbn, copies, bookstore_id)
                .map(ApiResponse::ok)
                .onErrorResume(e -> {
                    if (e instanceof BookstoreNotFoundException) {
                        return (Mono.just(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage())));
                    } else if (e instanceof InventoryNotFoundException) {
                        return ( Mono.just(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage())));
                    } else {
                        log.error("Failed to update inventory:"+e.getMessage());
                        return (Mono.just(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while updating inventory.")));
                    }
                });
    }

    @GetMapping(value = "/copies", produces = APPLICATION_JSON_VALUE)
    Mono<ApiResponse<InventoryGlobalDto>> getInventoryTotalCopies() {
        return inventoryService.getTotalCopies()
                .map(ApiResponse::ok)
                .switchIfEmpty(Mono.error(new InventoryNotFoundException()));
    }
}
