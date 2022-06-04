package in.succinct.bpp.cabs.db.model.demand;

import com.venky.geo.GeoLocation;
import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.validations.Enumeration;
import com.venky.swf.db.annotations.column.validations.IntegerRange;
import com.venky.swf.db.annotations.model.MENU;
import com.venky.swf.db.model.Model;
import in.succinct.bpp.cabs.db.model.routes.Route;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@MENU("Trip")
public interface Trip extends Model, GeoLocation {
    public static  final String NotStarted = "Not Started";
    public static  final String Started = "Started";
    public static  final String Ended = "Ended";


    @Enumeration(NotStarted + "," + Started  + "," + Ended)
    public String getStatus();
    public void setStatus(String status);

    @IS_NULLABLE(false)
    public long getDeploymentPurposeId();
    public void setDeploymentPurposeId(long id);
    public DeploymentPurpose getDeploymentPurpose();

    public Timestamp getScheduledStart();
    public void setScheduledStart(Timestamp requestedStart);

    public Timestamp getStartTs();
    public void setStartTs(Timestamp start);

    public Timestamp getEndTs();
    public void setEndTs(Timestamp end);

    public String getVehicleTags();
    public void setVehicleTags(String vehicleTags);

    public Long getPassengerId();
    public void setPassengerId(Long id);
    public User getPassenger();

    /* Route may be static or dynamic */
    public Long getRouteId();
    public void setRouteId(Long id);
    public Route getRoute();

    public Long getDriverLoginId();
    public void setDriverLoginId(Long id);
    public DriverLogin getDriverLogin();


    @IntegerRange(min = 1,max = 5)
    public Double getDriverRating();
    public void setDriverRating(Double driverRating);

    @IntegerRange(min = 1,max = 5)
    public Double getPassengerRating();
    public void setPassengerRating(Double passengerRating);

    @IntegerRange(min = 1,max = 5)
    public Double getVehicleRating();
    public void setVehicleRating(Double vehicleRating);


    @IS_VIRTUAL
    public BigDecimal getLat();

    @IS_VIRTUAL
    public BigDecimal getLng();

    public List<TripStop>  getTripStops();


    public void rate();
    public void allocate();

}
