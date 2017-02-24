package hr.prism.board.service;

import com.stormpath.sdk.account.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
@Transactional
public class UserService {

    public Account getCurrentAccount(HttpServletRequest request) {
        return (Account) request.getAttribute("account");
    }

}
