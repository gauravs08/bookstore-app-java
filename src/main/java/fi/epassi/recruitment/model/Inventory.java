package fi.epassi.recruitment.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;



@Data
//@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table("inventory")
public class Inventory {//implements Persistable {

    @Id
    //@Unique
    @Column("id")
    private UUID isbn;

    private int copies;

    @Column("bookstore_id") // Foreign key column
    private Long bookstoreId; // Foreign key referencing Bookstore

//    //TODO
//    // -- there is an ongoing issue and trying to by-pass the failure as one of the work around
//    // -- ISSUE: Failed to update table [books]; Row with Id [1fa85f64-5717-4562-b3fc-2c963f66afa6] does not exist
//    // -- https://github.com/spring-projects/spring-data-r2dbc/issues/275
//    @Transient
//    private boolean newInventory;
//
//    @Override
//    public Object getId() {
//        return this.isbn;
//    }
//
//    @Override
//    @Transient
//    public boolean isNew() {
//        return this.newInventory || isbn == null;
//    }
//
//    public Inventory setAsNew(){
//        this.newInventory = true;
//        return this;
//    }
}
