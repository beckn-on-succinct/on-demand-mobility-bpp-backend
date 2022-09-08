package in.succinct.bpp.cabs.db.model.demand;

import com.venky.core.collections.SequenceSet;
import com.venky.geo.GeoLocation;
import com.venky.swf.db.annotations.column.COLUMN_DEF;
import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.defaulting.StandardDefault;
import com.venky.swf.db.annotations.column.indexing.Index;
import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import com.venky.swf.db.annotations.column.ui.PROTECTION;
import com.venky.swf.db.annotations.column.ui.PROTECTION.Kind;
import com.venky.swf.db.annotations.column.validations.Enumeration;
import com.venky.swf.db.annotations.column.validations.IntegerRange;
import com.venky.swf.db.annotations.model.MENU;
import com.venky.swf.db.model.Model;
import com.venky.swf.routing.Config;
import in.succinct.bpp.cabs.db.model.routes.Route;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@MENU("Trip")
public interface Trip extends Model, GeoLocation {
    public static  final String UnConfirmed = "Not Confirmed";
    public static  final String Confirmed = "Confirmed";
    public static  final String Canceled = "Canceled";

    public static  final String Started = "Started";
    public static  final String Ended = "Ended";

    public static String[] STATUSES = new String[]{UnConfirmed,Confirmed,Started,Ended,Canceled};
    public static List<String> STATUS_LIST = new SequenceSet<String>(){{
        for (int i = 0 ; i < STATUSES.length ; i++){
            add(STATUSES[i]);
        }
    }};


    public static  final String Rejected = "Rejected";
    public static  final String Accepted = "Accepted";

    @COLUMN_DEF(StandardDefault.NULL)
    @IS_NULLABLE
    @Enumeration(" ," + Accepted + "," + Rejected)
    @Index
    public String getDriverAcceptanceStatus();
    public void setDriverAcceptanceStatus(String driverAcceptanceStatus);

    @Enumeration(UnConfirmed +"," + Confirmed + "," + Started  + "," + Ended + "," + Canceled )
    @Index
    public String getStatus();
    public void setStatus(String status);

    @Index
    public Long getDeploymentPurposeId();
    public void setDeploymentPurposeId(Long id);
    public DeploymentPurpose getDeploymentPurpose();

    @Index
    public Timestamp getScheduledStart();
    public void setScheduledStart(Timestamp requestedStart);

    public Timestamp getStartTs();
    public void setStartTs(Timestamp start);

    public Timestamp getEndTs();
    public void setEndTs(Timestamp end);

    public String getVehicleTags();
    public void setVehicleTags(String vehicleTags);

    @Index
    public Long getPassengerId();
    public void setPassengerId(Long id);
    public User getPassenger();

    @Index
    public Long getPayerId();
    public void setPayerId(Long id);
    public User getPayer();


    /* Route may be static or dynamic */
    public Long getRouteId();
    public void setRouteId(Long id);
    public Route getRoute();

    @Index
    @PARTICIPANT
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
    @PROTECTION(Kind.DISABLED)
    public BigDecimal getLat();

    @IS_VIRTUAL
    @PROTECTION(Kind.DISABLED)
    public BigDecimal getLng();

    public List<TripStop>  getTripStops();


    public void allocate();

    public Double getPrice();
    public void setPrice (Double price);

    public Double getIGst();
    public void setIGst(Double gst);

    public Double getCGst();
    public void setCGst(Double gst);

    public Double getSGst();
    public void setSGst(Double gst);

    //* This is what is paid by the client.
    public Double getSellingPrice();
    public void setSellingPrice(Double totalPrice);

    public void start();
    public void end();

    public void cancel();
    public void accept();
    public void reject();
}
