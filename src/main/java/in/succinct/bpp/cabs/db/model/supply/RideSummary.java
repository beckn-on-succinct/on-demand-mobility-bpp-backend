package in.succinct.bpp.cabs.db.model.supply;

import com.venky.core.util.Bucket;
import com.venky.swf.db.model.Model;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;

public interface RideSummary extends Model {
    public Long getCompanyId();
    public void setCompanyId(Long id);
    public Company getCompany();


    public Bucket getRideCount();
    public void setRideCount(Bucket rideCount);
    
    public Bucket getTotalRevenue();
    public void setTotalRevenue(Bucket totalRevenue);

}
