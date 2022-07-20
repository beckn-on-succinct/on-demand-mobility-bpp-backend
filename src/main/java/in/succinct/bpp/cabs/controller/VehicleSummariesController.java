package in.succinct.bpp.cabs.controller;

import com.venky.cache.Cache;
import com.venky.core.util.Bucket;
import com.venky.swf.controller.VirtualModelController;
import com.venky.swf.db.Database;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.security.db.model.Role;
import com.venky.swf.plugins.security.db.model.UserRole;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import com.venky.swf.views.View;
import in.succinct.bpp.cabs.db.model.supply.User;
import in.succinct.bpp.cabs.db.model.supply.UserSummary;
import in.succinct.bpp.cabs.db.model.supply.Vehicle;
import in.succinct.bpp.cabs.db.model.supply.VehicleDocument;
import in.succinct.bpp.cabs.db.model.supply.VehicleSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VehicleSummariesController extends VirtualModelController<VehicleSummary> {

    public VehicleSummariesController(Path path) {
        super(path);
    }

    public View index(){
        User user = getSessionUser();
        List<Long> companyIds = user.getCompanyIds();
        StringBuilder companyIn = new StringBuilder();
        companyIds.forEach(cid->{
            companyIn.append(cid).append(",");
        });

        companyIn.append(-1);

        Map<Long,VehicleSummary> map = new Cache<Long, VehicleSummary>() {
            @Override
            protected VehicleSummary getValue(Long c) {
                VehicleSummary summary = Database.getTable(VehicleSummary.class).newRecord();
                summary.setCompanyId(c);
                summary.setUnverifiedVehicleCount(new Bucket());
                summary.setVehicleCount(new Bucket());
                return summary;
            }
        };

        List<VehicleSummary> out = new ArrayList<>();
        for (Long c :companyIds){
            Select select = new Select().from(Vehicle.class);
            select.add(" where exists (select 1 from users where company_id = " + c + " and users.id = vehicles.creator_id )");
            VehicleSummary summary = map.get(c);
            for (Vehicle vehicle : select.execute(Vehicle.class)) {
                summary.getVehicleCount().increment();
                if (!vehicle.isVerified()){
                    summary.getUnverifiedVehicleCount().increment();
                }
            }
            out.add(summary);
        }



        return list(out,true);
    }
}
