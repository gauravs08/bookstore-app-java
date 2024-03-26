package fi.epassi.recruitment.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table("books")
public class BookStore {//implements Persistable {


    @Column("id")
    @Id
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String address;

//    @NotBlank
//    @Column("created_at")
//    private LocalDateTime createdAt;
//    //TODO
//    // -- there is an ongoing issue and trying to by-pass the failure as one of the work around
//    // -- Failed to update table [books]; Row with Id [1fa85f64-5717-4562-b3fc-2c963f66afa6] does not exist
//    // -- https://github.com/spring-projects/spring-data-r2dbc/issues/275
//    @Transient
//    private boolean newBookStore;
//
//    @Override
//    public Object getId() {
//        return this.id;
//    }
//
//    @Override
//    @Transient
//    public boolean isNew() {
//        return this.newBookStore || id == null;
//    }
//
//    public BookStore setAsNew() {
//        this.newBookStore = true;
//        return this;
//    }
}
