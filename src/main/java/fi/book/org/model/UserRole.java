package fi.book.org.model;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table("user_roles")
@AllArgsConstructor
public class UserRole {
    @Column("user_id")
    private UUID userId;

    @Column("role")
    private String role;
}
