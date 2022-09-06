package in.succinct.bpp.cabs.db.model.supply;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.table.ModelImpl;
import com.venky.swf.sql.Select;

import in.succinct.bpp.cabs.db.model.demand.Trip;
import in.succinct.bpp.cabs.db.model.demand.TripStop;

public class UserImpl extends ModelImpl<User> {
    public UserImpl(User u){
        super(u);
    }

    public List<DriverLogin> getMaxDriverLogins(int max){
        Select select = new Select().from(DriverLogin.class);
        select.add(" where authorized_driver_id in  ( select id from authorized_drivers where driver_id = " + getProxy().getId() + ")");

        List<DriverLogin> logins = select.orderBy("ID DESC").execute(max);
        return logins;
    }

    //TODO get availabke time.!!
    public Timestamp getAvailableAt(){
        long availableAt = System.currentTimeMillis();

        List<DriverLogin> logins = getMaxDriverLogins(1);
        if (logins.isEmpty()){
            return null;
        }
        DriverLogin last = logins.get(0);
        if (last.getLoggedOffAt() != null){
            return null; //Logged off!!
        }

        Optional<Trip> optionalTrip = last.getTrips().stream().filter(t->t.getStartTs() != null ).findFirst();

        if (optionalTrip.isPresent()){
            Trip lastTrip = optionalTrip.get();

            if (lastTrip.getEndTs() == null){
                //Not Ended.
                List<TripStop> stops =  lastTrip.getTripStops();
                if (!stops.isEmpty()) {
                    long lstarted = lastTrip.getStartTs() == null ? (lastTrip.getScheduledStart() == null ? availableAt : lastTrip.getScheduledStart().getTime()) : lastTrip.getStartTs().getTime();
                    long lended = lstarted;
                    for (TripStop stop : stops){
                        lended += (stop.getMinutesFromLastStop() > 0? stop.getMinutesFromLastStop() : stop.getEstimatedMinutesFromLastStop())*60L*1000L;
                    }
                    availableAt = Math.max(availableAt,lended);
                }
            }else {
                availableAt = Math.max(availableAt, lastTrip.getEndTs().getTime());
            }
        }
        return new Timestamp(availableAt);
    }

    public boolean isAvailable() {
        Timestamp availableAt = getAvailableAt();
        return availableAt != null && availableAt.getTime() < System.currentTimeMillis() + 10 * 60 * 1000L ; // 10 minutes
    }

    public boolean isApproved(){
        User u = getProxy();
        Set<String> validatedDocuments = new HashSet<>();
        u.getDriverDocuments().forEach(d->{
            if (d.getVerificationStatus().equals(VerifiableDocument.APPROVED) && !d.isExpired()) {
                validatedDocuments.add(d.getDocument());
            }
        });
        for (String document : DriverDocument.DOCUMENTS_NEEDED) {
            if (!validatedDocuments.contains(document)){
                return false;
            }
        }
        return true;
    }

    public Timestamp getDateOfJoining(){
        User user = getProxy();
        return user.getCreatedAt();
    }

}
