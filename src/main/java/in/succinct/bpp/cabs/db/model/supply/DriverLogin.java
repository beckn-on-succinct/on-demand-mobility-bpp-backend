package in.succinct.bpp.cabs.db.model.supply;

import com.venky.geo.GeoLocation;
import com.venky.swf.db.model.Model;
import in.succinct.bpp.cabs.db.model.demand.Trip;

import java.sql.Timestamp;
import java.util.List;

public interface DriverLogin extends Model , GeoLocation {
    public Long getAuthorizedDriverId();
    public void setAuthorizedDriverId(Long id);
    public AuthorizedDriver getAuthorizedDriver();


    public Timestamp getLoggedInAt();
    public void setLoggedInAt(Timestamp loggedInAt);

    public Timestamp getLoggedOffAt();
    public void setLoggedOffAt(Timestamp loggedOffAt);

    public Integer getOedometerAtStart();
    public void setOedometerAtStart(Integer oedometerReading);

    public Integer getOedometerAtEnd();
    public void setOedometerAtEnd(Integer oedometerReading);


    public List<Trip> getTrips();

}
