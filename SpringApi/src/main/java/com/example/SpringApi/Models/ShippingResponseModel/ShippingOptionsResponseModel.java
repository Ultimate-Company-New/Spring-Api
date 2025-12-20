package com.example.SpringApi.Models.ShippingResponseModel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * Response model for Shiprocket courier serviceability API.
 * Variable names use underscores to match Shiprocket API response format.
 */
@Getter
@Setter
public class ShippingOptionsResponseModel {
    public boolean company_auto_shipment_insurance_setting;
    public CovidZones covid_zones;
    public String currency;
    public Data data;
    public int dg_courier;
    public int eligible_for_insurance;
    public boolean insurace_opted_at_order_creation;
    public boolean is_allow_templatized_pricing;
    public int is_latlong;
    public boolean is_old_zone_opted;
    public boolean is_zone_from_mongo;
    public int label_generate_type;
    public int on_new_zone;
    public ArrayList<Object> seller_address;
    public int status;
    public boolean user_insurance_manadatory;

    @Getter
    @Setter
    public static class AvailableCourierCompany {
        public String air_max_weight;
        public int assured_amount;
        public Object base_courier_id;
        public String base_weight;
        public int blocked;
        public String call_before_delivery;
        public double charge_weight;
        public String city;
        public int cod;
        public double cod_charges;
        public double cod_multiplier;
        public String cost;
        public int courier_company_id;
        public String courier_name;
        public String courier_type;
        public int coverage_charges;
        public String cutoff_time;
        public String delivery_boy_contact;
        public double delivery_performance;
        public String description;
        public String edd;
        public int entry_tax;
        public String estimated_delivery_days;
        public String etd;
        public int etd_hours;
        public double freight_charge;
        public int id;
        public int is_custom_rate;
        public boolean is_hyperlocal;
        public int is_international;
        public boolean is_rto_address_available;
        public boolean is_surface;
        public int local_region;
        public int metro;
        public double min_weight;
        public int mode;
        public boolean odablock;
        public int other_charges;
        public String others;
        public String pickup_availability;
        public double pickup_performance;
        public String pickup_priority;
        public int pickup_supress_hours;
        public String pod_available;
        public String postcode;
        public int qc_courier;
        public String rank;
        public double rate;
        public double rating;
        public String realtime_tracking;
        public int region;
        public double rto_charges;
        public double rto_performance;
        public int seconds_left_for_pickup;
        public boolean secure_shipment_disabled;
        public int ship_type;
        public String state;
        public String suppress_date;
        public String suppress_text;
        public SuppressionDates suppression_dates;
        public String surface_max_weight;
        public double tracking_performance;
        public int volumetric_max_weight;
        public double weight_cases;
        public String zone;
    }

    @Getter
    @Setter
    public static class SuppressionDates {
        public String action_on;
        public String delay_remark;
        public int delivery_delay_by;
        public String delivery_delay_days;
        public String delivery_delay_from;
        public String delivery_delay_to;
        public int pickup_delay_by;
        public String pickup_delay_days;
        public String pickup_delay_from;
        public String pickup_delay_to;
    }

    @Getter
    @Setter
    public static class CovidZones {
        public Object delivery_zone;
        public Object pickup_zone;
    }

    @Getter
    @Setter
    public static class Data {
        public ArrayList<AvailableCourierCompany> available_courier_companies;
        public Object child_courier_id;
        public int is_recommendation_enabled;
        public int recommendation_advance_rule;
        public RecommendedBy recommended_by;
        public int recommended_courier_company_id;
        public int shiprocket_recommended_courier_id;
    }

    @Getter
    @Setter
    public static class RecommendedBy {
        public int id;
        public String title;
    }
}
