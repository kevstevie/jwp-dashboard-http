package nextstep.jwp.presentation;


import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.model.User;
import nextstep.jwp.util.FileIOReader;
import org.apache.coyote.http11.ContentType;
import org.apache.coyote.http11.Header;
import org.apache.coyote.http11.Session;
import org.apache.coyote.http11.request.HttpRequest;
import org.apache.coyote.http11.response.HttpResponse;
import org.apache.coyote.http11.response.StatusCode;

public class LoginController implements Controller {

    private static final String INDEX = "/index.html";
    private static final String JSESSIONID = "JSESSIONID";

    private static final LoginController instance = new LoginController();
    private static final String UNAUTHORIZED = "/401.html";

    private LoginController() {
    }

    public static LoginController getInstance() {
        return instance;
    }

    @Override
    public HttpResponse service(HttpRequest request, HttpResponse response) {
        if (request.getMethod().equalsIgnoreCase("POST") && request.getRequestUrl().equals("/login")) {
            return tryLogin(request, response);
        }
        if (request.getMethod().equalsIgnoreCase("GET") && request.getRequestUrl().equals("/login")) {
            return loginPage(request, response);
        }
        if (request.getMethod().equalsIgnoreCase("GET") && request.getRequestUrl().equals("/register")) {
            return registerPage(request, response);
        }
        if (request.getMethod().equalsIgnoreCase("POST") && request.getRequestUrl().equals("/register")) {
            return register(request, response);
        }
        return null;
    }

    private HttpResponse register(HttpRequest request, HttpResponse response) {
        InMemoryUserRepository.save(new User(
                request.getBodyValue("account"),
                request.getBodyValue("password"),
                request.getBodyValue("email")
        ));
        return response.contentType(request.getAccept())
                       .redirect(INDEX);
    }

    private HttpResponse registerPage(HttpRequest request, HttpResponse response) {
        String responseBody = FileIOReader.readFile(request.getRequestUrl());
        return response.contentType(request.getAccept())
                       .body(responseBody);
    }

    private HttpResponse loginPage(HttpRequest request, HttpResponse response) {
        if (request.getSession().getAttribute("user") != null) {
            return response.contentType(request.getAccept())
                           .redirect(INDEX);
        }
        String responseBody = FileIOReader.readFile(request.getRequestUrl());
        return response.contentType(request.getAccept())
                       .body(responseBody);
    }

    private HttpResponse tryLogin(HttpRequest request, HttpResponse response) {
        try {
            login(request, response);
            return response.contentType(request.getAccept())
                           .redirect(INDEX);

        } catch (IllegalArgumentException e) {
            String responseBody = FileIOReader.readFile(UNAUTHORIZED);
            return response.contentType(ContentType.HTML.getType())
                           .statusCode(StatusCode.UNAUTHORIZED)
                           .body(responseBody);
        }
    }

    private void login(HttpRequest request, HttpResponse response) {
        User find = InMemoryUserRepository.findByAccount(request.getBodyValue("account"))
                                          .filter(user -> user.checkPassword(request.getBodyValue("password")))
                                          .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 틀립니다."));

        Session session = request.getSession();
        session.setAttribute("user", find);
        response.addHeader(Header.SET_COOKIE.getName(), makeCookie(session.getId()));
        log.info("로그인 성공: {}", find.getAccount());
    }

    private String makeCookie(String sessionId) {
        return String.format("%s=%s", JSESSIONID, sessionId);
    }
}
