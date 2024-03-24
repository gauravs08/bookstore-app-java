package fi.epassi.recruitment.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode
@Table("books")
public class BookModel  implements Persistable {


    @Column("id")
    @Id  // This is removed for open issue that is discussed here, in short, if @Id is used then it won't treat any data as new. it will always go from  save -> update() instead of save-> insert()
    //https://stackoverflow.com/questions/59468908/reactive-repository-throws-exception-when-saving-a-new-object/74440442#74440442
    //@Unique
    private UUID isbn;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotNull
    @DecimalMin(value = "0.00", message = "Book price must be higher than 0.00")
    private BigDecimal price;


    //TODO
    // -- there is an ongoing issue and trying to by-pass the failure as one of the work around
    // -- Failed to update table [books]; Row with Id [1fa85f64-5717-4562-b3fc-2c963f66afa6] does not exist
    // -- https://github.com/spring-projects/spring-data-r2dbc/issues/275
    @Transient
    private boolean newBook;

    @Override
    public Object getId() {
        return this.isbn;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.newBook || isbn == null;
    }

    public BookModel setAsNew(){
        this.newBook = true;
        return this;
    }
}
