package in.succinct.bpp.cabs.extensions;

import com.venky.swf.plugins.collab.extensions.participation.CompanyNonSpecificParticipantExtension;
import in.succinct.bpp.cabs.db.model.routes.Route;

public class RouteParticipantExtension extends CompanyNonSpecificParticipantExtension<Route> {
    static{
        registerExtension(new RouteParticipantExtension());
    }

}
