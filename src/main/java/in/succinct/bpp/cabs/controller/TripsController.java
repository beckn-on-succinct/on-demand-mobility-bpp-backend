package in.succinct.bpp.cabs.controller;

import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.Database;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.path.Path;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.bpp.cabs.db.model.demand.Trip;
import org.json.simple.JSONObject;

import java.nio.charset.StandardCharsets;

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


}
