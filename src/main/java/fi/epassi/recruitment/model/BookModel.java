package fi.epassi.recruitment.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table("books")
public class BookModel {//implements Persistable {


    @Column("id")
    @Id
    private UUID isbn;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotNull
    @DecimalMin(value = "0.00", message = "Book price must be higher than 0.00")
    private BigDecimal price;

}
