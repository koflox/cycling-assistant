# Module Dependency Graph

```mermaid
%%{
  init: {
    'theme': 'neutral'
  }
}%%

graph LR
  subgraph :feature
    :feature:nutrition["nutrition"]
    :feature:profile["profile"]
    :feature:connections["connections"]
    :feature:poi["poi"]
    :feature:settings["settings"]
    :feature:theme["theme"]
    :feature:locale["locale"]
    :feature:dashboard["dashboard"]
    :feature:destinations["destinations"]
    :feature:session["session"]
  end
  subgraph :feature:bridge:connection-session
    :feature:bridge:connection-session:api["api"]
    :feature:bridge:connection-session:impl["impl"]
  end
  subgraph :feature:bridge:destination-nutrition
    :feature:bridge:destination-nutrition:impl["impl"]
    :feature:bridge:destination-nutrition:api["api"]
  end
  subgraph :feature:bridge:destination-poi
    :feature:bridge:destination-poi:impl["impl"]
    :feature:bridge:destination-poi:api["api"]
  end
  subgraph :feature:bridge:destination-session
    :feature:bridge:destination-session:api["api"]
    :feature:bridge:destination-session:impl["impl"]
  end
  subgraph :feature:bridge:nutrition-session
    :feature:bridge:nutrition-session:api["api"]
    :feature:bridge:nutrition-session:impl["impl"]
  end
  subgraph :feature:bridge:nutrition-settings
    :feature:bridge:nutrition-settings:api["api"]
    :feature:bridge:nutrition-settings:impl["impl"]
  end
  subgraph :feature:bridge:poi-settings
    :feature:bridge:poi-settings:api["api"]
    :feature:bridge:poi-settings:impl["impl"]
  end
  subgraph :feature:bridge:profile-session
    :feature:bridge:profile-session:impl["impl"]
    :feature:bridge:profile-session:api["api"]
  end
  subgraph :feature:bridge:session-settings
    :feature:bridge:session-settings:api["api"]
    :feature:bridge:session-settings:impl["impl"]
  end
  subgraph :feature:sensor
    :feature:sensor:power["power"]
  end
  subgraph :shared
    :shared:concurrent["concurrent"]
    :shared:design-system["design-system"]
    :shared:di["di"]
    :shared:ble["ble"]
    :shared:error["error"]
    :shared:id["id"]
    :shared:sensor-protocol["sensor-protocol"]
    :shared:altitude["altitude"]
    :shared:distance["distance"]
    :shared:location["location"]
    :shared:map["map"]
    :shared:observability["observability"]
    :shared:graphics["graphics"]
  end
  :feature:bridge:destination-nutrition:impl --> :feature:bridge:destination-nutrition:api
  :feature:bridge:destination-nutrition:impl --> :feature:nutrition
  :feature:bridge:profile-session:impl --> :feature:bridge:profile-session:api
  :feature:bridge:profile-session:impl --> :feature:profile
  :feature:connections --> :shared:concurrent
  :feature:connections --> :shared:design-system
  :feature:connections --> :shared:di
  :feature:connections --> :feature:sensor:power
  :feature:connections --> :shared:ble
  :feature:connections --> :shared:error
  :feature:connections --> :shared:id
  :feature:connections --> :shared:sensor-protocol
  :feature:bridge:destination-poi:impl --> :feature:bridge:destination-poi:api
  :feature:bridge:destination-poi:impl --> :feature:poi
  :feature:settings --> :shared:concurrent
  :feature:settings --> :shared:design-system
  :feature:settings --> :shared:di
  :feature:settings --> :feature:theme
  :feature:settings --> :feature:locale
  :feature:settings --> :feature:profile
  :feature:settings --> :feature:bridge:nutrition-settings:api
  :feature:settings --> :feature:bridge:poi-settings:api
  :feature:settings --> :feature:bridge:session-settings:api
  :app --> :baselineprofile
  :app --> :feature:connections
  :app --> :feature:bridge:connection-session:api
  :app --> :feature:bridge:connection-session:impl
  :app --> :feature:bridge:destination-nutrition:api
  :app --> :feature:bridge:destination-nutrition:impl
  :app --> :feature:bridge:destination-poi:api
  :app --> :feature:bridge:destination-poi:impl
  :app --> :feature:bridge:destination-session:api
  :app --> :feature:bridge:destination-session:impl
  :app --> :feature:bridge:nutrition-session:api
  :app --> :feature:bridge:nutrition-session:impl
  :app --> :feature:bridge:nutrition-settings:api
  :app --> :feature:bridge:nutrition-settings:impl
  :app --> :feature:bridge:poi-settings:api
  :app --> :feature:bridge:poi-settings:impl
  :app --> :feature:bridge:profile-session:api
  :app --> :feature:bridge:profile-session:impl
  :app --> :feature:bridge:session-settings:api
  :app --> :feature:bridge:session-settings:impl
  :app --> :feature:dashboard
  :app --> :feature:destinations
  :app --> :feature:locale
  :app --> :feature:nutrition
  :app --> :feature:poi
  :app --> :feature:profile
  :app --> :feature:sensor:power
  :app --> :feature:session
  :app --> :feature:settings
  :app --> :feature:theme
  :app --> :shared:altitude
  :app --> :shared:ble
  :app --> :shared:concurrent
  :app --> :shared:design-system
  :app --> :shared:di
  :app --> :shared:distance
  :app --> :shared:id
  :app --> :shared:location
  :app --> :shared:error
  :app --> :shared:map
  :app --> :shared:observability
  :app --> :shared:sensor-protocol
  :feature:bridge:destination-session:api --> :shared:location
  :feature:session --> :shared:concurrent
  :feature:session --> :shared:design-system
  :feature:session --> :shared:di
  :feature:session --> :shared:altitude
  :feature:session --> :shared:distance
  :feature:session --> :shared:error
  :feature:session --> :shared:graphics
  :feature:session --> :shared:id
  :feature:session --> :shared:map
  :feature:session --> :shared:location
  :feature:session --> :feature:bridge:connection-session:api
  :feature:session --> :feature:bridge:nutrition-session:api
  :feature:session --> :feature:bridge:profile-session:api
  :feature:session --> :feature:theme
  :feature:dashboard --> :feature:destinations
  :feature:dashboard --> :feature:bridge:destination-session:api
  :feature:dashboard --> :shared:design-system
  :feature:nutrition --> :shared:concurrent
  :feature:nutrition --> :shared:design-system
  :feature:nutrition --> :shared:di
  :feature:nutrition --> :feature:bridge:nutrition-session:api
  :shared:map --> :shared:graphics
  :feature:bridge:destination-session:impl --> :feature:bridge:destination-session:api
  :feature:bridge:destination-session:impl --> :feature:session
  :feature:bridge:destination-session:impl --> :shared:concurrent
  :feature:bridge:destination-session:impl --> :shared:location
  :shared:concurrent --> :shared:di
  :feature:bridge:connection-session:impl --> :feature:bridge:connection-session:api
  :feature:bridge:connection-session:impl --> :feature:connections
  :feature:bridge:connection-session:impl --> :feature:sensor:power
  :feature:bridge:nutrition-session:impl --> :feature:nutrition
  :feature:bridge:nutrition-session:impl --> :feature:bridge:nutrition-session:api
  :feature:bridge:nutrition-session:impl --> :feature:session
  :feature:bridge:session-settings:impl --> :feature:bridge:session-settings:api
  :feature:bridge:session-settings:impl --> :feature:session
  :shared:observability --> :shared:concurrent
  :shared:error --> :shared:concurrent
  :shared:error --> :shared:design-system
  :shared:error --> :shared:di
  :feature:sensor:power --> :shared:concurrent
  :feature:sensor:power --> :shared:design-system
  :feature:sensor:power --> :shared:di
  :feature:sensor:power --> :shared:ble
  :feature:sensor:power --> :shared:error
  :feature:sensor:power --> :shared:sensor-protocol
  :feature:theme --> :shared:concurrent
  :feature:theme --> :shared:di
  :baselineprofile --> :shared:design-system
  :baselineprofile --> :app
  :shared:ble --> :shared:concurrent
  :feature:locale --> :shared:concurrent
  :feature:locale --> :shared:di
  :feature:bridge:nutrition-settings:impl --> :feature:bridge:nutrition-settings:api
  :feature:bridge:nutrition-settings:impl --> :feature:nutrition
  :feature:bridge:poi-settings:impl --> :feature:bridge:poi-settings:api
  :feature:bridge:poi-settings:impl --> :feature:poi
  :shared:location --> :shared:concurrent
  :shared:location --> :shared:di
  :feature:destinations --> :shared:concurrent
  :feature:destinations --> :shared:design-system
  :feature:destinations --> :shared:di
  :feature:destinations --> :feature:bridge:destination-nutrition:api
  :feature:destinations --> :feature:bridge:destination-poi:api
  :feature:destinations --> :feature:bridge:destination-session:api
  :feature:destinations --> :shared:distance
  :feature:destinations --> :shared:graphics
  :feature:destinations --> :shared:location
  :feature:destinations --> :shared:map
  :feature:profile --> :shared:concurrent
  :feature:profile --> :shared:di
  :feature:poi --> :shared:concurrent
  :feature:poi --> :shared:design-system
  :feature:poi --> :shared:di
```