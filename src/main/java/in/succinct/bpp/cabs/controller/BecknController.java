package in.succinct.bpp.cabs.controller;

import com.venky.core.util.ExceptionUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.geo.GeoCoordinate;
import com.venky.geo.GeoDistance;
import com.venky.swf.controller.Controller;
import com.venky.swf.controller.annotations.RequireLogin;
import com.venky.swf.db.Database;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.exceptions.AccessDeniedException;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.collab.db.model.config.City;
import com.venky.swf.plugins.collab.db.model.config.Country;
import com.venky.swf.plugins.collab.db.model.config.PinCode;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;
import com.venky.swf.plugins.collab.db.model.user.Email;
import com.venky.swf.plugins.collab.db.model.user.Phone;
import com.venky.swf.routing.Config;
import com.venky.swf.sql.Conjunction;
import com.venky.swf.sql.Expression;
import com.venky.swf.sql.Operator;
import com.venky.swf.sql.Select;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.beckn.Address;
import in.succinct.beckn.Agent;
import in.succinct.beckn.Billing;
import in.succinct.beckn.BreakUp;
import in.succinct.beckn.BreakUp.BreakUpElement;
import in.succinct.beckn.Catalog;
import in.succinct.beckn.Categories;
import in.succinct.beckn.Category;
import in.succinct.beckn.Contact;
import in.succinct.beckn.Context;
import in.succinct.beckn.Descriptor;
import in.succinct.beckn.Error;
import in.succinct.beckn.Error.Type;
import in.succinct.beckn.Fulfillment;
import in.succinct.beckn.FulfillmentStop;
import in.succinct.beckn.Fulfillments;
import in.succinct.beckn.Image;
import in.succinct.beckn.Images;
import in.succinct.beckn.Item;
import in.succinct.beckn.Items;
import in.succinct.beckn.Location;
import in.succinct.beckn.Locations;
import in.succinct.beckn.Message;
import in.succinct.beckn.Order;
import in.succinct.beckn.Person;
import in.succinct.beckn.Price;
import in.succinct.beckn.Provider;
import in.succinct.beckn.Providers;
import in.succinct.beckn.Quote;
import in.succinct.beckn.Request;
import in.succinct.beckn.Tags;
import in.succinct.beckn.Tracking;
import in.succinct.beckn.Vehicle;
import in.succinct.bpp.cabs.BecknUtil;
import in.succinct.bpp.cabs.db.model.demand.AllocationOption;
import in.succinct.bpp.cabs.db.model.demand.Trip;
import in.succinct.bpp.cabs.db.model.demand.TripStop;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BecknController extends Controller {

    public BecknController(Path path) {
        super(path);
    }

    private BecknUtil becknUtil = new BecknUtil();


    @RequireLogin(false)
    public View search(){
        return api();
    }
    @RequireLogin(false)
    public View select(){
        return api();
    }
    @RequireLogin(false)
    public View support(){
        return api();
    }


    @RequireLogin(false)
    public View init(){
        return api();
    }
    @RequireLogin(false)
    public View confirm(){
        return api();
    }
    @RequireLogin(false)
    public View status(){
        return api();
    }
    @RequireLogin(false)
    public View cancel(){
        return api();
    }

    @RequireLogin(false)
    public View track(){
        return api();
    }

    @RequireLogin(false)
    public View update(){
        return api();
    }

    public void validateProtocolServer(){
        String accelerator = Config.instance().getProperty("l1.accelerator");
        String token = Config.instance().getProperty(String.format("l1.%s.token",accelerator));
        String value = Config.instance().getProperty(String.format("l1.%s.value",accelerator));
        if (token != null && value !=null){
            if (!ObjectUtil.equals(getPath().getHeader(token),value)){
                throw  new AccessDeniedException("Unauthorized");
            }
        }
    }

    /** this is the api called by the protocol adaptor */
    @RequireLogin(false)
    public View api(){
        validateProtocolServer();
        Request reply = new Request();
        try  {
            JSONObject object = (JSONObject) JSONValue.parse(new InputStreamReader(getPath().getInputStream()));
            Request request = new Request(object); // Request parser;;

            Method method = becknUtil.getClass().getMethod(request.getContext().getAction(),Request.class,Request.class);
            //Based on action, we call the right method here.
            becknUtil.createReplyContext(request,reply);
            method.invoke(becknUtil,request,reply);
        }catch (Exception ex){
            Config.instance().getLogger(getClass().getName()).log(Level.WARNING,"BPP Failed" , ex);
            reply.rm("message");
            reply.setError(new Error());
            reply.getError().setCode(ExceptionUtil.getRootCause(ex).toString());
            reply.getError().setMessage(ExceptionUtil.getRootCause(ex).toString());
            reply.getError().setType(Type.CORE_ERROR);
            // Do nothing;
        }
        return new BytesView(getPath(),reply.toString().getBytes(StandardCharsets.UTF_8),MimeType.APPLICATION_JSON);

    }
    protected View no_content(){
        return new BytesView(getPath(),new byte[]{}, MimeType.APPLICATION_JSON){
            @Override
            public void write() throws IOException {
                super.write(HttpServletResponse.SC_NO_CONTENT);
            }
        };
    }
}
