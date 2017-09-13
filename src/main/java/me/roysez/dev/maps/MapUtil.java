package me.roysez.dev.maps;

import com.github.messenger4j.receive.events.AttachmentMessageEvent;

import java.awt.geom.Point2D;

public class MapUtil {

    private static final double earthRadius = 6371;

    public static double distanceInKm(AttachmentMessageEvent.Coordinates currentLocation,
                                String longitude,String latitude){


        Point2D first = new Point2D.Double();
        first.setLocation(currentLocation.getLongitude(),currentLocation.getLatitude());
        Point2D second = new Point2D.Double();
        second.setLocation(Double.valueOf(longitude),Double.valueOf(latitude));


        double dLat = deg2rad(second.getY()-first.getY());  // deg2rad below
        double dLon = deg2rad(second.getX()-first.getX());
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(first.getY())) * Math.cos(deg2rad(second.getY())) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = earthRadius * c; // Distance in km
        return d;

    }

    private  static double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
}
