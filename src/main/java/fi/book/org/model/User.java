package fi.book.org.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;
import java.util.UUID;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Table("users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Persistable<UUID> {
    @Builder.Default
    @Transient
    public boolean isNew = true;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String username;
    private String password;
    @Transient // Prevents roles from being persisted automatically
    private Set<String> roles;

}
