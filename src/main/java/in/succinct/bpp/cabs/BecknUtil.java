package in.succinct.bpp.cabs;

import com.venky.core.util.ObjectUtil;
import com.venky.geo.GeoCoordinate;
import com.venky.geo.GeoDistance;
import com.venky.swf.db.Database;
import com.venky.swf.db.JdbcTypeHelper.TypeConverter;
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
import in.succinct.beckn.Fulfillment;
import in.succinct.beckn.FulfillmentStop;
import in.succinct.beckn.Fulfillments;
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
import in.succinct.bpp.cabs.db.model.demand.AllocationOption;
import in.succinct.bpp.cabs.db.model.demand.Trip;
import in.succinct.bpp.cabs.db.model.demand.TripStop;
import in.succinct.bpp.cabs.db.model.supply.DeploymentPurpose;
import in.succinct.bpp.cabs.db.model.supply.DriverLogin;
import in.succinct.bpp.cabs.db.model.supply.User;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BecknUtil {
    private static final String BECKN_UPDATE_ERROR_MSG="Update not supported here, please Cancel and Select";

    private Trip getTrip(String fulfilmentId, boolean confirm){
        String tripId = getLocalUniqueId(fulfilmentId,Entity.fulfillment);
        Trip trip = Database.getTable(Trip.class).get(Long.parseLong(tripId));
        if (trip!= null ){
            if (confirm) {
                trip.setStatus(Trip.Confirmed);
                trip.allocate();
            }
        }

        return trip;
    }
    private Trip getTripFromOrderId(String orderId){
        String tripId = getLocalUniqueId(orderId,Entity.order);
        return Database.getTable(Trip.class).get(Long.parseLong(tripId));
    }
    public void setProvider(Trip trip, Provider provider, Context context, Company company) {
        provider.setId(getBecknId(company.getId(), Entity.provider, context));
        provider.setDescriptor(new Descriptor());
        provider.getDescriptor().setName(company.getName());
        Images images = new Images();
        provider.getDescriptor().setImages(images);
        images.add(Config.instance().getServerBaseUrl()+"/companies/view/"+company.getId());
    }
    public void setProviders(Trip trip, Providers providers, Context context, AllocationOption ao) {
        Provider provider = new Provider();

        User driver = ao.getDriverLogin().getAuthorizedDriver().getDriver();
        Company company = driver.getCompany();
        if (company == null) {
            List<Company> companies = driver.getRawRecord().getAsProxy(User.class).getCompanies();
            if (companies.size() > 0) {
                company = companies.get(0);
            }
        }
        if (company != null){
            setProvider(trip,provider,context,company);
            if (providers.get(provider.getId()) == null) {
                providers.add(provider);
            }

            setProviderLocations(trip,provider,context,ao);
            setCategories(trip,provider,context,ao);
            setItems(trip,provider,context,ao);
        }
    }
    public void setCategories(Trip trip, Provider provider,Context context){
        setCategories(trip,provider,context,null);
    }
    public void setCategories(Trip trip, Provider provider,Context context,AllocationOption ao){
        Categories categories = provider.getCategories();
        if (categories == null){
            categories = new Categories();
            provider.setCategories(categories);
        }
        Category category = new Category();
        DeploymentPurpose deploymentPurpose = null;
        if (ao == null){
            deploymentPurpose = trip.getDeploymentPurpose();
        }else {
            deploymentPurpose = ao.getDeploymentPurpose();
        }
        category.setId(getBecknId(deploymentPurpose.getId(),Entity.category,context));
        category.setDescriptor(new Descriptor());
        category.getDescriptor().setName(deploymentPurpose.getName());
        categories.add(category);
    }
    public void setItems(Trip trip, Provider provider,Context context){
        setItems(trip,provider,context,null);
    }
    public void setItems(Trip trip, Provider provider,Context context,AllocationOption ao){
        Items items = provider.getItems();
        if (items == null) {
            items = new Items();
            provider.setItems(items);
        }
        Item item = new Item();
        item.setDescriptor(new Descriptor());
        item.setPrice(new Price());
        item.getPrice().setCurrency("INR");
        item.setFulfillmentId(getBecknId(trip.getId(),Entity.fulfillment,context));

        DriverLogin login = null;
        DeploymentPurpose purpose = null;
        if (ao != null) {
            login =ao.getDriverLogin();
            purpose = ao.getDeploymentPurpose();
            item.getPrice().setValue(ao.getSellingPrice());
        }else {
            login = trip.getDriverLogin();
            purpose = trip.getDeploymentPurpose();
            item.getPrice().setValue(trip.getSellingPrice());
        }
        item.setCategoryId(getBecknId(purpose.getId(), Entity.category, context));

        StringBuilder itemName = new StringBuilder();
        itemName.append(purpose.getName());
        if (!ObjectUtil.isVoid(trip.getVehicleTags())) {
            itemName.append("-").append(trip.getVehicleTags());
        }

        item.setId(getBecknId(login.getId(), Entity.item, context));
        item.getDescriptor().setName(itemName.toString());
        item.getDescriptor().setCode(itemName.toString());
        if (!ObjectUtil.isVoid(purpose.getImageUrl())){
            Images images = new Images();
            item.getDescriptor().setImages(images);
            images.add(Config.instance().getServerBaseUrl() + "/" + purpose.getImageUrl());
        }
        Tags tags = new Tags();
        for (String tag : login.getAuthorizedDriver().getVehicle().getTagSet()) {
            String[] splits = tag.split(":");
            if (splits.length == 2) {
                tags.set(splits[0], splits[1]);
            }
        }
        item.setTags(tags);
        items.add(item);
    }
    public void setProviderLocations(Trip trip, Provider provider, Context context){
        setProviderLocations(trip,provider,context,null);
    }
    public void setProviderLocations(Trip trip, Provider provider, Context context,AllocationOption ao){
        Locations locations = provider.getLocations();
        if (locations == null){
            locations = new Locations();
            provider.setLocations(locations);
        }
        Location location = new Location();
        DriverLogin driverLogin = null;
        if (ao == null) {
            driverLogin = trip.getDriverLogin();
        }else {
            driverLogin = ao.getDriverLogin();
        }

        location.setGps(new GeoCoordinate(driverLogin.getLat(), driverLogin.getLng()));
        location.setId(getBecknId(driverLogin.getId(), Entity.provider_location, context));
        locations.add(location);
    }
    public void setFulfillment(@NotNull Trip trip, Order order, Context context){

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


        if (trip.getDriverLoginId() != null ) {
            Vehicle vehicle = new Vehicle();
            fulfillment.setVehicle(vehicle);
            vehicle.setRegistration(trip.getDriverLogin().getAuthorizedDriver().getVehicle().getVehicleNumber());
            fulfillment.setId(getBecknId(trip.getId(),Entity.fulfillment,context));

            User driver = trip.getDriver();

            Agent agent = new Agent();
            fulfillment.setAgent(agent);
            agent.setName(driver.getLongName());
            agent.setPhone(driver.getPhoneNumber());
        }

    }
    public void setFulfillment(@NotNull Trip trip, Catalog catalog, Context context){
        Fulfillment fulfillment = new Fulfillment();
        catalog.setFulfillments(new Fulfillments());
        catalog.getFulfillments().add(fulfillment);
        setFulfillment(trip,fulfillment,context);
    }
    public void setQuote(Trip trip, Order order, Context context) {
        Quote quote = new Quote();
        order.setQuote( quote);
        quote.setPrice(new Price());
        quote.getPrice().setValue(trip.getSellingPrice());
        quote.getPrice().setCurrency("INR");
        BreakUp breakUp = new BreakUp();
        quote.setBreakUp(breakUp);

        Price price = new Price();
        BreakUpElement fareBreakup = breakUp.createElement("item","Fare",price);
        price.setCurrency("INR");
        price.setValue(trip.getPrice());

        Price tax = new Price();
        BreakUpElement taxBreakup = breakUp.createElement("item","Tax",tax);
        TypeConverter<Double> typeConverter = trip.getReflector().getJdbcTypeHelper().getTypeRef(double.class).getTypeConverter();
        tax.setValue(typeConverter.valueOf(trip.getCGst()) +typeConverter.valueOf(trip.getIGst()) + typeConverter.valueOf((trip.getSGst())));
        tax.setCurrency("INR");
        breakUp.add(fareBreakup);
        breakUp.add(taxBreakup);
    }

    public Order getBecknOrder(Trip trip, Request reply){
        Order order = new Order();
        order.setId(getBecknId(trip.getId(),Entity.order,reply.getContext()));
        order.setState(trip.getDisplayStatus());

        if (trip.getDriverLoginId() != null) {
            Provider provider = new Provider();
            setProvider(trip, provider, reply.getContext(), trip.getDriver().getCompany());
            order.setProvider(provider);

            setCategories(trip, provider, reply.getContext());
            setItems(trip, provider, reply.getContext());
            order.setItems(provider.getItems());
            setProviderLocations(trip, provider, reply.getContext());
            setFulfillment(trip, order, reply.getContext());
            setCustomer(trip, order, reply.getContext());
            setBilled(trip, order, reply.getContext());
            setQuote(trip, order, reply.getContext());
        }
        return order;
    }

    private void setBilled(Trip trip, Order order, Context context) {
        if (trip.getPayerId() == null){
            return;
        }
        Billing billing = new Billing();
        Address address = new Address();
        billing.setAddress(address);
        order.setBilling(billing);

        User payer = trip.getPayer();
        billing.setEmail(payer.getEmail());
        billing.setPhone(payer.getPhoneNumber());
        billing.setName(payer.getLongName());
        if (payer.getPinCodeId() != null) {
            address.setAreaCode(payer.getPinCode().getPinCode());
        }
        if (payer.getCityId() != null) {
            address.setCity(payer.getCity().getCode());
        }
        if (payer.getCountryId() != null){
            address.setCountry(payer.getCountry().getIsoCode());
        }
        if (payer.getAddressLine1() != null) {
            address.setName(payer.getAddressLine1());
        }
        if (payer.getAddressLine2() != null) {
            String[] line2 = payer.getAddressLine2().split(",");
            address.setDoor(line2[0]);
            address.setBuilding(payer.getAddressLine2().substring(line2[0].length()));
        }
        if (payer.getAddressLine3()  != null) {
            address.setLocality(payer.getAddressLine3());
        }
    }

    private void setCustomer(Trip trip, Order order, Context context) {
        if (trip.getPassengerId() == null){
            return;
        }
        in.succinct.beckn.User customer = new in.succinct.beckn.User();
        order.getFulfillment().setCustomer(customer);
        customer.setContact(new Contact());
        customer.setPerson(new Person());
        Contact contact =customer.getContact();
        contact.setEmail(trip.getPassenger().getEmail());
        contact.setPhone(trip.getPassenger().getPhoneNumber());

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
                tagSet.add(String.format("%s:%s",k.toString(),tags.get(k.toString())));
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
        trip.setBapId(request.getContext().getBapId());
        trip.setTransactionId(request.getContext().getTransactionId());

        trip.setStatus(Trip.UnConfirmed);
        trip.save();
        TripStop startStop = Database.getTable(TripStop.class).newRecord();
        startStop.setTripId(trip.getId());
        startStop.setName("Start");
        startStop.setSequenceNumber(0);
        startStop.setLat(start.getGps().getLat());
        startStop.setLng(start.getGps().getLng());
        startStop.setMinutesFromLastStop(0);
        startStop.setDistanceFromLastStop(0.0);
        startStop.setEstimatedMinutesFromLastStop(0);
        startStop.save();

        TripStop endStop = Database.getTable(TripStop.class).newRecord();
        endStop.setTripId(trip.getId());
        endStop.setName("End");
        endStop.setSequenceNumber(1);
        endStop.setLat(end.getGps().getLat());
        endStop.setLng(end.getGps().getLng());
        endStop.setDistanceFromLastStop(GeoDistance.getDrivingDistanceKms(startStop.getLat(),startStop.getLng(), endStop.getLat(), endStop.getLng(),Config.instance().getGeoProviderParams()));
        endStop.setEstimatedMinutesFromLastStop((int)Math.ceil(endStop.getDistanceFromLastStop() * 60 /40)); //TODO 40 km/hr average hardcoded!! Need tfix this.(May be with analytics)
        endStop.save();

        trip.loadAllocationOptions();

        reply.setMessage(new Message());
        Catalog catalog = new Catalog();
        reply.getMessage().setCatalog(catalog);
        catalog.setDescriptor( new Descriptor());
        Descriptor d = catalog.getDescriptor();
        d.setCode(Config.instance().getHostName());
        d.setName(d.getCode());


        catalog.setProviders(new Providers());

        trip.getAllocationOptions().forEach(ao-> {
            setProviders(trip, catalog.getProviders(), reply.getContext(),ao);
        });
        setFulfillment(trip,catalog,reply.getContext());
    }
    public void support(Request request,Request reply){
        reply.setMessage(new Message());
        reply.getMessage().setEmail("support@"+ getFQDomainName(Config.instance().getHostName()));
    }
    public static String getFQDomainName(String domainName) {
        List<String> domainParts = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(domainName,".");
        while (tok.hasMoreTokens()){
            domainParts.add(tok.nextToken());
        }
        while (domainParts.size() > 2){
            domainParts.remove(0);
        }
        StringBuilder fQdomainName = new StringBuilder();
        for (String part: domainParts){
            if(fQdomainName.length() > 0){
                fQdomainName.append(".");
            }
            fQdomainName.append(part);
        }
        return fQdomainName.toString();

    }
    public void select(Request request,Request reply){
        Order order = request.getMessage().getOrder();
        Trip trip = getTrip(order.getFulfillment().getId(),false);
        if (order.getItems().size() >0){
            Item item = order.getItems().get(0);
            Long deploymentPurposeId = Long.parseLong(getLocalUniqueId(item.getCategoryId(),Entity.category));
            Long driverLoginId = Long.parseLong(getLocalUniqueId(item.getId(),Entity.item));

            Optional<AllocationOption> oao = trip.getAllocationOptions().stream().filter(allocationOption ->
                    ObjectUtil.equals(allocationOption.getDeploymentPurposeId(),deploymentPurposeId) &&
                            ObjectUtil.equals(driverLoginId,allocationOption.getDriverLoginId())).findFirst();

            AllocationOption ao = oao.get();

            trip.setDeploymentPurposeId(ao.getDeploymentPurposeId()); //Set Deployment purpose
            trip.setDriverLoginId(ao.getDriverLoginId()); //Set Login
            trip.setVehicleTags(ao.getDriverLogin().getAuthorizedDriver().getVehicle().getTags());
            trip.setPrice(ao.getPrice());
            trip.setSellingPrice(ao.getSellingPrice());
            trip.setCGst(ao.getCGst());
            trip.setSGst(ao.getSGst());
            trip.setIGst(ao.getIGst());
            trip.save();
        }

        Order tripOrder = getBecknOrder(trip,reply);
        reply.setMessage(new Message());
        reply.getMessage().setOrder(tripOrder);
    }
    public void status(Request request,Request reply){
        Message message = request.getMessage();
        Trip trip = getTripFromOrderId(message.get("order_id"));
        Order tripOrder = getBecknOrder(trip,reply);
        reply.setMessage(new Message());
        reply.getMessage().setOrder(tripOrder);
    }
    public void track(Request request,Request reply){
        Message message = request.getMessage();
        Trip trip = getTripFromOrderId(message.get("order_id"));

        reply.setMessage(new Message());
        Tracking tracking = new Tracking();
        reply.getMessage().setTracking(tracking);
        tracking.setStatus(ObjectUtil.equals(trip.getStatus(),Trip.Started)? "active" : "inactive");
        tracking.setUrl(Config.instance().getServerBaseUrl()+"/trips/location/"+trip.getId());
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
        List<User> users = select.where(where).execute();
        if (users.isEmpty()){
            User user = Database.getTable(User.class).newRecord();
            user.setLongName(passenger.getPerson().getName());
            user.setPhoneNumber(Phone.sanitizePhoneNumber(passenger.getContact().getPhone()));
            user.setEmail(passenger.getContact().getEmail());
            user.setName(user.getPhoneNumber());
            user = Database.getTable(User.class).getRefreshed(user);
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
        List<User> users = select.where(where).execute();
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
        if (ObjectUtil.isVoid(address.getName())) {
            address.setName(billing.getName());
        }
        user.setAddressLine1(address.getName());
        user.setAddressLine2(address.getDoor() + "," + address.getBuilding());
        user.setAddressLine3(address.getLocality());
        user.setCountryId(Country.findByISO(address.getCountry()).getId());
        user.setCityId(Objects.requireNonNull(City.findByCode(address.getCity())).getId());
        user.setPinCodeId(Objects.requireNonNull(PinCode.find(address.getPinCode())).getId());
        user = Database.getTable(User.class).getRefreshed(user);
        user.save();
        return user;
    }

    public void init(Request request,Request reply){
        Order order = request.getMessage().getOrder();
        Trip trip = getTrip(order.getFulfillment().getId(),false);

        in.succinct.beckn.User passenger = order.getFulfillment().getCustomer();
        User user = ensurePassenger(passenger);
        trip.setPassengerId(user.getId());



        Billing billing = order.getBilling();
        User biller = ensureBiller(billing);
        trip.setPayerId(biller.getId());
        trip.save();

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

    public void cancel(Request request,Request reply){
        Message message = request.getMessage();
        Trip trip = getTripFromOrderId(message.get("order_id"));
        if (trip!= null){
            trip.cancel();
        }
        Order tripOrder = getBecknOrder(trip,reply);
        reply.setMessage(new Message());
        reply.getMessage().setOrder(tripOrder);
    }

    public void update(Request request,Request reply){
        Message message = request.getMessage();
        Trip trip = getTripFromOrderId(message.get("order_id"));
        Order tripOrder = getBecknOrder(trip,reply);
        reply.setMessage(new Message());
        reply.getError().setMessage(BECKN_UPDATE_ERROR_MSG);
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

    public void createReplyContext(Request from , Request to){
        Context newContext = ObjectUtil.clone(from.getContext());
        String  action = from.getContext().getAction();

        if (action.startsWith("get_")){
            newContext.setAction(action.substring(4));
        }else {
            newContext.setAction("on_" + from.getContext().getAction());
        }
        to.setContext(newContext);
    }
}
