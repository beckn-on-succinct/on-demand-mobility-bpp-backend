package in.succinct.bpp.cabs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.ParticipantExtension;
import com.venky.swf.db.model.User;
import com.venky.swf.plugins.calendar.db.model.WorkCalendar;
import com.venky.swf.pm.DataSecurityFilter;
import in.succinct.bpp.cabs.db.model.supply.Vehicle;

import java.util.Arrays;
import java.util.List;

public class VehicleParticipantExtension extends ParticipantExtension<Vehicle> {
    static {
        registerExtension(new VehicleParticipantExtension());
    }
    @Override
    public List<Long> getAllowedFieldValues(User user, Vehicle partiallyFilledModel, String fieldName) {
        in.succinct.bpp.cabs.db.model.supply.User u = user.getRawRecord().getAsProxy(in.succinct.bpp.cabs.db.model.supply.User.class);;
        if (ObjectUtil.equals(fieldName,"CREATOR_USER_ID")){
            if (u.isStaff() ){
                if (u.getCompanyId()  != null){
                    return DataSecurityFilter.getIds(u.getCompany().getUsers());
                }else {
                    return null;
                }
            }else {
                return Arrays.asList(user.getId());
            }
        }
        return null;
    }
}
