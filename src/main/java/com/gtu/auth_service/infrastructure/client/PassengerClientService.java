package com.gtu.auth_service.infrastructure.client;

import org.springframework.stereotype.Service;

import com.gtu.auth_service.domain.model.Passenger;
import com.gtu.auth_service.domain.service.PassengerService;
import com.gtu.auth_service.infrastructure.client.dto.PassengerRequest;

@Service
public class PassengerClientService implements PassengerService {

    private final PassengerClient passengerClient;
    
    public PassengerClientService(PassengerClient passengerClient) {
        this.passengerClient = passengerClient;
    }

    @Override
    public Passenger createPassenger(String name, String email, String password) {
       var passengerRequest = passengerClient.registerPassenger(new PassengerRequest(name, email, password));
       return new Passenger(passengerRequest.getName(), passengerRequest.getEmail(), passengerRequest.getPassword());
    }
    
}
