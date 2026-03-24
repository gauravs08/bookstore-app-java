package fi.book.org.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table("inventory")
public class Inventory implements Persistable<UUID> {
    @Id
    @Column("id")
    private UUID id;

    private int copies;

    @Column("bookstore_id")
    private Long bookstoreId;

    @Builder.Default
    @Transient
    private boolean isNew = true;
}
