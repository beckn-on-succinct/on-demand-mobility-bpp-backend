package in.succinct.bpp.cabs.controller;

import com.venky.core.util.ObjectUtil;
import com.venky.geo.GeoCoordinate;
import com.venky.swf.controller.Controller;
import com.venky.swf.db.Database;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
import com.venky.swf.db.annotations.column.ui.mimes.MimeType;
import com.venky.swf.path.Path;
import com.venky.swf.plugins.collab.db.model.config.City;
import com.venky.swf.plugins.collab.db.model.config.Country;
import com.venky.swf.plugins.collab.db.model.config.PinCode;
import com.venky.swf.plugins.collab.db.model.config.State;
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
import in.succinct.beckn.Billing;
import in.succinct.beckn.BreakUp;
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
import in.succinct.beckn.Message;
import in.succinct.beckn.Order;
import in.succinct.beckn.Price;
import in.succinct.beckn.Provider;
import in.succinct.beckn.Providers;
import in.succinct.beckn.Quote;
import in.succinct.beckn.Request;
import in.succinct.beckn.Tags;
import in.succinct.beckn.Vehicle;
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
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BecknController extends Controller {
    public BecknController(Path path) {
        super(path);
    }


    private Trip getTrip(String fulfilmentId,boolean confirm){
        String tripId = getLocalUniqueId(fulfilmentId,Entity.fulfillment);
        Trip trip = Database.getTable(Trip.class).get(Long.parseLong(tripId));
        if (trip!= null ){
            trip.setStatus(Trip.Confirmed);
            trip.save();
        }
        return trip;
    }

    public void setProvider(Trip trip, Provider provider, Context context){
        Company company = trip.getDriverLogin().getAuthorizedDriver().getCreatorUser().getRawRecord().getAsProxy(User.class).getCompany();
        provider.setId(getBecknId(company.getId(),Entity.provider,context));
        provider.setDescriptor(new Descriptor());
        provider.getDescriptor().setName(company.getName());
    }
    public void setCategories(Trip trip, Provider provider,Context context){
        provider.setCategories(new Categories());
        Category category = new Category();
        category.setId(getBecknId(trip.getDeploymentPurposeId(),Entity.category,context));
        category.setDescriptor(new Descriptor());
        category.getDescriptor().setName(trip.getDeploymentPurpose().getName());
    }
    public void setItems(Trip trip, Provider provider,Context context){
        provider.setItems(new Items());
        Item item = new Item();
        item.setCategoryId(provider.getCategories().get(0).getId());
        item.setId(getBecknId(trip.getDriverLoginId(),Entity.item,context));
        item.setDescriptor(new Descriptor());
        StringBuilder itemName = new StringBuilder();
        itemName.append(trip.getDeploymentPurpose().getName());

        for (String tag : trip.getDriverLogin().getAuthorizedDriver().getVehicle().getTagSet()) {
            itemName.append("-").append(tag);
        }

        item.getDescriptor().setName(itemName.toString());
        item.getDescriptor().setCode(itemName.toString());
        item.setPrice(new Price());
        item.getPrice().setCurrency("INR");
        item.getPrice().setValue(trip.getSellingPrice());

        item.setFulfillmentId(getBecknId(trip.getId(),Entity.fulfillment,context));
    }
    public void setProviderLocations(Trip trip, Provider provider, Context context){
        provider.setLocations(new Locations());
        Location location = new Location();
        provider.getLocations().add(location);
        DriverLogin driverLogin = trip.getDriverLogin();
        location.setGps(new GeoCoordinate(driverLogin.getLat(),driverLogin.getLng()));
        location.setId(getBecknId(driverLogin.getId(),Entity.provider_location,context));
    }
    public void setFulfillment(@NotNull Trip trip, Order order,Context context){

        Fulfillment fulfillment = new Fulfillment();
        order.setFulfillment(fulfillment);
        setFulfillment(trip,fulfillment,context);
    }
    public void setFulfillment(Trip trip,Fulfillment fulfillment,Context context){
        FulfillmentStop start = new FulfillmentStop();
        FulfillmentStop end = new FulfillmentStop();
        fulfillment.setStart(start);
        fulfillment.setEnd(end);

        List<TripStop> stops = trip.getTripStops();

        start.setLocation(new Location());
        end.setLocation(new Location());
        start.getLocation().setGps(new GeoCoordinate(stops.get(0)));
        end.getLocation().setGps(new GeoCoordinate(stops.get(stops.size()-1)));

        Vehicle vehicle = new Vehicle();
        fulfillment.setVehicle(vehicle);
        vehicle.setRegistration(trip.getDriverLogin().getAuthorizedDriver().getVehicle().getVehicleNumber());
        fulfillment.setId(getBecknId(trip.getId(),Entity.fulfillment,context));

    }
    public void setFulfillment(@NotNull Trip trip, Catalog catalog,Context context){
        Fulfillment fulfillment = new Fulfillment();
        catalog.setFulfillments(new Fulfillments());
        catalog.getFulfillments().add(fulfillment);
        setFulfillment(trip,fulfillment,context);
    }
    public void setQuote(Trip trip, Order order, Context context) {
        if (ObjectUtil.equals(trip.getStatus(),Trip.UnConfirmed)){
            Quote quote = new Quote();
            order.setQuote( quote);
            quote.setPrice(new Price());
            quote.getPrice().setValue(trip.getSellingPrice());
            quote.getPrice().setCurrency("INR");
            BreakUp breakUp = new BreakUp();
            quote.setBreakUp(breakUp);
            Price price = new Price();
            breakUp.createElement("item","Fare",price);
            price.setCurrency("INR");
            price.setValue(trip.getPrice());

            Price tax = new Price();
            breakUp.createElement("item","Tax",tax);
            TypeConverter<Double> typeConverter = trip.getReflector().getJdbcTypeHelper().getTypeRef(double.class).getTypeConverter();

            tax.setValue(typeConverter.valueOf(trip.getCGst()) +typeConverter.valueOf(trip.getIGst()) + typeConverter.valueOf((trip.getSGst())));
        }
    }

    public Order getBecknOrder(Trip trip, Request reply){
        Order order = new Order();
        Provider provider = new Provider();
        order.setProvider(provider);
        setProvider(trip,provider,reply.getContext());
        setCategories(trip,provider,reply.getContext());
        setItems(trip,provider,reply.getContext());
        setProviderLocations(trip,provider,reply.getContext());
        setFulfillment(trip,order,reply.getContext());
        if (ObjectUtil.equals(trip.getStatus(),Trip.UnConfirmed)){
            setQuote(trip,order,reply.getContext());
        }

        return order;
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
        setProvider(trip,provider,reply.getContext());
        setProviderLocations(trip,provider,reply.getContext());
        setCategories(trip,provider,reply.getContext());
        setItems(trip,provider,reply.getContext());
        setFulfillment(trip,catalog,reply.getContext());
    }
    public void select(Request request,Request reply){
        Order order = request.getMessage().getOrder();
        Trip trip = getTrip(order.getFulfillment().getId(),false);
        Order tripOrder = getBecknOrder(trip,reply);
        reply.setMessage(new Message());
        reply.getMessage().setOrder(tripOrder);
    }
    public User ensurePassenger(in.succinct.beckn.User passenger){
        Select select = new Select().from(User.class);
        Expression where = new Expression(select.getPool(), Conjunction.AND);
        if (!ObjectUtil.isVoid(passenger.getContact().getEmail())){
            Email.validate(passenger.getContact().getEmail());
            where.add(new Expression(select.getPool(), "EMAIL", Operator.EQ, passenger.getContact().getEmail()));
        }
        if (!ObjectUtil.isVoid(passenger.getContact().getPhone())){
            where.add(new Expression(select.getPool(), "PHONE_NUMBER", Operator.EQ, Phone.sanitizePhoneNumber(passenger.getContact().getPhone())));
        }
        List<User> users = select.execute();
        if (users.isEmpty()){
            User user = Database.getTable(User.class).newRecord();
            user.setLongName(passenger.getPerson().getName());
            user.setPhoneNumber(Phone.sanitizePhoneNumber(passenger.getContact().getPhone()));
            user.setEmail(passenger.getContact().getEmail());
            user.setName(user.getPhoneNumber());
            user.save();
            return user;
        }else {
            return users.get(0);
        }
    }
    public User ensureBiller(Billing billing){
        Select select = new Select().from(User.class);
        Expression where = new Expression(select.getPool(), Conjunction.AND);
        if (!ObjectUtil.isVoid(billing.getEmail())){
            Email.validate(billing.getEmail());
            where.add(new Expression(select.getPool(), "EMAIL", Operator.EQ, billing.getEmail()));
        }
        if (!ObjectUtil.isVoid(billing.getPhone())){
            where.add(new Expression(select.getPool(), "PHONE_NUMBER", Operator.EQ, Phone.sanitizePhoneNumber(billing.getPhone())));
        }
        List<User> users = select.execute();
        User user = null;
        if (users.isEmpty()){
            user = Database.getTable(User.class).newRecord();
            user.setLongName(billing.getName());
            user.setPhoneNumber(Phone.sanitizePhoneNumber(billing.getPhone()));
            user.setEmail(billing.getEmail());
            user.setName(user.getPhoneNumber());
        }else {
            user = users.get(0);
        }
        Address address = billing.getAddress();
        user.setAddressLine1(address.getName());
        user.setAddressLine2(address.getDoor() + "," + address.getBuilding());
        user.setAddressLine3(address.getLocality());
        user.setCountryId(Country.findByISO(address.getCountry()).getId());
        user.setCityId(Objects.requireNonNull(City.findByCode(address.getCity())).getId());
        user.setPinCodeId(Objects.requireNonNull(PinCode.find(address.getPinCode())).getId());
        user.save();
        return user;
    }

    public void init(Request request,Request reply){
        Order order = request.getMessage().getOrder();
        Trip trip = getTrip(order.getFulfillment().getId(),false);
        in.succinct.beckn.User passenger = order.getFulfillment().getCustomer();
        User user = ensurePassenger(passenger);
        Billing billing = order.getBilling();
        User biller = ensureBiller(billing);


        Order tripOrder = getBecknOrder(trip,reply);
        reply.setMessage(new Message());
        reply.getMessage().setOrder(tripOrder);
    }

    public void confirm(Request request, Request reply){
        Order order = request.getMessage().getOrder();
        Trip trip = getTrip(order.getFulfillment().getId(),true);
        Order tripOrder = getBecknOrder(trip,reply);
        reply.setMessage(new Message());
        reply.getMessage().setOrder(tripOrder);
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
