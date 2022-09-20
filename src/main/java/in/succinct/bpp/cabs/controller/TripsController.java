package in.succinct.bpp.cabs.controller;

import com.venky.geo.GeoCoordinate;
import com.venky.geo.GeoDistance;
import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.Database;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.path.Path;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.bpp.cabs.db.model.demand.Trip;
import in.succinct.bpp.cabs.db.model.demand.TripStop;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;
import org.json.simple.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TripsController extends ModelController<Trip> {
    public TripsController(Path path) {
        super(path);
    }

    @RequireLogin(value = false)// Not sure of auth here.
    public View location(long id){
        Trip trip  = Database.getTable(Trip.class).get(id);
        JSONObject location = new JSONObject();
        location.put("gps",String.format("%f,%f",trip.getLat(),trip.getLng()));
        return new BytesView(getPath(),location.toString().getBytes(StandardCharsets.UTF_8), MimeType.APPLICATION_JSON);
    }

    public View allocate(long id){
        Trip trip = Database.getTable(Trip.class).get(id);
        trip.allocate();
        return show(trip);
    }

    public View start(long id){
        Trip trip = Database.getTable(Trip.class).get(id);
        trip.start();
        return show(trip);
    }
    public View end(long id){
        Trip trip = Database.getTable(Trip.class).get(id);
        trip.end();
        return show(trip);
    }

    public View accept(long id){
        Trip trip = Database.getTable(Trip.class).get(id);
        trip.accept();
        return show(trip);

    }
    public View reject(long id){
        Trip trip = Database.getTable(Trip.class).get(id);
        trip.reject();
        return show(trip);
    }

    public View cancel(long id){
        Trip trip = Database.getTable(Trip.class).get(id);
        trip.cancel();
        return show(trip);
    }

    public View next(int maxRecords){
        User user = (User)getPath().getSessionUser();
        List<DriverLogin> logins = user.getMaxDriverLogins(1);
        DriverLogin login = logins.isEmpty()? null : logins.get(0);
        if (login == null || user.getCurrentLat() == null || user.getCurrentLng() == null){
            return list(new ArrayList<>(),true);
        }
        GeoCoordinate userLocation = new GeoCoordinate(user.getCurrentLat(),user.getCurrentLng());
        Expression where = new Expression(getReflector().getPool(), Conjunction.AND);
        where.add(new Expression(getReflector().getPool(),"DRIVER_LOGIN_ID", Operator.EQ,login.getId()));
        where.add(new Expression(getReflector().getPool(),"STATUS", Operator.EQ,Trip.Confirmed));

        Expression daWhere = new Expression(getReflector().getPool(), Conjunction.OR);
        daWhere.add(new Expression(getReflector().getPool(),"DRIVER_ACCEPTANCE_STATUS", Operator.EQ,Trip.Rejected));
        daWhere.add(new Expression(getReflector().getPool(),"DRIVER_ACCEPTANCE_STATUS", Operator.EQ));
        where.add(daWhere);

        Select select = new Select().from(Trip.class).where(where);
        select.add(" and not exists ( select 1 from rejected_trips where driver_login_id = trips.driver_login_id and trip_id = trips.id ) ");

        List<Trip> trips = select.execute();
        if (trips.isEmpty()){
            return list(trips,true);
        }else {
            trips.sort((t1, t2) -> {
                TripStop o1 = t1.getFirstStop();
                TripStop o2 = t2.getFirstStop();
                double ret = (new GeoCoordinate(o1).distanceTo(userLocation) - new GeoCoordinate(o2).distanceTo(userLocation));
                if (ret == 0) {
                    ret = t1.getId() - t2.getId();
                }
                return (int) ret;

            });
            if (trips.size() > maxRecords){
                trips = trips.subList(0,maxRecords-1);
            }
        }


        return list(trips,true);
    }

}
