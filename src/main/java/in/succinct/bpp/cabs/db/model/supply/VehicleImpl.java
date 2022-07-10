package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.table.ModelImpl;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class VehicleImpl extends ModelImpl<Vehicle> {
    public VehicleImpl(){

    }
    public VehicleImpl(Vehicle proxy){
        super(proxy);
    }

    public boolean isVerified(){
        Vehicle v = getProxy();
        Set<String> validatedDocuments = new HashSet<>();
        v.getDocuments().forEach(d->{
            if (d.isVerified() && !d.isExpired()) {
                validatedDocuments.add(d.getDocument());
            }
        });
        for (String document : VehicleDocument.DOCUMENTS_NEEDED) {
            if (!validatedDocuments.contains(document)){
                return false;
            }
        }
        return true;
    }

    SortedSet<String> tagSet = null;
    public SortedSet<String> getTagSet() {
        if (tagSet == null){
            tagSet = new TreeSet<>();
            StringTokenizer tokenizer = new StringTokenizer(getProxy().getTags(),",");
            while (tokenizer.hasMoreTokens()){
                tagSet.add(tokenizer.nextToken());
            }
        }
        return tagSet;
    }

    public Timestamp getDateOfRegister(){
        Vehicle vehicle = getProxy();
        return vehicle.getCreatedAt();
    }
}
