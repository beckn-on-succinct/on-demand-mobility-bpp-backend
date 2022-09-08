package in.succinct.bpp.cabs.db.model.supply;

import com.venky.core.util.ObjectHolder;
import com.venky.core.util.ObjectUtil;
import com.venky.geo.GeoLocation;
import com.venky.swf.db.model.User;
import com.venky.swf.db.table.ModelImpl;
import in.succinct.bpp.cabs.db.model.demand.Trip;

import java.sql.Timestamp;
import java.util.Optional;

public class DriverLoginImpl extends ModelImpl<DriverLogin> {
    public DriverLoginImpl(DriverLogin p){
        super(p);
    }
    public void updateLocation(GeoLocation geoLocation){

        DriverLogin driverLogin = getProxy();
        driverLogin.setLat(geoLocation.getLat());
        driverLogin.setLng(geoLocation.getLng());
        driverLogin.setLastLocationUpdatedAt(new Timestamp(System.currentTimeMillis()));
        driverLogin.save();
    }

    public boolean isDriver(User user) {
        if (user == null){
            return false;
        }
        return (ObjectUtil.equals(user.getId(),
                getProxy().getAuthorizedDriver().getDriverId()));
    }

    ObjectHolder<Trip> lastTrip = null;
    public Trip getLastTrip(){
        if (lastTrip == null){
            Optional<Trip> optionalTrip = getProxy().getTrips().stream().filter(t->t.getStartTs() != null ).findFirst();
            lastTrip = new ObjectHolder<>(optionalTrip.orElse(null));
        }
        return lastTrip.get();
    }
}
