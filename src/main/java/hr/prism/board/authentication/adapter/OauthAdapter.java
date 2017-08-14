package hr.prism.board.authentication.adapter;

import hr.prism.board.domain.User;
import hr.prism.board.dto.OauthDTO;

public interface OauthAdapter {

    User exchangeForUser(OauthDTO oauthDTO);

}
