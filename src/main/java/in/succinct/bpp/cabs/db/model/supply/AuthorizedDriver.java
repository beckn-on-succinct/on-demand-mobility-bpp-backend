package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.UNIQUE_KEY;
import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import com.venky.swf.db.model.Model;

public interface AuthorizedDriver extends Model {
    @UNIQUE_KEY
    @PARTICIPANT
    @IS_NULLABLE(false)
    public Long getVehicleId();
    public void setVehicleId(Long id);
    public Vehicle getVehicle();

    @UNIQUE_KEY
    @IS_NULLABLE(false)
    public Long getDriverId();
    public void setDriverId(Long id);
    public User getDriver();


    public DriverLogin login();



}
