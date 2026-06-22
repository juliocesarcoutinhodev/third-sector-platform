package br.com.toponesystem.thirdsector.auth.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.id = :id")
    void revokeById(UUID id);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.familyId = :familyId")
    void revokeByFamilyId(String familyId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.userId = :userId")
    void revokeByUserId(UUID userId);
}
