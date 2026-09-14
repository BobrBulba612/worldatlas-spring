package com.worldatlas.bot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cities", indexes = {
    @Index(name = "idx_cities_name", columnList = "name")
})
public class City {
    @Id
    private String name;
    private String timezone;
    private String country;
    private String continent;
}
