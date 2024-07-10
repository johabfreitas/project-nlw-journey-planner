package com.rocketseat.planner.trip;

import com.rocketseat.planner.activity.ActivityData;
import com.rocketseat.planner.activity.ActivityRequestPayload;
import com.rocketseat.planner.activity.ActivityResponse;
import com.rocketseat.planner.activity.ActivityService;
import com.rocketseat.planner.link.LinkRequestPayload;
import com.rocketseat.planner.link.LinkResponse;
import com.rocketseat.planner.link.LinkService;
import com.rocketseat.planner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private TripRepository repository;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private LinkService linkService;

    //Método de criação da viagem
    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload){
        Trip newTrip = new Trip(payload);
        this.repository.save(newTrip);
        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);
        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    //Método de consultar a viagem criada
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id){
        Optional<Trip> trip = this.repository.findById(id);
        /*
         * Monte uma resposta do tipo ResponseEntity com status OK, e coloque o trip dentro do body.
         * Se o trip for nulo retorne um ResponseEntity com status notFound.
         */
        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Método de atualização de uma viagem
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.repository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    //Método confirmação de viagem
    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);

            this.repository.save(rawTrip);

            //Enviar email de confirmação para os participantes
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            ActivityResponse activityResponse = this
                    .activityService
                    .registerActivity(payload, rawTrip);

            return ResponseEntity.ok(activityResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{tripId}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID tripId){
        List<ActivityData> activityDataList = this.activityService.getAllActivitiesFromId(tripId);

        return ResponseEntity.ok(activityDataList);
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            ParticipantCreateResponse participantResponse = this.participantService.registerParticipantToEvent(payload.email(), rawTrip);

            if(rawTrip.getIsConfirmed()){
                this.participantService.triggerConfirmationEmailToParticipant(payload.email());
            }

            return ResponseEntity.ok(participantResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{tripId}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID tripId){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromEvent(tripId);

        return ResponseEntity.ok(participantList);
    }

    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            LinkResponse linkResponse = this
                    .linkService
                    .registerLink(payload, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    }

}
