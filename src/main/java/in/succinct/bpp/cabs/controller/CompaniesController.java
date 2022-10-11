package in.succinct.bpp.cabs.controller;

import com.venky.swf.controller.ModelController;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;
import com.venky.swf.views.View;

public class CompaniesController extends ModelController<Company> {
    public CompaniesController(Path path) {
        super(path);
    }

    @Override
    @RequireLogin(false)
    public View index() {
        return super.index();
    }

    @Override
    @RequireLogin(false)
    public View view(long id) {
        return super.view(id);
    }
}
