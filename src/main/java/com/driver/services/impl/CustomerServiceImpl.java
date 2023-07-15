package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteCustomer(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList = driverRepository2.findAll();
		for(Driver driver:driverList){
			Cab cab = driver.getCab();
			if(cab.getAvailable()){
				Optional<Customer> optionalCustomer = customerRepository2.findById(customerId);
				Customer customer = optionalCustomer.get();
				TripBooking tripBooking = new TripBooking();
				tripBooking.setCustomer(customer);
				tripBooking.setDistanceInKm(distanceInKm);
				tripBooking.setFromLocation(fromLocation);
				tripBooking.setToLocation(toLocation);
				tripBooking.setStatus(TripStatus.CONFIRMED);

				TripBooking savedTrip = tripBookingRepository2.save(tripBooking);
				driver.getTripBookingList().add(savedTrip);
				driverRepository2.save(driver);
				return savedTrip;
			}
		}
		throw new Exception("No cab available!");
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
		TripBooking tripBooking = optionalTripBooking.get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBookingRepository2.deleteById(tripId);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
		TripBooking tripBooking = optionalTripBooking.get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		Driver driver = tripBooking.getDriver();
		Cab cab = driver.getCab();
		int ratePerKm = cab.getPerKmRate();
		int totalDistance = tripBooking.getDistanceInKm();
		int totalBill = ratePerKm * totalDistance;
		tripBooking.setBill(totalBill);
		tripBookingRepository2.save(tripBooking);
	}
}
