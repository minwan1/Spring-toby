#토비의 스프링
이글은 [토비님의 스프링책](http://book.naver.com/bookdb/book_detail.nhn?bid=7006516)을 보고 요약한 내용입니다

# 1. 오브젝트와 의존관계
**들어가며**
스프링이 자바에서 가장 중요하게 가치를 두는것은 바로 객체지향프로그래밍이 가능한 언어라는 점이다.  자바 엔터프라이즈 기술의 혼란속에서 잃어버렸던 객체지향 기술의 진정한 가치를 회복시키고 폭넓은 혜택을 누릴 수 있도록하는것이 스프링 철학이다. 그래서 스프링에서 관심을 많이두는것은 오브젝트의 생명주기이다. 그렇기 때문에 우리는 스프링이 어떤것이고, 무엇을 제공하는지 보다는 스프링이 관심을 갖는 대상인 오브젝트의 설계와 구현, 동작원리에대해 관심을 가져야한다.

## 1.1 초난감 DAO
먼저 간단하게 아래와같이 JDBC를 이용한 User CURD를 다룰것이다. 예제에서는 롬복을 사용하여 객체를 다룰것이다. 그리고 데이터베이스는 Inmemory DB인 H2를 사용할것이다.

```java
@Getter
@Setter
@NoArgsConstructor
public class User {
    private String id;
    private String name;
    private String password;


    @Builder
    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }


}

```

유저의 정보에 접근할 수 있는 DAO를 아래와같이 만들자.

```java
public class UserDao {
    public void add(User user) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/mem:testdb;MVCC=TRUE", "sa", "");

        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values (?, ?, ?)");
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        ps.executeUpdate();

        ps.close();
        c.close();
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        Connection c = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/mem:testdb;MVCC=TRUE", "sa", "");

        PreparedStatement ps = c.prepareStatement("select * from users where id = ?");
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();
        rs.next();

        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));

        rs.close();
        ps.close();
        c.close();

        return user;
    }
}

```
그다음은 실제 User 정보가 담길 디비를 아래와같이 만들어보자.
```sql
CREATE TABLE users (
  id          varchar(10) primary key,
  name varchar(20) not null,
  password varchar(10) not null
);
```

이제 만들어진 코드를 바탕으로 테스트 코드를 아래와같이 작성해보자.

```java
public static void main(String[] args) throws ClassNotFoundException, SQLException {
    UserDao dao = new UserDao();

    User user = new User();
    user.setId("whiteship");
    user.setName("백기선");
    user.setPassword("married");

    dao.add(user);

    System.out.println(user.getId() + " 등록 성공");

    User user2 = dao.get(user.getId());
    System.out.println(user2.getName());
    System.out.println(user2.getPassword());
    System.out.println(user2.getId() + " 조회 성공");
}

```

위와같이 이제 테스트코드를 돌리게되면 데이터베이스를 통해 유저를 등록하고 조회를 할 수 있게 된다. 사실 위코드에서 소스가 작동하는데는 큰문제가 없다. 하지만 잠재적으로 엄청난 버그를 가지고있는 소스임은 분명하다. 그래서 위의 소스를 좀더 안전하고, 유지보수하기 좋은 소스를 만들기위해서 스프링과 결합하여 좀 더 효과적으로, 객체지향적으로 만들어나가는 방법에 대해 알아볼것이다.

## 1.2 DAO 분리

### 커넥션만들기의 추출

먼저 중복된 코드가 무엇인지에대해 생각하여야한다. 먼저 앞에서 만든 UserDao의 중복코드를 제거하기 위해서 관심사항들을 뽑아보자.

* DB와 연결을 위한 커넥션을 가져오는 부분이다.
* 사용자 등록을 위해 DB에 보낼 SQL 문장을 담을 Statement를 만들고 실행하는 것 또한 계속 중복되고 있다.
* 셋째는 작업이 끝나고 리소스들을 종료해주는 부분이다.

먼저 DB와 연결을 위한 커넥션 부분을 제거해보자. getConnetion 메소드로 빼서 연결이 필요할때는 그메소드를 호출하는것이다.
```java
public User get(String id)  throws ClassNotFoundException,SQLException{
  Connection c = getConnetion();
}

private Connection getConnection() throws ClassNotFoundException,SQLException{
  class.forName("com.mysql.jdbc.Driver");
  Connection C = DriverManager.getConnection("jdbc:mysql://localhost/springbook","spring",book);
  return c
}
```
위에 getConnection메소드는 관심의 종류에 따라 메소드 구분 해놓았다. 그렇기 때문에 한가지 관심 예를들어 데이터베이스의 종류를 바꾸는 등의  문제에서 하나의 메소드에 소스만 변경하게 되면 손쉽게 데이터 베이스를 변경할 수 있게 해준다. 기존 소스같은경우에는 스키마등이 변경된다고 치면 모든 스키마 URL등을 변경해줘야 했을것이다.

### DB커넥션 만들기의 독립
이번에는 좀더 변화를 반기는 DAO를 만들것이다.
**상속을 통한 확장**
![](https://i.imgur.com/zpT9cHZ.png)
위그림과 같이 UserDao를 추상클래스로 선언 하면 좀더 확장성 있는 소스가 될 것이다. 이렇게 선언하게되면 추상클래스에 add와 get만 메소드를 구현하고 getConnection은 깡통메소드로 나두게되면 NUserDao, DUserDao에서 getConnection만 구현하게되면 2개의 각기 다른 디비를 붙일 수 있다.

이렇든 UserDao와같은 슈퍼클래스는 기본적인 로직의 흐름(커넥션가져오기,sql생성,실행,반환)만들고 서브클래스는 메소드를 구현하는 패턴을 템플릿 메소드 패턴이라고 한다. 템플릿 메소드 패턴은 스프링에서 애용하는 패턴이다.

![](https://i.imgur.com/RSH9tOC.png)

## 1.3 DAO의 확장
모든 오브젝트는 변한다. 그런데 오브젝트가 다 동일한 방식으로 변하는건 아니고 공통 관심사에 따라 변한다. 지금까지 크게 두가지를 다뤘다.
1. 데이터를 어떻게 접근할것인가
2. 어떻게 데이터베이스를 쉽게 변경할것인가.

위의 소스로 장점은 데이터 접근 방식이 수정된다면 우리는 UserDao를 변경할것이다. 만약 DB에서 꺼내온 정보를바탕으로 뭔가 정보가 수정된다면 그 구현체들 MUserDao, DUserDao가 수정될것이다. 이것은 것으로 보면 서로에게 영향을 안주는것같지만 상속이라는 방법을 사용했기때문에 지독한 의존성을 가진다. 다음에서부터는 그 의존성을 좀더 느슨하게 할것이다.

### 클래스의분리
DB 커넥션과 관련된 부분을 서브클래스가 아니라, 아예 별도의 클래스로 분리해보자. 하지만 여전히 UserDao 에서 클래스를 생성해야하기때문에 UserDao는 ConnectionMaker클래스에 의존적이다.
![](https://i.imgur.com/818CZkt.png)

### 인터페이스의 도입

위에서 결국엔 클래스를 분리해도 데이터베이스를 연결하기 위해서는 지독한 의존성을 가지게된다. 이문제를 해결하기위해서는 서로 긴밀하게 연결되어 있지 않도록 중간에 추상적인 느스한 연결고리가 필요하다. 추상화란 어떤 것들의 공통적인 성격을 뽑아내어 이를 따로 분리하는 작업이다. 자바가 추상화를 위해 제공하는 가장 유용한 도구는 바로 인터페이스이다. 인터페이스를 통해 추상화를 하게되면 최소환의 통로를 통해 접근하는 쪽에서는 오브젝트를 만들때 사용할 클래스가 무엇인지 몰라도 된다.

![](https://i.imgur.com/iCN1k2t.png)

인터페이스를 도입했지만 여전히 자바에서 인터페이스를 통해 객체를 생성할때 아래와같이 new DConnectionMaker(); 선언을 해줘야하기떄문에 근본적인 문제가 해결되지않는다.
```java
public UserDao(){
  connectionMaker = new DConnectionMaker();
}
```
### 관계설정 책임의 분리
![](https://i.imgur.com/ZBKuVCa.png)
그래서 이러한 근본적인 문제를 해결하기 위해서는 인터페이스를 통해서 처음부터 관계를 설정해주면된다. 아래와같이.

```java
public interface ConnectionMaker {
    public Connection makeConnection() throws ClassNotFoundException, SQLException;
}

public class DConnectionMaker implements ConnectionMaker {
    public Connection makeConnection() throws ClassNotFoundException, SQLException {
        // D 사의 독자적인 방법으로 Connection 을 생성하는 코드
    }
}
public class UserDao {
    private ConnectionMaker connectionMaker; ㅡ> 인터페이스를 통해 오브젝트에 접근, so 구체적 클래스 정보가 필요 없다.

    public UserDao(ConnectionMaker connectionMaker){
        this.connectionMaker = connectionMaker;  
    }

    public void add(User user) throws ClassNotFoundException, SQLException {
        Connection c = connectionMaker.makeConnection();
      ㅡ> 인터페이스에 정의된 메소드를 사용하므로 클래스가 바뀐다고 메소드 이름이 변경 되지 않는다.
    }

    public user get(String id) throws ClassNotFoundException, SQLException {
        Connection c = connectionMaker.makeConnection();
    }
}

```
이제 이렇게 의존성 주입을 해주게되면 ConnectionMaker의 역할을 할 수 있는 어떤 클래스가 들어와도 UserDao기능을 수행할 수 있게 된다.



```java
public class UserDaoTest{
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        ConnectionMaker connectionMaker = new DConnectionMaker();
        // UserDao가 사용할 ConnectionMaker 구현 클래스를 결정하고 오브젝트를 만든다.

        UserDao dao = new UserDao(connectionMaker);
        // 1. UserDao 생성
        // 2. 사용할 ConnectionMaker 타입의 오브젝트 제공, 결국 두 오브젝트 사이의 의존관계 설정
    }
}
```

최종적으로 UserDaoTest와같이 UserDao를 즉 3자 호출하는곳에서 의존성 주입을하게되면 아래와같이 불필요한 의존관계를 끊을 수 있게된다. 이렇게되면 어떤 Connection이 들어와도 UserDao를 클래스 수정없이 UserDao클래스 기능을 사용할 수 있게된다.

![](https://i.imgur.com/XghdksD.png)

### 1.3.4 원칙과 패턴

#### 개방 폐쇄원칙
개방 폐쇄 원칙은 깔끔한 설계를 위해 적용 가능한 객체지향 설계 원칙 중의 하나다. 클래스나 모듈은 확장에는 열려 있어야하고 변경에는 닫혀 있어야한다고 할 수 있다. UserDao는 DB연결 방법이라는 기능을 확장하는데는 열려있다. UserDao에는 전혀 영향을 주지 않고도 얼마든지 확장 가능하다. 잘설계된 객체지향 클래스의 구조를 살펴보면 바로 이 개방 폐쇄 원칙을 아주 잘 지키고 있다. 인터페이스를 사용해 확장 기능을 정의한 대부분의 API는 바로 개방 폐쇄 원칙을 잘 따르고 있다고 볼 수 있다.


#### 높은 응집도와 낮은 결합도

**높은 응집도 (같은 기능끼리 모여있다는것)**
응집도가 높다는것은 하나의 모듈, 클래스가 하나의 책임 또는 관심사에만 집중되어 있다는 뜻, 하나의 모듈에서만 변경이 많이 일어나면 다른 모듈은 변경이 필요가 없을때를 말한다. 하나의 클래스에 여러가지 관심사가 모여있다면 그 관심사 하나가 변경되면 다른 영향을 미치짖 않는지 확인해야하는 이중의 부담이 생긴다. 예를들어 기존의 NConnectionMaker를 개선해서 2.0을 만들었다해보자. 그렇게되면 NConnectionMaker를 테스트하기위해서 모든 DAO를 테스트할필요는없다. 높은 응집도때문에 그냥 NConnectionMaker만 테스트해도 충분하다. ConnectionMaker를 분리해서 높은 응집도를 가지고있기 때문이다.

**낮은 결합도**
낮은 결합도는 높은 응집도 보다 더 민감한 원칙이다. 책임과 관심사가 다른 오브젝트 또는 모듈과는 낮은 결합도, 즉 느슨하게 연결된 형태를 유지하는것이 바람직하다. 결합도가 낮아지면 변화에 대응하느 속도가 높아지고, 구성이 깔끔해진다. 또한 확장하기에도 매우 편리하다. 예를들면 위에서 보여준 예제처럼 인터페이스를 통한 ConnectionMaker구현이 있을 수 있다.

**전략패턴**
개선한 UserDaoTest - UserDao - ConnectionMaker 구조를 디자인 패턴의 시각으로 보면 전략 패턴에 해당한다고 볼 수 있다. 전략패턴은 디자인 패턴의 꽃이라고 불릴만큼 다양하게 자주 사용되는 패턴이다. 개방 폐쇄 원칙의 실현에도 가장 잘 들어 맞는 패턴이라고 볼 수 있다. 전략 패턴은 자신의 기능 Context에서,필요에 따라 변경이 필요한 알고리즘을 인터페이스를 통해 통째로 외부로 분리시키고, 이를 구현한 구체적인 알고리즘 클래스를 필요에 따라 바꿔서 사용 할 수 있게 하는 디자인 패턴이다.
UserDao는 전략패턴의 컨텍스트에 해당한다. 컨텍스트는 자신의 기능을 수행하는데 필요한 기능 중에서 변경 가능한, DB 연결 방식이라는 알고리즘을 ConnectionMaker라는 인터페이스로 정의하고, 이를 구현한 클래스, 즉 전략을 바꿔가면서 사용할 수 있게 분리했다.

전략패턴은 컨텍스트(UserDao)를 사용하는 클라이언트(UserDaoTest)는 컨텍스트가 사용할 전략 (ConnectionMaker를 구현한 클래스, 예 DConnectionMaker)을 컨텍스트의 생성자등을 통해 제공해주는게 일반적이다.

## 1.4 제어의 역전(IoC)

### 오브젝트 팩토리

UserDaoTest는 기존에 UserDao가 직접 담당하던 기능, 즉 어떤 ConnectionMaker구현 클래스를 사용할지를 결정하는 기능을 엉겁결에 떠 맡았다. UserDao가 ConnectionMaker인터페이스를 구현한 특정 클래스로부터 완벽하게 독립할 수 있도록 UserDao의 클라이언트인 UserDaoTest가 그 수고를 담당하게 된 것이다.
그런데 처음목적은 UserDaoTest는 UserDao의 기능이 잘 동작하는지를 테스트하려고 만든 것이다. 그런데 지금 또다른 책임까지 떠 맡고 있으니 UserDaoTest는 2개의 관심사를 가지고 있는것이다. 그러므로 두개의 관심사를 분리해줘야한다.

#### 팩토리
팩토리 클래스의 역할은 객체의 생성방법을 결정하고 그렇게 만들어진 오브젝트를 돌려주는 것이다. 팩토리 클래스의 역할을 맡을 클래스는 DaoFactory이다. DaoFactory는 UserDaoTest에서는 DaoFactory에 요청해서 미리 만들어진 UserDao오브젝트를 가져와 사용하게 만든다.


```java

public class DaoFactory{
  public UserDao userDao(){
    //팩토리메소는 UserDao탕ㅂ의 오브젝트를 어떻게 만들고 어떻게 준비시킬지 결정한다.
    ConnectionMaker connectionMaker = new DConnectionMaker();
    UserDao userDao = new UserDao(connectionMaker);

    return userDao;
  }
}

```
아래와같이 이제 UserDaoTest는 더이상 UserDao가 어떻게 생성되는지 신경쓰지 않고 테스트하는것에만 집중할 수 있게된다.

```java
public class UserDaoTest{
  public static void main(String[] args) throws ClassNotFoundException,SQLException{
    UserDao dao = new DaoFactory.userDao();
    ...
  }
}

```

#### 설계도로서의 팩토리
![](https://i.imgur.com/IxtsfLd.png)
그림과 같이 UserDaoTest는 DaoFactory를통해 DConnectionMaker를 주입받아 사용하게된다.

### 오브젝트 팩토리의 활용
만약 추가적인 Dao들이 추가된다고 생각해보자. 그러면 아래와같이 반복되는 코드가 발생하게된다.
```java
public class DaoFactory{


  public UserDao userDao(){
    return new UserDao(new DConnectionMaker();)
  }

  public AccountDao userDao(){
    return new UserDao(new DConnectionMaker();)
  }

  ...

}
```
위의 문제를 해결할려면 ConnectionMaker를생성하는 아래의소스와같이 공통으로 뽑아내는 방법이있다.

```java

public class DaoFactory{


  public UserDao userDao(){
    return new UserDao(new DConnectionMaker();)
  }

  public AccountDao userDao(){
    return new UserDao(new DConnectionMaker();)
  }

  ...

  public ConnectionMaker connectionMaker(){
    return new DConnectionMaker();
  }

}
```
위와같이 소스를 변경하게되면 ConnectionMaker부분만 수정하게되면 Dao팩토리 메소드가 많아져도 ConnectionMaker의 메소드만 변경하게되면 모든 커넥션마커의 기능들이 수정되기때문에 시간을 절약할수 있게 된다.

### 제어권의 이전을 통한 제어관계 역전
일반적으로 프로그램의 흐름은 main() 메소드와 같이 프로그램이 시작되는 지점에서 다음에 사용할 오브젝트를 결정하고 결정한 오브젝트를 생성하고, 만들어진 오브젝트를 사용한다. 언제 어떻게 오브젝트를 만들지를 스스로 관장한다. 모든 종류의 작업을 사용하는 쪽에서 제어하는 구조이다. 제어의 역전은 이런 제어 흐름의 개념을 거꾸로 뒤집는 것이다. 제어의 역전에서는 모든 제어의 권한을 자신이 아닌 다른 대상에게 위임한다.
제어의 역전 개념은 이미 서블릿에서도 사용되고있다. 서블릿에 대한 제어권한을 가진 컨테이너가 적절한 시점에 서블릿 클래스의 오브젝트를 만들고 그안의 메소드를 호출한다. 이렇게 서블릿이나, JSP, EJB에서는 간단한 방식으로 제어의 역전 개념이 적용되어 있다.

## 1.5 Spring IoC
스프링의 핵심을 담당하는 건 바로 빈팩토리, 또는 ApplicationContext라고 불리는 것이다. 이것은 우리가 만든 DaoFactory가 하는 일을 스프링에서 좀더 일반화한 것이라고 할 수 있다

### 제어권의 이전을 통한 제어관계 역전
스프링에서는 스프링이 제어권을 가지고 직접 만들고 관계를 부여하는 오브젝트를 빈이라고 부른다. 스프링에서 빈의 생성과 관계설정 같은 제어를 담당하는 IoC 오브젝트를 빈팩토리라고 한다. 보통 빈팩토리보다는 이를 좀더 확장한 ApplicationContext를 주로 사용한다.
ApplicationContext는 별도의 정보를 참고해서 빈의 생성, 관계설정등의 제어 작업을 총괄한다. DaoFactory에서는 어떤 클래스의 오브젝트를 생성할지 어떻게 연결할것인지에 대해 설정을 해줬었다. 하지만 ApplicationContext는 직접 이런정보를 담지 않고 별도로 설정정보를 담고 있는 자바 @Configuration 설정되어져 있는 자바클래스의 정보를 가져와 이를 활용하는 범용적인 Ioc 엔진이라고 할 수 있다.

#### DaoFactory를 사용하는 ApplicationContext
DaoFactory를 스프링의 빈팩토리(ApplicationContext)가 사용할수있도록 설정 정보를 만들 것이다. 먼저 빈 팩토리를 위한 오브젝트 설정을 담당하는 클래스라고 인식할수 있도록 @Configuration 이라는 어노테이션을 추가한다. 그리고 오브젝트를 만들어주는 메소드에는 @Bean 이라는 어노테이션을 추가한다.

```java

@Configuration // 애플리케이션 컨텍스트 또는 빈 팩토리가 사용할 설정정보라는 표시
public class DaoFactory{

  @Bean // 오브젝트를 새엉을 담당하는 IoC용 메소드라는 표시
  public UserDao userDao(){
    return new UserDao(connectionMaker());
  }

  @Bean
  public ConnectionMaker connectionMaker(){
    return new DConnectionMaker();
  }
}

```
이제 아래와같이 준비된 ApplicationContext의 getBean()이라는 메소드를 이용해서 UserDao의 오브젝트를 가져올 수 있다.

```java

public class UserDaoTest{
  public static void main(String[] args) throws ClassNotFoundException,SQLException{
    ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
    UserDao dao = context.getBean("userDao",UserDao.class);
    ...
  }

}

```
context.getBean("userDao",UserDao.class);의 userDao는 DaoFactory 메소드의 이름이다. 어떻게 메소드의 이름을 설정하느냐에따라 빈을 가지고올때 String값을 변경해주면 된다.


### ApplicationContext의 동작 방식
스프링에서 ApplicationContext를 IoC컨테이너라고 하기도하고 간단히 스프링 컨테이너라고도 한다. @Configuration이 붙은 DaoFactory는 ApplicationContext가 활용하는 IoC설정정보다. 내부적으로 애플리케이션 컨텍스가 DaoFactory의 userDao() 메소드를 호출해서 오브젝트를 가져온 것을 클라이언트가 getBean()으로 요청할 때 전달 해준다. 아래 그림은 ApplicationContext의 사용되는 방식이다.

![](https://i.imgur.com/Uf9J8vi.png)

#### ApplicationContext를 사용했을때 장점

```
1. 클라이언트는 구체적인 팩토리 클래스를 알 필요가 없다.
2. 애플리케이션 컨텍스트는 종합 IoC서비스를 제공해준다.
  오브젝트가 만들어지는 방식, 오브젝트에 대한 후처리, 정보의 조합 설정 방식의 다변화, 인터셉티등 오브젝트를 효과적으로 활용할 수 있는 다양한 기능을 제공한다.
3. 애플리케이션 컨텍스트는 빈을 검색하는 다양한방법을 제공한다.
  타입만으로도 빈을 검색하거나 특별한 애노테이션 설정이 되어있는 빈을 찾을 수 도 있다.
```


## 1.6 싱글톤 레지스틀와 오브젝트 스코프
DaoFactory와 @Configuratuon애노테이션을 추가해서 스프링의 애플리케이션 컨텍스트를 사용하는것의 차이점에 대해서 알아볼것이다. 둘의 큰 차이점은 리턴해주는 빈의 동일성을 보장 해주는것이다. 먼저 아례의 예제를 보자

```java
DaoFactory factory = new DaoFactory();
UserDao dao1 = factory.userDao();
UserDao dao2 = factory.userDao();

System.out.println(dao1); //springbook.dao.UserDao@118f375
System.out.println(dao2); //springbook.dao.UserDao@117a8bd
```

출력결과에서 알수 있듯이 동일성이 보장되지 않는 객체들이 출력된다. 그럼 스프링 컨테이너를 이용해서 빈을 호출해 볼 것이다.

```java
ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);

UserDao dao3 = factory.userDao();
UserDao dao4 = factory.userDao();

System.out.println(dao3); //springbook.dao.UserDao@ee22f7
System.out.println(dao4); //springbook.dao.UserDao@ee22f7
```
스프링 컨테이너 즉 ApplicationContext를 통해 생성한 오브젝트는 동일성을 보장해준다. 여러번에 걸쳐 빈을 요청하더라도 매번 동일한 오브젝트를 리턴해준다.

### 싱글톤 레지스트리로서의 애플리케이션 컨텍스트
ApplicationContext는 싱글톤을 저장하고 관리하는 싱글톤 레지스트리이다. 스프링은 기본적으로 별다른 설정을 하지 않으면 내부에 생성하는 빈오브젝트를 모두 싱글톤으로 생성한다.

#### 서버 애플리케이션 싱글톤
스프링에서 싱글톤을 사용하는 이유는 스프링이 주로 적용되는 대상이 자바 엔터프라이즈 기술을 사용하는 서버 환경이기 때문이다. 물론 스프링으로 PC등에서 동작하는 독립형 윈도우 프로그램으로도 개발할수 있긴 하지만 극히 드물다.
스프링이 처음 설계됐던 대규모의 엔터프라이즈 서버환경은 서버 하나당 최대로 초당 수십에서 수백 번씩 브라우저나 여타 시스템으로부터의 요청을 받아 처리할 수 있는 높은 성능이 요구되는 환경이었다. 또 하나의 요청을 처리하기위해 데이터 액세스로직, 서비스로직, 비지니스로직,프레젠테이션로직등의 다양한 기능을 담당하는 오브젝트들이 참여하는 계층형 구조였다.
그런데 매번 클라이언트에서 요청이 들어 올때마다 각로직을 담당하는 오브젝트를 새로 만들어본다고 생각해보자. 요청 한번에 5개의 오브젝트가 새로 만들어지고 초당 500개의 요청이 들어오면 초당 2500개의 새로운 오브젝트가 생성된다. 1분이면 십오만개, 한시간이면 9백만개의 새로운 오브젝트가 만들어진다. 아무리 자바의 가비지컬렉션의 성능이 좋아졌다고 한들 이렇게 부하가 걸리면 서버가 감당하기 힘들다.
엔터프라이즈 환경에서의 서블릿은 대부분 멀티스레드 환경에서 싱글톤으로 동작한다. 서블릿 클래스당 하나의 오브젝트만 만들어 두고, 사용자의 요청을 담당하는 여러 스레드에서 하나의 오브젝트를 공유해 동시에 사용된다.
이렇게 애플리케이션 안에 제한된 수, 대개 한개의 오브젝트만 만들어서 사용하는 것이 싱글톤 패턴의 원리이다.따라서 서버 환경에서는 서비스 싱글톤의 사용이 권장된다.

#### 싱글톤 패턴의 한계
싱글통 패턴을 적용한 UserDao

```java
public class UserDao{
  private static UserDao INSTANCE;
  ...

  private UserDao(ConnectionMaker connectionMaker){
    this.connectionMaker = connectionMaker;
  }

  public static synchronized UserDao getInstance(){
    if(INSTANCE == null) INSTANCE = new UserDao(???);
    return INSTANCE;
  }
}
```
싱글톤의 단점
```
-private  생성자를 갖고 있기 때문에 상속할 수 없다.
-싱글톤은 테스트하기가 힘들다.
-서버환경에서는 싱글톤이 하나만 만들어지는것을 보장하지 못한다
-싱글톤의 사용은 전역상태를 만들 수 있기 때문에 바람직하지 못하다.
```

#### 싱글톤 레지스트리
자바의 기본적인 싱글톤 패턴의 구현 방식은 여러가지 단점이 있기 때문에 스프링은 직접 싱글톤 형태의 오브젝트를 만들고 관리하는 기능을 제공한다. 그것이 바로 싱글톤 레지스트리이다. 스프링 컨테이너는 싱글톤을 생성하고, 관리하고 공급하는 싱글톤 관리 컨테이너이기도 하다. 싱글톤 레지스트리는 스태틱메소드와 private생성자등을 사용해야하는 비정상적인 클래스가 아닌 평범한 자바 클래스를 싱글톤으로 활용하게 해준다.


### 싱글톤과 오브젝트의 상태
싱글톤은 멀티스레드 환경이라면 여러 스레드가 동시에 접근해서 사용할 수 있다. 따라서 상태 관리(전역변수)에 주의 해야한다. 기본적으로 클래스내에서 전역변수를 사용하지 말아야한다. 다중 사용자의 요청을 한꺼번에 처리하는 스레드들이 동시에 싱글톤오브젝트의 전역변수를 수정하는것은 매우 위험하다. 저장할 공간이 하나 뿐이니 서로 값을 덮어쓰고 자신이 저장하지 않은값을 읽어 오거나 삭제할수있기때문이다. 물론 읽기전용의 전역변수는 사용해도좋다. 이런 변수들은 final로 선언해주는거시 안전하다. 메소드안에는 지연변수(스택 영역에 저장되어져있다)이기떄문에 메소드 호출후에 초기화된다.

### 스프링 빈의 스코프
스프링 내에서 빈이 생성되고 존재하고 등의 적용되는 범위에대해 스프링에서는 이것을 스코프라고한다. 기본적으로 프로토타입,요청,세션등으로 분리하여 관리해주는 스코프 범위가 존재한다.


## 1.7 의존과계 주입(DI)
### 제어의 역전과 의존관계 주입
여기에서 한가지 짚고 넘어갈 것은 IoC라는 용어인데, IoC가 매우 느슨하게 정의돼서 폭넓게 사용되는 용어라는것이다. 때문에 스프링을 IoC 컨테이너라고만 해서는 스프링이 제공하는 기능의 특징을 명확하게 설명하지 못했다. 그래서 나온말이 IoC방식을 핵심을 짚어주는 의존관계 주입(DI)이다. 스프링의 IoC 기능의 대표적인 동작원리는 주로 의존관계 주입이라고 불린다. 스프링이 여타 프레임워크와 차별화돼서 제공해주는 기능은 의존관계 주입이라는 새로운 용어를 사용할 때 분명하게 드러난다. 초기에는 주로 IoC 컨테이너라고 불리던 스프링이 지금은 의존관계 주입 컨테이너 또는 그영문약자를 써서 DI컨테이너라고 더 많이 불리고 있다.

### 런타임 의존관계 설정
#### 의존관계
두 개의 클래스 또는 모듈이 의존관계에 있다고 말할 때는 항상 방향성을 부여해줘야 한다. 즉 누가 누구에게 의존하는 관계 있다는 식어야한다. A클래스가 B에 의존하고 있을때 B가 변동사항이 있으면 A에 영향을 미치게된다. 하지만 A가 변경된다고해서 B는 영향 받지 않는다.

#### UserDao의 의존관계
![](https://i.imgur.com/Am2R3Z3.png)
위 그림은 UserDao가 ConnectionMaker에 의존하고 있는 형태이다. ConnectionMaker인터페이스가 변한다면 그영향은 UserDao가 받을 것이다. 하지만 ConnectionMaker의 구현체인 DConnectionMaker등이 바뀌거나 내부적으로 메소드가 변화가 생겨도 UserDao에 영향을 주지 않는다. 이렇게 인터ㅔ이스대해서만 의존관계를 만들어두면 인터페이스를 구현 클래스와의 관계는 느슨해지면서 변환에 영향을 덜 받는 상태가 된다. 인터페이스를 통해 의존관계를 제한해주면 그만큼 변경에서 자유로워지는 셈이다.
그런데 모델이나 코드에서 클래스와 인터페이스를 통해 드러나는 의존관계말고, 런타임시에 오브젝트 사이에서 만들어지는 의존관계도 있다. 의존과계 또는 오브젝트 의존 관계인데, 설계 시점의 의존관계가 실체화된 것이라고 볼 수 있다. 런타임 의존관계는 모델링 시점의 의존관계와는 성격이 분명히 다르다.
인터페이스를 통해 설계 시점에 느슨한 의존관계를 갖는 경우에는 UserDao의 오브젝트가 런타임 시에 사용할 오브젝트가 어떤 클래스로 만든것인지 미리 알 수 가없다. 프로그램이 시작되고 UserDao 오브젝트가 만들어지고 나서 런타임 시에 의존관계를 맺는 대상, 즉 실제 사용대상인 오브젝트를 의존 오브젝트라고 한다.

의존관계 주입은 이렇게 구체적인 의존 오브젝트와 그것을 사용할 주체, 보통 클라이언트라고 부르는 오브젝트를 런타임시에 연결해주는 작업을 말한다. UserDao는 ConnectionMaker 인터페이스라는 매우 단순한 조건만 만족하면 어떤 클래스로부터 만들어졌든 상관없이 오브젝트를 받아 들이고 사용한다.

```
의존관계주입이란
-클래스 모델(UML)이나 코드에는 런타임 시점의 의존관계가 드러나지 않는다. 그러기 위해서는 인터페이스에만 의존하고 있어야한다.
-런타임 시점의 의존관계는 컨테이너나 팩토리 같은 제 3의 존재가 결정한다.
-의존관계는 사용할 오브젝트에 대한 레퍼런스를 외부주에서 제공(주입)해줌으로써 만들어진다.
```
의존관계 주입의 핵심은 설계 시점에는 알지 못했던 두 오브젝트의 관계를 맺도록 도와주는 제 3의 존재가 있다는것이다. DI에서 말하는 제 3의 존재는 바로 관계설정 책임을 가진 클래스 라고 볼 수 있다. DaoFactory,ApplicationContext,Ioc컨테이너등이 모두 외부에서 오브젝트 사이의 런타임 관계를 맺어주는 책임을 지닌 제 3의 존재라고 볼 수 있다.

#### UserDao의 의존관계 주입
다시 처음에 봤던 UserDao 에 적용된 의존관계 주입 기술을 다시 살펴보자. 인터페이스를 사이에 두고 UserDao와 ConnectionMaker구현클래스간에 의존관계를 느슨하게 만들긴 했지만, 마지막으로 남은 문제가 있었는데 아래소스와같이 UserDao가 사용할 구체적인 클래스를 알고 있어야 하는점이다.

```java
public UserDao(){
  connectionMaker = new DConnectionMaker();
}

```
이코드는 이미 설계 시점에 구체적인 런타임 의존관계 오브젝트를 알고 있다. 즉 DConnectionMaker오브젝트를 사용하겠다는 것 까지 UserDao가 결정하고 관리하고 있는셈이다.

이코드의 문제는 이미 런타임시의 의존관계가 코드 속에 미리 다 결정되어있다는 것이다. 그래서 Ioc방식을 써서 UserDao로부터 런타임 의존관계를 드러내는 코드를 제거하고, 제 3의 존재에 런타임 의존관계 결정을 권한을 위함하는것이다. 그래서 최종적으로 만들어졌던것이 DaoFactory이다. DaoFactory는 런타임시점에 UserDao가 사용할 ConnectionMaker타입의 오브젝트를 결정하고 이를 생성한후에 UserDao의 생성자 파라미터로 주입해줌으로써 런타임 의존관계를 맺게 해준다. 이렇게 런타임시 주입의 기능을 수행함으로써 DaoFactory를 DI/IoC컨테이너라고도 한다.

DI컨테이너느 자신이 결정한 의존관계를 맺어줄 클래스의 오브젝트를 만들고 이생성자를 파라미터로 오브젝트의 레퍼런스를 전달해준다. 그럼 아래 소스같이 생성자 파라미터를 통해 전달받은 런타임 의존관계를 갖는 오브젝트는 인스턴스 변수에 저장 해둔다.

```java
public class UserDao{

  private ConnectionMaker connectionMaker;

  public UserDao(ConnectionMaker connectionMaker){
    this.connectionMaker = connectionMaker;
  }

}
```
이렇게 해서 두 개의 오브젝트 간에 런타임 의존관계 만들어졌다. UserDao 오브젝트는 이제 생성자를 통해 주입받은 DConnectionMaker 오브젝트를 언제든 사용하면 된다. DI는 자신이 사용할 오브젝트에 대한 선택권과 생성 제어권을 외부로 넘기고 자신은 런타임시에 수동적로 주입받은 오브젝트를 사용한다는 점에서 IoC의 개념에 잘 들어 맞는다. 스프링 컨테이너의 IoC는 주로 의존관계 주입 또는 DI라는 데 초점이 맞춰져 있다. 그래서 스프링을 IoC컨테이너 외에도 DI컨테이너 또는 DI프레임워크라 한다.



### 의존관계 검색과 주입
스프링이 제공하는 IoC방법에서 오브젝트를 주입하는 방법 의존성 주입만 있는것은 아니다. 의존 관계를 맺는 방법이 외부로부터의 주입이 아니라 스스로 검색을 이용하는 의존관계 검색이 있다. 의존관계 검색은 자신이 필요로 하는 의존 오브젝트를 능동적으로 찾는다. 물론 자신이 어떤 클래스의 오브젝트를 이용할지는 결정하지 않는다. 의존관계 검색은 런타임 시 의존관계를 맺을 오브젝트를 결정하는 것과 오브젝트의 생성작업은 외부 컨테이너에게 맡기지만, 이를 가져올때는 메소드나 생성자를 통한 파라미터 전달이 아닌 아래 소스와같이 직접 컨테이너에게 요청하는 방식이다.

```java
public UserDao(){
  DaoFactory daoFactory = new DaoFactory();
  this.connectionMaker = daoFactory.connectionMaker();
}
```
위와같이 생성해도 여전히 자신이 어떤 ConnectionMaker 오브젝트를 사용할지 미리 알지 못한다. 여전히 코드의 의존대상은 ConnectionMaker인터페이스이기 때문이다. 런타임시에 DaoFactory가 만들어서 돌려주는 오브젝트와 다이내믹하게 런타임 의존관계를 맺는다. 따라서 IoC개념을 잘 따르고 있으며, 그혜택을 받는 코드다. 하지만 적용방법은 외부로부터의 주입이 아니라 스스로 IoC 컨테이너인 DaoFactory에게 요청하는것이다.
위소스의 작업을 일반화한 스프링의 애플리케이션 컨텍스트라면 미리 정해놓은 이름을 전달해서 그 이름에 해당하는 오브젝트를 찾게 된다. 따라서 이를 일종의 검색이라고 볼 수 있다. 또한 그 대상이 런타임 의존관계를 가질 오브젝트이므로 의존관계 검색이라고 부르는 것이다.
스프링의 IoC 컨테이너인 애플리케이션 컨텍스트는 getBean이라는 메소드를 제공한다. 바로 이메소드가 의존관계를 검색에 사용되는것이다. 이것은 아래의 예제와같이 사용 할 수 있다.
```java
public UserDao(){
  AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.clas);

  this.connectionMaker = context.getBean("connectionMaker",ConnectionMaker.class);
}
```

의존관계 검색은 기존 의존관계 주입의 거의 모든 장점을 갖고 있다. 하지만 코드면에서 좀 더 의존관계주입이 단순하고 깔끔하다. 의존관계 검색은 코드안에 오브젝트 팩토리 클래스나 스프링 API가 나타난다. 검색 방식을 이용하면 그안에서 컨텍스트를 만들고, 호출하고등의 불필요한 소스를 넣어줘야하기 때문이다. 따라서 대개는 의존관계주입방식을 사용하는것이 낫다.
```java

public class UserDaoTest{
  public static void main(String[] args) throws ClassNotFoundException,SQLException{
    ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
    UserDao dao = context.getBean("userDao",UserDao.class);
    ...
  }

}

```
하지만 위에 소스처럼 static 메소드인 Main같은경우에는 DI를 이용해 오브젝트를 주입받을 방법이 없기 때문이다. 이러한경우 말고는 의존성주입을 받아 사용하는게 소스면에서 훨씬 깔끔하다.

### 의존관계 주입의 응용
#### 부가기능 추가
다음 이런경우를 한번 생각해보자. DAO가 DB를 얼마나 많이 연결해서 사용하는지 파악하고 싶다. DB연결횟수를 카운팅하기 위해 무식한 방법으로 모든 DAO의 makeConnection() 메소드를 호출하는 부분에 새로 추가한 카운터를 증가시키는 코드를 넣는것은 엄청난 낭비이고 노가다다.  그리고 무엇보다 DAO코드를 손대는것은 지금까지 피하려고했던 행동을 하는 것이다.
DI 컨테이너에서라면 아주 간단한 방법으로 해결가능하다. DAO와 DB 커넥션을 만드는 오브젝트 사이에 열결횟수를 카운팅하는 오브젝트를 하나 더 추가하는 것이다.

```java
public class CountingConnectionMaker implements ConnectionMaker{
  int counter = 0;
  private ConnectionMaker realConnectionMaker;

  public CountingConnectionMaker realConnectionMaker(ConnectionMaker realConnectionMaker){
    this.realConnectionMaker = realConnectionMaker;
  }

  public Connection makeConnection() throws ClassNotFoundException,SQLException{
    this.counter++;
    return realConnectionMaker.makeConnection();
  }

  public int getCounter(){
    return this.counter;
  }
}
```
CountingConnectionMaker 클래스는 ConnectionMaker 인터페이스를 구현했지만 내부에서 직접 DB커넥션을 만들지 않는다. 대신 DAO가 DB커넥션을 가져올 때마다 호출하는 makeConnection()에서 DB연결횟수 카운터를 증가시킨다.CountingConnectionMaker는 자신의 관심사인 DB 연결횟수 카운팅 작업을 마치면 실제 DB 커넥션을 만들어주는 realConnectionMaker에 저장된 ConnectionMaker타입 오브젝트의 makeConnection()호출해서 그 결과를 DAO에 돌려준다.

생성자를 보면 ConnectionConnectionMaker도 DI를 받는것을 알 수 있다. UserDao는 ConnectionMaker의 인터페이스에만 의존하고 있기 때문에 ConnectionMaker 를 구현하고있는 어떤 구현체라도 DI가 가능하다. 그래서 DI받는 오브젝트를 DConnection오브젝트를 대신 CountingConnectionMaker를 받은것이다. 그리고 그 CountingConnectionMaker는 DConnectionMaker를 의존주입을 받은것이다.
![](https://i.imgur.com/EfluSD4.png)
![](https://i.imgur.com/Am2R3Z3.png)
이렇게해서 CountingConnectionMaker 재구성된 새로운 런타임 의존관계는 위 그림과 같다. 우의 그림을 코드라바꾸면 아래와같이 만들 수 있다.
```java
@Configuration
public class CountingDaoFactory{
  @Bean
  public UserDao userDao{
    return new UserDao(ConnectionMaker());
  }

  @Bean
  public ConnectionMaker connectionMaker(){
    return new CountingConnectionMaker(realConnectionMaker());
  }

  @Bean
  public ConnectionMaker realConnectionMaker(){
    return new DConnectionMaker();
  }
}
```

이제 커넥션 카운팅을 위한 실행 코드를 만들어보자. 기본적으로 UserDaoTest와 같지만 설정요 클래스를 CountingDaoFactory로 변경해줘야한다. 그리고 CountingConnectionMaker빈을 가져온다. 설정정보에 지정된 이름과 타입만 알면 특정 빈을 가져 얼 수 있으니 CoutingConnectionMaker 오브젝트를 가져오는 건 간단하다.

```java

public class UserDaoConnectionCountingTest{
  public static void main(String[] args) throws ClassNotFoundException,SQLException{
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CountingDaoFactory.class);
    UserDao dao = context.getBean("userDao",UserDao.class);

    //
    //DAO 사용코드
    //

    CountingConnectionMaker ccm = context.getBean("connectionMaker",CountingConnectionMaker.class);
    System.out.println("Connection counter :" + ccm.getCounter());
  }
}
```
이렇게 함으로써 DBConnection 분석이 끝나면 다시 CountingDaoFactory 설정 클래스를 DaoFactory로 변경하거나 connectionMaker()메소드를 수정하는 것만으로 DAO의 런터암 의존관계는 이정상태로 복구된다.

### 메소드를 이용한 의존관계 주입
간단하게 설명하자면 스프링에서 의존섭을 주입하는 방법은 생성자를 이용하는 방법뿐만 아니라 아래와 같이 setter아니면 직접 메소드를 정의해서 의존성을 주입하는 방법이있다.
```java
public class UserDao{
  private ConnectionMaker connectionMaker;

  public void setConnectionMker(ConnectionMaker connectionMaker){
    this.connectionMaker = connectionMaker;
  }
}
```
```java
@Bean
public UserDao userDao(){
  UserDao userDao = new UserDao();
  userDao.setConnectionMaker(connectionMaker());
  return userDao;
}
```
단지 의존관계를 주입하는 시점과 방법이 달라졌을 뿐 결과는 동일하다.


## 정리
- 단일 책임원칙 기반하여 책임별로 클래스들을 나누었다. 예제를 보면 UserDao 같은경우에는 데이터 액세스에 관한 관심의 역할을 부여받음으로써 데이터 엑세스에대한 책임을 수행하고있다. ConnectionMaker 같은경우에는 데이터베이스 Connection방법을 설정을 할수 있는 역할을 부여받음으로써 데이터베이스 Connection방법에 관한 책임만을 가진다. 이렇게 명확하게 둘의 책임을 나눔으로써 데이터엑스세에 대한 비지니스로직을 변경한다거나 데이터베이스 Connection 방법을 변경한다거나할 때 서로의 영향을 안받는 독립적인 구조를 만들어 냈다.
- 개방 폐쇄 원칙에 기반하여 클래스들을 나누었다. ConnectionMaker같은경우 인터페이스로 가져감으로써 데이터베이스 Connection 방법을 변경할때 거기에 맞는 구현체만 구현하게 되면 쉽게 데이베이스 Connection방법을 바꿀 수 있게 되었다. 또한 그 구현체 ConnectionMaker들은 데이터베이스 Connection 방법이 변경되지 않는 이상 바뀌지 않을 소스이다. 이런점을 보아서 개방 폐쇄원칙을 잘 준수했다고 볼 수 있다.
- 오브젝트를 생성되고 관계를맺는 제어권을 별도의 오브젝트 팩토리로 만들었다. 객체를 직접 관리하지않고 팩토리를 통해 관리함으로써 기능들을 부품처럼 쉽게 바꿀수 있게 되었다.(제어의 역전/IOC)
