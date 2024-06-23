# HI
이 프로젝트는 Spring Boot를 사용하여 도메인 이벤트를 구현하는 예제입니다. 이 프로젝트는 사용자(User) 엔터티의 이름 변경 시 이벤트를 발행하고, 이를 리스너가 수신하여 로그를 기록하는 기능을 포함합니다.

`AbstractAggregateRoot`를 사용하면 `changeName()`과 같이 더티 체킹의 경우에도 명시적으로 `save()` 혹은 `delete()`를 호출해 줘야 한다는 점때문에 좋은 방법이 없을까 고민하다가 작성한 코드입니다.

서비스가 아닌 도메인에서 AbstractAggregateRoot를 사용하여 이벤트를 발행하는 방법과 ApplicationEventPublisher를 사용하여 이벤트를 발행하는 방법에 대해 살펴봅니다.

## 프로젝트 구조
```plaintext
.
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── domaineventtest
│   │   │               ├── application
│   │   │               │   ├── UserEventListener.java
│   │   │               │   └── UserService.java
│   │   │               ├── domain
│   │   │               │   ├── User.java
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── config
│   │   │               │   │   ├── Events.java
│   │   │               │   │   └── EventsConfiguration.java
│   │   │               │   └── event
│   │   │               │       └── UserChangedNameEvent.java
│   │   │               ├── infra
│   │   │               │   ├── UserJpaRepository.java
│   │   │               │   └── UserRepositoryImpl.java
│   │   │               └── web
│   │   │                   └── UserController.java
│   │   └── resources
│   │       └── application.yml
└── pom.xml
```

## 주요 클래스 설명
### UserEventListener
이 클래스는 UserChangedNameEvent를 수신하여 로그를 기록하는 이벤트 리스너입니다.
```java
@Component
public class UserEventListener {
    Logger logger = LoggerFactory.getLogger(UserEventListener.class);

    @TransactionalEventListener
    public void onUserNameChanged(UserChangedNameEvent event) {
        logger.info("User name changed from: {}", event.from());
    }
}
```

### UserService
이 클래스는 사용자 생성을 처리하고, 사용자 이름 변경 시 도메인 이벤트를 발행합니다. userRepository.save(user); 주석을 해제하면 AbstractAggregateRoot의 registerEvent 메서드가 호출되어 이벤트가 발행됩니다.
```java
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(String name) {
        User user = new User(name);
        userRepository.save(user);
    }

    public void changeUserName(Long id, String name) {
        User user = userRepository.findById(id)
                .orElseThrow();
        
        user.changeName(name);

        /**
         * https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/AbstractAggregateRoot.html
         * save() 또는 delete()를 호출해야만 이벤트가 발행됨.
         */
        // userRepository.save(user);
    }
}
```

### Events
이 클래스는 이벤트를 발행하는 헬퍼 클래스입니다.
````java
public class Events {
    private static ApplicationEventPublisher publisher;

    static void setPublisher(ApplicationEventPublisher publisher) {
        Events.publisher = publisher;
    }

    public static void raise(Object event) {
        if (publisher != null) {
            publisher.publishEvent(event);
        }
    }
}
````

### User
User 엔터티는 이름 변경 시 두 개의 이벤트를 발행합니다. registerEvent 메서드를 통해 AbstractAggregateRoot 기반의 이벤트를 발행하고, Events.raise 메서드를 통해 직접 이벤트를 발행합니다.
```java
@Entity
public class User extends AbstractAggregateRoot<User> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    protected User() {
    }

    public User(String name) {
        this.name = name;
    }

    public void changeName(String name) {
        this.name = name;
        registerEvent(new UserChangedNameEvent("AbstractAggregateRoot", this.name));
        Events.raise(new UserChangedNameEvent("Events.raise", this.name));
    }
}
```

## 이벤트 발행
`User` 엔터티의 `changeName` 메서드는 두 가지 방법으로 이벤트를 발행합니다.

1. `registerEvent(new UserChangedNameEvent("AbstractAggregateRoot", this.name))`;

- `AbstractAggregateRoot`의 메서드를 사용하여 이벤트를 발행합니다.
- `UserService`에서 changeUserName 메서드를 호출하고,`userRepository.save(user);`를 호출할 때 이벤트가 발행됩니다.

2. `Events.raise(new UserChangedNameEvent("Events.raise", this.name))`;

- `Events` 클래스의 `raise` 메서드를 사용하여 직접 이벤트를 발행합니다.
- `UserService`의 `changeUserName` 메서드가 호출될 때마다 이벤트가 발행됩니다.

**save() 여부에 따른 이벤트 발행**
- `userRepository.save(user)`; 주석을 해제하면 `AbstractAggregateRoot`를 통해 이벤트가 발행됩니다.
- `userRepository.save(user)`; 주석이 있을 경우 `Events.raise`를 통해 이벤트가 발행됩니다.



## 수신 타이밍
`Listener`가 `@EventListener`냐 `@TransactionalEventListener`에 따라 이벤트를 수신하는 시점이 다릅니다. 이것에 관해서는 추가로 알아보시기 바랍니다.
