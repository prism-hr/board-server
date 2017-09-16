package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.SigninDTO;

public interface OauthAdapter {

    User exchangeForUser(SigninDTO signinDTO);

}
