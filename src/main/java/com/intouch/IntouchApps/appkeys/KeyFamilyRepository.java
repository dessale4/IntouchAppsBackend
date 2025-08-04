package com.intouch.IntouchApps.appkeys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeyFamilyRepository extends JpaRepository<KeyFamily, Integer> {
     Optional<KeyFamily> findByKeyFamilyId(Integer keyFamilyId);
}
