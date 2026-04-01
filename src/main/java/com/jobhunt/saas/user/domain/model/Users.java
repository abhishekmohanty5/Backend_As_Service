package com.jobhunt.saas.user.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.jobhunt.saas.tenant.domain.model.Tenant;
import com.jobhunt.saas.shared.domain.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "saas_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "email", "tenant_id" })
})
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean setEnable;

    @Column(nullable = false)
    private boolean emailVerified;

    @PrePersist
    public void onCreation(){
        this.setEnable=false;
        this.emailVerified=false;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private Role role;
}
