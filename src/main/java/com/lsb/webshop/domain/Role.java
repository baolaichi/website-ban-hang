package com.lsb.webshop.domain;

import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore; // Import quan trọng

@Entity
@Data
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "role")
    @JsonIgnore // <--- CẮT VÒNG LẶP
    private List<User> users;
}