package fi.epassi.recruitment.inventory;

import fi.epassi.recruitment.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/inventory", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping(value = "/isbn/{isbn}/copies")
    ApiResponse<InventoryDto> getInventoryCopiesByIsbn(
            @PathVariable("isbn") @Validated UUID isbn) {
        return ApiResponse.ok(inventoryService.getCopiesByIsbn(isbn));
    }

    @GetMapping("/author/{author}/copies")
    ApiResponse<InventoryDto> getInventoryCopiesByAuthor(
            @PathVariable("author") String author) {
        return ApiResponse.ok(inventoryService.getCopiesByAuthor(author));
    }

    @GetMapping("/title/{title}/copies")
    ApiResponse<InventoryDto> getInventoryCopiesByTitle(
            @PathVariable("title") String title) {
        return ApiResponse.ok(inventoryService.getCopiesByTitle(title));
    }

    @PutMapping("/isbn/{isbn}/copies")
    ApiResponse<UUID> updateInventoriesCopiesByIsbn(@RequestBody @Validated InventoryDto inventoryDto) {
        var ret = inventoryService.updateInventory(inventoryDto);
        return ApiResponse.ok(ret);
    }

    @GetMapping("/copies")
    ApiResponse<InventoryGlobalDto> getInventoryTotalCopies() {
        return ApiResponse.ok(inventoryService.getTotalCopies());
    }
}
