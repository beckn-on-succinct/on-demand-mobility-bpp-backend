package in.succinct.bpp.cabs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ParticipantExtension;
import com.venky.swf.db.model.User;
import com.venky.swf.pm.DataSecurityFilter;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import in.succinct.bpp.cabs.db.model.supply.Vehicle;
import in.succinct.bpp.cabs.db.model.supply.AuthorizedDriver;
import in.succinct.bpp.cabs.db.model.supply.WorkCalendar;

import java.util.Collections;
import java.util.List;

public class AuthorizedDriverParticipantExtension extends ParticipantExtension<AuthorizedDriver> {
    static {
        registerExtension(new AuthorizedDriverParticipantExtension());
    }
    @Override
    protected List<Long> getAllowedFieldValues(User user, AuthorizedDriver partiallyFilledModel, String fieldName) {
        if (ObjectUtil.equals(fieldName,"VEHICLE_ID")) {
            List<Vehicle> vehicles = DataSecurityFilter.getRecordsAccessible(Vehicle.class,user);
            return DataSecurityFilter.getIds(vehicles);
        }else if (ObjectUtil.equals(fieldName,"DRIVER_ID")){
            return Collections.singletonList(user.getId());
        }
        return null;
    }
}
