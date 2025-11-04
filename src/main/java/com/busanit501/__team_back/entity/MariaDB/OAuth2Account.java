package com.busanit501.__team_back.entity.MariaDB;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.busanit501.__team_back.entity.MariaDB.User;

@Entity
@Table(name = "oauth2_account",
       uniqueConstraints = @UniqueConstraint(name = "uk_oauth_provider_pid", columnNames = {"provider", "provider_id"}))
public class OAuth2Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String provider; // google | naver

    @Column(name = "provider_id", nullable = false, length = 128)
    private String providerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
