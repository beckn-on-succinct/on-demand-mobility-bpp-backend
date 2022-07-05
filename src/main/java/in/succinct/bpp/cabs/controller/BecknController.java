package in.succinct.bpp.cabs.controller;

import com.venky.core.string.StringUtil;
import com.venky.core.util.ObjectUtil;
import com.venky.geo.GeoCoordinate;
import com.venky.swf.controller.Controller;
import com.venky.swf.db.Database;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.collab.db.model.participants.admin.Company;
import com.venky.swf.routing.Config;
import com.venky.swf.views.BytesView;
import com.venky.swf.views.View;
import in.succinct.beckn.Catalog;
import in.succinct.beckn.Categories;
import in.succinct.beckn.Category;
import in.succinct.beckn.Context;
import in.succinct.beckn.Descriptor;
import in.succinct.beckn.Fulfillment;
import in.succinct.beckn.FulfillmentStop;
import in.succinct.beckn.Fulfillments;
import in.succinct.beckn.Item;
import in.succinct.beckn.Items;
import in.succinct.beckn.Location;
import in.succinct.beckn.Locations;
import in.succinct.beckn.Price;
import in.succinct.beckn.Provider;
import in.succinct.beckn.Providers;
import in.succinct.beckn.Request;
import in.succinct.beckn.Tags;
import in.succinct.beckn.Vehicle;
import in.succinct.bpp.cabs.db.model.demand.Trip;
import in.succinct.bpp.cabs.db.model.demand.TripStop;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.tag.Tag;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BecknController extends Controller {
    public BecknController(Path path) {
        super(path);
    }
    public void search(Request request,Request reply){

        Location start = request.getMessage().getIntent().getFulfillment().getStart().getLocation();
        Location end = request.getMessage().getIntent().getFulfillment().getEnd().getLocation();

        Trip trip = Database.getTable(Trip.class).newRecord();
        trip.setScheduledStart(new Timestamp(System.currentTimeMillis()));
        Tags tags = request.getMessage().getIntent().getTags();
        if (tags != null){
            SortedSet<String> tagSet = new TreeSet<>();
            for (Object k : tags.getInner().keySet()){
                boolean value = Database.getJdbcTypeHelper("").getTypeRef(boolean.class).getTypeConverter().valueOf(tags.get(k.toString()));
                if (value){
                    tagSet.add(k.toString());
                }
            }
            StringBuilder sTags = new StringBuilder();
            tagSet.forEach(t->{
                if (sTags.length() > 0){
                    sTags.append(",");
                }
                sTags.append(t);
            });
            trip.setVehicleTags(sTags.toString());
        }
        Category category = request.getMessage().getIntent().getCategory();
        if (category != null){
            DeploymentPurpose purpose = Database.getTable(DeploymentPurpose.class).newRecord();
            purpose.setName(category.getDescriptor().getName());
            purpose = Database.getTable(DeploymentPurpose.class).getRefreshed(purpose);
            if (!purpose.getRawRecord().isNewRecord()){
                trip.setDeploymentPurposeId(purpose.getId());
            }
            if (ObjectUtil.isVoid(category.getId())){
                category.setId(getBecknId(purpose.getId(),Entity.category,reply.getContext()));
            }
        }

        trip.setStatus(Trip.UnConfirmed);
        trip.save();
        TripStop startStop = Database.getTable(TripStop.class).newRecord();
        startStop.setTripId(trip.getId());
        startStop.setName("Start");
        startStop.setSequenceNumber(0);
        startStop.setLat(start.getGps().getLat());
        startStop.setLng(start.getGps().getLat());
        startStop.save();

        TripStop endStop = Database.getTable(TripStop.class).newRecord();
        endStop.setTripId(trip.getId());
        endStop.setName("End");
        endStop.setSequenceNumber(1);
        endStop.setLat(end.getGps().getLat());
        endStop.setLng(end.getGps().getLat());
        endStop.save();

        trip.rate();
        trip.allocate();

        Catalog catalog = new Catalog();
        reply.getMessage().setCatalog(catalog);
        catalog.setDescriptor( new Descriptor());
        Descriptor d = catalog.getDescriptor();
        d.setCode(Config.instance().getHostName());
        d.setName(d.getCode());

        Provider provider = new Provider();
        catalog.setProviders(new Providers());
        catalog.getProviders().add(provider);
        provider.setDescriptor(new Descriptor());
        provider.setId(getBecknId(trip.getDriverLoginId(),Entity.provider,reply.getContext()));
        provider.getDescriptor().setName(trip.getDriverLogin().getAuthorizedDriver().getDriver().getName());
        provider.setLocations(new Locations());
        Location location = new Location();
        location.setId(getBecknId(trip.getDriverLoginId(),Entity.provider_location,reply.getContext()));
        location.setGps(new GeoCoordinate(trip.getDriverLogin().getLat(),trip.getDriverLogin().getLng()));
        provider.getLocations().add(location);

        Categories categories = new Categories();
        provider.setCategories(categories);
        Category outCategory = new Category();
        categories.add(outCategory);
        outCategory.setId(getBecknId(trip.getDeploymentPurposeId(),Entity.category,reply.getContext()));
        outCategory.setDescriptor(new Descriptor());
        outCategory.getDescriptor().setName(trip.getDeploymentPurpose().getName());
        outCategory.getDescriptor().setCode(outCategory.getDescriptor().getName());


        provider.setItems(new Items());
        Item item = new Item();
        item.setLocationId(location.getId());
        item.setCategoryId(category.getId());
        item.setDescriptor(new Descriptor());
        item.setId(getBecknId(trip.getDriverLoginId(),Entity.item,reply.getContext()));
        item.setPrice(new Price());
        item.getPrice().setCurrency("INR");
        item.getPrice().setValue(trip.getSellingPrice());

        StringBuilder itemName = new StringBuilder();
        itemName.append(trip.getDeploymentPurpose().getName());

        for (String tag : trip.getDriverLogin().getAuthorizedDriver().getVehicle().getTagSet()) {
            itemName.append("-").append(tag);
        }
        item.getDescriptor().setName(itemName.toString());


        Fulfillment fulfillment = new Fulfillment();
        catalog.setFulfillments(new Fulfillments());
        catalog.getFulfillments().add(fulfillment);
        fulfillment.setVehicle(new Vehicle());
        fulfillment.getVehicle().setRegistration(trip.getDriverLogin().getAuthorizedDriver().getVehicle().getVehicleNumber());
        fulfillment.setId(getBecknId(trip.getId(),Entity.fulfillment,reply.getContext()));

    }

    public static String getIdPrefix(){
        return "./mobility/ind.blr/";
    }

    public static String getIdSuffix(Context context){
        return context.getBppId();
    }
    public enum Entity {
        fulfillment,
        category,
        provider,
        provider_category,
        provider_location,
        item,
        catalog,
        cancellation_reason,
        return_reason,
        order
    }
    public static String getBecknId(Long localUniqueId,Entity becknEntity, Context context){
        return getBecknId(String.valueOf(localUniqueId),becknEntity,context);
    }
    public static String getBecknId(String localUniqueId,Entity becknEntity,Context context){
        return getBecknId(getIdPrefix(),localUniqueId, getIdSuffix(context), becknEntity);
    }
    public static String getLocalUniqueId(String beckId, Entity becknEntity) {
        String pattern = "^(.*/)(.*)@(.*)\\." + becknEntity + "$";
        Matcher matcher = Pattern.compile(pattern).matcher(beckId);
        if (matcher.find()){
            return matcher.group(2);
        }
        return "-1";
    }
    public static String getBecknId(String prefix, String localUniqueId, String suffix , Entity becknEntity){
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        if (!ObjectUtil.isVoid(localUniqueId)){
            builder.append(localUniqueId);
        }else {
            builder.append(0);
        }
        builder.append("@");
        builder.append(suffix);
        if (becknEntity != null){
            builder.append(".").append(becknEntity);
        }
        return builder.toString();
    }

    private void createReplyContext(Request from , Request to){
        Context newContext = ObjectUtil.clone(from.getContext());
        String  action = from.getContext().getAction();

        if (action.startsWith("get_")){
            newContext.setAction(action.substring(4));
        }else {
            newContext.setAction("on_" + from.getContext().getAction());
        }
        to.setContext(newContext);
    }

    /** this is the api called by the protocol adaptor */
    public View api(){
        try  {
            JSONObject object = (JSONObject) JSONValue.parse(new InputStreamReader(getPath().getInputStream()));
            Request request = new Request(object); // Request parser;;

            Method method = getClass().getMethod(request.getContext().getAction(),Request.class,Request.class);
            //Based on action, we call the right method here.
            Request reply = new Request();
            createReplyContext(request,reply);
            method.invoke(this,request,reply);
            return new BytesView(getPath(),reply.toString().getBytes(StandardCharsets.UTF_8),MimeType.APPLICATION_JSON);
        }catch (Exception ex){
            // Do nothing;
        }
        return no_content();

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
