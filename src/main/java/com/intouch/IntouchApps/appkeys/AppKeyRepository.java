package com.intouch.IntouchApps.appkeys;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppKeyRepository extends JpaRepository<AppKey, Integer> {
  public Optional<AppKey> findAppKeyByKeyFamilyIdAndKeyId(Integer keyFamilyId, Integer keyId);
}
