package in.succinct.bpp.cabs.extensions;

import com.venky.core.util.ObjectUtil;
import com.venky.swf.db.extensions.BeforeModelSaveExtension;
import in.succinct.bpp.cabs.db.model.routes.RouteStop;
import in.succinct.bpp.cabs.db.model.routes.stops.NamedStop;

public class BeforeSaveRouteStop extends BeforeModelSaveExtension<RouteStop> {
    static {
        registerExtension(new BeforeSaveRouteStop());
    }
    @Override
    public void beforeSave(RouteStop model) {
        if (!ObjectUtil.isVoid(model.getName())){
            NamedStop stop = NamedStop.find(model.getName());
            if (stop != null) {
                model.getRawRecord().load(stop.getRawRecord());
            }
        }
    }
}
