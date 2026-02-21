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
    :feature:settings["settings"]
    :feature:theme["theme"]
    :feature:locale["locale"]
    :feature:dashboard["dashboard"]
    :feature:destinations["destinations"]
    :feature:session["session"]
  end
  subgraph :feature:bridge:destination-nutrition
    :feature:bridge:destination-nutrition:impl["impl"]
    :feature:bridge:destination-nutrition:api["api"]
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
  subgraph :feature:bridge:profile-session
    :feature:bridge:profile-session:impl["impl"]
    :feature:bridge:profile-session:api["api"]
  end
  subgraph :shared
    :shared:concurrent["concurrent"]
    :shared:design-system["design-system"]
    :shared:di["di"]
    :shared:altitude["altitude"]
    :shared:distance["distance"]
    :shared:id["id"]
    :shared:location["location"]
    :shared:error["error"]
    :shared:graphics["graphics"]
  end
  :feature:bridge:destination-nutrition:impl --> :feature:bridge:destination-nutrition:api
  :feature:bridge:destination-nutrition:impl --> :feature:nutrition
  :feature:bridge:profile-session:impl --> :feature:bridge:profile-session:api
  :feature:bridge:profile-session:impl --> :feature:profile
  :feature:settings --> :feature:theme
  :feature:settings --> :feature:locale
  :feature:settings --> :feature:profile
  :feature:settings --> :feature:bridge:nutrition-settings:api
  :feature:settings --> :shared:concurrent
  :feature:settings --> :shared:design-system
  :feature:settings --> :shared:di
  :app --> :feature:bridge:destination-nutrition:api
  :app --> :feature:bridge:destination-nutrition:impl
  :app --> :feature:bridge:destination-session:api
  :app --> :feature:bridge:destination-session:impl
  :app --> :feature:bridge:nutrition-session:api
  :app --> :feature:bridge:nutrition-session:impl
  :app --> :feature:bridge:nutrition-settings:api
  :app --> :feature:bridge:nutrition-settings:impl
  :app --> :feature:bridge:profile-session:api
  :app --> :feature:bridge:profile-session:impl
  :app --> :feature:dashboard
  :app --> :feature:destinations
  :app --> :feature:locale
  :app --> :feature:nutrition
  :app --> :feature:profile
  :app --> :feature:session
  :app --> :feature:settings
  :app --> :feature:theme
  :app --> :shared:altitude
  :app --> :shared:concurrent
  :app --> :shared:design-system
  :app --> :shared:distance
  :app --> :shared:id
  :app --> :shared:location
  :app --> :shared:error
  :feature:bridge:destination-session:api --> :shared:location
  :feature:session --> :shared:altitude
  :feature:session --> :shared:concurrent
  :feature:session --> :shared:design-system
  :feature:session --> :shared:di
  :feature:session --> :shared:distance
  :feature:session --> :shared:error
  :feature:session --> :shared:id
  :feature:session --> :shared:location
  :feature:session --> :feature:bridge:nutrition-session:api
  :feature:session --> :feature:bridge:profile-session:api
  :feature:dashboard --> :feature:destinations
  :feature:dashboard --> :feature:bridge:destination-session:api
  :feature:dashboard --> :shared:design-system
  :feature:nutrition --> :feature:bridge:nutrition-session:api
  :feature:nutrition --> :shared:concurrent
  :feature:nutrition --> :shared:design-system
  :feature:bridge:destination-session:impl --> :feature:bridge:destination-session:api
  :feature:bridge:destination-session:impl --> :feature:session
  :feature:bridge:destination-session:impl --> :shared:concurrent
  :feature:bridge:destination-session:impl --> :shared:location
  :shared:concurrent --> :shared:di
  :feature:bridge:nutrition-session:impl --> :feature:nutrition
  :feature:bridge:nutrition-session:impl --> :feature:bridge:nutrition-session:api
  :feature:bridge:nutrition-session:impl --> :feature:session
  :shared:error --> :shared:concurrent
  :shared:error --> :shared:design-system
  :feature:theme --> :shared:concurrent
  :feature:theme --> :shared:di
  :feature:locale --> :shared:concurrent
  :feature:locale --> :shared:di
  :feature:bridge:nutrition-settings:impl --> :feature:bridge:nutrition-settings:api
  :feature:bridge:nutrition-settings:impl --> :feature:nutrition
  :shared:location --> :shared:concurrent
  :feature:destinations --> :feature:bridge:destination-nutrition:api
  :feature:destinations --> :feature:bridge:destination-session:api
  :feature:destinations --> :shared:concurrent
  :feature:destinations --> :shared:design-system
  :feature:destinations --> :shared:di
  :feature:destinations --> :shared:distance
  :feature:destinations --> :shared:graphics
  :feature:destinations --> :shared:location
  :feature:profile --> :shared:concurrent
  :feature:profile --> :shared:di
```