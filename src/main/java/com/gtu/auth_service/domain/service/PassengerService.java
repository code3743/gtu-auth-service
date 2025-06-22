package com.gtu.auth_service.domain.service;

import com.gtu.auth_service.domain.model.Passenger;

public interface PassengerService {
    Passenger createPassenger(String name, String email, String password);
}