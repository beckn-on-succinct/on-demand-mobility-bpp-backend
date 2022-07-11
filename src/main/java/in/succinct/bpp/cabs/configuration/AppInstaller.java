package in.succinct.bpp.cabs.configuration;

import com.venky.swf.configuration.Installer;
import com.venky.swf.db.Database;
import com.venky.swf.plugins.security.db.model.Role;
import com.venky.swf.sql.Select;
import in.succinct.bpp.cabs.db.model.supply.User;

import java.util.List;

public class AppInstaller implements Installer {

    public void install() {
        List<Role> roleList = new Select().from(Role.class).execute(1);
        if (roleList.isEmpty()){
            for (String allowedRole : User.ALLOWED_ROLES) {
              Role role = Database.getTable(Role.class).newRecord();
              role.setName(allowedRole);
              //role = Database.getTable(Role.class).getRefreshed(role);
              role.save();
            }
        }
    }
}

