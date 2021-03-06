package com.automationintesting.api;

import com.automationintesting.model.report.Entry;
import com.automationintesting.model.report.Report;
import com.automationintesting.model.room.Booking;
import com.automationintesting.model.room.Bookings;
import com.automationintesting.model.room.Room;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.automationintesting.requests.BookingRequests;
import com.automationintesting.requests.RoomRequests;


import java.sql.SQLException;
import java.util.*;

@RestController
public class ReportController {

    private RoomRequests roomRequests;
    private BookingRequests bookingRequests;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String originHost = "http://localhost:3003";

                if(System.getenv("cors") != null){
                    originHost = System.getenv("cors");
                }

                registry.addMapping("/*")
                        .allowedOrigins(originHost)
                        .allowCredentials(true);
            }
        };
    }

    public ReportController() throws SQLException {
        roomRequests = new RoomRequests();
        bookingRequests = new BookingRequests();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Report getAllRoomReports() {
        List<Room> rooms = roomRequests.searchForRooms().getBody().getRooms();
        List<Entry> parsedRooms = new ArrayList<>();

        for(Room r : rooms){
            Bookings roomBookings = bookingRequests.getBookings(r.getRoomid()).getBody();

            for(Booking b : roomBookings.getBookings()){
                Entry entry = new Entry(b.getBookingDates().getCheckin(), b.getBookingDates().getCheckout(), b.getFirstname() + " " + b.getLastname() + " - Room: " + r.getRoomNumber());
                parsedRooms.add(entry);
            }
        }

        return new Report(parsedRooms);
    }

    @RequestMapping(value = "/room/{id:[0-9]*}", method = RequestMethod.GET)
    public Report getSpecificRoomReport(@PathVariable(value = "id") int id){
        List<Entry> parsedRooms = new ArrayList<>();

        Bookings roomBookings = bookingRequests.getBookings(id).getBody();

        for(Booking b : roomBookings.getBookings()){
            Entry entry = new Entry(b.getBookingDates().getCheckin(), b.getBookingDates().getCheckout(), "Unavailable");
            parsedRooms.add(entry);
        }

        return new Report(parsedRooms);
    }

}
