package in.succinct.bpp.cabs.db.model.routes;

import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.IS_VIRTUAL;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import in.succinct.bpp.cabs.db.model.routes.stops.Stop;

@IS_VIRTUAL(false)
public interface RouteStop extends Stop {
    @UNIQUE_KEY
    public String getName();
    public void setName(String name);

    @UNIQUE_KEY
    @PARTICIPANT
    @IS_NULLABLE(false)
    public Long getRouteId();
    public void setRouteId(Long id);
    public Route getRoute();

    @UNIQUE_KEY
    public Integer getSequenceNumber();
    public void setSequenceNumber(Integer sequenceNumber);

    public Double getDistanceFromLastStop();
    public void setDistanceFromLastStop(Double distanceFromLastStop);

    @IS_NULLABLE
    public Integer getEstimatedMinutesFromLastStop();
    public void setEstimatedMinutesFromLastStop(Integer minutesFromLastStop);

}
