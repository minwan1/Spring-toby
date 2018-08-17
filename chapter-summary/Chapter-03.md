# 토비의 스프링
이글은 [토비님의 스프링책](http://book.naver.com/bookdb/book_detail.nhn?bid=7006516)을 보고 요약한 내용입니다

# 3. 템플릿
1장에서 초난감 DAO 코드에 DI를 적용해나가는 과정을 통해서 관심이 다른 코드를 다양한 방법으로 분리하고, 확장과 변경에 용이하게 대응할 수 있는 설계 구조로 개선하는 작업을 했다.
확장에는 자유롭게 열려있고 변경에는 굳게 닫혀 있다는 객체지향 설계의 핵심 원칙인 개방폐쇄 원칙이 있다. 이원칙은 코드에서 어떤 부분은 변경을 통해 그 기능이 다양해지고 확장하려는 성질이 있고, 어떤 부분은 고정되어 있고 변하지 않으려는 성질이 있음을 말해준다. 변화의 특성이 다른부분을 구분해주고, 각각 다른 목적과 다른 이유에 의해 다른 시점에 독립적으로 변경될 수 있는 효율적인 구조를 만들어주는것이 바로 이 개방 폐쇄원칙이다.


템플릿이란 이렇게 바뀌는 성질이 다른 코드중에서 변경이 거의 일어나지 않으며, 일정한 패턴으로 유지되는 특성의 부분을 자유롭게 변경되는 성질의 부분으로부터 독립시켜서 효과적으로 활용할 수 있게 해준다.

## 3.1 다시보는 초난감 DAO
UserDao에는 DB연결과 관련된 여러가지 개선작업을 했지만 예외상황에대한 처리가 아직 미흡하다. 이부분에대해서 해결하려고한다.
### 3.1.1 예외처리 기능을 갖춘 DAO
DB커넥션이라는 제한적인 리소스를 공유해 사용하는 서버에서 동작하는 JDBC코드에는 반드시 지켜야할 원칙이 있다. 바로 예외처리이다. 정상적인 JDBC 코드의 흐름을 따르지 않고 중간에 어떤 이유로든 예외가 발생했을 경우에도 사용한 리소스를 반드시 반환하도록 만들어야 하기 때문이다. 그렇지 않으면 시스템에 심각한 문제를 일으킬 수 있다.

```java
public void deleteAll() throws SQLException{
  Connection c = dataSource.getConnetion();

  PreparedStatement ps = c.prepareStatement("delete from users");
  ps.executeUpdate();

  ps.close();
  c.close();
}
```

일반적으로 서버에서는 제한된 개수의 DB커넥션을만들어서 재상용 가능한 풀로 관리한다. DB풀은 매번 getConnection()으로 가져간 커넥션을 명시적으로 close()해서 돌려줘야만 다시 풀에 넣었다가 다음 커넥션 요청이 있을 때 재사용 될 수 있다. 그런데 위에서 Connection을 close하지 못하고 Exception이 나서 Connection이 반환되지 않고 쌓인다면 풀에 커넥션의 여유가 없어지고 리소스가 모자란다는 심각한 오류를 내며 서버가 중단 될 수 있다.

```
리소스 반환과 close()
이름으로 보면 열린것을 닫는다는 의미이지만 보통 리소스를 반환한다는 의미로 이해하는것이 좋다. Connection과 PreparedStatement는 보통 풀(Pool)방식으로 운영된다. 미리 정해진 풀 안에 제한된 수의 리소스를 만들어두고 필요할때 이를 할당하고, 반환하면 다시 풀에 넣는다 방식이다. 리소스를 항생 새로 생성하는 방식보다 풀안에 넣어두고 돌려가며 사용하는 편이 훨씬 효율적이다. 대신 사용한 리소스는 빠르게 반환해야한다. 그렇지 않으면 풀에 있는 리소스가 고갈되고 문제가 발생한다. close() 메소드는 사용한 리소스스를 풀로 다시 돌려주는 역할을 한다.
```

```java
public void deleteAll() throws SQLException{
  Connection c = dataSource.getConnetion();

 try{
   PreparedStatement ps = c.prepareStatement("delete from users");
   ps.executeUpdate();
 }catch (SQLException e){
   throw e;
 }finally{
   //널검사등, 자원 close()실행
 }
  ps.close();
  c.close();
}
```
이제 위와같이 소스를 작성하면 예외상황에서도 안전한 코드가 됐다. 하지만 누가봐도 DAO에서 Connection 해지하기위한 중복된 코드가 발생할것으로 보인다.
## 3.2 변하는 것과 변하지 않는것
위에서 다뤘던 소스들은 누가 보더라도 중복 코드가 발생할 것으로 보인다. 그래서 이러한 문제들을 어떻게 효과적으로 다룰지에 대해 생각해보자.
### 전략 패턴의 적용
개방 폐쇄 원칙을 잘지키는 구조이면서도 템플릿 메소드 패턴(상속을 통해 구현하다보니 슈퍼클래스에 의존적이다)보다 유연하고 확장성이 뛰어난 것이, 오브젝트를 아예 둘로 분리하고 클래스 레벨에서는 인터페이스를 통해서만 의존하도록 만드는 전략 패턴이다.전략 패턴은 OCP 관점에 보면 확장에 해당하는 변하는 부분을 별도의 클래스로 만들어 추상화된 인터페이스를 통해 위임하는 방식이다.
아래의 그림은 전략 패턴의 구조를 나타낸것이다. 좌측에 있는 Context의 contextMethod()에서 일정한 구조를 가지고 동작하다가 특정 확장 기능은 Strategy인터페이스를 통해 외부의 독립된 전략클래스에 위임하는 것이다.
![](https://i.imgur.com/Kcz8D4I.png)
deleteAll() 메소드에서 변하지 않는 부분이라고 명시한것이 바로 이 context Method() 가된다. deleteAll()은 JDBC를 이용해 DB를 업데이트하는 작업이라는 변하지 않는 Context를 갖는다. 간단히 contextMethod에 역할을 정리하면 다음과 같다.
contextMethod()는 변하지 않는 DB 커넥션 가져오기, PreparedStatement를 만들어줄 외부 기능 호출하기, 예외처리하기등의 작업을 처리한다.

contextMethod() 역할
* DB 커넥션 가져오기
* PreparedStatement를 만들어줄 외부 기능 호출하기
* 전달받은 PreparedStatement 실행하기
* 예외가 발생하려면 이를 다시 메소드 밖으로 던지기
* 모든 경우에 만들어진 PreparedStatement와 Connection을 적절히 닫아주기

두 번째 작업에서 사용하는 PreparedStatement를 만들어주는 외부 기능이 바로 전략 패턴에서 말하는 전략이라고 볼 수 있다. 전략 패턴의 구조를 따라 이 기능을 인터페에이스로 만들어두고 인터페이스의 메소드를 통해 PreparedStatement 생성 전략을 호출해주면 된다. 하지만 여기서 주요한점은 PreparedStatement를 생성하는 전략을 호출할 때는 이 컨텍스트 내에 만들어둔 DB커넥션을 전달해야하는 점이다. 커넥션이 없으면 PreparedStatement를 만들수가 없다.
PreparedStatement를 만드는 전략의 인터페이스는 컨텍스트가 만들어둔 Connection을 전달받아서, PreparedStatement를 만들고 만들어진 PreparedStatement오브젝트를 돌려준다,

```java
public interface StatementStrategy{
  PreparedStatement makePreparedStatement(Connection c) throws SQLException;
}
```
이 인터페이스를 상속해서 실제 전략, 즉 바뀌는 부분인 PreparedStatement를 생성하는 클래스가 있다. 이바뀌는 부분만 구현해서 사용할 수 있다.

```java
public class DeleteAllStatement implements StatementStrategy{

  public PreparedStatement makePreparedStatement(Connection c) throws SQLException{
    PreparedStatement ps = c.prepareStatement("delete from users");
    return ps;
  }
}
```
위에 클래스는 확장된 PreparedStatement전략인 DeleteAllStatement가 만들어 졌다. 이것을 아래와같이 contextMethod()에 해당하는 UserDao의 deleteAll() 메소드에서 사용하면 전략패턴을 적용했다고 볼 수 있다.
```java
//UserDao
public void deleteAll() throws SQLException {
  try{
    c = dataSource.getConnection();
    StatementStrategy strategy = new DeleteAllStatement();
    ps = strategy.makePreparedStatement(c);

    ps.executeUpdate(); // 실행메소드
  }catch (SQLException e){
  ... // 익셉션등 자원 close 처리되어짐
}
```

하지만 전략 패턴은 필요에 따라 컨텍스트는 그대로 유지되면서 전략을 바꿔 쓸 수 있다는것인데, 하지만 위에것은 DeleteAllStatement()라고 명시적으로 고정되어있는데 뭔가 이상하다. 이것은 UserDaoFactory처럼 context를 관리할 것을 만들어줘야한다.
![](https://i.imgur.com/zCzUfyH.png)

아래 메소드는 컨텍스트의 핵심적인 내용을 잘 담고 있다.
```java
public void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException{
  Connection c = null;
  PreparedStatement ps = null;

  try {

    c = dataSource.getConnection();
    ps = stmt.makePreparedStatement(c);

  } catch (SQLException e){
    throw e;
  } finally {
    if(ps != null){try{ps.close();}catch(SQLException e)}
    if(c != null){try{c.close();}catch(SQLException e)}
  }
}
```
클라이언트로부터 StatementStrategy 타입의 전략 오브젝트를 제공받고 JDBC try/catch/finally 구조로 만들어진 컨텍스트 내에서 작업을 수행한다. 클라이언트로부터 (StatementStrategy stmt) 제공받은 파라미터를 이용하여 생성이 필요한 시점에 호출해서 사용할 수 있다.


```java
public void deleteAll() throws SQLException {
  StatemetnStrategy st = new DeleteAllStatement(); // 생성한 전략클래스의 오브젝트 생성
  jdbcContextWithStatementStrategy(st); // 컨텍스트 호출, 전략오브젝트 전달
}
```
**최종 템플릿패턴 결과물**


```java
public interface StatementStrategy{ //33
  PreparedStatement makePreparedStatement(Connection c) throws SQLException;
}

public class DeleteAllStatement implements StatementStrategy{ //3

  public PreparedStatement makePreparedStatement(Connection c) throws SQLException{
    PreparedStatement ps = c.prepareStatement("delete from users");
    return ps;
  }
}

public void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException{ //2
  Connection c = null;
  PreparedStatement ps = null;

  try {

    c = dataSource.getConnection();
    ps = stmt.makePreparedStatement(c);

  } catch (SQLException e){
    throw e;
  } finally {
    if(ps != null){try{ps.close();}catch(SQLException e){}}
    if(c != null){try{c.close();}catch(SQLException e){}}
  }
}


public void deleteAll() throws SQLException { //1
  StatemetnStrategy st = new DeleteAllStatement(); // 생성한 전략클래스의 오브젝트 생성
  jdbcContextWithStatementStrategy(st); // 컨텍스트 호출, 전략오브젝트 전달
}
```
deleteAll의 임무는 오브젝트를 만들고 컨텍스트를 호출하는 책임을 지고 있다. 그리고 jdbcContextWithStatementStrategy 컨텍스를 호출하는 임무가 있다.

## 3.3 JDBC 전략 패턴의 최적화
### 3.3.1 전략 클래스의 추가 정보
이번엔 add() 메소드를 만들어 적용해보자

```java
public class AddStatement implements StatementStrategy{
  public PreparedStatement makePreparedStatement(Connection c) throws SQLException{
    PreparedStatement ps = c.prepareStatement("insert into users(id,name,password) values(?,?,?)");
    ps.setString(1, user.getId());
    ps.setString(2, user.getName());
    ps.setString(3, user.getPassword());

    return ps;
  }

}

```
그런데 위와같이하고 컴파일을 하게되면 컴파일 에러가난다. user 선언하는 변수가 없기 떄문이다. 따라서 클라이언트가 AddStatement의 전략을 수행하려면 부가정보인 user를 제공해줘야한다.

```java
public class AddStatement implements StatementStrategy{

  User user;
  public AddStatement(User user){
    this.user = user;
  }
  위와 똑같음

}

```

이제 컴파일을하면 에러가 나지 않을것이다. 그리고 클라이언트인 UserDao에서 add() 메소드를 만든다

```java

public void add(User user) thorws SQLException {
  StatementStrategy st = new AddStatement(user);
  jdbcContextWithStatementStrategy(st);

}
```

이렇게 함으로 try/cat/finally로 범벅된 코드를 만들다가 실수할 염려는 없어 졌다.

### 3.3.2 전략과 클라이언트의 동거
지금 만들어진 구조에도 두가지 단점이 존재한다. 먼저 DAO메소드마다 새로운 StatementStrategy구현 클래스를 만들어야 한다는 점이다. 이렇게되면 UserDao때보다 클래스 파일의 개수가 많이 늘어난다. 이래서 런타임시에 다이내믹하게 DI해준다는점을 제외하면 로직마다 상속을 사용하는 템플릿 메소드 패턴을 적용했을 때 보다 그다지 나을게 없다. 또 다른 단점은 DAO메소드에서 StatementStrategy에 전달할 User와 같은 부가적인 정보가 있는경우, 이를 위해 오브젝트를 전달받는 생성자와 이를 저장해둘 인스턴스 변수를 번거롭게 만들어야 한다는 점이다. 이 두가지 문제를 해결할 수 있는 방법을 생각해보자.


```java

public void add(final User user) thorws SQLException {

  public class AddStatement implements StatementStrategy{
    public PreparedStatement makePreparedStatement(Connection c) throws SQLException{
      PreparedStatement ps = c.prepareStatement("insert into users(id,name,password) values(?,?,?)");
      ps.setString(1, user.getId());
      ps.setString(2, user.getName());
      ps.setString(3, user.getPassword());

      return ps;
    }

  }

  StatementStrategy st = new AddStatement();
  jdbcContextWithStatementStrategy(st);

}

```

위와같이 내부 클래스를 사용하면 매번클래스 파일을 새로 생성해주지 않아도 되는 장점과 클래내에 User변수를 지역변수처럼 사용할 수 있다는 장점이있다. 이렇게하면 User라는 전역변수를 안쓰고 바로바로 접근해서 사용할 수 있다.

**익명 내부 클래스**
한가지 더 욕심을 낸다면 AddStatement 클래스는 add()메소드에서만 사용할 용도로 만들어졌다. 그렇다면 좀더 간결하게 클래스 이름도 제거할 수 있다.

```java

public void add(final User user) thorws SQLException {

  jdbcContextWithStatementStrategy(
  new StatementStrategy(){
    PreparedStatement ps = c.prepareStatement("insert into users(id,name,password) values(?,?,?)");
    ps.setString(1, user.getId());
    ps.setString(2, user.getName());
    ps.setString(3, user.getPassword());

    return ps;
  });
}

```
위에 소스는 AddStatement를 익명 내부클래스로 전환한것이다. 나머지 DeleteAllStatement도 익맹내부 클래스로 처리하면 간략하게 만들 수 있다.

## 3.4 컨텍스트와 DI
### 3.4.1 jdbcContext 분리
전략 패턴의 구조로 보자면 UserDao의 메소드가 클라이언트이고, 익명 내부클래스로 만들어지는것이 개별적인 전략이고, jdbcContextWithStatementStrategy() 메소드는 컨텍스트다. 컨텍스 메소드는 UserDao내의 PreparedStatement를 실행하는 기능을 가진 메소드에서 공유할 수 있다. 그런데 JDBC의 일반적인 작업 흐름을 담고 있는 jdbcContextWithStatementStrategy는 다른 DAO에서도 사용 가능하다. 그러니ㅣ jdbcContextWithStatementStrategy()를 UserDao클래스 밖으로 돕리시켜서 모든 DAO가 사용할 수 있게 해보자.

**클래스 분리**
분리해서 만들 클래스의 이름은 jdbcContext라고 하자. jdbcContext에 UserDao에 있던 컨텍스트 메소드를 workWithStatementStrategy()라는 이름으로 옮겨 놓는다. 이렇게하면 DataSource가 필요한것은 UserDao가 아니라 jdbc Context가 돼버린다. DB커넥션을 필요로 하는 코드는 jdbcContext안에 있기 때문이다. 따라서 JdbcContext가 DataSource에 의존하고 있으므로 DataSource 타입 빈을 DI받을 수 있게 해줘야한다.

```java
@Service
public class JdbcContext {

	@Autowired
	private DataSource dataSource; // bean이 등록되어있으면 이렇게 사용가능함


	public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException{
		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = dataSource.getConnection();
			ps = stmt.makePreparedStatement(c);

		} catch (SQLException e) {
		    throw e;
		} finally {
		    if(ps != null){try{ps.close();}catch(SQLException e){}}
		    if(c != null){try{c.close();}catch(SQLException e){}}
		}
	}
}
```

UserDao에도 기존의 jdbcContextWithStatementStrategy메소드가 지워졌기때문에 주입되어진 JdbcContext의 workWithStatementStrategy메소드를 사용하면된다.

```java
@Autowired
private JdbcContext jdbcContext;

public void deleteAll() throws SQLException {
  jdbcContext.workWithStatementStrategy(new StatementStrategy() {
    @Override
    public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
      return c.prepareStatement("delete from users");
    }
  });
}

```


**빈 의존관계 변경**
위에서 사용하는 JdbcContext는 인터페이스인 DataSource와는 달리 구체적인클래스이다. 스프링의 DI는 기본적으로 인터페이스를 사이에 두고 의존 클래스를 바꿔서 사용하도록 하는게 목적이다. 하지만 이경우 JdbcContext는 그 자체로 독립적인 JDBC컨텍스트를 제공해주는 서비스 오브젝트로써 의미가 있을뿐이고 구현 방법이 바뀔 가능성은 없다. 따라서 인터페이스를 구현하도록 만들지 않았고, UserDao와 JdbcContext는 인터페이스를 사이에 두지 않고 DI를 적용하는 틀별한 구조가 된다.아래는 JdbcContext가 추가된 의존관계를 나타내주는 클래스 다이어그램이다.
![](https://i.imgur.com/PSPcBdh.jpg)


## 3.5 템플릿과 콜백
지금까지 UserDao와 StatementStrategy, jdbcContext를 이용해 만든 코드는 일종의 전략패턴이 적용된 것이라고 볼 수 있다. 복잡하지만 바뀌지 않는 일정한 패턴을 갖는 작업흐름이 존재하고 그중 일부분만 자주 바꿔서 사용해야하는 경우에 적합한 구조다. 전략 패턴의 기본 구조에 익명 내부 클래스를 활용한 방식이다. 이런 방식을 스프링 에서는 템플릿/콜백 패턴이라한다. 전략 패턴의 컨텍스트를 템플릿이라고 부르고, 익명 내부클래스로 만들어지는 오브젝트를 콜백이라한다.

### 3.5.1 템플릿/콜백의 동작 원리
템플릿은 고정된 작업 흐름을 가진 코드를 재사용 한다는 의미에서 붙인 이름이다. 콜백은 템플릿 안에서 호출되는것을 목적으로 만들어진 오브젝트를 말한다.

***템플릿/콜백의특징***
여러개의 메소드를 가진 일반적인 인터페이스를 사용할 수 있는 전략 패턴의 전략과 달리 템플릿/콜백 패턴의 콜백은 보통 단일 메소드 인터페이스를 사용한다. 템플릿의 작업 흐름중 특정 기능을 위해 한번 호출되는 경우가 일반적이기 때문이다. 하나의 템플릿에서 여러 가지 종류의 전략을 사용해야 한다면 하나 이상의 콜백 오브젝트를 사용할 수 도있다. 콜백은 일반적으로 하나의 메소드를 가진 인터페이스를 구현한 익명 내부 클래스로 만들어진다고 보면 된다.
콜백 인터페이스의 메소드에는 보통 파라미터가 있다. 이 파라미터는 템플릿의 작업 흐름 중에 만들어지는 컨텍스트 정보를 전달 받을 때 사용된다. JdbcContext에서는 템플릿인 workWithStatementStrategy() 메소드 내에서 생성한 Connection 오브젝트를 콜백의 메소드인 makePreparedStatement()를 실행할 때 파라미터로 넘겨준다. PreparedStatement를 만들기 위해서는 JDBC 컨텍스트/템플릿 안에서 만들어진 DB커넥션이 필요하기 때문이다. 그림 3-7은 템플릿/콜백 패턴의 일반적인 작업 흐름을 보여준다.

![](https://i.imgur.com/OXD1u0M.png)

* 클라이언트의 역할은 템플릿안에서 실행될 로직을 담은 콜백 오브젝트를 만들고, 콜백이 참조할 정보를 제공하는 것이다. 만들어진 콜백은 클라이언트가 템플릿의 메소드를 호출할 때 파라미터로 전달된다.
* 템플릿은 정해진 작업 흐름을 따라 작업을 진행하다가 내부에 생성한 참조정보를 가지고 콜백 오브젝트의 메소드를 호출한다. 콜백은 클라이언트 메소드에 있는 정보와 템플릿이 제공한 참조 정보를 이용해서 작업을 수행하고 그결과를 다시 템플릿에 돌려준다.
* 템플릿은 콜백이 돌려준 정보를 사용해서 작업을 마저 수행한다. 경우에 따라 최종 결과를 클라이언트에 다시 돌려주기도 한다.

조금 복잡해보이지만 DI방식의 전략 패턴 구조라고 생각하면된다. 클라이언트가 템플릿 메소드를 호출하면서 콜백 오브젝트를 전달하는 것은 메소드 레벨에서 일어나는 DI이다. 템플릿/콜백 방식은 전략 패턴과 DI의 장점을 익명 내부 클래스 사용 전략과 결합한 독특한 활용법이라고 이해할 수 있다. 단순히 전랴개패턴으로만 보기엔 독특한 특징이 많으므로 템플릿/콜백을 하나의 고유한 디자인 패턴으로 기억하면 좋겟다.

***jdbcContext에 적용된 템플릿/콜백***

![](https://i.imgur.com/6IUSSyT.png)
JdbcContext의 workWithStatementStrategy() 템플릿은 리턴 값이 없는 단순한 구조다. 조회 작업에서는 보통 템플릿의 작업 결과를 클라이언트에 리턴해준다. 템플릿의 작업흐름이 좀 더 복잡한 경우에는 한번 이상 콜백을 호출하기도하고, 여러개의 콜백을 클라이언트로 바다서 사용하기도 한다.

### 3.5.2 편리한 콜백의 재활용
템플릿/콜백 방식은 템플릿에 담긴 코드를 여기 저기서 반복적으로 사용하는 원시적인 방법에 비해 많은 장점이 있다. 당장에 jdbcContext를 사용하기만해도 기존에 JDBC기반의 코드를 만들었을 때 발생했던 여러 가지 문제점과 불편한점을 제거할 수 있다. 클라이언트인 DAO의 메소드는 간결해지고 최소한의 데이터 액세스 로직만 갖고 있게 된다. 하지만 템플릿/콜백방식은 익명내부클래스를 사용해야해서 코드를 읽는데 불편하다는점이다.
