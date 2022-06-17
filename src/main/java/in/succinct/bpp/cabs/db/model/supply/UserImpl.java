package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.table.ModelImpl;
import in.succinct.bpp.cabs.db.model.demand.Trip;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserImpl extends ModelImpl<User> {
    public UserImpl(User u){
        super(u);
    }

    public List<DriverLogin> getMaxDriverLogins(int max){
        List<DriverLogin> logins = getChildren(DriverLogin.class,"AUTHORIZED_DRIVER_ID",null,1);
        return logins;
    }
    public boolean isAvailable() {
        List<DriverLogin> logins = getProxy().getDriverLogins();
        if (logins.isEmpty()){
            return false;
        }
        DriverLogin last = logins.get(0);
        if (last.getLoggedOffAt() == null){
            List<Trip> trips= last.getTrips();
            Trip lastTrip = null;
            if (!trips.isEmpty()){
                lastTrip = trips.get(0);
                return lastTrip.getEndTs() != null;
            }
        }
        return false;
    }

    public boolean isVerified(){
        User u = getProxy();
        Set<String> validatedDocuments = new HashSet<>();
        u.getDriverDocuments().forEach(d->{
            if (d.isVerified() && !d.isExpired()) {
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
}
