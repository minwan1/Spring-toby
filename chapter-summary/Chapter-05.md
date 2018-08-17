# 토비의 스프링
이글은 [토비님의 스프링책](http://book.naver.com/bookdb/book_detail.nhn?bid=7006516)을 보고 요약한 내용입니다

# 5 서비스 추상화

## 사용자 레벨 관리 기능 추가

자바에는 표준 스펙, 사용 제품, 오픈소스를 통틀어서 사용방법과 혁시은 다르지만 기능과 목적이 유사한 기술이 존재한다. 환경과 상황에 따라서 기술이 바뀌고, 그에 따른 다른 API를 사용하고 다른 스타일의 접근 방법을 따라야 한다는 건 매우 피곤한일이다.

다음장에서는 스프링이 어떻게 성격이 비슷한 여러 종류의 기술을 추상화하고 이를 일관된 방법으로 사용할 수 있도록 지원하는지 확인할것이다.

### 5.1 사용자 레벨 관리 기능 추가
이전까지는 DAO를 이용해 간단한 CRUD를 진행했는데 이장부터는 약간의 비지니스로직을 추가할것이다. 내용은 다음과 같다.
* 사용자의 레벨은 BASIC, SILVER, GOLD 세가지중 하나이다.
* 사용자가 처음 가입하면 BASIC 레벨이 되며, 이후 활동에 따라서 한단꼐씩 업그레이드 될 수 있다.
* 가입 후 50회 이상 로그인을 하면 BASIC에서 SIRVER레벨이 된다.
* SILVER레벨이면서 30번 이상 추천을 받으면 GOLD레벨이 된다.
* 사용자 레벨의 변경 작업은 일정한 주기를 가지고 일관적으로 진행된다. 변경 작업전에는 조건을 충족하더라도 레벨의 변경이 일어나지 않는다.

사용자 관리의 기본 로직은 정해진 조건에 따라 사용자의 레벨을 주기적으로 변경한다이다.

#### 5.1.1 필드 추가
```java
public class User {

    private static final int BASIC = 1;
    private static final int SILVER = 2;
    private static final int GOLD = 3;

    private int level;

    public void setLevel(int level){
        this.level = level;
    }
}
```
먼저 위와같은 코드로 유저의 레벨을 관리한다고 해보자. 예전같으면 위와같이 상수로 레벨들을 관리 했을 수 있다. 하지만 상수로 관리하게 되면 많은 문제들을 가지고 있다. 먼저 실수로 다른 인트값이 들어간다고해도 같은 인트형 타입이기 떄문에 숫자가 들어가진다. 이러한문제로인해 3이상의 값이 들어간다고해도 컴파일러단에서 에러는 나지않을것이다. 이러한문제는 심각한 문제를 만들어낸다.
하지만 이러한 문제를 컴파일단계에서 막을 수 있는 방법이 있다. 그것은 바로 아래와같은 enum타입이다. enum은 같은 타입은 타입을 선언하기때문에 안전하게 사용할 수 있다.

```java
public enum Level {
    BASIC(1),
    SILVER(2),
    GOLD(3)
    ;

    private final int value;

    Level(int value){
        this.value = value;
    }

    public int value(){
        return value;
    }
}
```
그다음 다음과 같이 유저를 선언한다.
```java
@Getter
@Setter
@NoArgsConstructor
public class User {
    private String id;
    private String name;
    private String password;
    private Level level;
    private int login;
    private int recommend;


    @Builder
    public User(String id, String name, String password, Level level, int login, int recommend) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.level = level;
        this.login = login;
        this.recommend = recommend;
    }
}

```

#### 사용자 수정 기능 추가
사용자 관리 비즈니스로직에 따르면 사용자 정보는 여러 번 수정될 수 있다. 이과정에서 사용자 기능은 계속 반복적으로 다른 클래스들에서도 호출될 수 있으니 UserService를 생성한다. 그리고 UserService에서 유저에관한 비지니스로직을 다룰것이다.

```java
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    ....
}
```

```java
@Bean
public UserService userService(){
      return  new UserService(userRepository);
  }
```
먼저 앞에서 말한대로 UserService를 생성하고 그곳에서 영속성에 접근할 수 있게 userRepository를 DI해줬다. 위와같이 등록하게되면 이제 스프링 컨테이너가 userService를 관리할 수 있게되고 아래와같이 변수로 접근할 수 있다.

```java
@Autowired
private UserService userService;
```

이제 수정 메소드도 추가했으니 다음은 유저를추가하는 add()메소드를 넣어보자. 이메소드는 어디에 넣는게 좋을까. 그렇다 UserService에 넣어야할것같다. 왜냐 지금 user에관한 관심이 userService에 집중되고 있기 때문이다.

##### 사용자 레벨 업 기능(upgradeLevels) 리팩토링
다음은 사용자 접속에 따른 userService에 레벨업 기능을 구현해보자. 아래와같이 level과 방문 횟수에따라 레벨이 결정되고, 변경여부에따라 유저가 업데이트된다.

```java
public List<User> upgradeLevels(){
        List<User> users = (List<User>) userRepository.findAll();
        for(User user : users){
            boolean changed = false;

            if(user.getLevel() == Level.BASIC &&user.getLogin() >= 50){
                user.updateLevel(Level.SILVER);
                changed = true;
            }
            else if(user.getLevel() == Level.SILVER &&user.getLogin() >= 30){
                user.updateLevel(Level.GOLD);
                changed = true;
            }else if(user.getLevel() == Level.GOLD){
                changed = false;
            }else{
                changed false;
            }

            if(changed){
                userRepository.save(users)
            }
        }

        return users;
    }
```


##### 코드 개선
비지니스로직이 완성되고 기능이 정상적으로 완료됬다고해서 개발이 끝난게아니다. 리팩토링을 해야하한다. 리팩토링은 선택사항이 아니라 필수사항이다. 그러기위해서는 다음과같은것들을 체크해야한다
1. 코드에 중복된 부분은없는가
2. 코드가 무어을 하는것인지 이해하기 불편하지 않은가?
3. 코드가 자신이 있어야할 자리에 있는가 ?
4. 앞으로 변경이 일어난다면 어떤것이 있을 수 있고, 그변화에 쉽게 대응할 수 있는가?

**upgradeLevels 문제점**
일단 for 루프속에 들어있는 if/elseif/else 블록들이 읽기 불편하다. 레벨의 변화 단계와 업그레이드조건, 조건이 충족됐을 때 해야 할 작업이 한데 섞여 있어서 로직을 이해하기 어렵다.

코드가 깔끔해보이지 않는 이유는 이렇게 성격이 다른 여러가지 로직이 한데 섞여 있기 때문이다.
```java
if(user.getLevel() == Level.BASIC &&user.getLogin() >= 50){
    user.updateLevel(Level.SILVER);
    changed = true;
}
...
if(changed){
    userRepository.update(users); // UserRepository에는 update가없다.
}
```
흐름
1. 현재 레벨이 무엇인지 파악하는 로직이다.
2. 업그레이드 조건을 담은 로직이다.
3. 다음 단계의 레벨이 무엇이면 업그레이드를 위한 작업이 무엇인지 플래그하는게있다.
4. 플래그를 이용한 업데이트

유저 레벨에 업그레이드라는 관심사항이 모여있는것같지만 성격이 조금씩 다른 것들이 섞여 있거나 분리돼서 나타나는 구조다. 이런 if 조건 블록이 레벨 개수 만큼 반복된다고 생각하면 엄청난 if문이 생길거고 경우의 수가 엄청나게 복잡 해질것이다. 이렇게 소스가 지저분해지면 버그가 발생한다면 아주 큰일일것이다.

**upgradeLevels 리팩토링**
이제 코드를 리팩토링 해볼것이다. 가장 먼저 추상적인 레벨에서 로직을 작성해보자. 기존의 upgradeLevels()메소드는 자주 변경될 가능성이 있는 구체적인 내용이 추상적인 로직의 흐름과 함께 섞여 있다.
먼저 구체적인 내용과 추상적인 로직을 분리해서 생각해보자. 일단 유저의 level을 업그레이드 하는것이다. 이것을 진행하기위해서는 다시생각해보면 다음과 같을 수 있다
1. 유저 레벨을 업그레이드한다.
2. 모든 유저를 조회해온다.
3. 유저는 업그레이드 할 수 있는가?
4. 업그레이드한다.
어떻게보면 다음과 같은 기능인데 소스가 엄청 난잡해졌다. 그럼 방금 말한대로 소스를 고쳐보겠다.
```java
public void upgradeLevels(){
  List<User> users = (List<User>) userRepository.findAll();
  for(User user : users){
    if(canUpgradeLevel(user))
      upgradeLevel(user);

  }
}
```
그렇다 정말 간단해졌다. 이제 여기에따라 구체적인 내용을 담은 메소드를 만들면된다.
```java
private boolean canUpgradeLevel(User user){
    Level currentLevel = user.getLevel();
    switch (currentLevel){
        case BASIC: return (user.getLogin() >= 50);
        case SILVER: return (user.getLogin() >= 30);
        case GOLD: return false;
        default: throw new IllegalArgumentException("Unknown Level");
    }
}
```
위와같이 선언하게되면 역할과 책임이 명료해진다. 업그레이드가 가능한지 확인하는 방법은 User 오브젝트에서 레벨을 가져와서, switch문으로 레벨을 구분하고, 각 레벨에 대한 업그레이드 조건을 만족하는지를 확인해주면된다.
다음은 레벨 업그레이드 작업메소드이다.
```java
private void upgradeLevel(User user){
    if(user.getLevel() == Level.BASIC) user.updateLevel(Level.SILVER);
    else if(user.getLevel() == Level.SILVER) user.updateLevel(Level.GOLD);
    userRepository.save(user);
}
```

하지만 여기에서 upgradeLevel메소드 한번더 세분화 할 수 있다. 마음에 안드는 점은 다음단계가 무엇인가하는 로직과 그때 사용자 오브젝트의 level필드를 변경해준다는 로직이 함께 있는데다, 너무 노골적으로 드러나 있다. 거기에다가 골드 이상에 등급이 들어온다면 Exception 발생할것이다.
먼저 레벨의 순서와 다음 단계 레벨이 무엇인지를 결정하는 일은 Level에게 맡기자.

```java
public enum Level {
    GOLD(3, null),
    SILVER(2, GOLD),
    BASIC(1, SILVER),
    ;

    private final int value;
    private final Level next;

    Level(int value, Level next){
        this.value = value;
        this.next = next;
    }

    public int value(){
        return value;
    }

    public Level nextLevel(){
        return this.next;
    }
}
```
다음과같이 level 관한 책임은 레벨객체에게 넘기면 된다. 이렇게하면 if문으로 레벨을 체크할필요도없이 nextLevel함수를 호출하게되면 다음 레벨로 객체가 변하게 된다.

```java
//user객체
public void updateLevel(Level level){
        final Level nextLevel = level.nextLevel();
        if(nextLevel == null){
            throw new IllegalStateException(this.level + "은 업그레이드가 불가능합니다.");
        }else{
            this.level = nextLevel;
        }
    }
```
이제 유저의 레벨을 올리기위해서는 user에게 시키면되고, 그 레벨의 관리 책임도 유저가 가지게된다. 이렇게되면 훨씬 코드를 읽기 편해진다.

```java
private void upgradeLevel(User user){
    user.updateLevel();
    userRepository.save(user);
}
```
이렇게함으로 훨씬더 가독성 좋은 코드가 되었다.

지금 개선한 코드를 살펴보면 각 오브젝트와 메소드가 각각 자기 몫의 책임을 맡아 일을 하는 구조로 만들어졌음을 알 수 있을 것이다. 객체지향적인 코드는 다른 오브젝트의 데이터를 가져와서 작업하는 대신 데이터를 갖고 있는 다른 오브젝트에게 작업을 해달라고 요청한다. 오브젝트에게 데이터를 요구 하지말고 작업을 요청하라는 것이 객체 지향 프로그래밍의 가장 기본이 되는 원리이기도 하다.


### 5.2 트랜잭션 서비스 추상화
이번장은 위에서 만든 레벨 업그레이드 시스템이 수행도중에 문제가 생겼을 경우 어떻게 처리할지에 대해서 다루어 진다. 만약 10만명의 유저가있고, 이것이 스케줄링에 의해 UserService에 upgradeLevels 메소드가 시행됬다고 가정해보자. 만약에 천명에 유저들이 업그레이드가 됬는데 도중에 네트워크나, 예상치 못한 exception으로 에러가 났다고 해보자. 그럼 당연히 모든것이 롤백이 되어야한다. 왜냐 일단 천명만 업그레이드가 되고 그후 9천명은 업그레이드가 되지 않으면 안되기 때문이다. 그래서 차라리 모든것을 롤백하는것이 낫다.

먼저 간단하게 테스트 케이스를 만들어보자. 이것은 중간에 Exception 난 상황을 가정해야한다. 하지만 UserService를 바로 건드는건 좋은 생각이 아니다. 그래서 이런경우엔 테스트용으로 특별히 만든 UserService의 대역을 사용하는 방법이 좋다. 이테스트를 효율적으로하기위해서는 UserService를 상속해서 UserService의 기능을 사용하는게 장기적으로 효율적이다.

테스트용 UserService의 서브클래스는 UserService 기능의 일부를 오버라이딩해서 특정 시점에서 강제로 예외가 발생하도록 만들 것이다. 그런데 UserService의 메소드 대부분은 현재 private 접근 제한이 걸려 있어서 오버라이딩이 불가능하다. 테스트 코드는 테스트 대상 클래스의 내부의 구현 내용을 고려해서 밀접하게 접근해야하는데, private 처럼 제약이 강한 접근 제한자를 사용하면 불편하다. 테스트를 위해 어플리케이션이 코드를 직접 수정하는 일은 가능한 피하는것이 좋지만 이번은 예외로 해야할것같다.

먼저 UserService의 upgradeLevel() 메소드 접근 권한을 다음과 같이 protected로 수정해서 상속을 통해 오버라이딩이 가능하게 하자.

protected void upgradeLevel()메소드 접근권한을 다음과 같이 protected로 수정해서 상속을 통해 오버라이딩이 가능하게 하자.

```java
protected void upgradeLevel(User user){...}
```

그래서 이제 이것을 상속해서 강제로 Exception이 나도록 해볼 것이다.  UserService를 상속해서 upgradeLevel() 메소드를 오버라이딩한 UserService대역을 맡을 클래스를 UserServiceTest안에 다음과 같이 넣는다.

```java
public class TestUserService extends UserService{

    private String id;

    public TestUserService(UserRepository userRepository, String id) {
        super(userRepository);
        this.id = id;
    }

    protected void upgradeLevel(User user){
        if(user.getId().equals(this.id)) throw new TestUserServiceException();
        super.upgradeLevel(user);
    }
}
```
위에처럼 특정 id가 들어오면 Exception이 발생하게 TestUserService를 만들었다. 이제 아래의 테스트 케이스를 실행해보자
```java
@Test
public void upgradeAllOrNothing(){

    UserService testUserService = new TestUserService(userRepository, users.get(3).getId());
    for(User user :users) userRepository.save(user);

    try {
        testUserService.upgradeLevels();
        fail("TestUserServiceException expected");
    }catch (TestUserServiceException e){

    }
    checkLevel(users.get(1), false);
}
```
위에 테스트로 기대한것은 3번째 유저가 들어갔을때 Exception이 발생하고 1번인덱스 유저의 레벨이 롤백될것을 예상했다. 하지만 아래를 보면 이것은 롤백되지 않았다.
```
java.lang.AssertionError:
Expected: is <BASIC>
     but: was <SILVER>
Expected :is <BASIC>
```

**테스트 실패의 원인**
이것의 문제는 바로 트랜잭션의 문제이다. 트랜잭션이란 더이상 나눌수 없는 단위 작업을 말한다. 작업을 쪼개서 작은 단위로 만들 수 없다는것은 트랜잭션의 핵심 속성인 원자성을 의미한다.

#### 트랜잭션 경계 설정
DB는 그 자체로 완벽한 트랜잭션을 지원한다. SQL을 이용해 다중 로우의 수정이나 삭제를 위한 요청을 했을 때 일부 로우만 삭제되고 나머지는 안된다거나 일부필드는 수정했는데 나머지 필드는 수정이 안되고 실패로 끝나는 경우는 없다. 하나의 SQL명령을 처리하는 경우는 DB가 트랜잭션을 보장해준다고 믿을 수 있다.
하지만 여러개의 SQL이 사용되는 작업을 하나의 트랜잭션으로 취급해야하는 경우도 있다. 트랜잭션을 설명할 때 자주 언급되는 계좌이체라든가 이장에서 만든 유저레벨업 기능등이 그렇다.
은행 시스템의 계좌 이체 작업은 반드시 하나의 트랝개션으로 묶여서 일어나야한다. 이렇게 하나의 트랜잭션이 성공하는것을 커밋이라고하고, 반대로 실패하는것을 롤백이라고한다.

**JDBC트랜잭션의 트랜잭션 경계설정**
모든 트랜잭션은 시작하는 지점과 끝나는 지점이 있다. 시작하는 지점과 끝나는 위치를 트랜잭션의 경계라고 한다. 다음은 트랜잭션을 처리하는 코드이다. 단순히 소스를 위한 코드라 문법적 오류가 있을 수 있다.

```java
Connection c = dataSource.getConnection();

c.setAutoCommit(false);
try {
    PreparedStatement st1 = c.prepareStatement("update users ...");
    st1.executeUpdate();

    PreparedStatement st2 = c.prepareStatement("delete users ...");
    st2.executeUpdate();

    c.commit(); //--> 트랜잭션 커밋
}catch (Exception e){
    c.rollback();
}
c.close();
```

 JDBC의 트랜잭션은 하나의 Connection을 가져와 사용하다가 닫는 사이에 일어난다. 트랜잭션은 Connection 오브젝트를 통해 이루어진다. 기본적으로 자동 커밋옵션은 true인데 위에처럼 false로 설정을 해줘야 트랜잭션을 제어할 수 있다. 트랜잭션이 한번 시작하면 commit이나 또는 rollback()메소드가 호출될때 까지 하나의 트랜잭션으로 묶인다.
 이러한 설정을 하는것을 트른잭션의 경계 설정이라고 한다. 트랜잭션의 시작을 선언하고 coomit또는 rollback으로 트랜잭션을 종료하는 작업을 트랜잭션의 경계설정이라한다. 그리고 이러한 트랜잭션이 만들어지는범위를 로컬 트랜잭션이라고도 한다.

 **UserService와 UserDao의 트랜잭션 문제**
지금까지 만든 소스에서는 어디에서도 트랜잭션 관리를 하지 않았다. JDBC의 트랜잭션 경계 설정 메소드는 모두 Connection오브젝트를 사용하게 되어 있는데, jdbcTemplate을 사용하기 시작한 이후로부터 이 Connection 오브젝트가 없었다.

jdbcTemplate은 직접 만들어봤던 JdbcContext와 작업 흐름이 거의 동일하다. 하나의 템플릿 메소드 안에서 DataSource의 getConnection()메소드를 호출해서 Connection 오브젝트를 가져오고, 작업을 마치면 Connection을 확실하게 닫아주고 템플릿 메소드를 빠져 나온다. 결국 템플릿 메소드 호출 한번에 한개의 DB커넥션이 만들어지고 닫히는 일까지 일어나는 것이다. 일반적으로 트랜잭션은 커넥션보다 존재 범위가 짧다. 따라서 템플릿 메소드가 호출될 때마다 트랝개션이 새로 만들어지고 메소드를 빠져나오기전에 종료된다. 결국 JdbcTemplate의 메소드를 사용하는 UserDao는 각 메소드 마다 하나의 독립적인 트랜잭션으로 실행될 수 밖에 없다.

![](https://i.imgur.com/NdpwiXL.jpg)
데이터에 액세스 코드를 DAO로 만들어서 분리해놓았을 경우에는 이처럼 DAO메소드를 호출할 때 마다 하나의 새로운 트랜잭션이 만들어지는 구조이다. 이럴경우 비지니스로직을 담고있는 User service내에서 진행되는 여러가지 작업을 하나의 트랜잭션으로 묶는것은 불가능해진다.
그렇다면 upgaradeLevels()와 같이 여러번 DB에 업데이트를 해야하는 작업을 하나의 트랜잭션으로 묶으려면 그 작업이 진행되는 동안 DB 커넥션도 하나만 사용돼야 한다.앞에서 설명한것처럼 트랜잭션은 Connection 오브젝트 안에서 만들어지기 때문이다. 하지만 현재는 UserService에서 DB커넥션을 다룰 수 있는 방법이 없다.

**비지니스 로직 내의 트랜잭션 경계설정**
이 문제를 해결하기 위해 DAO메소드 안으로 upgradeLevels 메소드의 내용을 옮기는 방법을 생각해볼 수 있다. 하지만 이렇게 되면 비지니스로직과 데이터로직을 한데 묶어버리는 결과를 만들 수 있다. 이렇게 되면 성격과 책임이 다른 코드를 분리하고, 느슨하게 연결해서 확장성을 좋게 하려고 한 노력들이 허투루 될 수 있다.
결국엔 이런문제를 해결하기위해서는 트랜잭션을 UserDao에서 UserService로 가지고와야한다. UserDao가 가진 SQL이나 JDBC API를 이용한 데이터 액세스 코드는 최대한 그대로 남겨둔 채로, UserService에는 트랜잭션 시작과 종료를 담당하는 최소한의 코드만 가져오게 만들면 어느 정도 책임이 다른 코드를 분리해둔 채로 트랜잭션 문제를 해결할 수 있다.
트랜잭션 경계를 upgradeLevels() 메소드 안에 두려면 DB커넥션도 이 메소드 안에서 만들고, 종료시킬필요가 있다. 결국 upgradeLevels() 메소드 안에 트랜잭션의 경계 설정을 리스트 다음과 같은 구조로 만들어야한다.

```java
//UserService
public void upgradeLevel () throw Exception{
  (1) DB Connection 생성
  (2) 트랜잭션 시작
  try {
    (3) DAO 메소드 호출
    (4) 트랜잭션 커밋

  }catch(Exception e){
    (5)트랜잭션 롤백
    throw e;
  }finally{
    (6)db Connection 종료
  }
}
```
위는 트랜잭션을 사용하는 전형적인 JDBC 구조이다. 그런데 여기서 생성된 Connection 오브젝트를 가지고 데이터 액세스 작업을 진행하는 코드의 UserDao의 update()메소드 안에 있어야 한다. 트랜잭션 때문에 DB커넥션과 트랜잭션 관련 코드는 어쩔 수 없이 UserService로 가져왔지만, 순수한 데이터 액세스 로직은 UserDao에 둬야 하기 때문이다.
여기에서 중요한점은 UserDao의 update()메소드는 반드시 UserService에서 만든 Connection 오브젝트를 파라미터로 받아 사용해야한다. 결국 UserDao소스는 아래와같이 수정되어야 할 것이다.
```java
public interface UserDao{
  public void add(Connection c, User user);
  public User get(Connection c, String id);
  ...
  public void update(Connection c, User user1);
}
```
트랜잭션을 담고 있는 Connection을 공유하려면 더해줄 일이 있다. UserService의 upgradeLevels함수는 직접 UserDao의 update()를 직접 호출하지 않는다. UserDao를 사용하는것은 사용자별로 업그레이드를 진행하는 upgradeLevel()메소드다. 결국 다음과 같이 update메소드에 Connection오브젝트를 사용하도록 파라미터로 전달해줘야한다.
```java
class UserService{
  public void upgradeLevels() throws Exception{
    Connection c = ...;
    ...

    try{
      ...
      upgradeLevel(c, user);
      ...
    }
    ....

    protected void upgradeLevel(Connection c, User user){
      user.upgradeLevel();
      userDao.update(c, user);
    }
  }
}

```
이렇게 Connection을 파라미터로 던짐으로써 upgradeLevels()메소드안에서 트랜잭션의 경계설정이 되었다.

**UserService트랜잭션 경계설정의 문제점**
Userservice 와 UserDao를 이런식으로 수정하면 트랜잭션 문제는 해결할 수 있겠지만 많은 문제점이 생긴다.

첫째는 DB커넥션을 비롯한 리소스의 깔끔한 처리르 가능하게 했던 JdbcTemplate을 더이상 활용할 수 없다는 점이다. 결국 JDBC API를 직접사용하는 초기 방식으로 돌아가야한다.
둘째는 DAO의 메소드와 비지니스 로직을 담고 있는 UserService의 메소드에 Connection 파라미터가 추가 돼야한다는 문제점이다.
셋째는 Connection 파라미터가 UserDao 인터페이스에 메소드가 추가되면 더이상 데이터 액세스 기술에 독릭적일 수가 없다. JPA나 하이버네이트로 UserDao의 구현방식을 변경하려곻면 Connection 대신 EntityManager나 Session 오브젝트를 UserDao메소드가 전달받도록 해야한다. 결국 UserDao인터페이스는 바뀔것이고, 그에 따라 UserService코드도 함께 수정돼야 한다.
마지막은 DAO메소드에 Connection 파라미터를 받게하면 테스트 코드에도 영향을 미친다. 지금까지 DB커넥션은 전혀 신경 쓰지 않고 테스트에서 UserDao를 사용할 수 있었는데 이제는 테스트 코드도 직접 Connection오브젝트를 일일이 만들어 DAO메소드를 호출해야한다.

####  트랜잭션 동기화
먼저 Connection을 파라미터로 넘겨야하는 문제 부터 해결해보자. UserService의 upgradeLevels()메소드가 트랜잭션 경계설정을 해야 한다는건 피할 수 없다. 대신 여기서 생성된 Connection 오브젝트를 계속 메소드의 파라미터로 전달하다가 DAO 를 호출할 때 사용하게 하는건 피하고 싶다. 이를 위해 스프링이 제안하는 방법은 독립적인 트랜잭션 동기화 방식이다. 트랜잭션 동기화란 UserService에서 트랜잭션을 시작하기 위해 만든 Connection 오브젝트를 특별한 저장소에 보관해두고, 이후에 호출되는 DAO의 메소드에서는 저장된 Connection을 가져다가 사용하게 하는것이다.

![](https://i.imgur.com/VaK8jK0.png)

(1) UserService는 Connection을 생성한다
(2) 이를 트랜잭션 동기화 저장소에 저장해두고 Connection의 Autocommit을 false로 변경한다
(3)update 메소드가 호출되고, update()메소드 내부에서 이용하는 JdbcTemplate에서는 먼저 4번행동을 진행한다.
(4)트랜잭션 동기화저장소에 Connection이 있는지 확인한다.
(5)connection을 이용해 PreparedStatement를 만들어 수정 SQL을 실행한다.
(6) 3번행동
(7) 4번행동
(8) 5번행동
(9) 3번행동
(10) 4번행동
(11) 5번행동
(12) 트랜잭션 커밋한다.
(13) 트랜잭션 동기화저장소에 커넥션을 제거한다.

트랜잭션 동기화 저장소는 작업 스레드마다 독립적으로 Connection 오브젝트를 저장하고 관리하기 때문에 다중 사용자를 처리하는 서버의 멀티스레드를 환경에서동이 날 염려는 없다.

**트랝개션 동기화 적용**
문제는 멀티스레드 환경에서도 안전한 트랜잭션 동기화 방법을 구현하는 일이 기술적으로 간단하지 않다는 점인데, 다행히도 스프링은 JdbcTemplate과 더불어 이런 트랜잭션 동기화 기능을 지원하는 간단한 유틸리티 메소드를 제공하고 있다.

아래는 트랜잭션 동기화 방법을 적용한 UserService클래스의 코드다.

```java
public void upgradeLevels() throws SQLException {

        TransactionSynchronizationManager.initSynchronization();
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);

        try {
            final List<User> users = userDao.getAll();

            for (User user : users) {
                if (canUpgradeLevel(user)) {
                    upgradeLevel(user);
                }
            }

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(this.dataSource);
            TransactionSynchronizationManager.clearSynchronization();
        }

    }
```
스프링이 제공하는 트랜잭션 동기화 관리 클래스는 TransactionSynchronizationManager다. 클래스를 이용해 먼저 트랜잭션 동기화작업을 초기화 하도록요청한다. 그다음 DataSourceUtils 메소드를 통해 트랜잭션 동기화 저장소와 바인딩을 하고 Connection을 얻는다. 그다음 흐름에 맞게 트랜잭션에을 Commit해주거나 롤백을 해주면 된다.

**JdbcTemplate과 트랜잭션 동기화**
JdbcTemplate을 호출하면 혼자서 Connection을 열고 알아서 닫는 역할을 한다. 여기에 더불어 만약 트랜잭션 동기화 저장소에 커넥션이 있으면 JdbcTemplte은 새로 Connection을 생성하지않는다.

####  트랜잭션 서비스 추상화
만약 하나의 애플리케이션에 2개의 데이터베이스를 붙여야한다면 어떻게 처리할 수 있을까. 우선 한개 이상의 DB로의 작업을 하나의 트랜잭션으로 만드는건 JDBC의 Connection을 이용한 트랜잭션 방식인 로컬 트랜잭션으로는 불가능하다. 왜냐하면 로컬트랜잭션은 하나의 DB Connection에 종속되기 때문이다. 따라서 각 DB와 독립적으로 만들어지는 Connection을 통해서가 아니라, 별도의 트랜잭션 관리자를 통해 트랜잭션을 관리하는 글로벌 트랜잭션 방식을 사용해야한다. 글로벌 트랜잭션을 적용해야 트랜잭션 매니저를 통해 여러개의 DB가 참여하는 작업을 하나의 트랜잭션으로 만들 수 있다. 또 JMS(자바 메시징 시스템)와 같은 트랜잭션 기능을 지원하는서비스도 트랜잭션에 참여시킬 수 있다.
자바는 JDBC외에 이런 글로벌 트랜잭션을 지원하는 트랜잭션 매니저를 지원하기 위한 API인 JTA(JAVA TRANSACTION API)를 제공한다.
아래는 JTA를 통해 여러개의 DB또는 메시징 서버에대한 트랜잭션을 관리하는 방법을 보여준다.

![](https://i.imgur.com/cFYRi44.png)
애플리케이션에서는 기존의 방법대로 DB는 JDBC, 메시징 서버라면 JMS같은 API를 사용해서 필요한 작업을 수행한다. 단, 트랜잭션은 JDBC나 JMS API를 사용해서 직접 제어하지 않고 JTA를 통해 트랜잭션 메니저가 관리하도록 위임한다. 트랜잭션 매니저는 DB와 메시징 서버를 제어하고,관리한느 각각의 리소스매니저와 XA 프로토콜을통해 연결된다. 이를 통해 트랜잭션 매니저가 실제 DB와 메시징 서버의 트랜잭션을 종합적으로 제어할 수 있게 되는것이다.

아래는 JTA를 이용한 트랜잭션 처리 코드의 전형적인 구조는 아래와같다.
```java
InitialContext ctx = new InittialContext();
UserTransaction tx = (UserTransaction)ctx.lookup(USER_TX_JNDI_NAME);
// JNDI를 이용해 서버의 UserTransaction오브젝트를 가져온다.

tx.begin();
Connection c = dataSource.getConnection();

try{
  tx.commit();
}catch (Exception e){
  tx.rollback();
  throw e;
}finally{
  c.close();
}

```
코드의 구조는 JDBC 트랜잭션과 비슷하다. 만약 JDBC트랜잭션을 글로벌 트랜잭션으로 바꾸려면 UserService 의 기존 트랜잭션 소스를 변경해줘야한다.
문제는 여기에서 하이버네이트를 이용한 트랜잭션 관리코드로 다시바꿔야한다면 다시한번 UserService에 모든 소스를 수정해야한다.

**스프링의 트랜잭션 서비스 추상화**
스프링은 이러한 문제를 해결하기위해서 추상화 기술을 제공한다. 이를 이용하면 각각의 트랜잭션 관리 API를 이용하지 일관된 방식으로 트랜잭션을 제어하는 트랜잭션 겨계설정 작업이 가능해진다. 아래는 스프링이 제공하는 트랜잭션 추상화 계층구조다.

![](https://i.imgur.com/Qcj9WFK.png)

스프링이 제공하는 트랜잭션 추상화 방법을 UserService에 적용해보면 아래와 같은 코드가 될 수 있다.

```java
public void upgradeLevels() {

PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
    TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

    //트랜잭션의 시작

    try {
        final List<User> users = userDao.getAll();

        for (User user : users) {
            if (canUpgradeLevel(user)) {
                upgradeLevel(user);
            }
        }

        transactionManager.commit(status);
    } catch (RuntimeException e) {
        transactionManager.rollback(status);
        throw e;
    }
}
```
스프링이 제공하는 트랜잭션 경계설정을 위한 추상 인터페이스는 PlatformTransactionManager다. JDBC의 로컬 트랜잭션을 이용한다면 PlatformTransactionManager를 구현한 DataSourceTransactionManager를 사용하면 된다. 사용할 DB의 DataSource를 생성자 파라미터로 넣으면서 DataSourceTransactionManager의 오브젝트를 만든다. 그리고 트른잭션을 시작하고 작업을 완료하고 커밋후 커넥션이 종료 된다.

**트랜잭션의 기술 분리**
트랜잭션 처리를 PlatformTransactionManager를 통해 추상화했기때문에 어떤 트랜잭션 API 구현체를 쓰느냐에 따라서 트랜잭션 API가 결정된다. 위와 같은경우에는 DataSourceTransactionManager를 생성함으로 DataSourceTransactionManager API를 사용하고 만약 JTATransactionManager를 생성하게되면 JTATransactionManager API를 사용하게 되는것이다.

### 5.3 서비스 추상화 단일 책임 원칙
**수직, 수평 계층구조와 의존관계**
UserDao와 UserService는 각각 담당하는 코드의 기능적인 관심에 따라 분리되고, 트랜잭션 추상화는 이와 좀 다르게 계층의 특성을 갖는 코드로 분리 되었다.
다음은 만들어진 사용자 관리 모듈의 의존관계를 나타낸다.
![](https://i.imgur.com/G4zey8Z.jpg)
위 그림을 보면UserService는 UserDao 액세스로직이 바뀌거나, 데이터 액세스 기술이 바뀐다고 할지라도 UserService의 코드에는 영향을 주지 않게 되었다.

**단일 책임 원칙**
이런 적절한 분리가 가져오는 특징은 객체지향 설계의 원칙중의 하나인 단일책임원칙으로 설명할 수 있다. 단일책임원칙은 하나의 모듈은 한가지 책임을 가져야한다는 의미다.
지금 UserService는 트랜잭션이 기술이 바뀌거나, 서버 환경이 바뀌거나 등등 바뀌어도 UserService는 변할일이 없다. UserService에 비지니스로직이 추가되지 않는이상 이것이 단일 책임원칙을 잘 준수한것이다.

### 5.4 메일 서비스 추상화

#### 메일 발송 기능
다음은 비지니스 요구사항에 의해 메일 기능이 추가되었다고 가정해보자. 그 요구 사항으로 유저의 티어를 업그레이드하고 이메일 보내야한다. 그래서 유저 서비스의 아래와 같은 소스가 추가되어졌다.
```java
protected void upgradeLevel(User user) {
    user.upgradeLevel();
    userDao.update(user);
    sendUpgradeEMail(user);
}

private void sendUpgradeEMail(User user) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "mail.ksug.org");
		Session s = Session.getInstance(props, null);

		MimeMessage message = new MimeMessage(s);
		try {
			message.setFrom(new InternetAddress("useradmin@ksug.org"));
			String email = user.getEmail();
			InternetAddress address = new InternetAddress(email);

			message.addRecipient(Message.RecipientType.TO, address);
			message.setSubject("Upgrade 안내");
			message.setText("사용자님의 등급이 " + user.getLevel().getName() + "로 업그레이드 되었습니다.");
		} catch (AddressException e) {
			throw new RuntimeException();
		} catch (MessagingException e) {
			throw new RuntimeException();
		}
	}
```
하지만 위의 메일 테스트를 하기위해서는 JavaMail메일서버또는 메일을 보내기위한 기능이 반드시 필요하다. 하지만 테스트코드를 돌려서 테스트할 때 메일서버를 사용하여 메일을 테스트할 때마다 보내지는것은 자원낭비이다. 이러한 문제점을 해결하기 위해서 JavaMail을 보내는 기능 자체를 인터페이스를 만들어 로컬과 운영환경에따라서 메일이 나가게 추상화 할 수 있다.

#### 테스트를 위한 서비스 추상화
메일 서버 테스트 또한 DataSoure처럼 로컬에서는 local 디비로 교체되어지고 개발에서는 개발디비 처럼 바꿔주는것처럼 말이다. 하지만 JavaMail API는 이방법을 적용할수 없다. JavaMail은 DataSource처럼 인터페이스로 만들어져서 구현을 바꿀 수 있는게 없다. JavaMail은 확장이나 지원이 불가능하도록 만들어진 가장 악명높은 표준 API중 하나로 알려져있다.
이러한 문제점을 해결해주기위해 스프링에서는 JavaMail에 대한 추상화 기능을 제공해주는 MailSender Interface가 아래와 같이 있다.

```java
pulbic interface MailSender{

  void send(SimpleMailMessage simpleMailMessage) {}
  void send(SimpleMailMessage[] simpleMailMessages) {}
}
```
위에 인터페이스를 사용하게 되면 유저 서비스에서 메일 보내는 기능이 아래와같이 될 수 있다.
```java
private void sendUpgradeEMail(User user) {
    SimpleMailMessage mailMessage = new SimpleMailMessage();
    mailMessage.setTo(user.getEmail());
    mailMessage.setFrom("useradmin@ksug.org");
    mailMessage.setSubject("Upgrade 안내");
    mailMessage.setText("사용자님의 등급이 " + user.getLevel().name());

    mailSender.send(mailMessage);
}
```
MailSender Interface를 쓰니 소스도 한결 깔끔해졌다. 이제 아래와같이 어떤 mailSender를 삽입하느냐에 따라 메일을 실제로 나가게 할수 있고 아닌경우에는 메일을 안나가게 할 수 있다.

```java
public UserService(UserDao userDao, PlatformTransactionManager transactionManager, MailSender mailSender) {
    this.userDao = userDao;
    this.transactionManager = transactionManager;
    this.mailSender = mailSender;
}
```
지금 아래 같은경우에는 DummyMailService를 삽입함으로써 메일 테스트가 가능한경우이다. 만약 실제 메일서비스를 원하면 JavaMailSenderImpl 을 의존성 주입을 하면된다.
```java
@Bean
public UserService userService() throws ClassNotFoundException {
    return new UserService(userDao(), platformTransactionManager(), new DummyMailService());
}
```
아래는 DummyMailService클래스의 소스이다.
```java
public class DummyMailService implements MailSender {
    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
    }
    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
    }
}
```
다음 그림은 스프링이 제공하는 MailSender 인터페이스를 핵심으로하는 메일 전송 서비스 추상화 구조이다.
![](https://i.imgur.com/WsHWkzj.png)

이제 JavaMail이 아닌 다른 메시징 서버의 API를 이용해 메일을 전송해야할경우 그기능을 구현하고 의존성 주입만 하게되면 UserService와같이 다른 소스에 영향을 안미치고 다른 메시징 서버의 API기능으로 쉽게 변경할 수 있게 된다.

#### 목 오브젝트를 이용한 테스트
방금 MailService같이 요청만하고 응답을 받지 않는경우가 있을 수 있고 반대로 요청하고 결과값을 받아 그것으로 후속 처리를 해야할경우가 있을 수 있다. 이런 후속 처리를 하기위해서는 우리는 목오브젝트를 이용할 수 있다.
