package fi.book.org.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {

    private UUID id;
    private String title;
    private String author;
    private int copies;
    private Long bookstoreId;

}
