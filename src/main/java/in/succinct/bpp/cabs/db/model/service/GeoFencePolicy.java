package in.succinct.bpp.cabs.db.model.service;

import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.model.Model;

public interface GeoFencePolicy extends Model {
    @IS_NULLABLE
    public String getStartCity();
    public void setStartCity(String startCity);

    @IS_NULLABLE
    public String getEndCity();
    public void setEndCity(String endCity);

    @IS_NULLABLE
    public String getDriverCity();
    public void setDriverCity(String driverCity);

}
