package in.succinct.bpp.cabs.extensions;

import com.venky.swf.plugins.collab.extensions.participation.CompanyNonSpecificParticipantExtension;
import in.succinct.bpp.cabs.db.model.supply.WorkCalendar;

public class WorkCalendarParticipantExtension extends CompanyNonSpecificParticipantExtension<WorkCalendar> {
    static {
        registerExtension(new WorkCalendarParticipantExtension());
    }
}
