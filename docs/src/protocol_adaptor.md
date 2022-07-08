# /search 
## Request 
	curl -H "content-type:application/json" \
	-H "X-CallBackToBeSynchronized:Y" \
	-H "Authorization: Basic Zmlyc3RBcHA6dmVua3kxMg==" \
	"https://becknify.humbhionline.in/mobility/beckn_open/firstApp/bap/api/bg/search" \
	-d '{
	    "context": { "city": "std:080" },
	  "message": {
	    "intent": {
		    "fulfillment": {
		        "start": {
		            "location": {
		                "gps": "12.910793,77.696316"
		            }
		        },
		        "end": {
		            "location": {
		                "gps": "12.900793,77.596316"
		            }
		        }
		    }
		}
	  }
	}';

## Response
	[
	  {
	    "context": {
	      "transaction_id": "7f4b2711-6028-4793-9468-022e07df8a70",
	      "bpp_id": "becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in",
	      "country": "IND",
	      "city": "std:080",
	      "message_id": "7f4b2711-6028-4793-9468-022e07df8a70",
	      "core_version": "0.9.3",
	      "ttl": "PT10S",
	      "bap_id": "becknify.humbhionline.in.mobility.BAP/beckn_open/firstApp",
	      "domain": "nic2004:60221",
	      "bpp_uri": "https://becknify.humbhionline.in/mobility/beckn_open/app1-succinct-in/bpp",
	      "action": "on_search",
	      "bap_uri": "https://becknify.humbhionline.in/mobility/beckn_open/firstApp/bap",
	      "timestamp": "2022-07-08T19:34:13.970Z"
	    },
	    "message": {
	      "catalog": {
		"bpp/providers": [
		  {
		    "locations": [
		      {
		        "gps": "12.920000,77.700000",
		        "id": "./mobility/ind.blr/3@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.provider_location"
		      }
		    ],
		    "id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.provider",
		    "descriptor": { "name": "Venky Travels" },
		    "categories": [
		      {
		        "id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.category",
		        "descriptor": { "name": "Taxi" }
		      }
		    ],
		    "items": [
		      {
		        "category_id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.category",
		        "price": { "currency": "INR", "value": "272.6923332029759" },
		        "id": "./mobility/ind.blr/3@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.item",
		        "descriptor": {
		          "code": "Taxi- Wifi-AC-Car-Santro",
		          "name": "Taxi- Wifi-AC-Car-Santro"
		        },
		        "fulfillment_id": "./mobility/ind.blr/313@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.fulfillment"
		      }
		    ]
		  }
		],
		"bpp/fulfillments": [
		  {
		    "start": { "location": { "gps": "12.910793,77.696316" } },
		    "end": { "location": { "gps": "12.900793,77.596316" } },
		    "id": "./mobility/ind.blr/313@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.fulfillment",
		    "vehicle": { "registration": "KA05Z 3910" }
		  }
		],
		"bpp/descriptor": {
		  "code": "cabs.succinct.in",
		  "name": "cabs.succinct.in"
		}
	      }
	    }
	  }
	]

#/select
## Request  
 	 curl -H 'content-type:application/json' -H 'X-CallBackToBeSynchronized:Y' -H 'Authorization: Basic Zmlyc3RBcHA6dmVua3kxMg==' https://becknify.humbhionline.in/mobility/beckn_open/firstApp/bap/api/bg/select -d '
	  {
	    "context": { "city": "std:080" , "bpp_id" : "becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in" , "action" : "select" },
	  "message": {
	    "order": {
		    "fulfillment": {
		        "id": "./mobility/ind.blr/313@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.fulfillment",

		    }
		}
	  }
	}'

## Response
	{
		  "context": {
		    "transaction_id": "779668a5-218c-4223-92aa-cfeaad7b2aa8",
		    "bpp_id": "becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in",
		    "country": "IND",
		    "city": "std:080",
		    "message_id": "779668a5-218c-4223-92aa-cfeaad7b2aa8",
		    "core_version": "0.9.3",
		    "ttl": "PT10S",
		    "bap_id": "becknify.humbhionline.in.mobility.BAP/beckn_open/firstApp",
		    "domain": "nic2004:60221",
		    "bpp_uri": "https://becknify.humbhionline.in/mobility/beckn_open/app1-succinct-in/bpp",
		    "action": "on_select",
		    "bap_uri": "https://becknify.humbhionline.in/mobility/beckn_open/firstApp/bap",
		    "timestamp": "2022-07-08T19:46:50.804Z"
		  },
		  "message": {
		    "order": {
		      "quote": {
			"breakup": [
					  {
					    "price": { "currency": "INR", "value": "231.0952" },
					    "type": "item",
					    "title": "Fare"
					  },
					  {
					    "price": { "currency": "INR", "value": "41.5972" },
					    "type": "item",
					    "title": "Tax"
					  }
					],
			"price": { "currency": "INR", "value": "272.6923" }
	      		},
			"price": { "currency": "INR", "value": "272.6923" }
		      },
		      "provider": {
			"locations": [
			  {
			    "gps": "12.920000,77.700000",
			    "id": "./mobility/ind.blr/3@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.provider_location"
			  }
			],
			"id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.provider",
			"descriptor": { "name": "Venky Travels" },
			"categories": [
			  {
			    "id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.category",
			    "descriptor": { "name": "Taxi" }
			  }
			],
			"items": [
			  {
			    "category_id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.category",
			    "price": { "currency": "INR", "value": "272.6923" },
			    "id": "./mobility/ind.blr/3@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.item",
			    "descriptor": {
			      "code": "Taxi- Wifi-AC-Car-Santro",
			      "name": "Taxi- Wifi-AC-Car-Santro"
			    },
			    "fulfillment_id": "./mobility/ind.blr/313@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.fulfillment"
			  }
			]
		      },
		      "fulfillment": {
			"start": { "location": { "gps": "12.910793,77.696316" } },
			"end": { "location": { "gps": "12.900793,77.596316" } },
			"id": "./mobility/ind.blr/313@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.fulfillment",
			"vehicle": { "registration": "KA05Z 3910" }
		      }
		    }
		  }
		}
	# init 
	## Request 
		{
		  "context": {
		    "city": "std:080",
		    "bpp_id": "becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in",
		    "action": "init"
		  },
		  "message": {
		    "order": {
		      "fulfillment": {
			"id": "./mobility/ind.blr/313@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.fulfillment",
			"customer": {
			  "person": {
			    "name": "./Venky/Mr./Venky/ /Mahadevan/",
			    "gender": "male"
			  },
			  "contact": {
			    "phone": "+919845114558",
			    "email": "venky@example.com"
			  }
			}
		      },
		      "billing": {
			"address": {
			  "door": "A302",
			  "country": "IND",
			  "city": "std:080",
			  "street": "Bannergatta Road",
			  "area_code": "560076",
			  "state": "KA",
			  "building": "Pride Apartments"
			},
			"phone": "9845114558",
			"name": "./Venky/Mr./Venky/ /Mahadevan/",
			"email": "venkatramanm@gmail.com"
		      }
		    }
		  }
		}
# Response 
{
  "context": {
    "transaction_id": "0cdf2173-8e72-484f-ad26-87ef478771ea",
    "bpp_id": "becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in",
    "country": "IND",
    "city": "std:080",
    "message_id": "0cdf2173-8e72-484f-ad26-87ef478771ea",
    "core_version": "0.9.3",
    "ttl": "PT10S",
    "bap_id": "becknify.humbhionline.in.mobility.BAP/beckn_open/firstApp",
    "domain": "nic2004:60221",
    "bpp_uri": "https://becknify.humbhionline.in/mobility/beckn_open/app1-succinct-in/bpp",
    "action": "on_init",
    "bap_uri": "https://becknify.humbhionline.in/mobility/beckn_open/firstApp/bap",
    "timestamp": "2022-07-08T21:35:31.041Z"
  },
  "message": {
    "order": {
      "quote": {
        "breakup": [
          {
            "price": { "currency": "INR", "value": "231.0952" },
            "type": "item",
            "title": "Fare"
          },
          {
            "price": { "currency": "INR", "value": "41.5972" },
            "type": "item",
            "title": "Tax"
          }
        ],
        "price": { "currency": "INR", "value": "272.6923" }
      },
      "provider": {
        "locations": [
          {
            "gps": "12.920000,77.700000",
            "id": "./mobility/ind.blr/3@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.provider_location"
          }
        ],
        "id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.provider",
        "descriptor": { "name": "Venky Travels" },
        "categories": [
          {
            "id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.category",
            "descriptor": { "name": "Taxi" }
          }
        ],
        "items": [
          {
            "category_id": "./mobility/ind.blr/1@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.category",
            "price": { "currency": "INR", "value": "272.6923" },
            "id": "./mobility/ind.blr/3@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.item",
            "descriptor": {
              "code": "Taxi- Wifi-AC-Car-Santro",
              "name": "Taxi- Wifi-AC-Car-Santro"
            },
            "fulfillment_id": "./mobility/ind.blr/313@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.fulfillment"
          }
        ]
      },
      "fulfillment": {
        "start": { "location": { "gps": "12.910793,77.696316" } },
        "end": { "location": { "gps": "12.900793,77.596316" } },
        "id": "./mobility/ind.blr/313@becknify.humbhionline.in.mobility.BPP/beckn_open/app1-succinct-in.fulfillment",
        "vehicle": { "registration": "KA05Z 3910" }
      }
    }
  }
}
