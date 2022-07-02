package in.succinct.bpp.cabs.controller;

import com.venky.swf.controller.Controller;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.path.Path;
import com.venky.swf.views.View;

public class ServerController extends Controller {
    public ServerController(Path path) {
        super(path);
    }

    @RequireLogin(false)
    public View restart(){
        
        return back();
    }
}
