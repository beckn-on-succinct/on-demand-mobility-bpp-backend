package in.succinct.bpp.cabs.db.model.demand;

import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.model.Model;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;

public interface RejectedTrip extends Model {
    @UNIQUE_KEY
    public Long getDriverLoginId();
    public void setDriverLoginId(Long id);
    public DriverLogin getDriverLogin();


    @UNIQUE_KEY
    public Long getTripId();
    public void setTripId(Long id);
    public Trip getTrip();


}
