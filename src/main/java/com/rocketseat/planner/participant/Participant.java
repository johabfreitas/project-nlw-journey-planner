package com.rocketseat.planner.participant;

import com.rocketseat.planner.trip.Trip;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "participants")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String ownerEmail;

    @Column(name = "is_confirmed", nullable = false)
    private Boolean isConfirmed;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
}
