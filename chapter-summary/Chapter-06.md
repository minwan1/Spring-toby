# 토비의 스프링
이글은 [토비님의 스프링책](http://book.naver.com/bookdb/book_detail.nhn?bid=7006516)을 보고 요약한 내용입니다

# AOP
AOP의 필연적인 등장 배경과 스프링이 그것을 도입한 이유를 충분한 이해가 필요하다 그래야지만 AOP의 가치를 이해하고 효과적으로 사용할 방법을 찾을 수 있다.
스프링에서 적용된 가장인기 있는 AOP의 적용 대상은 바로 선언적 트랜잭션 기능이다. 서비스 추상화를 통해 많은 근본적인 문제를 해결했던 트랜잭션 경계설정 기능을 AOP를 이용해 변경할것이다.

## 6.1 트랜잭션 코드의 분리
그전 UserService에 트랜잭션을 추가했었다. 하지만 무시무시하게 생긴 트랜잭션 코드가 있는 모습은 뭔가 한가지가 부족한 느낌이다. 하지만 논리적으로 비지니스 로직 전후에 있어야하는것만은 확실하다.

우리는 이것들의 로직을 클래스로 분리하여 둘의 동거를 분리할 수 있다. 그렇게되면 트랜잭션담당 클래스와 유저서비스비지니스로직 클래스로 나뉘어질것이다. 그리고 유저 서비스 기능을 쓰기위해서는 트랜잭션 담당 클래스를 호출하고, 그다음에 트랜잭션 담당클래스가 UserService비지니스로직을 호출함으로 유저 기능과 트랜잭션 기능이 공존할 수 있다.
다시 정리하면 소스는 분리해서 각각 관리가되고 기능은 공존할 수있게되는것이다.

## 6.2 고립된 단위 테스트
가장 편하고 좋은 테스트 방법은 가능한 작은 단위로 쪼개서 테스트하는 것이다. 작은 단위의 테스트가 좋은 이유는 테스트가 실패했을 때 그원인을 찾기 쉽기 때문이다. 반대로 테스트에서 오류가 발견됐을 떄 그 테스트가 진행되는 동안 실행된 코드의 양이 많다면 그 원인을 찾는데 매우 힘들어질 것이다.
![](https://i.imgur.com/3U9PsIb.png)
UserService의 경우를 봐보면 간단한 UserService관리 할뿐인데 3개의 의존성 주입이 필요하다, 데이터베이스, 메일 서버, 트랜잭션관리의 의존성 주입이필요하다. 문제는 그 3가지의 API들도 다른 API에 의존적이라는것이다.
따라서 UserService를 테스트하는 것처럼 보이지만 사실은 그 뒤에 존재하는 훨씬 더 많은 오브젝트와 환경, 서비스, 서버, 심지어 네트워크 까지 함께 테스트하게 되는셈이다. 얼핏보면 한번에 모든것이 테스트되는것처럼 보이지만 만약 에러가 난다면 에러를 찾는데 상당한 고생을 할것이다.
또 예를들어 만약 A 개발자가가 UserService를 만들어야하고 B개발자가 UserDao를 만들어서 제공한다고 해보자. 그런경우 A개발자는 UserDao가 완성될때 계속 기다려야할것이다.

**테스트 대상 오브젝트 고립시키기**
먼저 UserService를 각각 테스트하기위해서는 목 데이터를 만들어야한다. 트랜잭션 같은경우에는 이미 UserServiceTx로 의존성이 분리되어 있으니까 생각하지 않아도 될것같다.  그럼 아래와같이 2개의 클래스를 만들어서 UserService 테스트부분에 있어서 의존성을 분리해내야 한다.
![](https://i.imgur.com/t3lmA4h.png)
이렇게 되면 UserServiceImpl에 대해 테스트가 진행될 때 완벽하게 고립된 테스트 대상으로 되었다.

만약 위와같이 목테스트를 사용하여 테스트하게 된경우 더이상 스프링에도 의존적이지 않아 모든 빈을 등록할필요가 없이 2개의 목클래스만 생성하면 되기 때문에 테스트를 하는 시간도 현저히 줄어들게 된다.

**단위 테스트와 통합 테스트**
보통 목데이터를 이용한 외부의 자원에 영향없이 단위만 테스트한느것을 단위테스트라고 한다. 반면 2개이상의 성격이나 계층이 다른 오브젝트가 연동하도록 하는것을 통합 테스트라고 부른다.

여기서 말하는 단위 테스트와 통합테스트는 모두 개발자가 스스로 자신이 만든 코드를 테스트하기 위해 만드는것이다.
코드를 작성하면서 테스트는 어떻게 만들것인가 생각하는 것은 좋은 습관이다. 테스트하기 편하게 만들어진 코드는 깔끔하고 좋은코드가 될 가능성이 높다.
스프링이 지지하고 권장하는 깔끔하고 유연한 코드를 만들다보면 테스트도 그만큼 만딜기 쉬워지고, 테스트는 다시 코드의 품질을 높여주고, 리팩토링과 개선에 대한 용기를 주기도 할 것이다. 반대로 좋은 코드를 만들려는 노력을 게을리하면 테스트 작성이 불편해지고, 테스트를 잘 만들지 않게 될 가능성이 높아진다. 테스트가 없으니 과감하게 리팩토링할 엄두를 내지 못할 것이고 코드의 품질은 점점 떨어지고 유연성과 확장성을 잃어갈지 모른다.

**목 프레임워크**
목 클래스를 만들어 테스트하는것은 단위테스트를 할수 있게해주고, 속도면에서도 시간을 아낄수있는 효과가 있다. 하지만 목클래스를 만드는것은 힘들고 시간이 많이드는 일이다. 다행히도 이런 버거러운 목오브젝트를 편리하게 작성하도록 도와주는 다양한 목오브젝트 지원프레임워크가 있다.

**Mockito 프레임워크**
그중에서도 Mockito라는 프레임워크는 사용하기도 편리하고, 코드도 직관적이라 최근 많은 인기를 끌고 있다. 무엇보다 위에서 작성한 MockClass들을 작성안해도 된다는점에서 큰장점이 있다.

## 6.3 다이내믹 프로시와 팩토리빈
6.1에서 UserService 비지니스로직과 트랜잭션 기능을 아래 그림과 같이 나누어서 관리를 했다. 하지만 아래의 기능의 단점은 핵심 기능을 쓰기 위해서는 부가기능을 호출해야하는 모순적인 상황이 발상했다.
그리고 핵심 기능을 바로 호출한다면 부가기능을 사용못한다는 치명적인 단점이 있다. 여기에서 부가 기능이지만 그 부가기능은 트랜잭션이니 말이 부가기능이지 상당히 중요한 기능이기 때문이다.
![](https://i.imgur.com/hYOSfgB.png)
이러한 문제를 해결하기위해서는 부가기능은 자기가 핵심기술인것처럼 꾸미고 자기를 먼저 호출하게 꾸며야한다. 그러기 위해서는 클라이언트는 인터페이스를 통해서만 핵심기능을 사용하게하고, 부가기능 자신도 같은 인터페이스를 구현한뒤에 자신은 아래와같이 그사이에 끼어 들어야한다.
![](https://i.imgur.com/ruAvlTA.png)
이렇게 마치 자신이 클라이언트가 사용하려고하는 실제 대상인것처럼 위장해서 클라이언트의 요청을 받아는 것을 대리자, 대리인과 같은 역할을 한다고해서 프록시라고 부른다. 또 프록시를 통해 요청을 위임을 받아 처리하는 실제 오브젝트 타깃을 타깃 또는 실체라고 한다. 아래는 클라이언트아가 프록시를 통해 타깃을 사용하는 구조를 보여준다.
![](https://i.imgur.com/IBRNs02.png)

프록시의 특징은 타깃과 같은 인터페이스를 구현했다는것과 프록시가 타깃을 제어할 수 있는 위체이 있다는것이다.
프록시는 사용목적에 따라 두가지로 구분한다.
1. 첫째는 클라이언트가 타깃에 접근하는 방법을 제어하기 위해서이다.
2. 두번째는 타깃에 부가적인 기능을 부여해주기 위해서다.
두가지 모두 대리 오브젝트라는 개념의 프록시를 두고 사용한다는 점은 동일하지만, 목적에따라서 디자인 패턴에서는 다른 패턴으로 구분한다.

**데코레이터 패턴**
데코레이터 패턴은 타깃에 부가적인 기능을 런타임 시 다이내믹하게 부여해주기 위해 프록시를 사용하는 패턴을 말한다. 다이내믹하게 기능을 부가한다는 의미는 컴파일시점, 즉 코드상에서는 어떤 방법과 순서로 프록시와 타깃이 연결되어 상요되는지 정해져 있지 않다는 뜻이다.
데코레이터 패턴에서는 프록시가 꼭 한개로 제한되지 않는다. 프록시가 직접 타깃을 사용하도록 고정시킬 필요도 없다. 이를 위해 데코레이터 패턴에서는 같은 인터페이스를 구현한 타겟과 여러개의 프록시를 사용할 수 있다. 프록시가 여러개인 만큼 순서를 정해서 단계적으로 위임하는 구조로 만들면된다. 아래와같은 구조이다.
![](https://i.imgur.com/QNRF8Kf.png)
프록시로서 동작하는 각 데코레이터는 위임하는 대상에도 인터페이스로 접근하기 때문에 자신이 최종 타깃으로 위임하는지, 아니면 다음 단계의 데코레이터 프록시로 위임하는지도 모른다.

**프록시 패턴**
프록시 패턴의 프록시는 타깃의 기능을 확장하거나 추가하지 않는다. 대신 클라이언트가 타깃에 접근하는 방식을 변경해준다.
타깃 오브젝트에 대해 미리 레퍼런스가 필요할 수 가 있다 이럴때 프록티 패턴을 적용하면 좋다. 실제 타깃 오브젝트는 만드는 대신 프록시를 넘겨준다. 그리고 프록시의 메소드를 통해 타깃을 사용하려고 시도하면 , 그때 프록시가 타깃 오브젝트를 생성하고 요청을 위임해준다.
또는 특별한 상황에서 타깃에 대한 접근권한을 제어하기 위해 프록시 패턴을 사용할 수 있다.

**다이내믹 프록시 생성방법**
프록시는 기존 코드에 영향을 주지않으면서 타깃의 기능을 확장하거나 접근 방법을 제어하는 유용한 방법이다. 하지만 이것도 MockClass 와같이 개발자가 만들어 줘야하는 문제점이 있다. 이러한 문제점을 해결해주기위해서 스프링에서 java.lang.reflect 패키지안에 프록시를 손쉽게 만들수 있도록 지원해주는 클래스들이 있다.

**리플렉션**
다이내믹 프록시는 리플렉션 기능을 이용해서 프록시를 만들어준다. 리플렉션은 자바의 코드 자체를 추상화해서 접근하도록 만든것이다. 예를들어 아래와같은 소스가있다고 가정하자.

```java
string name = "Spring";
```
이 스트링의 길이를 알고싶으면 String class의 length()함수를 호출해야한다.
자바의 모든 클래스는 그 클래스의 자체의 구성정보를 담은 Class 타입의 오브젝트를 하나씩 갖고 있다. 예를들어 getClass()등을 통해 클래스네임을 가지고올 수 있고 이것 외에도 메소드정보 필드네임 등을 가지고올 수 있다.
리플렉션에는 이러한 기능을 이용해 클래스의 메소드를 호출하는 기능도 가능하다.

**다이내믹 프록시 적용**
![](https://i.imgur.com/vShkZQA.png)
다이내믹 프록시는 프록시 패토리에 의해 런타임시 다이내믹하게 만들어지는 오브젝트다.  다이내믹 프록시 오브젝트는 타깃의 인터페이스와 같은 타입으로 만들어진다. 클라이언트는 다이내믹 프록시 오브젝트를 타깃 인터페이스를 통해 사용할 수 있다. 이덕분에 프록시를 만들 때 인터페이스를 모두 구현해가면서 클래스를 정의할 필요는 없다.
그다음 InvocationHandler Interface를 구현해줘야한다. 클라이언트가 타깃을 호출하게되면 다이내믹프록시를 통해 InvocationHandler의 구현체를 타게되고 최종적으로 타깃으로 이동한다. InvocationHandler인터페이스는 다음과 같은 메소드 하나만 가진다.
```java
public Object invoke(Object proxy, Method method, Object[] args)
```
InvocationHandler인터페이스를 구현한 오브젝트를 제공해주면 다이내믹 프록시가 받는 모든 요청을 InvocationHandler의 invoke()메소드로 보내준다. 실행하고자하는 인터페이스의 메소드가 아무리 많아도 invoke메소드 하나로 사후 처리랄 할 수 있게되었다.
아래는 InvocationHandler를 통한 요청처리 구조이다.
![](https://i.imgur.com/e9OmgVb.png)

**다이내믹 프록시를 이용한 트랜잭션 부가 기능**
그럼 이제 UserServiceTx를 다이내믹 프록시 방식으로 만들어보자. UserServiceTx는 서비스 인터페이스의 메소드를 모두 구현해야하고 트랜잭션이 필요한 메소드마다 트랜잭션 처리코드가 중복돼서 나타나는 비효율적인 방법으로 만들어져있다.
따라서 트랜잭션 부가기능을 제공하는 다이내믹 프록시를 만들어 적용하는 방법이 효율적이다.
다음은 트랜잭션 InvocationHandler이다.
```java
public class TransactionHandler {
    private Object target;
    private PlatformTransactionManager transactionManager;
    private String pattern;

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith(pattern)) {
            return invokeInTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private Object invokeInTransaction(Method method, Object[] args) throws Throwable {
        TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            Object returnValue = method.invoke(target, args);
            this.transactionManager.commit(status);
            return returnValue;
        } catch (InvocationTargetException e) {
            this.transactionManager.rollback(status);
            throw e.getTargetException();
        }
    }
}

```
이제 UserServiceTx보다 코드는 복잡하지 않으면서도 UserService뿐 아니라 모든 트랜잭션이 필요한 오브젝트에 적용 가능한 트랜잭션 프록시 핸들러가 만들어졌다.

**다이내믹 프록시를 이용한 팩토리 빈**
앞에서 TransactionHandler 를 만들었고, 이를 이용해 다이내믹 프록시를 UserService에 적용하는 테스트를 만들어봤다. 이제 TransactionHandler와 다이내믹 프록시를 스프링의 DI를 통해 사용할수있도록 해야한다.
하지만 DI대상이 되는 다이내믹 프록시 오브젝트는 이랍ㄴ적인 스프링의 빈으로 등록할 방법이 없다. 스프링의 빈은 기본적으로 클래스 이름과 프로퍼티로 정의된다. 스프링은 지정된 클래스의 이름을 가지고 리플렉션을 이용해 해당클래스의 오브젝트를 만든다. 클래스의 이름을 갖고 있다면 다음과 같은 방법으로 새로운 오브젝트를 생성할 수 있다.

```java
Date now = (Date) Class.forName("java.util.Date").newInstance();
```
문제는 다이내믹 프록시 오브젝트는 이렇게 생성할수 없다는것이다. 다이내믹 프록시는 Proxy클래스의 newProxyInstance()라는 스태틱 팩토리 메소드를 통해서만 만들 수 있다.

**팩토리 빈**
사실 스프링은 클래스의 정보를 가지고 디폴트 생성자를 통해 오브젝트를 만드는 방법외에도 빈을 만들수 잇는 여러가지 방법을 제공한다. 대표적으로 팩토리 빈을 이용한 방법이 있다.

```java
public interface FactoryBean<T> {
    @Nullable
    T getObject() throws Exception; //빈을생성해준다.

    @Nullable
    Class<?> getObjectType(); //생성되는 오브젝트타입을 알려준다.

    default boolean isSingleton(); // 싱글톤여부

}
```
팩토리 빈은 전형적인 팩토리 메소드를 가진 오브젝트다. 스프링은 FactoryBean 인터페이스를 구현한 클래스가 빈의 클래스로 지정되면, 팩토리 빈 클래스의 오브젝트이 getObject()를 통해 오브젝트를 가져오고, 이를 빈 오브젝트로 사용한다. 빈의 클래스로 등록된 팩토리 빈은 빈 오브젝트를 생성하는 과정에서만 사용될 뿐이다.

**다이내믹 프록시를 만들어주는 팩토리 빈**
Proxy의 newProxyInstance() 메소드를 통해서만 생성이 가능한 다이내믹 프록시 오브젝트는 일반적인 방법으로는 스프링의 빈으로 등록할수 없다. 대신 팩토리빈을 사용하면 다이내믹 프록시 오브젝트를 스프링의 빈으로 만들어줄수가 있다. getObject() 메소드에 다이내믹 프록시 오브젝트를 만들어주는 코드를 넣으면 되기 때문이다. 아래는 팩토리 빈을 이용하여 트랜잭션 다이내믹 프록시의 적용한것이다.

![](https://i.imgur.com/2z9ADt5.png)
그림을 보면 먼저 팩토리빈에서 UserServiceImpl과 TransactionHandler를 기반으로 proxy를 생성하다. 그리고 그것을  빈으로 등록한다. 그렇게되면 클라이언트는 UserService에 접근하게되면 그 프록시 객체를 사용하게된다.

**프록시 팩토리 빈 방식의 장점과 한계**
장점
프록시 팩토리 빈을 재사용할 수 있어 어떤 서비스에도 붙일 수 있다.

단점
한번에 여러개의 클래스에 공통적인 부가기능을 제공하는일은 지금까지 살펴본 방법으로는 불가능하다. 하나의 타깃 오브젝트에만 부여되는 부가기능이라면 상관없겠지만, 트랜잭션과 같이 비즈니스 로직을 담은 많은 클래스의 메소드에 적용할필요가있다면 거의 비슷한 프록시 팩토리 빈의 설정이 중복되는것을 막을 수 없다.

## 6.4 스프링의 프록시 팩토리 빈
자바 JDK에서 제공하는 다이내믹 프록시외에도 편리하게 프록시를 만들수 있도록 지원해주는 다양한 기술이 존재한다. 스프링에서는 일관된 방법으로 프록시를 만들 수 있게 도와주는 추상 레이어를 제공한다. 아래는 Spring의 프록시 팩토리 빈을 생성하는 예제이다.

```java
@Test
public void proxyFactoryBean() { // Spring 팩토리 빈 생성

    ProxyFactoryBean pfBean = new ProxyFactoryBean();
    pfBean.setTarget(new HelloTarget());
    pfBean.addAdvice(new UppercaseAdvice());

    Hello proxyHello = (Hello) pfBean.getObject();

    assertThat(proxyHello.sayHello("Toby"), is("HELLO TOBY"));
}

static class UppercaseAdvice implements MethodInterceptor{
  // 타깃 오브젝트를 전달할 필요가 없다. MethodInvocation은 메소드 정보와 함께 타깃 오브젝트를 알고 있다.
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        String ret = (String) methodInvocation.proceed();
        return ret.toUpperCase();
    }
}
```
[Spring proxy factory bean](https://github.com/minwan1/Spring-toby/commit/bd39081d626e880a411a6fc58db0ba1d067b90f8)

### 어드바이스 : 타깃이 필요 없는 순수한 부가 기능
MethodInterceptor를 구현한 UppercaseAdvice에는 타깃 오브젝트가 등장하지 않는다. MethodInvocation은 타깃 오브젝트의 메소드를 실행할 수 있는 기능이 있기 때문에 MethodInterceptor는 부가기능을 제공하는데만 집중할 수 있다.
어드바이스는 타깃 오브젝트에 종속되지 않는 순수한 부가기능을 담은 오브젝트라는 사실을 잘기억해두자.
**JDK 프록시와 스프링 프록시 차이**

JDK 프록시 생성방법
```java
final Hello hello =  (Hello) Proxy.newProxyInstance(getClass().getClassLoader()
        ,new Class[]{Hello.class}
        ,new UppercaseHandler(new HelloTarget())
```
Spring 프록시 생성방법
```java
ProxyFactoryBean pfBean = new ProxyFactoryBean();
pfBean.setTarget(new HelloTarget());
pfBean.addAdvice(new UppercaseAdvice());

Hello proxyHello = (Hello) pfBean.getObject();
```
먼저 위를 보면 차이점은
1. 먼저 위의 첫번 째 소스처럼 부가기능 UppercaseHandler 클래스에 타겟을 따로 등록을 안해줘도 된다. 이렇게 되면 부가기능에만 집중하면 되게 된다.
2. Spring ProxyFactoryBean은 작은 단위의 템플릿/콜백 구조를 응용해서 적용했기 때문에 싱글톤으로 MethodInvocation를 관리할 수 있다.
3. Java jdk proxy 생성으로할경우 스프링에 Bean을 등록해주기 위해서 계속 계속해서 스프링에 빈을 등록해줘야하는 문제도 해결된다. Spring proxy bean factory를 사용하게되면 하나의 빈으로 관리가 가능해진다.
4. 따로 위의 JDK 프록시 생성법과 같이 Hello타입이라고 Proxy에게 전해주지 않아도 된다.

Srping Proxy Factory Bean이  타입을 받을 필요가없는이유는 넘겨받은 타깃을 기반으로 인터페이스 자동검출 기반으로 인터페이스를 알아내고 그 인터페이스를 기반으로 모든 구현하는 프록시들을 만들어낸다.


### 포인트컷 : 부가 기능 적용 대산 메소드 선정 방법
메소드의 이름을 가지고 부가기능을 적용대상 메소드를 선정하는것을 포인트컷이라 한다.
![](https://i.imgur.com/exLK1Ut.png)
위의 JDK 다이나믹 프록시를 이용한 방식을 보면 InvocationHander가 타깃과 메소드 선정 알고리즘 코드에 의존하고 있다는 점이다. 만약 타깃이 다르고 메소드 선정 방식이 다르다면 InvocationHandler 오브젝트를 여러 프록시가 공유할 수가 없다.
타깃 메소드가 부가기능지정(선정)알고리즘은  DI를 통해 분리할 수는 있지만 한번 빈으로 구성된 InvocationHandler 오브젝트는, 오브젝트 차원에서 특정 타깃을 위한 프록시에 제한된다. 그래서 InvocationHandler는 빈으로 등록을 안하고 아래와같이 계속 생성하게 된것이다.
```java
,new UppercaseHandler(new HelloTarget())
```
![](https://i.imgur.com/x0Lbxpn.png)
반면 Spring의 ProxyFactoryBean 방식은 두가지 확장 기능인 부가기능(Advice)와 메소드 선정 알고리즘(point cut)을 활용하는 유연하 구조를 가진다.
스프링은 부가기능을 제공하는 오브젝트를 어드바이스라고 부르고, 메소드 선정알고리즘을 담은 오브젝트를 포인트 컷이라고 부른다.
어드바이스와 포인트컷은 모두 프록시에 DI로 주입돼서 사용된다. 두가지 모두 여러 프록시에서 공유가 가능하도록 만들어지기 때문에 스프링의 싱글톤 빈으로 등록이 가능하다.
또 프록시로부터 어드바이스와 포인트컷을 독립시키고 DI를 사용하게 한것은 전형적인 전략 패턴 구조이다. 덕분에 여러 프록시가 공유해서 사용할 수도 있게 되었다. 아래는 Spring FactoryBean을 이용해 Advice와 pointcut을 등록한 소스예제이다.

```java
@Test
public void pointcutAdvisor() { // Spring 팩토리 빈 생성

    ProxyFactoryBean pfBean = new ProxyFactoryBean();
    pfBean.setTarget(new HelloTarget());

    NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
    pointcut.setMappedName("sayH*");

    pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

    Hello proxyHello = (Hello) pfBean.getObject();

    assertThat(proxyHello.sayHello("Toby"), is("HELLO TOBY"));
    assertThat(proxyHello.sayHi("Toby"), is("HI TOBY"));
    assertThat(proxyHello.sayThankYou("Toby"), is("Thank You Toby"));
}
```

여기에서 포인트컷을 함께 등록할 때는 어드바이스와 포인트컷을 Advisor타입으로 묶어서 AddAdvisor()메소드를 호출한다. 이 이유는 각각의 포인트컷에 따라서 Advice가 달라질수 있기 때문이다. 그리고 이 어드바이스와 포인트컷의 한조합을 어드바이저라고한다.
어드바이져 = 포인트컷(메소드선정알고리즘) + 어드바이스(부가기능)


ProxyFactoryBean 은 스프링의 DI와 템플릿/콜백, 서비스 추상화 등의 기법이 모두 적용된 것이다. 따라서 어드바이스를 여러 프록시가 공유할 수 있게 되었고 포인트컷과 자유롭게 조합이 가능하다. 메소드 선정 방식이 달라지는 경우에는 포인트컷의 설정을 따로 등록하고 어드바이저로 조합해서 적용하면 된다.

![](https://i.imgur.com/WuCKWjf.png)

## 6.5 스프링 AOP

### 자동 프록시 생성
그전에 트랜잭션을 구현할 때 크게 2가지 문제가 있었다.
1. 타깃 오브젝트마다 부가기능이 만들어지는 이슈(Spring Proxy factory bean으로 해결)
2. 남은것은 아래와같이 부가기능의 적용이 필요한 타깃 오브젝트마다 거의 비슷한 내용의 ProxyFactoryBean 설정 정보 추가이다.

```java
       ProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", ProxyFactoryBean.class);
       txProxyFactoryBean.setTarget(testUserService);
       UserService userServiceTx = (UserService) txProxyFactoryBean.getObject();
```

**빈 후처리기를 이용한 자동 프록시 생성기**
Spring은 변하지 않는 핵심적인 부분외에는 대부분확장할 수 있도록 확장 포인트를 제공해준다. 그중에서도 관심을 가질만한 부분은 BeanPostProcessor인터페이스이다. 빈 후처리기는 이름 그대로 스프링 빈 오브젝트로 만들어지고 난 후에, 빈오브젝트를 다시 가공할 수 있게 해준다. 여기에서 사용할것은 DefaultAdvisorAutoProxyCreator이다.
 DefaultAdvisorAutoProxyCreator는 이름에서도 알 수 있듯이 어드바이저를 이용한 자동 프록시 생성기이다. 이것을 이용해 자체적으로 빈등록을 하고 빈오브젝트가 생성될 때마다 빈후처리기에 보내서 후처리 작업을 요청한다. 빈 후처리기는 빈 오브젝트의 프로퍼티를 강제로 수정할 수도 있고 별도의 초기화 작업을 수행할 수도 있다.이를 활용하면 스프링이 생성하는 빈 오브젝트의 일부를 프록시로 포장하고, 프록시를 빈으로 대신 등록할 수도 있다.
![](https://i.imgur.com/RnQX6Mr.png)
DefaultAdvisorAutoProxyCreator가 빈 후처리기로 등록되어 있으면 스프링은 빈 오브젝트를 만들 때 마다 후처리기에게 빈을 보낸다. DefaultAdvisorAutoProxyCreator는 빈으로 등록된 모든 어드바이저 내의 포인트컷을 이용해 전달받은 빈이 프록시 적용대상인지 확인한다. 프록시 적용대상이라면 그때는 내장된 프록시 생성기에게 현재 빈에 대한 프록시를 만들게하고, 만들어진 프록시에 어드바이저를 연결해준다. 빈 후처리기는 프록시가 생성되면 원래 컨테이너가 전달해준 빈 오브젝트 대신 프록시 오브젝트를 컨테이너에게 돌려준다.

지금까지 아래의 인터페이스를 구현한 NameMatchMethodPointcut를 사용을해서  getMethodMatcher메소드만 구현해서 사용했다. 왜냐하면 직접 클래스를 ProxyFactoryBean에 할당하고 그것으로 프록시 객체를 만들었기때문이다. 지금부터는 DefaultAdvisorAutoProxyCreator를 이용해서 프록시 객체를 자동으로 생성할거기때문에 어떤클래스를 Proxy를 생성할지 알아야 하기때문에 getClassFilter 메소드 또한 사용해야한다. 이것을 사용하면 어떤 오브젝트에 프록시 객체를 생성할지 지정할수 있다.
```java
public interface pointcut{
  ClassFilter getClassFilter();
  MethodMatcher getMethodMatcher();
}
```

**간단한 예제**
위의 기능이 잘작동하는지 간단한 예제를 테스트해보자. 아래는 예제 테스트이다.
```java
@Test
    public void classNamePointcutAdvisorTest() {

        NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut() {
            public ClassFilter getClassFilter() {
                return new ClassFilter() {
                    @Override
                    public boolean matches(Class<?> clazz) {
                        return clazz.getSimpleName().startsWith("HelloT");
                    }
                };
            }
        };

        classMethodPointcut.setMappedName("sayH*");

        checkAdviced(new HelloTarget(), classMethodPointcut, true);

        class HelloWorld extends HelloTarget {};
        checkAdviced(new HelloWorld(), classMethodPointcut, false);

        class HelloToby extends HelloTarget {};
        checkAdviced(new HelloToby(), classMethodPointcut, true);
    }

    private void checkAdviced(Object target, Pointcut pointcut, boolean adviced) {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(target);
        proxyFactoryBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

        Hello proxyHello = (Hello)proxyFactoryBean.getObject();

        if (adviced) {
            assertThat(proxyHello.sayHello("Toby"), is("HELLO TOBY"));
            assertThat(proxyHello.sayHi("Toby"), is("HI TOBY"));
            assertThat(proxyHello.sayThankYou("Toby"), is("Thank You Toby"));
        } else {
            assertThat(proxyHello.sayHello("Toby"), is("Hello Toby"));
            assertThat(proxyHello.sayHi("Toby"), is("Hi Toby"));
            assertThat(proxyHello.sayThankYou("Toby"), is("Thank You Toby"));
        }
    }
```
[예제 소스 ](https://github.com/minwan1/Spring-toby/commit/af7684c466df3417687c5f3b5ae5927726acf9d9)

소스에서 보이듯이 HelloT로 시작하는 클래스만 Proxy객체를 생성하게 설정했다. 이렇게함으로써 2번째 테스트인 HelloWorld 대문자로 바꾸는 Advice기능이 적용되지 않은 모습을 볼 수 있다.

### DefaultAdvisorAutoProxyCreator의 적용
이제 실제로 DefaultAdvisorAutoProxyCreator 클래스를 이용해 지정된 클래스만 Advice가 작동하도록 하는 포인트컷 기능을 구현해보자.
```java
public class NameMatchClassMethodPointcut extends NameMatchMethodPointcut{
    public void setMappedClassName(String mappedClassName){
        this.setClassFilter(new SimpleClassFilter(mappedClassName));
    }
    static class SimpleClassFilter implements ClassFilter{
        String mappedName;
        private SimpleClassFilter(String mappedName){
            this.mappedName = mappedName;
        }
        @Override
        public boolean matches(Class<?> clazz) {
            return PatternMatchUtils.simpleMatch(mappedName, clazz.getSimpleName());
        }
    }
}
```
먼저 클래스 포인트컷기능이 작동하도록 위에처럼 클래스를 구현해준다. 그리고 이기능이 작동할수 있도록 어떤 클래스들을 프록시로 생성할지 설정해주는 포인트 컷을 등록하자.
```java
@Bean
public NameMatchClassMethodPointcut transactionPointcut(){
    NameMatchClassMethodPointcut pointcut = new NameMatchClassMethodPointcut();
    pointcut.setMappedName("upgrade*");
    pointcut.setMappedClassName("*ServiceImpl");
    return pointcut;
}
```
위와같이 빈을 등록함으로써 ServiceImpl로 끝나는 모든 클래스들은 자동으로 Advice가 적용된 프록시 객체들이 생성된다. 물론 아래와같이 DefaultPointcutAdvisor를 등록해줘야한다.

```java
@Bean
public DefaultPointcutAdvisor transactionAdvisor(){
    DefaultPointcutAdvisor defaultPointcutAdvisor = new DefaultPointcutAdvisor();
    defaultPointcutAdvisor.setAdvice(transactionAdvice());
    defaultPointcutAdvisor.setPointcut(transactionPointcut());
    return defaultPointcutAdvisor;
}
```
최종적으로 매번 어떤 클래스를 프록시로 생성할지 지정할필요가 없어졌다. 매번 아래와같이 프록시 객체를 선언하기 위한 노력또한 하지 않아도 된다.
```java
       ProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", ProxyFactoryBean.class);
       txProxyFactoryBean.setTarget(testUserService);
       UserService userServiceTx = (UserService) txProxyFactoryBean.getObject();
```
그리고 해당 클래스가 Proxy객체인지 확인하는테스트는 아래와같이 확인할 수 있다.
```java
@Test
public void advisorAutoProxyCreator() {
    assertThat(testUserService instanceof Proxy, is(true));
}
```

### 포인트컷 표현식을 이용한 포인트 컷
지금까지 클래스이름이나 메소드이름을 각각 의 메소드 매처를 구현하거나 스프링이 제공하는 필터나 매처클래스를 가져와 프로포티를 설정하는 방식이였다.
스프링에서는 아주 간단하고 효과적인 포인트컷의 클래스와 메소드를 선정하는 알고리즘을 작성할 수 있는 알고리즘을 작성할 수 있는 방버을 제공한다. 이것을 포인트컷 표현식이라고 한다

**포인트컷 표현식**
앞에서 구현한 Pointcut 인터페이스를 구현해야하는  스프링의 포인트컷은 클래스 선정을 위한 클래스 필터와 메소드 선정을 위한 메소드 매처 두 가지를 각각 메소드명, 클래스 제공해야한다.
하지만 AspectJExpressionPointcut은 클래스와 메소드의 선정 알고리즘을 포인트컷 표현식을 이용해 한 번에 지정할 수 있게 해준다. 사실 스프링이 사용하는 포인트컷 표현식은 AspectJ라는 유명한 프레임워크에서 제공하는것을 가져와 일부 문법을 확장한것이다. 그래서 이를 Aspect포인트컷 표현식이라 한다.
```java
@Test
public void methodSignaturePointcut() throws SecurityException, NoSuchMethodException {

    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
    pointcut.setExpression("execution(public int  com.example.demo.learningtest.spring.pointcut.Target.minus(int, int) throws java.lang.RuntimeException)");
    // minus 메소드 지정

    assertThat(pointcut.getClassFilter().matches(Target.class) &&
            pointcut.getMethodMatcher().matches(Target.class.getMethod("minus", int.class, int.class), null), is(true));

```
위는 포인트컷 표현식을 사용한 간단한 예제이다. 표현식을 이용해 대상을 지정하고 그것이 맞는지 테스트하는 예제이다. AspectJExpressionPointcut를 통해 오브젝트를 만들고 포인트컷 표현식을 expression 프로포티에 넣어주면 포인트컷을 사용할 수 가 있다. 간단하게 설명하면 public이고 리턴갓이 int이고 com.example.demo.learningtest.spring.pointcut.Target.minus(int, int) 의 메소드에 RuntimeException을 던지는 메소드를 지정한것이다.
[예제 소스](https://github.com/minwan1/Spring-toby/commit/4234080fcc60715faec1686971d366b41a76d4ba)

이제 위 포인트컷 표현식을 이용해 기존 포인트 컷방법을 아래와같이 변경해보자.
```java
@Bean
public AspectJExpressionPointcut transactionPointcut(){
    AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
    aspectJExpressionPointcut.setExpression("execution(* *..*ServiceImpl.upgrade*(..))");
    return aspectJExpressionPointcut;
}
```
그리고 기존 표현식을 등록지우고 테스트를 돌려보면 정상 작동할것이다. 포인트컷 표현식 사용하면 로직이 짧은 문자열에 담기기 때문에 클래스나 코드를 추가할 필요가 없어서 코드와 설정이 모두 단순해진다.

그리고 여기에서 중요한점이 하나있다. execution(* *..*ServiceImpl.upgrade*(..)) 이렇게 표현식을 등록했는데 이렇게 등록되면 ServiceImpl 클래스를 지정한게 아니라 타입을 지정한것이다. 그럼으로 UserService와 관련된 모든 타깃은 트랜잭션이 먹히는것이다. 기존에 TestUserService를 트랜잭션을 사용을 위해 TestUserServiceImpl로 변경했는데 이걸 다시 기존이름으로 변경하고 테스트해도 트랜잭션이 적용되는것을 알 수 있다. 포인트컷 표현식은 기본적으로 타입을 기반으로 작동하기 때문이다.

### AOP란 무엇인가.
트랜잭션 경계 설정 코드와 같은 부가기능을, 다른 비즈니스 로직과는 다르게 일반적인 객체지향적인 설계 방법으로는 독립적인 모듈화가 불가능하였다. 다이내믹 프록시라든가 빈 후처리기와 같은 복잡한 기술까지 동원하였기 때문이다.

이런 부가기능에 대해서 모듈화 작업을 진행할 때 객체지향 설계 패러다임과는 다른 새로운 특성을 가지고 있어, 모듈화된 부가기능을 오브젝트라고 부르지 않고 애스팩트(Aspect) 라고 부른다.

애스팩트는 그 자체로 애플리케이션의 핵심 기능을 담고 있지는 않으나, 애플리케이션을 구성하는 중요 요소이고 핵심기능에 부가되어 의미를 갖는 특별한 모듈을 말한다. 애스팩트는 부가될 기능을 정의한 어드바이스 와 어드바이스를 어디에 적용할지를 결정하는 포인트컷 을 함께 갖고 있다
![](https://i.imgur.com/5CwMiM2.png)


## 6.6 트랜잭션 속성
### 트랜잭션 정의
트랜잭션 경계안에서 진행된 작업은 commit을 통해 모두 성공하든지 rollback()을 통해 모두 취소돼야 한다. 그런데 이밖에도 트랜잭션의 동작방식을 제어할 수 있는 몇가지 조건이 있다.

#### 트랜잭션 전파
트랜잭션 전파란 트랜잭션의 경계에서 이미 진행중인 트랜잭션이 있을 때 또는 없을때 어떻게 동작할것인가를 결정하는 방식을 말한다. 아래는 트랜잭션 몇개의 전파 속성이다.

* PROPAGATION_REQUIRED
진행중인 트랜잭션이 없으면 시작하고 있으면 참여한다.
* PROPAGATION_REQUIRES_NEW
새로운 트랜잭션을 시작한다
* PROPAGATION_NOT_SUPPORTED
트랜잭션없이 동작한다.

그전에 TransactionAdvice를 통해 아래와같이 소스를 관리했었다.

```java
@Override
public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
    ...
}
```
getTransaction() 함수는 트랜잭션을시작는게 아니라 현재 트랜잭션 여부를 가지고 오는기능이다. 그리고 그트랜잭션 여부에 따라 위의 속성중 어떤 속성을 적용할지 선택하게된다.

#### 격리수준
모든 DB트랜잭션은 격리수준을 갖고 있어야한다. 서버 환경에서는 여러개의 트랜잭션이 동시에 진행될 수 있다. 가능하면 모든 트랜잭션이 순차적으로 진행되서 다른 트랜잭션의 작업에 독립적인 것이 좋지만 그러면 성능이 크게 떨어질 수 있다. 따라서 적절한 격리수준을 조정해서 관리해야한다. 기본적으로 격리수준은 IOSLATION_DEFAULD다. 이는 DataSource에 설정되어 있는 디폴트 격리수준을 그대로 따른다는 뜻이다.

#### 트랜잭션 인터셉터와 트랜잭션 속성
메소드별로 다른 트랜잭션 정의를 적용하려면 어드바이스의 기능을 확장해야한다. 이제 기존에 만들었던 TransactionAdvice 대신 스프링에서 만든 TransactionInterceptor를 이용해보자. TransactionInterceptor는 메소드 이름패턴을 이용해서 다르게 지정할 수 있는 방법을 추가로 제공할 수 있다.  

#### 프록시 방식 AOP는 같은 타깃 오브젝트내의 메소드를 호출할 때는 적용되지 않는다.
![](https://i.imgur.com/InzBb0t.png)
만약 다음과 같이 delete기능과 update기능이 있고, update기능이 새로운 트랜잭션이라고 가정한다면 위 2번과 같이 update메소드를 호출하면 delete트랜잭션이 그냥 전위될 것이다. 그이유는 프록시 객체를 거치지 않고 가기때문이다.

### 트랜잭션 속성 적용
#### 트랜잭션 경계설정의 일원화
트랜잭션 경계 설정의 부가 기능을 여러 계층에서 중구난방으로 적용하는건 좋지 않다. 일반적으로 특정계층의 경계를 두는것이 바람직하다. 일반적으로는 서비스층을 기반으로 둔다.
만약 서비스 계층을 트랜잭션이 시작되고 종료되는 경계로 정했다면, 테스트와 같은 특별한 이유가 아니고는 다른 계층이나 모듈에서 DAO에 직접 접근하는것은 차단해야한다. 그래야 그 층에 aop를 이용한 부가기능을 넣기가 쉽다. 그리고 안전하게 코딩하려면 다른모듈의 서비스 계층을 통해 접근하는 방법이 좋다.

[예제 소스](https://github.com/minwan1/Spring-toby/tree/master/chapter06-6)

## 6.7 애노테이션 트랜잭션 속성과 포인트컷
좀더 세밀하게 트랜잭션을 관리해야할경우에는 포인트컷은 한계에 직면할 수 있다. 아무래도 세밀하게 트랜잭션을 관리하다보면 포인트컷 설정파일들을 관리하기 어려워질 수 있다. 이런 세밀한 트랜잭션 속성의 제어가 필요한 경우를 위해 스프링이 제공하는 다른 방법이 있다.

### 트랜잭션 애노테이션
@Transaction 애노테이션의 코드는 단순하고 직관적이라서 쉽게 이해할 수 있다.
![](https://i.imgur.com/3lEAhIt.png)

이 애노테이션을 이용하면 좀더 세밀하게 트랜잭션을 관리할 수 있다. 아래의 방식은 그예제와 우선순위를 나타낸 소스이다.

```java
[1]
public interface Service {
  [2]
  void method1();

  void method2();
}

[3]
public class ServiceImpl implements Service {
  [4]
  public void method1();

  public void method2();
}
```
어노테이션 기능이 적용될 순서는 4 - 3 - 2 - 1 으로 스캔하여 기능이 적용된다.

## 6.8 트랜잭션 지원 테스트
### 선언적 트랜잭션과 트랜잭션 전파 속성
트랜잭션을 정의할 때 지정할 수 있는 트랜잭션 전파 속성은 매유 유용한 개념이다. 아래의 그림을 보면 메소드들에 REQIRED방식의 ㅡㅌ랜잭션 전파 속성을 지정햇을 때 트랜잭션이 시작되고 종료되는 경계를 보여준다.
![](https://i.imgur.com/MmBzyVP.png)
스프링에서 위에 방법을 구현할 수 있는방법은 크게 2가지가 존재한다

**선언적 트랜잭션(Declarative transaction)**
AOP를 이용해 코드 외부에서 트랜잭션의 기능을 부여해주고 속성을 지정할 수 있게 하는 방법을 선언적 트랜잭션이라한다.

**프로그램에의한 트랜잭션(Programmatic transaction)**
반대로 TransactionTemplaate이나 개별 데이터 기술의 트랜잭션 API를 사용해 직접 코드안에서 사용하는방법은 프로그램에 의한 트랜잭션이라 한다.

이렇게 트랜잭션의 자유론 전파와 그로인한 유연한 개발이 가능할 수 있었던 기술적인 배경에는 AOP가 있었다. 또 한가지 이런기술을 구현가능하게 했덨것은 바로 스프링의 트랜잭션 추상화이다. 스프링 추상화를 통해 트랜잭션기술에 상관없이 트랜잭션을 묶는것을 가능하게 했다.

#### 트랜잭션 매니저와 트랜잭션 동기화
트랜잭션 추상화 기술의 핵심은 트랜잭션 매니저와 트랜잭션 동기화다. PlatforTransactionManager 인터페이스를 구현한 트랜잭션 매니저를 통해 구체적인 트랜잭션 기술의 종류에 상관없이 일관된 트랜잭션 제어가 가능했다.
