package org.hddm.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Saila on 1/20/2017.
 */
public class Route {
    String routeId;
    String routeName;
    List<List<LatLng>> pointsOnPath;
    String note;
    String ride;
    String fare;
    String quality;
    String createdBy;
    long createdThrough;
    String createDate;
    User sharedBy;

    public User getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(User sharedBy) {
        this.sharedBy = sharedBy;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public List<List<LatLng>> getPointsOnPath() {
        return pointsOnPath;
    }

    public void setPointsOnPath(List<List<LatLng>> pointsOnPath) {
        this.pointsOnPath = pointsOnPath;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getRide() {
        return ride;
    }

    public void setRide(String ride) {
        this.ride = ride;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedThrough() {
        return createdThrough;
    }

    public void setCreatedThrough(long createdThrough) {
        this.createdThrough = createdThrough;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
