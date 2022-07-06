package in.succinct.bpp.cabs.extensions;

import com.venky.swf.db.Database;
import com.venky.swf.db.extensions.AfterModelSaveExtension;

import com.venky.swf.plugins.security.db.model.Role;
import com.venky.swf.plugins.security.db.model.UserRole;
import in.succinct.bpp.cabs.db.model.supply.User;

public class AfterSaveUser extends AfterModelSaveExtension<User> {
    static {
        registerExtension(new AfterSaveUser());
    }
    @Override
    public void afterSave(User model) {
        Role role = Role.getRole("DRIVER");
        boolean isDriver = model.getUserRoles().stream().anyMatch(userRole -> userRole.getRoleId() == role.getId());
        if (!isDriver){
            UserRole userRole = Database.getTable(UserRole.class).newRecord();
            userRole.setRoleId(role.getId());
            userRole.setUserId(model.getId());
            userRole.save();
        }


    }
}
