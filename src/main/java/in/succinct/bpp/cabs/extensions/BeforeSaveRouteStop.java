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
            model.getRawRecord().load(NamedStop.find(model.getName()).getRawRecord());
        }
    }
}
