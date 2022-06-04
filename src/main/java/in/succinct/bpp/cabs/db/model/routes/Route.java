package in.succinct.bpp.cabs.db.model.routes;

import com.venky.swf.db.annotations.column.IS_NULLABLE;
import com.venky.swf.db.annotations.column.pm.PARTICIPANT;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.User;
import com.venky.swf.plugins.collab.db.model.CompanyNonSpecific;

import java.util.List;

public interface Route extends Model, CompanyNonSpecific {

    @PARTICIPANT
    public Long getUserId();
    public void setUserId(Long id);
    public User getUser();

    @IS_NULLABLE
    public String getName();
    public void setName(String name);

    public List<RouteStop> getStops();

}
