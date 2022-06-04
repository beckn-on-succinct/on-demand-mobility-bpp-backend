package in.succinct.bpp.cabs.db.model.supply;

import com.venky.swf.db.annotations.model.MENU;
import com.venky.swf.plugins.collab.db.model.CompanyNonSpecific;

@MENU("Schedule")
public interface WorkCalendar extends com.venky.swf.plugins.calendar.db.model.WorkCalendar , CompanyNonSpecific {


}
