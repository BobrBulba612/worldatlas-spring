package com.worldatlas.bot.repository;

import com.worldatlas.bot.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, String> {
    List<City> findByNameContainingIgnoreCase(String name);
}