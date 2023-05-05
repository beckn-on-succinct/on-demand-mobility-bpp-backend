package in.succinct.bpp.cabs.controller;

import com.venky.swf.controller.annotations.SingleRecordAction;
import com.venky.swf.db.Database;
import com.venky.swf.db.model.Model;
import com.venky.swf.db.model.reflection.ModelReflector;
import com.venky.swf.exceptions.AccessDeniedException;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.security.db.model.Role;
import com.venky.swf.plugins.security.db.model.UserRole;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import com.venky.swf.views.ForwardedView;
import com.venky.swf.views.View;
import in.succinct.bpp.cabs.db.model.supply.User;
import in.succinct.bpp.cabs.db.model.supply.UserSummary;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UsersController extends com.venky.swf.plugins.collab.controller.UsersController {
    public UsersController(Path path) {
        super(path);
    }


    @SingleRecordAction(icon = "fas fa-bolt")
    public View makeAgent(long id){
        User user = Database.getTable(User.class).get(id);
        if (user == null){
            throw new AccessDeniedException();
        }
        UserRole userRole = Database.getTable(UserRole.class).newRecord();
        userRole.setUserId(id);
        Role role = Role.getRole(User.ROLE_AGENT);
        userRole.setRoleId(role.getId());
        userRole = Database.getTable(UserRole.class).getRefreshed(userRole);
        if (userRole.getRawRecord().isNewRecord()) {
            userRole.save();
        }
        return new ForwardedView( getPath(),"user_roles","show/"+userRole.getId());
    }
    @Override
    protected String[] getIncludedFields() {
        Map<Class<? extends Model>, List<String>> map  = getIncludedModelFields();
        if (map.containsKey(User.class)){
            return map.get(User.class).toArray(new String[]{});
        }else {
            return null;
        }
    }

    @Override
    protected Map<Class<? extends Model>, List<String>> getIncludedModelFields() {
        Map<Class<? extends Model>, List<String>> map = super.getIncludedModelFields();
        if (!map.containsKey(User.class)) {
            map.put(User.class, ModelReflector.instance(User.class).getVisibleFields());
        }
        List<String> fields = getReflector().getFields();
        map.get(User.class).retainAll(fields);
        return map;
    }
}
