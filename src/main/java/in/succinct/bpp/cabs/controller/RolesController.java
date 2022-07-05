package in.succinct.bpp.cabs.controller;

import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;
import com.venky.swf.plugins.security.db.model.Role;
import com.venky.swf.views.View;

public class RolesController extends ModelController<Role> {
    public RolesController(Path path) {
        super(path);
    }

    @Override
    @RequireLogin(false)
    public View index() {
        return super.index();
    }
    
}
