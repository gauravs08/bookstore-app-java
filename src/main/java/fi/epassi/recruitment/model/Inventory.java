package fi.epassi.recruitment.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table("inventory")
public class Inventory {
    @Id
    @Column("id")
    private UUID isbn;

    private int copies;

    @Column("bookstore_id")
    private Long bookstoreId;
}
