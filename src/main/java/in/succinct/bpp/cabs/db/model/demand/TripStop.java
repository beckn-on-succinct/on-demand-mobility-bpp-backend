package in.succinct.bpp.cabs.db.model.demand;

import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.model.ORDER_BY;
import in.succinct.bpp.cabs.db.model.routes.RouteStop;

@IS_VIRTUAL(false)
@ORDER_BY("TRIP_ID,SEQUENCE_NUMBER")
public interface TripStop extends RouteStop {
    public Long getTripId();
    public void setTripId(Long id);
    public Trip getTrip();

    @IS_NULLABLE
    public Long getRouteId();

    @IS_NULLABLE
    public Integer getMinutesFromLastStop();
    public void setMinutesFromLastStop(Integer minutesFromLastStop);
}
