package com.efreight.afbase.service;

import com.efreight.afbase.entity.Carrier;
import com.efreight.afbase.entity.Flight;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.efreight.afbase.entity.FlightDetail;

import java.text.ParseException;
import java.util.List;


public interface FlightService extends IService<Flight> {
	
	IPage<Flight> getListPage(Page page, Flight bean);

	List<Flight> getFlightList(Flight bean);

    IPage<Flight> getFlightListPage(Page page, Flight bean);

	Boolean addFlightAndDetail(Flight bean) throws ParseException;

	void removeFlightAndDetailById(String flightId);

	Integer isHavedFlight(String carrierCode);

	Boolean doUpdate(Flight bean) throws ParseException;

	Integer isHavedDepartureStation(String departureStation);

	Integer isHavedTransitStation(String transitStation);

	Integer isHavedArrivalStation(String arrivalStation);

}
