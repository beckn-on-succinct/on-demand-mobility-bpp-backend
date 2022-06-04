package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.table.ModelImpl;

import java.util.HashSet;
import java.util.Set;

public class UserImpl extends ModelImpl<User> {
    public UserImpl(User u){
        super(u);
    }

    public boolean isAvailable() {
        return false;
        //TODO based on schedule and trips
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
