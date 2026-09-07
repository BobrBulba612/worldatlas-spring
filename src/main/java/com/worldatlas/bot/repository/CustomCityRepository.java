package com.worldatlas.bot.repository;

import com.worldatlas.bot.entity.CustomCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomCityRepository extends JpaRepository<CustomCity, Long> {
    Optional<CustomCity> findByNameIgnoreCase(String name);
    List<CustomCity> findByUserId(Long userId);
    boolean existsByNameIgnoreCase(String name);
}
