#토비의 스프링
이글은 [토비님의 스프링책](http://book.naver.com/bookdb/book_detail.nhn?bid=7006516)을 보고 요약한 내용입니다

# 2. 테스트
스프링이 개발자에게 제공하는 가장 중요한 가치가 무엇이냐고 질문한다면 그것은 객체지향과 테스트라고 한다. 엔터프라이즈 앱은 변화가 계속해서 일어난다. 이런 변환에 대응하는 첫 번째 전략이 확장과 변화를 고려한 객체지향적 설계와 그것을 효과적으로 담아낼 수 있는 IoC/DI같은기술이다. 두번째 전략은 만들어진 코드를 확실할 수 있게 해주고, 변화에 유연하게 대처할 수 있는 자신감을 주는 테스트 기술이다.

## 2.1 UserDaoTest 다시 보기

### 테스트의 유용성
테스트 코드를 작성하게 되면 자신이 작성한 코드가 제대로 작동하는지 확인할 수 있다. 이를 통해 코드의 결함(의존성)을 제거해가는 작업, 디버깅을 거치게 되고 최종적으로 테스트가 성공하면 모든 결함이 제거됐다는 확인을 얻을 수 있다.

### UserDaoTest특징
다시 1장에서 작성한 UserDao 테스트의 장점을 간단하게 봐보자.

```java
public class UserDaoTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        final ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        final UserDao userDao = context.getBean("userDao",UserDao.class);

        User user = new User();
        user.setId("whiteship");
        user.setName("백기선");
        user.setPassword("married");
        userDao.add(user);
        System.out.println(user.getId() + " 등록 성공");

        User user2 = userDao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());
        System.out.println(user2.getId() + " 조회 성공");
    }

}
```
위의 UserDaoTest를 이용해 작성한 테스트 코드의 장점은 아래와 같을 수 있을것이다.
* 자바에서 가장 손쉽게 실행가능한 main()함수를 사용해 테스트할 수 있다.
* 테스트의 결과를 콘솔로 확인할 수 있다.
* 테스트에 사용할 객체들의 의존성을 쉽게 주입해줄 수 있다.

만약 테스트코드 작성이 아닌 구현을 통한 테스트는 아래와같은 문제점을 가질 수 있다.

**웹을 통한 DAO 테스트 방법의 문제점**
웹화면을 통해 DAO를 테스트하게되면 프리젠테이션계층, 서비스계층을 포함한 모든 입출력기능을 대충이라도 테스트해야만한다. 좀 더 자세히말하면 서비스클래스, 컨트롤러, 뷰단 모든 레이어의 기능을 다만들어야만 테스트를 할 수 있다는 단점이 있다.

**작은 단위의 테스트 필요성**
한꺼번에 너무많은것을 몰아서 테스트하면 테스트 수행과정도 복잡해지고 오류가 발생했을 때 정확한 원인을 찾기도 힘들어진다. 따라서 테스틑 가능하면 작은 단위로 쪼개서 집중해서 할수 있어야 한다.

때로는 긴 시나리오에 테스트를 하는경우도 있을것이다. 예를들어 회원가입 - 로그인 - 로그아웃 - 회원삭제, 하지만 이흐름을 한번에 시키다가 에러가 발생하면 어느부분에서 에러가 낫는지 확인하기위해 많은 디버깅 시간이 필요할 것 이다. 하지만 미리 단위 테스트하고 그다음 긴 시나리오를 진행하게되면 예외가 발생하거나 테스트가 실패할 수 있지만, 좀더 에러의 원인을 빨리 잡을 가망성이 커진다. 이렇기 때문에 각각의 기능을 쪼개서 테스트할필요가 있다.

### UserDaoTest의 문제점
위에 작성한 UserDaoTest가 직접 테스트하는 방법보다는 훨씬 효율적인 방법이다. 좋은 방법임에도 불구하고 몇가지 만족스럽지 못한것은 사실이다. 예를들어 테스트 코드를 개발자가 직접 실행시켜야한다거나 그결과를 직접 확인해야한다는것이다. 그래서 이러한문제를 체계적으로 해결할 수 있는 프레임워크를 스프링에서 제공해준다. 그것이 바로 Junit 테스트 프레임워크이다.

## 2.2 JUnit 테스트로 전환
### Junit테스트로 전환
JUnit프레임워크는 두가지 조건을 따라야 한다. 첫번째는 public 으로 선언 돼야하고, 다른하나는 메소드에 @test라는 애노테이션을 붙여야한다.

### 검증 코드 전환
Junit code에서 assertThat()메소드는 첫 번째 파라미터의 값을 뒤에나오는 matcher라고 불리는 조건으로 비교해서 일치하면 다음으로 넘어가고, 아니면 테스트가 실패하도록 만들어준다. is()는 matcher의 일종으로 equals()로 비교해주는 기능을 가졌다.

```java
assertThat(user2.getPassword(), is(user.getPassword())); // 같지않으면 다음단계로 못넘어감
if(!user.getName.equals(user2.getName())){
  //마찬가지
}
```

## 2.3 개발자를 위한 테스팅 프레임워크 JUnit
### 테스트 결과의 일관성
테스트를 하다 보면 외부 상태에 따라 성공하기도 실패하기도한다. 당연히 성공해야하는 상황도 실패하는경우가있다. 예를들어 데이터베이스가 이전에 테스트했던 데이터가 들어가있어 중복 문제로 인하여 테스트가 실팽하는 경우 등이 있을 수 있다. 가장 좋은 해결책은 addAndGet()같은 메소드가 테스트를 마치고 나면 테스트가 등록한 사용자 정보를 삭제해서, 테스트를 수행하기 이전 상태로 만들어주는 것이다. 그러면 아무리 많은 테스트를 해도 항상 동일한 테스트 결과를 얻을 것 이다.

#### deleteAll()의 getCount() 추가

```java
public void deleteAll() throws SQLException{
  System.out.println("delete from users"); //실제로는 해당기능 실제 수행
}

public void getCount() throws SQLException{
  System.out.println("select count(*) from users"); //실제로는 해당기능 실제 수행
}

```
##

테스트의 결과가 테스트(메소드) 실행 순서에 영향을 받는다면 테스트를 잘못 만든 것이다. 모든 테스트는 실행순서에 상관없이 독립적으로 항상 동일한 결과를 낼수 있도록 해야한다.

JUnit은 예외조건 테스트를 위한 특별한 방법을 제공해준다. Junit의 예외 테스트 기능을 이용하면 아래와 같이 테스트를 만들 수 있다.

```java
@Test(expected = EmptyResultDataAccessException.class)
public void getUserFailure(){
  //비지니스로직하다가 예상되어지는 exception발생시 성공
}

```

테스트 코드를 작성할때는 조건, 행위, 결과에대한 내용이 잘 표현되어 있어야 한다. 이렇게 구현하다보면 마치 잘작성된 하나의 기능정의서처럼 보인다. 그래서 보통 기능설계, 구현, 테스트라는 일반적인 개발 흐름의 기능설계에 해당하는 부분을 테스트 코드가 일부분 담당하고 있다고 볼 수 있다. 마치 코드로 된 설계 문서 처럼 만들어놓 것이라고 생각해보자.

#### 테스트 주도 개발(TDD)
만들고자 하는 기능의 내용을 담고 있으면서 만들어진 코드를 검증도 해줄 수 있도록 테스트 코드를 먼저 만들고, 테스트를 성공하게 해주는 코드를 작성하는 방식의 개발 방법을 테스트 주도 개발이라고한다. 또는 테스트우선개발 Test First Development라고도 한다.

#####Before
미리 JUnit테스트가 시작되기전에 before이라는 어노테이션을 이용해 아래와 같이 필요한 변수를 셋팅하고 시작할 수 있다.
```java

public class UserDaoTest {
  private UserDao dao;

  @Before
  public void setUp(){
    ApplicationContext context = new GenericXmlApplicationContext("ApplicationContext.xml");
    this.dao = contenxt.getBean("userDao",UserDao.class)
  }
}
```
Junit 프레임워크는 스스로 제어권을 가지고 주도적으로 동작하고, 개발자가 만든 코드는 프레임워크에 의해 수동적으로 실행된다. 그래서 프레임워크에 사용되는 코드만으로는 실행 흐름이 잘보이지 않기 때문에 프레임워크가 어떻게 사용할지를 잘 이해를 하고 있어야 한다.
JUnit이 하나의 테스트 클래스를 가져와 테스트를 수행하는 방식은 다음과 같다.

1. 테스트 클래스에서 @Test가 붙은 public이고 void형이며 파라미터가 없는 테스트 메소드를 모두 찾는다.
2. 테스트 클래스의 오브젝트를 하나 만든다.
3. @before가 붙은 메소드가 있으면 실행한다.
4. @test가 붙은 메소드를 하나 호출하고 테스트 결과를 저장해둔다.
5. @after가 붙은 메소드가 있으면 실행한다.
6. 나머지 테스트 메소드에 대해 2~5번을 반복한다.
7. 모든 테스트의 결과를 종합해서 돌려준다.
크게 이렇게 7단계를 거쳐서 진행된다고 볼 수 있다.

한가지 기억해야할점은 실행되는 각테스트 클래스는 실행때마다 새롭게 만들어진다. 한번 만들어진 테스트 클래스는 하나의 오브젝트는 테스트 메소드가 사용하고나면 버려진다. 만약 해당 클래스가 @test 메소드를 2개가지고있다면 2개의 오브젝트를 만들고 각각 테스트 될것이다.

![](https://i.imgur.com/eq09toD.png)        

## 2.4 스프링 테스트 적용
@Before메소드가 테스트 메소드 개수만큼 반복되기 떄문에 어플리케이션 컨텍스트도 3번 만들어진다. 지금은 이 빈을 생성하는데 얼마 걸리지 않지만 나중에 빈이 많이 등록되고 그러면 이 빈 컨텍스트를 만드는데 많은 시간이 걸린다. 그래서 ApplicationContext같은경우에는 스태틱필드에 저장해두고 공통 변수로 사용하면 효율적으로 사용할 수 있게 된다. JUnit은 테스트 클래스 전체에 걸쳐 딱 한번만 실행되는 @BeforeClass 스태틱 메소드를 지원한다. 하지만 이보다는 스프링이 직접제공해주는 어플리케이션 컨텍스트 테스트 지원기능을 사용하는것이다.

```java
@RunWith(SpringJUnit4ClassRunner.class) // Spring 테스트 컨텍스트 프레임워크의 JUnit확장 기능 지정
@ContextConfiguration(classes={DaoFactory.class}) // 테스트 컨텍스트가 자동으로 만들어줄 어플리케이션 컨텍스트의 위치
public class UserDaoTest{
  @Autowired
  private ApplicationContext context; //위에서 제공해주는 컨텍스트로 의존성주입됨

  @Before
  public void setUp(){
    this.dao = this.context.getBean("UserDao",UserDao.class);
    ...
  }
}
```

@RunWith는 Junit프레임워크의 테스트 실행방법을 확장할 때 사용하는 애노테이션이다. SpringJUnit4ClassRunner는 Junit용 테스트 컨텍스트 프레임워크 확장 클래스를 지정해주면 JUnit이 테스트를 진행하는 중에 테스트가 사용할 애플리케이션 컨텍스트를 만들고 관리하는 작업을 진행해준다.

@ContextConfiguration은 자동으로 만들어줄 애플리케이션 컨텍스트 설정위치를 지정한것이다.

### 테스트 메소드의 컨텍스트 공유
@Before은 매 테스트코드가 실행될때 마다 실행되게 메소드를 설정해주 어노테이션이다. 만약 2개의 테스트 메소드를 실행한다면 this.context 같은 오브젝트일것이다. 위에서 @RunWith 어노테이션때문에 한번만 context를 load할것이기 때문이다. 반면 this는 테스트메소드마다 새롭게 생성된 오브젝트 일것이다
```java
@Before
public void setUp(){
  System.out.println(this.context);
  System.out.println(this);
}
```

### 테스트 클래스의 컨텍스트 공유
만약 여러개의 테스트 클래스가 있는데 모두 같은 설정파일을 가진 애플리케이션 컨텍스트를 사용한다면, 스프링은 테스트클래스 사이에서도 애플리케이션 컨텍스트를 공유하여 사용한다.
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DaoFactory.class})
public class UserDaoTest{..}

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={DaoFactory.class})
public class GroupDaoTest{..}
```
만약 위와같이 두개의 다른 테스트클래스가 있더라도, DaoFactory에 관한 Context는 한번만 올라가지게 된다. 따라서 수백 개의 테스트 클래스를 만들어도 모두 같은 설정파일을 사용하게 된다. 당연히 테스트파일을 실행시켜도 한번만 Context가 load될것이다.

### @Autowired를 통한 의존성주입
아래는 dataSource를 의존성을 주입하는 방법이다.
```java
@Autowired
private SimpleDriverDataSource datasource;

@Autowired
private DataSource datasource;
```
위 방법중에 어느방식을 사용해서 의존성주입을 해도 상관은없다. 하지만 인터페이스를 선언한것이 좀더 확장성 있게 개발할 수 있다. 인터페이스를 사용하면 총 몇가지의 장점을 가진다.
첫째, 인터페이스를 사용하여 의존성주입을 하게되면 해당 의존성주입의 클래스부분만 bean을 등록해주면 되기때문이다.
둘째, 1장에서 만들었던 DB커넥션의 개수를 카운팅하는 부가기능이 대표적인예이다. 추가 기능을 확장하는데 아주 좋다.


### 테스트를 위한 별도의 DI설정

```java
@ContextConfiguration(classes={DaoFactory.class})
```

테스트를 위해 설정 클래스파일을 하나 더만들어 구성하면 쉽게 개발용 테스트 클래스를 실행할 수 있다. 이렇게 하면 테스트환경에서 적합한 구성을 가진 설정파일을 이용해서 테스트를 진행할 수 있다.

[예제소스](https://github.com/minwan1/Spring-toby)

## 정리
* Test 주도 개발로 개발을 하게 되면 너무나도 많은 장점을 가지는 것 같다. 일단 변화를 두려워 하지않는다. 소스 리팩토링, 기능변경 이 있더라도 개발을한 후 간단하게 테스트를 할 수 있기 때문이다.

* 또 개발 시간이 단축된다. 단순 개발시간만 놓고보면 당연히 개발시간이 단축된다고 말할 수 는 없다. 하지만 예를들어 User 정보 변경 기능을 개발한다고 치면 개발을 하고 매번 테스트 하기위해서는 프론트엔드와 연결을하고 테스트 해야한다. 단순 유저 정보 변경 기능 개발인데 프론트엔드가 없으면 개발 테스트를 못하게 된다. 물론 Postman, Curl등을 호출해 API 테스트를 할 수 있겠지만 문제는 API하나 테스트를 위해 몇분의 서버 로딩을 기다려야한다. 하지만 Unit 테스트등으로는 몇개의 빈만 올리고 테스트할 수 있기때문에 개발 및 테스트 시간을 아낄 수 있게 된다.

* 그리고 테스트 주도 개발을 하게 되면 자연스럽게 단일 책임 원칙, 개방 폐쇄 원칙을 준수하게 되는 것 같다. 왜냐 테스트 코드를 작성하다 보면 왠지 모르게 테스트 코드가 작성하기가 어려울 때 가 있는데 이때 단일 책임 원칙, 개방 폐쇄 원칙을 지키지 않고 있을가망성이 크기때문이다. 이럴 때 보면 보통 하나의 클래스에 너무 많은 책임을 줬기 때문에 테스트 코드를 작성하기가 어려워지는 경우가 많다. 일단 테스트 코드가 작성하기 어렵다는 것은 지금 만들고자 하는 기능이 뭔가 SOLID 원칙을 위반하고 있다는 것을 의심해봐야 한다.
