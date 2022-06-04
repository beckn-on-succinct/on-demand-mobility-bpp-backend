package in.succinct.bpp.cabs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.AfterModelSaveExtension;
import in.succinct.bpp.cabs.db.model.demand.Trip;
import in.succinct.bpp.cabs.db.model.demand.TripStop;

public class AfterSaveTrip extends AfterModelSaveExtension<Trip> {
    static {
        registerExtension(new AfterSaveTrip());
    }
    @Override
    public void afterSave(Trip trip) {
        trip.getTripStops().forEach(ts->{
            if (trip.getRouteId() == null){
                if (!ts.getReflector().isVoid(ts.getRouteId())){
                    ts.destroy();
                }
            }else {
                if (!ObjectUtil.equals(trip.getRouteId(),ts.getRouteId())){
                    ts.destroy();
                }
            }

        });
        if (trip.getRouteId() != null && trip.getTripStops().isEmpty()){
            trip.getRoute().getStops().forEach(stop->{
                TripStop tripStop = Database.getTable(TripStop.class).newRecord();
                tripStop.setTripId(trip.getId());
                tripStop.getRawRecord().load(stop.getRawRecord());
                tripStop.save();
            });
        }
    }
}
