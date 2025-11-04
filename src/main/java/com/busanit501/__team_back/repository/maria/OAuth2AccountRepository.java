package com.busanit501.__team_back.repository.maria;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.busanit501.__team_back.entity.MariaDB.OAuth2Account;

public interface OAuth2AccountRepository extends JpaRepository<OAuth2Account, Long> {
    Optional<OAuth2Account> findByProviderAndProviderId(String provider, String providerId);
}
